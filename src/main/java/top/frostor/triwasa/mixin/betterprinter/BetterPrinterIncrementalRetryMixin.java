package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.betterprinter.BetterPrinterIncrementalPlacement;

@Mixin(targets = "me.aleksilassila.litematica.printer.handler.handlers.print.PrintTargetScheduler")
public abstract class BetterPrinterIncrementalRetryMixin
{
	@Inject(method = "onResult", at = @At("HEAD"), cancellable = true)
	private void triwasa$retryIncrementalPlacement(
			BlockPos pos,
			@Coerce Object result,
			boolean multiStage,
			CallbackInfo cir
	)
	{
		BlockPos marked = BetterPrinterIncrementalPlacement.take();
		boolean success = BetterPrinterIncrementalPlacement.isSuccess(result);
		if(marked == null || !marked.equals(pos))
		{
			return;
		}

		if(!multiStage && success)
		{
			BetterPrinterIncrementalPlacement.requeueThroughScheduler(this, pos);
			cir.cancel();
		}
	}
}
