package top.frostor.triwasa.easyplace.adapter.directional;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class TrapDoorStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof TrapDoorBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int value = verticalDirectionIndex(state.getValue(TrapDoorBlock.FACING));
		if(state.getValue(TrapDoorBlock.HALF) == Half.TOP)
		{
			value |= 0x4;
		}
		if(!context.block().equals(Blocks.IRON_TRAPDOOR) && state.getValue(TrapDoorBlock.OPEN))
		{
			value |= 0x8;
		}
		return value;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		BlockState result = context.state()
				.setValue(TrapDoorBlock.FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
				.setValue(TrapDoorBlock.HALF, (rawValue & 0x4) != 0 ? Half.TOP : Half.BOTTOM);
		if(!context.block().equals(Blocks.IRON_TRAPDOOR))
		{
			result = result.setValue(TrapDoorBlock.OPEN, (rawValue & 0x8) != 0);
		}
		return result;
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
