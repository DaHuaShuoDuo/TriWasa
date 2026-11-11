package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.util.function.Function;

public final class EasyPlacePistonProtection
{
	private EasyPlacePistonProtection()
	{
	}

	public static boolean isEnabled()
	{
		return TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.getBooleanValue();
	}

	public static int protectionMask(
			@Nullable BlockState projectedTarget,
			@Nullable Function<BlockPos, BlockState> actualStates,
			@Nullable Function<BlockPos, BlockState> projectedStates,
			BlockPos placementPos
	)
	{
		int mask = isUnextendedPiston(projectedTarget)
				? EasyPlaceExtraProtocol.PISTON_TARGET_MATCH
				: 0;
		if(actualStates == null || projectedStates == null)
		{
			return mask;
		}

		for(Direction direction : Direction.values())
		{
			BlockPos neighborPos = placementPos.relative(direction);
			if(matchesProjectedPiston(
					actualStates.apply(neighborPos),
					projectedStates.apply(neighborPos)
			))
			{
				mask |= 1 << (direction.ordinal() + 1);
			}
		}
		for(BlockPos offset : EasyPlaceExtraProtocol.pistonQcTargetOffsets())
		{
			BlockPos qcPos = placementPos.offset(
					offset.getX(),
					offset.getY(),
					offset.getZ()
			);
			if(matchesProjectedPiston(
					actualStates.apply(qcPos),
					projectedStates.apply(qcPos)
			))
			{
				mask |= EasyPlaceExtraProtocol.pistonQcTargetMatchBit(
						placementPos,
						qcPos
				);
			}
		}
		return mask;
	}

	public static boolean matchesProjectedPiston(
			@Nullable BlockState actual,
			@Nullable BlockState projected
	)
	{
		return actual != null
				&& projected != null
				&& actual.getBlock() == projected.getBlock()
				&& actual.getBlock() instanceof PistonBaseBlock
				&& actual.equals(projected);
	}

	public static boolean isUnextendedPiston(@Nullable BlockState state)
	{
		return state != null
				&& state.getBlock() instanceof PistonBaseBlock
				&& !state.getValue(PistonBaseBlock.EXTENDED);
	}
}
