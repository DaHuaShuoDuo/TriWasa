package top.frostor.triwasa.easyplace.adapter.multiface;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.MultiFaceBlockRules;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class MultiFaceStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return MultiFaceBlockRules.isMultiFaceBlock(context.block());
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		Direction direction = context.level() != null && context.pos() != null
				? MultiFaceBlockRules.findAttachableMissingFace(
						context.currentState(),
						context.state(),
						context.level(),
						context.pos()
				)
				: MultiFaceBlockRules.findNextFace(context.currentState(), context.state());
		return direction == null
				? -1
				: MultiFaceBlockRules.wireDirectionIndex(context.block(), direction);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return MultiFaceBlockRules.applyPlacementState(
				context.block(),
				context.state(),
				context.placementContext(),
				context.rawValue()
		);
	}
}
