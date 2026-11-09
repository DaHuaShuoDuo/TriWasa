package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.HangingSignRules;

@Mixin(HangingSignItem.class)
public abstract class BetterEasyPlaceHangingSignItemMixin
{
	@Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
	private static void triwasa$allowUnsupportedWallHangingSign(
			LevelReader level,
			BlockState state,
			BlockPos pos,
			CallbackInfoReturnable<Boolean> cir
	)
	{
		if(HangingSignRules.shouldAllowUnsupportedWallPlacement(state))
		{
			cir.setReturnValue(true);
		}
	}
}