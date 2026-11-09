package top.frostor.triwasa.easyplace.adapter.rail;

import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

import java.util.Collection;
import java.util.List;

public final class RailStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof PoweredRailBlock
				|| context.block() instanceof RailBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		if(context.block() instanceof PoweredRailBlock)
		{
			int value = context.state().getValue(PoweredRailBlock.SHAPE).ordinal() & 0x7;
			if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
			{
				value |= 0x8;
			}
			return value;
		}

		int value = context.state().getValue(RailBlock.SHAPE).ordinal() & 0xf;
		if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
		{
			value |= 0x10;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int mask = context.block() instanceof PoweredRailBlock ? 0x7 : 0xf;
		return EasyPlaceStateSupport.replaceSlot(
				context.state(),
				railShapeProperty(context.state()),
				railShape(context.state(), context.rawValue() & mask)
		);
	}

	private static Property<RailShape> railShapeProperty(BlockState state)
	{
		return state.getBlock() instanceof PoweredRailBlock
				? PoweredRailBlock.SHAPE
				: RailBlock.SHAPE;
	}

	private static RailShape railShape(BlockState state, int index)
	{
		Collection<RailShape> values = railShapeProperty(state).getPossibleValues();
		List<RailShape> shapes = values.stream().toList();
		return shapes.get(Math.floorMod(index, shapes.size()));
	}
}
