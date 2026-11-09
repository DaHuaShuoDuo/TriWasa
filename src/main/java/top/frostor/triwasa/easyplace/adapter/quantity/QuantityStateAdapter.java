package top.frostor.triwasa.easyplace.adapter.quantity;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.easyplace.IncrementalBlockRules;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class QuantityStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CandleBlock
				|| context.block() instanceof FlowerBedBlock
				|| context.block() instanceof LeafLitterBlock
				|| context.block() instanceof SeaPickleBlock
				|| context.block() instanceof SnowLayerBlock
				|| context.block() instanceof TurtleEggBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		if(context.block() instanceof FlowerBedBlock)
		{
			int value = verticalDirectionIndex(state.getValue(FlowerBedBlock.FACING)) << 2;
			return value | ((state.getValue(FlowerBedBlock.AMOUNT) - 1) & 0x3);
		}
		if(context.block() instanceof LeafLitterBlock)
		{
			int value = verticalDirectionIndex(state.getValue(LeafLitterBlock.FACING)) << 2;
			return value | ((state.getValue(SegmentableBlock.AMOUNT) - 1) & 0x3);
		}
		IntegerProperty property = IncrementalBlockRules.getCountProperty(context.block());
		if(property == null)
		{
			return -1;
		}
		int mask = context.block() instanceof SnowLayerBlock ? 0x7 : 0x3;
		return (state.getValue(property) - 1) & mask;
	}

	@Override
	public @Nullable BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		IntegerProperty property = IncrementalBlockRules.getCountProperty(context.block());
		if(property == null)
		{
			return null;
		}
		int rawValue = context.rawValue();
		int requestedAmount;
		if(context.block() instanceof FlowerBedBlock)
		{
			state = state.setValue(
					FlowerBedBlock.FACING,
					VERTICAL_DIRECTIONS[(rawValue & 0xc) >>> 2]
			);
			requestedAmount = (rawValue & 0x3) + 1;
		}
		else if(context.block() instanceof LeafLitterBlock)
		{
			state = state.setValue(
					LeafLitterBlock.FACING,
					VERTICAL_DIRECTIONS[(rawValue & 0xc) >>> 2]
			);
			requestedAmount = (rawValue & 0x3) + 1;
		}
		else if(context.block() instanceof SnowLayerBlock)
		{
			requestedAmount = (rawValue & 0x7) + 1;
		}
		else
		{
			requestedAmount = (rawValue & 0x3) + 1;
		}
		return EasyPlaceStateSupport.validateIncremental(state, property, requestedAmount);
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
