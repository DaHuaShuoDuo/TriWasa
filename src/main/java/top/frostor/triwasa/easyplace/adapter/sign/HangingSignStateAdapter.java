package top.frostor.triwasa.easyplace.adapter.sign;

import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class HangingSignStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof HangingSignBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int value = state.hasProperty(BlockStateProperties.ROTATION_16)
				? state.getValue(BlockStateProperties.ROTATION_16) & 0xf
				: 0;
		if(state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED))
		{
			value |= 0x10;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState result = context.state();
		if(result.hasProperty(BlockStateProperties.ROTATION_16))
		{
			result = EasyPlaceStateSupport.replaceSlot(
					result,
					BlockStateProperties.ROTATION_16,
					context.rawValue() & 0xf
			);
		}
		if(result.hasProperty(BlockStateProperties.ATTACHED))
		{
			result = result.setValue(
					BlockStateProperties.ATTACHED,
					(context.rawValue() & 0x10) != 0
			);
		}
		return result;
	}
}
