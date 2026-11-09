package top.frostor.triwasa.easyplace.adapter.container;

import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.LegacyEasyPlaceStateCodec;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class ChestStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof ChestBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		int type = switch(context.state().getValue(ChestBlock.TYPE))
		{
			case SINGLE -> 0;
			case LEFT -> 1;
			case RIGHT -> 2;
		};
		return LegacyEasyPlaceStateCodec.encodeGeneric(context.state()) | 0x40 | (type << 4);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
	}
}
