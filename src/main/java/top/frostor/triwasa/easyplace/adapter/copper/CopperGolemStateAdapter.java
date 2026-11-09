package top.frostor.triwasa.easyplace.adapter.copper;

import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class CopperGolemStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CopperGolemStatueBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.encodeGeneric(context.state())
				| ((context.state().getValue(BlockStateProperties.COPPER_GOLEM_POSE).ordinal() & 0x3) << 4);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState generic = EasyPlaceStateSupport.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
		CopperGolemStatueBlock.Pose[] poses = CopperGolemStatueBlock.Pose.values();
		return EasyPlaceStateSupport.replaceSlot(
				generic,
				BlockStateProperties.COPPER_GOLEM_POSE,
				poses[(context.rawValue() >>> 4) & 0x3]
		);
	}
}
