package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.HangingSignRules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(targets = "me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementExecutor")
public abstract class BetterPrinterPlacementValidationMixin
{
	private static final Method DEFERRED_RESULT = findDeferredResult();

	@Inject(method = "execute", at = @At("HEAD"), cancellable = true)
	private static void triwasa$validateHangingSignSupport(
			@Coerce Object context,
			@Coerce Object action,
			@Coerce Object taskAction,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(!EasyPlaceRules.isPrinterEnabled()
				|| !(context instanceof BetterPrinterSchematicBlockContextAccessor accessor))
		{
			return;
		}

		ClientLevel level = accessor.triwasa$getLevel();
		BlockPos pos = accessor.triwasa$getBlockPos();
		BlockState required = accessor.triwasa$getRequiredState();
		if(HangingSignRules.hasRequiredCeilingHangingSignSupport(level, pos, required))
		{
			return;
		}

		try
		{
			cir.setReturnValue(DEFERRED_RESULT.invoke(null, true));
		}
		catch(IllegalAccessException | InvocationTargetException exception)
		{
			throw new IllegalStateException("Unable to defer invalid hanging sign placement", exception);
		}
	}

	private static Method findDeferredResult()
	{
		try
		{
			Class<?> resultClass = Class.forName(
					"me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementResult"
			);
			return resultClass.getMethod("deferred", boolean.class);
		}
		catch(ClassNotFoundException | NoSuchMethodException exception)
		{
			throw new IllegalStateException("Litematica Printer placement result API changed", exception);
		}
	}
}
