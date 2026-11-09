package top.frostor.triwasa.mixin.betterprinter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.betterprinter.BetterPrinterSupportOverride;

@Mixin(targets = "me.aleksilassila.litematica.printer.printer.action.Action")
public abstract class BetterPrinterActionSupportMixin
{
	@Shadow
	protected boolean requiresSupport;

	@Inject(
			method = "setRequiresSupport(Z)Lme/aleksilassila/litematica/printer/printer/action/Action;",
			at = @At("TAIL")
	)
	private void triwasa$clearSyntheticRequiresSupport(
			boolean value,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(BetterPrinterSupportOverride.isActive())
		{
			requiresSupport = false;
		}
	}
}
