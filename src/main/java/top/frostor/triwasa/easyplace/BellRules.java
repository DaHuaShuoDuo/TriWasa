package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BellRules
{
	private static final ThreadLocal<BellPlacementPlan> ACTIVE_PLAN = new ThreadLocal<>();

	private BellRules()
	{
	}

	public static @Nullable BellPlacementPlan getPlacementPlan(
			BlockPos targetPos,
			BlockState state
	)
	{
		Direction facing = wallFacing(state);
		if(facing == null)
		{
			return null;
		}

		return new BellPlacementPlan(
				targetPos,
				targetPos.relative(facing),
				facing.getOpposite()
		);
	}

	public static boolean hasRequiredWallSupport(
			LevelReader level,
			BellPlacementPlan plan
	)
	{
		return FaceAttachedHorizontalDirectionalBlock.canAttach(
				level,
				plan.targetPos(),
				plan.side().getOpposite()
		);
	}

	public static BlockPos resolveProtocolTarget(
			LevelReader level,
			BlockPos clickPos,
			BlockState state
	)
	{
		Direction facing = wallFacing(state);
		if(facing == null)
		{
			return clickPos;
		}

		BellPlacementPlan plan = ACTIVE_PLAN.get();
		if(plan != null && plan.supportPos().equals(clickPos))
		{
			return plan.targetPos();
		}

		BlockPos targetPos = clickPos.relative(facing.getOpposite());
		return FaceAttachedHorizontalDirectionalBlock.canAttach(level, targetPos, facing)
				? targetPos
				: clickPos;
	}

	public static void activatePlan(BellPlacementPlan plan)
	{
		ACTIVE_PLAN.set(plan);
	}

	public static @Nullable BellPlacementPlan takeClickPlan(BlockHitResult hitResult)
	{
		BellPlacementPlan plan = ACTIVE_PLAN.get();
		if(plan == null
				|| !plan.supportPos().equals(hitResult.getBlockPos()))
		{
			return null;
		}

		ACTIVE_PLAN.remove();
		return hitResult.getDirection() == plan.side() ? null : plan;
	}

	private static boolean isWallAttached(BlockState state)
	{
		if(!state.hasProperty(BellBlock.ATTACHMENT))
		{
			return false;
		}

		BellAttachType attachment = state.getValue(BellBlock.ATTACHMENT);
		return attachment == BellAttachType.SINGLE_WALL
				|| attachment == BellAttachType.DOUBLE_WALL;
	}

	private static @Nullable Direction wallFacing(BlockState state)
	{
		if(!(state.getBlock() instanceof BellBlock)
				|| !isWallAttached(state)
				|| !state.hasProperty(BellBlock.FACING))
		{
			return null;
		}

		return state.getValue(BellBlock.FACING);
	}

	public record BellPlacementPlan(BlockPos targetPos, BlockPos supportPos, Direction side)
	{
		public BellPlacementPlan
		{
			targetPos = targetPos.immutable();
			supportPos = supportPos.immutable();
		}

		public Vec3 hitVec()
		{
			return Vec3.atCenterOf(supportPos).relative(side, 0.5);
		}
	}
}
