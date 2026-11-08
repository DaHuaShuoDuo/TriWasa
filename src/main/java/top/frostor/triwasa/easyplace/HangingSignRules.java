package top.frostor.triwasa.easyplace;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACHED;

public final class HangingSignRules
{
	private HangingSignRules()
	{
	}

	public static boolean shouldAllowUnsupportedWallPlacement(BlockState state)
	{
		return EasyPlaceRules.isActive()
				&& state.getBlock() instanceof WallHangingSignBlock;
	}

	public static ClickTarget getForcedWallClickTarget(BlockPos targetPos, BlockState state)
	{
		if(state.getBlock() instanceof WallHangingSignBlock)
		{
			return new ClickTarget(
					targetPos,
					state.getValue(WallHangingSignBlock.FACING).getOpposite()
			);
		}
		return null;
	}

	public static boolean shouldSuppressEasyPlaceChaining(BlockState state, Item item)
	{
		return EasyPlaceRules.isActive()
				&& state.getBlock() instanceof WallHangingSignBlock
				&& item instanceof HangingSignItem;
	}

	public static boolean hasRequiredCeilingHangingSignSupport(
			ClientLevel level,
			BlockPos pos,
			BlockState requiredState
	)
	{
		if(!(requiredState.getBlock() instanceof CeilingHangingSignBlock)
				|| !requiredState.getValue(ATTACHED))
		{
			return true;
		}

		BlockState above = level.getBlockState(pos.above());
		return above.getBlock() instanceof ChainBlock
				&& above.getValue(ChainBlock.AXIS) == Direction.Axis.Y;
	}

	public record ClickTarget(BlockPos pos, Direction side)
	{
	}
}