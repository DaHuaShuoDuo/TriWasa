package top.frostor.triwasa.easyplace.adapter.directional;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class StairStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof StairBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int value = verticalDirectionIndex(state.getValue(StairBlock.FACING));
		if(state.getValue(StairBlock.HALF) == Half.TOP)
		{
			value |= 0x4;
		}
		return value | ((state.getValue(StairBlock.SHAPE).ordinal() & 0x7) << 3);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		return context.state()
				.setValue(StairBlock.FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
				.setValue(StairBlock.HALF, (rawValue & 0x4) != 0 ? Half.TOP : Half.BOTTOM)
				.setValue(StairBlock.SHAPE, StairsShape.values()[(rawValue & 0x38) >>> 3]);
	}

	private static int verticalDirectionIndex(Direction direction)
	{
		for(int index = 0; index < VERTICAL_DIRECTIONS.length; index++)
		{
			if(VERTICAL_DIRECTIONS[index] == direction)
			{
				return index;
			}
		}
		return 0;
	}
}
