package top.frostor.triwasa.easyplace;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.config.TriWasaConfigs;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACHED;

public final class HangingSignRules
{
	private HangingSignRules()
	{
	}

	public static boolean isForcedPlacementEnabled()
	{
		return TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				|| TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
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
}
