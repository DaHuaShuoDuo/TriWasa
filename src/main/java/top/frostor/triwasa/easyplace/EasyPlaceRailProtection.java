package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class EasyPlaceRailProtection
{
	private static final Direction[] RAIL_DIRECTIONS = {
			Direction.NORTH,
			Direction.SOUTH,
			Direction.WEST,
			Direction.EAST,
			Direction.UP
	};

	private EasyPlaceRailProtection()
	{
	}

	public static int neighborMatchMask(
			@Nullable Function<BlockPos, BlockState> actualStates,
			@Nullable Function<BlockPos, BlockState> projectedStates,
			BlockPos placementPos
	)
	{
		if(actualStates == null || projectedStates == null)
		{
			return 0;
		}

		int mask = 0;
		for(Direction direction : RAIL_DIRECTIONS)
		{
			BlockPos neighborPos = placementPos.relative(direction);
			if(matchesProjectedRail(
					actualStates.apply(neighborPos),
					projectedStates.apply(neighborPos)
			))
			{
				mask |= 1 << direction.ordinal();
			}
		}
		return mask;
	}

	public static boolean matchesProjectedRail(
			@Nullable BlockState actual,
			@Nullable BlockState projected
	)
	{
		if(actual == null
				|| projected == null
				|| !(actual.getBlock() instanceof BaseRailBlock)
				|| !(projected.getBlock() instanceof BaseRailBlock)
				|| actual.getBlock() != projected.getBlock())
		{
			return false;
		}

		for(Property<?> property : actual.getProperties())
		{
			if(property == BaseRailBlock.WATERLOGGED)
			{
				continue;
			}
			if(property == PoweredRailBlock.POWERED)
			{
				continue;
			}
			Object actualValue = actual.getValue(property);
			Object projectedValue = projected.getValue(property);
			if(!actualValue.equals(projectedValue))
			{
				return false;
			}
		}
		return true;
	}

	public static int applyNeighborMatchMask(int auxiliaryValue, int neighborMatchMask)
	{
		return (auxiliaryValue & ~EasyPlaceExtraProtocol.RAIL_NEIGHBOR_MATCH_MASK)
				| ((neighborMatchMask & 0x3f)
				<< Integer.numberOfTrailingZeros(EasyPlaceExtraProtocol.RAIL_NEIGHBOR_MATCH_BASE));
	}
}
