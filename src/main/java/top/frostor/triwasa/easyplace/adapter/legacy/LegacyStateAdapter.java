package top.frostor.triwasa.easyplace.adapter.legacy;

import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.LegacyEasyPlaceStateCodec;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class LegacyStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.encodeRegular(context.state(), context.currentState());
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.apply(
				context.block(),
				context.state(),
				context.placementContext(),
				context.rawValue()
		);
	}
}
