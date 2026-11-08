package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.betterprinter.BetterPrinterGuideRules;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.Guides$GuideRegistration")
public abstract class BetterPrinterGuideRegistrationMixin
{
	@Final
	@Shadow
	public Class<? extends Block>[] blockClass;

	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private void triwasa$matchExtendedGuide(Block block, CallbackInfoReturnable<Boolean> cir)
	{
		if(BetterPrinterGuideRules.shouldMatchMultifaceGuide(block, blockClass)
				|| BetterPrinterGuideRules.shouldMatchLeafLitterGuide(block, blockClass))
		{
			cir.setReturnValue(true);
		}
	}
}
