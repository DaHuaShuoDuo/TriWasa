package top.frostor.triwasa.easyplace.adapter.redstone;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class RedstoneStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof RedstoneWallTorchBlock
				|| context.block() instanceof RedstoneTorchBlock
				|| context.block() == Blocks.REDSTONE_WIRE;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		if(context.block() == Blocks.REDSTONE_WIRE)
		{
			boolean allDisconnected = state.getValue(BlockStateProperties.NORTH_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(BlockStateProperties.EAST_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(BlockStateProperties.SOUTH_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(BlockStateProperties.WEST_REDSTONE) == RedstoneSide.NONE;
			return allDisconnected ? 1 : 0;
		}
		return state.getValue(RedstoneTorchBlock.LIT) ? 1 : 0;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		if(context.block() instanceof RedstoneWallTorchBlock)
		{
			return EasyPlaceStateSupport.replaceSlot(
					context.state(),
					RedstoneWallTorchBlock.LIT,
					(rawValue & 1) != 0
			);
		}
		if(context.block() instanceof RedstoneTorchBlock)
		{
			return EasyPlaceStateSupport.replaceSlot(
					context.state(),
					RedstoneTorchBlock.LIT,
					(rawValue & 1) != 0
			);
		}
		if((rawValue & 1) == 0)
		{
			return context.state();
		}
		return context.state()
				.setValue(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
				.setValue(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
				.setValue(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
				.setValue(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE);
	}
}
