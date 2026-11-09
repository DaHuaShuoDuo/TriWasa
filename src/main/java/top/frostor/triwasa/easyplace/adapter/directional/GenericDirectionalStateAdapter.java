package top.frostor.triwasa.easyplace.adapter.directional;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import top.frostor.triwasa.easyplace.LegacyEasyPlaceStateCodec;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class GenericDirectionalStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		return !LegacyEasyPlaceStateCodec.hasSpecializedHandling(context.block())
				&& (hasDirectionProperty(state)
				|| state.hasProperty(BlockStateProperties.AXIS)
				|| state.hasProperty(BlockStateProperties.SLAB_TYPE)
				|| state.hasProperty(BlockStateProperties.HALF)
				|| state.hasProperty(RepeaterBlock.DELAY)
				|| state.hasProperty(ComparatorBlock.MODE));
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.encodeGeneric(context.state());
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		return LegacyEasyPlaceStateCodec.applyGeneric(
				context.state(),
				context.rawValue(),
				context.placementContext()
		);
	}

	private static boolean hasDirectionProperty(BlockState state)
	{
		for(Property<?> property : state.getProperties())
		{
			if(property.getPossibleValues().stream().anyMatch(value -> value instanceof Direction))
			{
				return true;
			}
		}
		return false;
	}
}
