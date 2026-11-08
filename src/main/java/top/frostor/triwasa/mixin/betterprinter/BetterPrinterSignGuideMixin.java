package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.world.level.block.WallHangingSignBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigs;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.guides.SignGuide")
public abstract class BetterPrinterSignGuideMixin
{
	private static boolean reflectionWarningLogged;

	@Inject(method = "onBuildActionMissingBlock", at = @At("RETURN"))
	private void triwasa$disableWallHangingSignSupportRequirement(
			@Coerce Object matchResult,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(!TriWasaConfigs.BETTER_PRINTER.getBooleanValue()
				|| !(((BetterPrinterGuideAccessor) (Object) this)
				.triwasa$getRequiredBlock() instanceof WallHangingSignBlock))
		{
			return;
		}

		Object result = cir.getReturnValue();
		if(result == null)
		{
			return;
		}

		try
		{
			Object action = result.getClass().getMethod("action").invoke(result);
			if(action != null)
			{
				action.getClass()
						.getMethod("setRequiresSupport", boolean.class)
						.invoke(action, false);
			}
		}
		catch(ReflectiveOperationException exception)
		{
			if(!reflectionWarningLogged)
			{
				reflectionWarningLogged = true;
				TriWasa.LOGGER.warn(
						"Printer guide/action API changed; unsupported wall signs will not be forced",
						exception
				);
			}
		}
	}
}
