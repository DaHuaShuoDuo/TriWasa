package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class EasyPlaceStateSupport
{
	private static final Direction[] DIRECTIONS = Direction.values();

	private EasyPlaceStateSupport()
	{
	}

	public static int encodeGeneric(BlockState state)
	{
		int value = 0;
		Optional<EnumProperty<Direction>> directionProperty = getFirstDirectionProperty(state);
		if(directionProperty.isPresent())
		{
			value = state.getValue(directionProperty.get()).get3DDataValue();
		}
		else if(state.hasProperty(BlockStateProperties.AXIS))
		{
			value = state.getValue(BlockStateProperties.AXIS).ordinal();
		}

		if(state.getBlock() instanceof RepeaterBlock)
		{
			value |= state.getValue(RepeaterBlock.DELAY) << 4;
		}
		else if(state.getBlock() instanceof ComparatorBlock
				&& state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT)
		{
			value |= 0x10;
		}
		else if(state.hasProperty(BlockStateProperties.HALF)
				&& state.getValue(BlockStateProperties.HALF) == Half.TOP)
		{
			value |= 0x10;
		}
		else if(state.hasProperty(BlockStateProperties.SLAB_TYPE)
				&& state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP)
		{
			value |= 0x10;
		}
		return value;
	}

	public static BlockState applyGeneric(BlockState state, int protocolValue, BlockPlaceContext context)
	{
		BlockState result = setFirstDirection(state, directionFromProtocol(state, protocolValue, context));
		if(result == state && state.hasProperty(BlockStateProperties.AXIS))
		{
			result = result.setValue(
					BlockStateProperties.AXIS,
					Direction.Axis.values()[Math.floorMod(protocolValue, 3)]
			);
		}
		if(result.hasProperty(BlockStateProperties.SLAB_TYPE)
				&& result.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE)
		{
			SlabType targetType = (protocolValue & 0x10) != 0 ? SlabType.TOP : SlabType.BOTTOM;
			result = result.setValue(BlockStateProperties.SLAB_TYPE, targetType);
		}

		int highBits = protocolValue & ~0xf;
		if(result.hasProperty(RepeaterBlock.DELAY) && highBits >= 0x10)
		{
			int delay = highBits >>> 4;
			if(delay >= 1 && delay <= 4) result = result.setValue(RepeaterBlock.DELAY, delay);
		}
		else if(highBits == 0x10)
		{
			if(result.hasProperty(ComparatorBlock.MODE)
					&& result.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE)
			{
				result = result.setValue(ComparatorBlock.MODE, ComparatorMode.SUBTRACT);
			}
			else if(result.hasProperty(BlockStateProperties.HALF)
					&& result.getValue(BlockStateProperties.HALF) == Half.BOTTOM)
			{
				result = result.setValue(BlockStateProperties.HALF, Half.TOP);
			}
		}
		return result;
	}

	public static Direction directionFromProtocol(
			BlockState state,
			int protocolValue,
			BlockPlaceContext context
	)
	{
		int index = protocolValue & 0xf;
		if(index <= 5) return DIRECTIONS[index];
		if(index == 6) return context.getHorizontalDirection().getOpposite();
		return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
				? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
				: Direction.NORTH;
	}

	public static Optional<EnumProperty<Direction>> getFirstDirectionProperty(BlockState state)
	{
		for(Property<?> property : state.getProperties())
		{
			if(property instanceof EnumProperty<?> enumProperty
					&& enumProperty.getValueClass() == Direction.class)
			{
				@SuppressWarnings("unchecked")
				EnumProperty<Direction> directionProperty = (EnumProperty<Direction>) enumProperty;
				return Optional.of(directionProperty);
			}
		}
		return Optional.empty();
	}

	public static Direction getFirstDirection(BlockState state)
	{
		return getFirstDirectionProperty(state)
				.map(state::getValue)
				.filter(direction -> direction.getAxis().isHorizontal())
				.orElse(Direction.NORTH);
	}

	public static int verticalDirectionIndex(Direction direction)
	{
		Direction[] directions = {
				Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
		};
		for(int index = 0; index < directions.length; index++)
		{
			if(directions[index] == direction) return index;
		}
		return 0;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static BlockState setFirstDirection(BlockState state, Direction direction)
	{
		Optional<EnumProperty<Direction>> property = getFirstDirectionProperty(state);
		if(property.isEmpty() || !property.get().getPossibleValues().contains(direction))
		{
			return state;
		}
		return state.setValue((Property) property.get(), direction);
	}

	public static Half halfFromBit(int protocolValue)
	{
		return (protocolValue & 0x4) != 0 ? Half.TOP : Half.BOTTOM;
	}

	public static int getInt(BlockState state, Property<Integer> property)
	{
		return state.hasProperty(property) ? state.getValue(property) : 0;
	}

	public static <T extends Comparable<T>> @Nullable BlockState replaceSlot(
			BlockState state,
			Property<T> property,
			T value
	)
	{
		if(!state.hasProperty(property) || !property.getPossibleValues().contains(value))
		{
			return null;
		}
		return state.setValue(property, value);
	}

	public static @Nullable BlockState validateIncremental(
			BlockState state,
			Property<Integer> property,
			int requestedMaximum
	)
	{
		if(!state.hasProperty(property))
		{
			return null;
		}
		return state.getValue(property) <= requestedMaximum ? state : null;
	}
}
