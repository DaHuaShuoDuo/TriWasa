package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.betterprinter.BetterPrinterIncrementalPlacement;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.IncrementalBlockRules;

@Mixin(targets = "me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementExecutor")
public abstract class BetterPrinterIncrementalPlacementMixin
{
	@Inject(method = "execute", at = @At("HEAD"))
	private static void triwasa$clearIncrementalRetry(
			@Coerce Object context,
			@Coerce Object action,
			@Coerce Object taskAction,
			CallbackInfoReturnable<Object> cir
	)
	{
		BetterPrinterIncrementalPlacement.clear();
	}

	@Inject(method = "execute", at = @At("RETURN"))
	private static void triwasa$markIncrementalRetry(
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

		Object result = cir.getReturnValue();
		if(!BetterPrinterIncrementalPlacement.isSuccess(result))
		{
			return;
		}

		BlockState currentState = accessor.triwasa$getCurrentState();
		BlockState requiredState = accessor.triwasa$getRequiredState();
		if(IncrementalBlockRules.shouldRetryAfterPlacement(currentState, requiredState))
		{
			BlockPos pos = accessor.triwasa$getBlockPos();
			BetterPrinterIncrementalPlacement.mark(pos);
		}
	}
}
