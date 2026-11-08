package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.IncrementalBlockRules;

@Mixin(targets = "me.aleksilassila.litematica.printer.handler.HudStatsManager")
public abstract class BetterPrinterIncrementalPendingMixin
{
	@Final
	@Shadow
	private Minecraft client;

	@Inject(method = "isPrintPlacementPending", at = @At("HEAD"), cancellable = true)
	private void triwasa$allowIncrementalRetry(
			BlockPos pos,
			CallbackInfoReturnable<Boolean> cir
	)
	{
		if(EasyPlaceRules.isPrinterEnabled()
				&& client.level != null
				&& IncrementalBlockRules.isIncrementalBlock(client.level.getBlockState(pos)))
		{
			cir.setReturnValue(false);
		}
	}
}
