package top.frostor.triwasa.easyplace.adapter.rail;

import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import top.frostor.triwasa.config.TriWasaConfigs;
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
		if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
		{
			value |= 0x8;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				DetectorRailBlock.SHAPE,
				RailShape.values()[(context.rawValue() & 0x7) % 6]
		);
	}
}
