package top.frostor.triwasa.easyplace;

import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CandleRules
{
	private CandleRules()
	{
	}

	public static boolean shouldAllowUnsupportedPlacement(BlockState state)
	{
		return EasyPlaceRules.isActive()
				&& state.getBlock() instanceof CandleBlock;
	}
}
