package top.frostor.triwasa.easyplace.adapter.sign;

import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class SignStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return (context.block() instanceof AbstractBannerBlock
				|| context.block() instanceof SignBlock
				|| context.block() instanceof SkullBlock)
				&& !(context.block() instanceof HangingSignBlock);
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		if(context.block() instanceof SkullBlock)
		{
			return getInt(state, SkullBlock.ROTATION) & 0xf;
		}
		return getInt(state, BlockStateProperties.ROTATION_16) & 0xf;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int rawValue = context.rawValue() & 0xf;
		if(context.block() instanceof SkullBlock)
		{
			return state.hasProperty(SkullBlock.ROTATION)
					? EasyPlaceStateSupport.replaceSlot(state, SkullBlock.ROTATION, rawValue)
					: state;
		}
		return state.hasProperty(BlockStateProperties.ROTATION_16)
				? EasyPlaceStateSupport.replaceSlot(
				state,
				BlockStateProperties.ROTATION_16,
				rawValue
		)
				: state;
	}

	private static int getInt(BlockState state, Property<Integer> property)
	{
		return state.hasProperty(property) ? state.getValue(property) : 0;
	}
}
