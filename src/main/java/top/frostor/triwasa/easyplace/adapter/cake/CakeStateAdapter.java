package top.frostor.triwasa.easyplace.adapter.cake;

import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class CakeStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CakeBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return context.state().getValue(BlockStateProperties.BITES) & 0x7;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				BlockStateProperties.BITES,
				context.rawValue() & 0x7
		);
	}
}
