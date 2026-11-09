package top.frostor.triwasa.easyplace.adapter.lantern;

import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class LanternStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof LanternBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return context.state().getValue(LanternBlock.HANGING) ? 1 : 0;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				LanternBlock.HANGING,
				(context.rawValue() & 1) != 0
		);
	}
}
