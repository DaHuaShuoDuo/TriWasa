package top.frostor.triwasa.easyplace.adapter.directional;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.LegacyEasyPlaceStateCodec;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class FenceGateStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof FenceGateBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.encodeGeneric(context.state())
				| (context.state().getValue(FenceGateBlock.OPEN) ? 0x10 : 0);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState generic = LegacyEasyPlaceStateCodec.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
		return LegacyEasyPlaceStateCodec.replaceSlot(
				generic,
				FenceGateBlock.OPEN,
				(context.rawValue() & 0x10) != 0
		);
	}
}
