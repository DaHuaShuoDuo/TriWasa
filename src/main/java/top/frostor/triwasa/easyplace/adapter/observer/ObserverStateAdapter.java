package top.frostor.triwasa.easyplace.adapter.observer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class ObserverStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof ObserverBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		if(!TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue())
		{
			return -1;
		}
		return context.state().getValue(ObserverBlock.FACING).get3DDataValue();
	}

	@Override
	public @Nullable BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		if(rawValue < 0 || rawValue >= Direction.values().length)
		{
			return null;
		}
		BlockState state = EasyPlaceStateSupport.replaceSlot(
				context.state(),
				ObserverBlock.FACING,
				Direction.values()[rawValue]
		);
		if(state == null)
		{
			return null;
		}
		return state.setValue(ObserverBlock.POWERED, false);
	}
}
