package top.frostor.triwasa.easyplace.adapter.daylight;

import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class DaylightDetectorStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof DaylightDetectorBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return context.state().getValue(DaylightDetectorBlock.INVERTED) ? 1 : 0;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				DaylightDetectorBlock.INVERTED,
				(context.rawValue() & 1) != 0
		);
	}
}
