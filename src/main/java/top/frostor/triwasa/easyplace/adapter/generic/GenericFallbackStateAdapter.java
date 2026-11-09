package top.frostor.triwasa.easyplace.adapter.generic;

import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class GenericFallbackStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return true;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.encodeGeneric(context.state());
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
	}
}
