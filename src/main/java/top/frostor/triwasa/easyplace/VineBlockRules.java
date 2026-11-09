package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public final class VineBlockRules
{
	private VineBlockRules()
	{
	}

	public static boolean hasFace(BlockState state, Direction direction)
	{
		Property<Boolean> property = VineBlock.getPropertyForFace(direction);
		return state.hasProperty(property) && state.getValue(property);
	}

	public static int countFaces(BlockState state)
	{
		int count = 0;
		for(Direction direction : Direction.values())
		{
			if(hasFace(state, direction))
			{
				count++;
			}
		}
		return count;
	}

	public static @Nullable Direction findNextFace(
			@Nullable BlockState currentState,
			BlockState requiredState
	)
	{
		if(!(requiredState.getBlock() instanceof VineBlock))
		{
			return null;
		}

		if(currentState == null || currentState.getBlock() != requiredState.getBlock())
		{
			return firstRequiredFace(requiredState);
		}

		for(Direction direction : Direction.values())
		{
			if(hasFace(currentState, direction) && !hasFace(requiredState, direction))
			{
				return null;
			}
		}

		for(Direction direction : Direction.values())
		{
			if(hasFace(requiredState, direction) && !hasFace(currentState, direction))
			{
				return direction;
			}
		}
		return null;
	}

	public static @Nullable Direction findAttachableMissingFace(
			@Nullable BlockState currentState,
			BlockState requiredState,
			BlockGetter level,
			BlockPos targetPos
	)
	{
		return requiredState.getBlock() instanceof VineBlock
				? MultiFaceBlockRules.findAttachableMissingFace(
						currentState,
						requiredState,
						level,
						targetPos
				)
				: null;
	}

	public static int directionIndex(Direction direction)
	{
		return MultiFaceBlockRules.wireDirectionIndex(Blocks.VINE, direction);
	}

	public static @Nullable Direction directionByIndex(int index)
	{
		return MultiFaceBlockRules.wireDirection(Blocks.VINE, index);
	}

	public static boolean canAttach(BlockGetter level, BlockPos targetPos, Direction direction)
	{
		if(direction == Direction.DOWN)
		{
			return false;
		}

		BlockPos supportPos = targetPos.relative(direction);
		if(VineBlock.isAcceptableNeighbour(level, supportPos, direction))
		{
			return true;
		}
		if(direction.getAxis() == Direction.Axis.Y)
		{
			return false;
		}

		BlockState above = level.getBlockState(targetPos.above());
		return above.is(Blocks.VINE) && above.getValue(VineBlock.getPropertyForFace(direction));
	}

	private static @Nullable Direction firstRequiredFace(BlockState requiredState)
	{
		for(Direction direction : Direction.values())
		{
			if(hasFace(requiredState, direction))
			{
				return direction;
			}
		}
		return null;
	}
}
