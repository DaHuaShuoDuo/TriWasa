package top.frostor.triwasa.mixin.betterprinter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.betterprinter.BetterPrinterSupportOverride;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.guides.TurtleEggGuide")
public abstract class BetterPrinterTurtleEggGuideMixin
{
	@Unique
	private boolean triwasa$overrideRequiresSupport;

	@Inject(method = "onBuildActionMissingBlock", at = @At("HEAD"))
	private void triwasa$beginSupportOverride(
			@Coerce Object matchResult,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(EasyPlaceRules.isPrinterEnabled())
		{
			BetterPrinterSupportOverride.begin();
			triwasa$overrideRequiresSupport = true;
		}
	}

	@Inject(method = "onBuildActionMissingBlock", at = @At("RETURN"))
	private void triwasa$endSupportOverride(
			@Coerce Object matchResult,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(triwasa$overrideRequiresSupport)
		{
			BetterPrinterSupportOverride.end();
			triwasa$overrideRequiresSupport = false;
		}
	}
}
