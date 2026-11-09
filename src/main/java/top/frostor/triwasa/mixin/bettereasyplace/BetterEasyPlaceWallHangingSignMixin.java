package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.HangingSignRules;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@Mixin(WallHangingSignBlock.class)
public abstract class BetterEasyPlaceWallHangingSignMixin
{
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void triwasa$suppressEasyPlaceChaining(
			ItemStack stack,
			BlockState state,
			Level level,
			BlockPos pos,
			Player player,
			InteractionHand hand,
			BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> cir
	)
	{
		if(HangingSignRules.shouldSuppressEasyPlaceChaining(state, stack.getItem()))
		{
			cir.setReturnValue(InteractionResult.SUCCESS);
		}
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void triwasa$forceUnsupportedWallHangingSign(
			BlockPlaceContext context,
			CallbackInfoReturnable<BlockState> cir
	)
	{
		if(!EasyPlaceRules.isEnabled() || cir.getReturnValue() != null)
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
