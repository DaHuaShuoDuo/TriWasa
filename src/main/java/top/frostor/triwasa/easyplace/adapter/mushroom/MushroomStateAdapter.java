package top.frostor.triwasa.easyplace.adapter.mushroom;

import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class MushroomStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof HugeMushroomBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		return (state.getValue(BlockStateProperties.NORTH) ? 0x01 : 0)
				| (state.getValue(BlockStateProperties.EAST) ? 0x02 : 0)
				| (state.getValue(BlockStateProperties.SOUTH) ? 0x04 : 0)
				| (state.getValue(BlockStateProperties.WEST) ? 0x08 : 0)
				| (state.getValue(BlockStateProperties.UP) ? 0x10 : 0)
				| (state.getValue(BlockStateProperties.DOWN) ? 0x20 : 0);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		return context.state()
				.setValue(BlockStateProperties.NORTH, (rawValue & 0x01) != 0)
				.setValue(BlockStateProperties.EAST, (rawValue & 0x02) != 0)
				.setValue(BlockStateProperties.SOUTH, (rawValue & 0x04) != 0)
				.setValue(BlockStateProperties.WEST, (rawValue & 0x08) != 0)
				.setValue(BlockStateProperties.UP, (rawValue & 0x10) != 0)
				.setValue(BlockStateProperties.DOWN, (rawValue & 0x20) != 0);
	}
}
