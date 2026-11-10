package top.frostor.triwasa.easyplace.adapter.rail;

import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.EasyPlaceExtraProtocol;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class DetectorRailStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof DetectorRailBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		int value = context.state().getValue(DetectorRailBlock.SHAPE).ordinal() & 0x7;
		if(TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue()
				&& context.state().getValue(DetectorRailBlock.POWERED))
		{
			value |= EasyPlaceExtraProtocol.RAIL_POWERED_MASK;
		}
		if(TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue())
		{
			value |= 0x8;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState state = EasyPlaceStateSupport.replaceSlot(
				context.state(),
				DetectorRailBlock.SHAPE,
				RailShape.values()[(context.rawValue() & 0x7) % 6]
		);
		if(state == null)
		{
			return null;
		}
		return state.setValue(
				DetectorRailBlock.POWERED,
				(context.rawValue() & EasyPlaceExtraProtocol.RAIL_POWERED_MASK) != 0
		);
	}
}
