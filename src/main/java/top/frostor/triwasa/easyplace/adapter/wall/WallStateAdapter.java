package top.frostor.triwasa.easyplace.adapter.wall;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class WallStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	private static final WallSide[] WALL_SIDES = WallSide.values();

	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof WallBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		if(!TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
		{
			return -1;
		}
		BlockState state = context.state();
		return state.getValue(WallBlock.NORTH).ordinal()
				+ state.getValue(WallBlock.EAST).ordinal() * 3
				+ state.getValue(WallBlock.SOUTH).ordinal() * 9
				+ state.getValue(WallBlock.WEST).ordinal() * 27;
	}

	@Override
	public int encodeAuxiliary(EasyPlaceCodecContext context)
	{
		return context.state().getValue(WallBlock.UP) ? 1 : 0;
	}

	@Override
	public @Nullable BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		if(rawValue < 0 || rawValue > 80)
		{
			return null;
		}

		BlockState state = context.state();
		state = EasyPlaceStateSupport.replaceSlot(
				state,
				WallBlock.NORTH,
				WALL_SIDES[rawValue % 3]
		);
		if(state == null)
		{
			return null;
		}
		state = EasyPlaceStateSupport.replaceSlot(
				state,
				WallBlock.EAST,
				WALL_SIDES[(rawValue / 3) % 3]
		);
		if(state == null)
		{
			return null;
		}
		state = EasyPlaceStateSupport.replaceSlot(
				state,
				WallBlock.SOUTH,
				WALL_SIDES[(rawValue / 9) % 3]
		);
		if(state == null)
		{
			return null;
		}
		state = EasyPlaceStateSupport.replaceSlot(
				state,
				WallBlock.WEST,
				WALL_SIDES[(rawValue / 27) % 3]
		);
		if(state == null)
		{
			return null;
		}
		return EasyPlaceStateSupport.replaceSlot(
				state,
				WallBlock.UP,
				context.auxiliaryValue() >= 0 && (context.auxiliaryValue() & 0x1) != 0
		);
	}
}
