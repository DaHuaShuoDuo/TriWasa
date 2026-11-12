package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;

/**
 * Captures the block position a printer click is really queued against.
 *
 * <p>{@code queueAction} either clicks the schematic target or the support block next to it, so
 * the click sink is the only place that knows the packet position without re-deriving the
 * support/air decision.</p>
 *
 * <p>The selector is name-only on purpose: spelling out the full descriptor broke on this build
 * with "invalid target descriptor / no refMap loaded", while {@code argsOnly} already resolves the
 * argument from the target method descriptor at runtime.</p>
 */
@Mixin(targets = "me.aleksilassila.litematica.printer.printer.ActionManager")
public abstract class PrinterQueuedTargetMixin
{
	@ModifyVariable(
			method = "queueClick",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0,
			require = 1
	)
	private BlockPos triwasa$recordPacketTarget(BlockPos target)
	{
		PrinterPacketAnchor.recordTarget(target);
		return target;
	}
}
