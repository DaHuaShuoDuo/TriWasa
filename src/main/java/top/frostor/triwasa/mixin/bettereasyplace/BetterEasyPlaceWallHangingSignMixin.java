package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.HangingSignRules;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@Mixin(WallHangingSignBlock.class)
public abstract class BetterEasyPlaceWallHangingSignMixin
{
	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void triwasa$forceUnsupportedWallHangingSign(
			BlockPlaceContext context,
			CallbackInfoReturnable<BlockState> cir
	)
	{
		if(!HangingSignRules.isForcedPlacementEnabled() || cir.getReturnValue() != null)
		{
			return;
		}

		Direction facing = context.getHorizontalDirection().getOpposite();
		BlockState state = ((WallHangingSignBlock) (Object) this)
				.defaultBlockState()
				.setValue(WallHangingSignBlock.FACING, facing)
				.setValue(
						WATERLOGGED,
						context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER)
				);
		cir.setReturnValue(state);
	}
}
