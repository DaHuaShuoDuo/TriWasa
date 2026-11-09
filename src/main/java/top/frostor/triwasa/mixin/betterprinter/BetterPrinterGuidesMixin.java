package top.frostor.triwasa.mixin.betterprinter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

import java.util.Map;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.Guides")
public abstract class BetterPrinterGuidesMixin
{
	@Shadow
	private Map<?, ?> registrationsByBlockClass;

	@Unique
	private boolean triwasa$lastPrinterEnabled;

	@Inject(method = "buildAction", at = @At("HEAD"))
	private void triwasa$refreshGuideCache(
			@Coerce Object context,
			CallbackInfoReturnable<Object> cir
	)
	{
		boolean enabled = EasyPlaceRules.isPrinterEnabled();
		if(triwasa$lastPrinterEnabled != enabled)
		{
			registrationsByBlockClass.clear();
			triwasa$lastPrinterEnabled = enabled;
		}
	}
}
