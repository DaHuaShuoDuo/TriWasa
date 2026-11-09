package top.frostor.triwasa.easyplace.adapter.directional;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class DoorStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof DoorBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int value = verticalDirectionIndex(state.getValue(DoorBlock.FACING));
		value |= (state.getValue(DoorBlock.HINGE).ordinal() & 0x1) << 2;
		if(!context.block().equals(Blocks.IRON_DOOR) && state.getValue(DoorBlock.OPEN))
		{
			value |= 0x8;
		}
		if(state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
		{
			value |= 0x10;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		BlockState result = context.state()
				.setValue(DoorBlock.FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
				.setValue(DoorBlock.HINGE, DoorHingeSide.values()[(rawValue & 0x4) >>> 2])
				.setValue(DoorBlock.HALF, (rawValue & 0x10) != 0
						? DoubleBlockHalf.UPPER
						: DoubleBlockHalf.LOWER);
		if(!context.block().equals(Blocks.IRON_DOOR))
		{
			result = result.setValue(DoorBlock.OPEN, (rawValue & 0x8) != 0);
		}
		return result;
	}

	private static int verticalDirectionIndex(Direction direction)
	{
		for(int index = 0; index < VERTICAL_DIRECTIONS.length; index++)
		{
			if(VERTICAL_DIRECTIONS[index] == direction)
			{
				return index;
			}
		}
		return 0;
	}
}
