package top.frostor.triwasa.easyplace.adapter.attachment;

import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import top.frostor.triwasa.easyplace.LegacyEasyPlaceStateCodec;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class FaceAttachedStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof FaceAttachedHorizontalDirectionalBlock
				&& !(context.block() instanceof RedstoneWallTorchBlock);
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int value = LegacyEasyPlaceStateCodec.encodeGeneric(state);
		value |= (state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE).ordinal() & 0x3) << 4;
		if(context.block() instanceof LeverBlock && state.getValue(LeverBlock.POWERED))
		{
			value |= 0x40;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState generic = LegacyEasyPlaceStateCodec.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
		BlockState result = LegacyEasyPlaceStateCodec.replaceSlot(
				generic,
				FaceAttachedHorizontalDirectionalBlock.FACE,
				AttachFace.values()[((context.rawValue() & 0x30) >>> 4) % 3]
		);
		if(context.block() instanceof LeverBlock)
		{
			return LegacyEasyPlaceStateCodec.replaceSlot(
					result,
					LeverBlock.POWERED,
					(context.rawValue() & 0x40) != 0
			);
		}
		return result;
	}
}
