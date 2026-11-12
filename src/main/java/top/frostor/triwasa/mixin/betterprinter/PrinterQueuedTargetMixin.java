package top.frostor.triwasa.mixin.betterprinter;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;

/**
 * Captures the block position a printer click is really queued against.
 *
 * <p>{@code queueAction} either clicks the schematic target or the support block next to it, so
 * the click sink is the only place that knows the packet position without re-deriving the
 * support/air decision.</p>
 */
@Mixin(targets = "me.aleksilassila.litematica.printer.printer.ActionManager")
public abstract class PrinterQueuedTargetMixin
{
	@Inject(method = "queueClick", at = @At("RETURN"))
	private void triwasa$recordPacketTarget(
			CallbackInfoReturnable<Boolean> cir,
			@Local(argsOnly = true, ordinal = 0) BlockPos target
	)
	{
		if(Boolean.TRUE.equals(cir.getReturnValue()))
		{
			PrinterPacketAnchor.recordTarget(target);
		}
	}
}
