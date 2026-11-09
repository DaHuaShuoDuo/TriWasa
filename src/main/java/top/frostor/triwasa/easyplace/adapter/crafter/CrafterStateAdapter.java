package top.frostor.triwasa.easyplace.adapter.crafter;

import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class CrafterStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CrafterBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return context.state().hasProperty(BlockStateProperties.ORIENTATION)
				? context.state().getValue(BlockStateProperties.ORIENTATION).ordinal() & 0xf
				: 0;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		FrontAndTop[] values = FrontAndTop.values();
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				BlockStateProperties.ORIENTATION,
				values[Math.floorMod(context.rawValue(), values.length)]
		);
	}
}
