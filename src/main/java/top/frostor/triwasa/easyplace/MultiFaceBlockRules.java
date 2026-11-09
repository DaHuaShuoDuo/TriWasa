package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class MultiFaceBlockRules
{
	private static final Direction[] DIRECTIONS = {
			Direction.DOWN, Direction.UP, Direction.NORTH,
			Direction.SOUTH, Direction.WEST, Direction.EAST
	};
	private static final Direction[] VINE_DIRECTIONS = Arrays.stream(DIRECTIONS)
			.filter(direction -> direction != Direction.DOWN)
			.toArray(Direction[]::new);

	private MultiFaceBlockRules()
	{
	}

	public static boolean isMultiFaceBlock(Block block)
	{
		return block instanceof VineBlock || block instanceof MultifaceBlock;
	}

	public static boolean isMultiFaceBlock(BlockState state)
	{
		return state != null && isMultiFaceBlock(state.getBlock());
	}

	public static boolean hasFace(BlockState state, Direction direction)
	{
		if(state.getBlock() instanceof VineBlock)
		{
			return direction != Direction.DOWN && VineBlockRules.hasFace(state, direction);
		}
		if(state.getBlock() instanceof MultifaceBlock)
		{
			return state.hasProperty(MultifaceBlock.getFaceProperty(direction))
					&& MultifaceBlock.hasFace(state, direction);
		}
		return false;
	}

	public static int countFaces(BlockState state)
	{
		int count = 0;
		for(Direction direction : DIRECTIONS)
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
		if(!isMultiFaceBlock(requiredState))
		{
			return null;
		}
		if(currentState == null || currentState.getBlock() != requiredState.getBlock())
		{
			return findMissingFace(null, requiredState);
		}

		for(Direction direction : DIRECTIONS)
		{
			if(hasFace(currentState, direction) && !hasFace(requiredState, direction))
			{
				return null;
			}
		}
		return findMissingFace(currentState, requiredState);
	}

	public static @Nullable Direction findMissingFace(
			@Nullable BlockState currentState,
			BlockState requiredState
	)
	{
		for(Direction direction : DIRECTIONS)
		{
			boolean attached = currentState != null
					&& currentState.getBlock() == requiredState.getBlock()
					&& hasFace(currentState, direction);
			if(hasFace(requiredState, direction) && !attached)
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
		if(!isMultiFaceBlock(requiredState))
		{
			return null;
		}
		if(currentState != null && currentState.getBlock() == requiredState.getBlock())
		{
			for(Direction direction : DIRECTIONS)
			{
				if(hasFace(currentState, direction) && !hasFace(requiredState, direction))
				{
					return null;
				}
			}
		}

		for(Direction direction : DIRECTIONS)
		{
			boolean attached = currentState != null
					&& currentState.getBlock() == requiredState.getBlock()
					&& hasFace(currentState, direction);
			if(hasFace(requiredState, direction)
					&& !attached
					&& canAttach(requiredState.getBlock(), level, targetPos, direction))
			{
				return direction;
			}
		}
		return null;
	}

	public static boolean canAttach(
			Block block,
			BlockGetter level,
			BlockPos targetPos,
			Direction direction
	)
	{
		if(block instanceof VineBlock)
		{
			return VineBlockRules.canAttach(level, targetPos, direction);
		}
		if(block instanceof MultifaceBlock)
		{
			return MultifaceBlock.canAttachTo(level, targetPos, direction);
		}
		return false;
	}

	public static int wireDirectionIndex(Block block, Direction direction)
	{
		Direction[] directions = block instanceof VineBlock ? VINE_DIRECTIONS : DIRECTIONS;
		return directionIndex(directions, direction);
	}

	public static int multifaceDirectionIndex(Direction direction)
	{
		return directionIndex(DIRECTIONS, direction);
	}

	private static int directionIndex(Direction[] directions, Direction direction)
	{
		for(int index = 0; index < directions.length; index++)
		{
			if(directions[index] == direction)
			{
				return index;
			}
		}
		return -1;
	}

	public static @Nullable Direction wireDirection(Block block, int index)
	{
		Direction[] directions = block instanceof VineBlock ? VINE_DIRECTIONS : DIRECTIONS;
		int face = index & 0x7;
		return face < directions.length ? directions[face] : null;
	}

	public static @Nullable BlockState applyPlacementState(
			Block block,
			BlockState state,
			BlockPlaceContext context,
			int rawValue
	)
	{
		Direction direction = wireDirection(block, rawValue & 0x7);
		if(direction == null)
		{
			return null;
		}

		BlockPos targetPos = context.getClickedPos();
		BlockState currentState = context.getLevel().getBlockState(targetPos);
		if(block instanceof MultifaceBlock multifaceBlock)
		{
			return multifaceBlock.getStateForPlacement(
					currentState,
					context.getLevel(),
					targetPos,
					direction
			);
		}
		if(block instanceof VineBlock)
		{
			if(!VineBlockRules.canAttach(context.getLevel(), targetPos, direction))
			{
				return null;
			}
			return applyVineFace(block, currentState, direction);
		}
		return null;
	}

	static @Nullable BlockState applyVineFace(
			Block block,
			BlockState currentState,
			Direction direction
	)
	{
		Property<Boolean> property = VineBlock.getPropertyForFace(direction);
		BlockState base = currentState.is(block) ? currentState : block.defaultBlockState();
		if(!base.hasProperty(property) || base.getValue(property))
		{
			return null;
		}
		return base.setValue(property, true);
	}
}
