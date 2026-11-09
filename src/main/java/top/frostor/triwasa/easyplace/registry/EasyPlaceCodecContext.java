package top.frostor.triwasa.easyplace.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record EasyPlaceCodecContext(
		BlockState state,
		@Nullable BlockState currentState,
		@Nullable BlockGetter level,
		@Nullable BlockPos pos,
		@Nullable BlockPlaceContext placementContext,
		int rawValue
)
{
	public Block block()
	{
		return state.getBlock();
	}
}
