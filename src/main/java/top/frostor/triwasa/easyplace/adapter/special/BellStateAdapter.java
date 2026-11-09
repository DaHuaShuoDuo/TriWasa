package top.frostor.triwasa.easyplace.adapter.special;

import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class BellStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof BellBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		int attachment = context.state().hasProperty(BellBlock.ATTACHMENT)
				? context.state().getValue(BellBlock.ATTACHMENT).ordinal()
				: BellAttachType.FLOOR.ordinal();
		return EasyPlaceStateSupport.encodeGeneric(context.state()) | ((attachment & 0x3) << 4);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState generic = EasyPlaceStateSupport.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
		return EasyPlaceStateSupport.replaceSlot(
				generic,
				BellBlock.ATTACHMENT,
				BellAttachType.values()[(context.rawValue() & 0x30) >>> 4]
		);
	}
}
