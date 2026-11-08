package top.frostor.triwasa.easyplace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public final class IncrementalBlockRules
{
	private IncrementalBlockRules()
	{
	}

	public static boolean canIncrement(BlockState currentState, BlockState requiredState)
	{
		if(currentState.getBlock() != requiredState.getBlock())
		{
			return false;
		}

		if(MultiFaceBlockRules.isMultiFaceBlock(requiredState))
		{
			return MultiFaceBlockRules.findNextFace(currentState, requiredState) != null;
		}

		IntegerProperty property = getCountProperty(currentState.getBlock());
		return property != null
				&& currentState.hasProperty(property)
				&& requiredState.hasProperty(property)
				&& currentState.getValue(property) < requiredState.getValue(property);
	}

	public static boolean shouldRetryAfterPlacement(
			@Nullable BlockState currentState,
			BlockState requiredState
	)
	{
		if(isMultiFaceBlock(requiredState))
		{
			return MultiFaceBlockRules.countFaces(requiredState) > 1
					&& MultiFaceBlockRules.findMissingFace(currentState, requiredState) != null;
		}

		IntegerProperty property = getCountProperty(requiredState.getBlock());
		if(property == null
				|| !requiredState.hasProperty(property)
				|| requiredState.getValue(property) <= 1)
		{
			return false;
		}
		if(currentState == null || currentState.getBlock() != requiredState.getBlock())
		{
			return true;
		}
		return currentState.hasProperty(property)
				&& currentState.getValue(property) < requiredState.getValue(property);
	}

	public static boolean isMultiFaceBlock(BlockState state)
	{
		return MultiFaceBlockRules.isMultiFaceBlock(state);
	}

	public static boolean isIncrementalBlock(BlockState state)
	{
		if(state == null)
		{
			return false;
		}
		if(isMultiFaceBlock(state))
		{
			return true;
		}

		IntegerProperty property = getCountProperty(state.getBlock());
		return property != null && state.hasProperty(property);
	}

	public static @Nullable Direction findNextFace(
			@Nullable BlockState currentState,
			BlockState requiredState
	)
	{
		return MultiFaceBlockRules.findNextFace(currentState, requiredState);
	}

	public static int multifaceDirectionIndex(Direction direction)
	{
		Direction[] directions = Direction.values();
		for(int index = 0; index < directions.length; index++)
		{
			if(directions[index] == direction)
			{
				return index;
			}
		}
		return -1;
	}

	public static @Nullable IntegerProperty getCountProperty(Block block)
	{
		if(block instanceof SegmentableBlock segmentableBlock)
		{
			return segmentableBlock.getSegmentAmountProperty();
		}
		if(block instanceof CandleBlock)
		{
			return CandleBlock.CANDLES;
		}
		if(block instanceof SeaPickleBlock)
		{
			return SeaPickleBlock.PICKLES;
		}
		if(block instanceof SnowLayerBlock)
		{
			return SnowLayerBlock.LAYERS;
		}
		if(block instanceof TurtleEggBlock)
		{
			return TurtleEggBlock.EGGS;
		}
		return null;
	}
}
