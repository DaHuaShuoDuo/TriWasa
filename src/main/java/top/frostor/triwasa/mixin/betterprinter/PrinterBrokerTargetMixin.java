package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;

/**
 * Records the queued click target at the broker, one level above {@code ActionManager}.
 *
 * <p>The printer exposes several {@code queueClick} overloads and its internals differ between
 * builds, so the target is captured at both the broker and the manager. Whichever runs first
 * consumes the encoder anchor; the other one is a no-op.</p>
 */
@Mixin(targets = "me.aleksilassila.litematica.printer.printer.action.ActionBroker")
public abstract class PrinterBrokerTargetMixin
{
	@ModifyVariable(
			method = "queueClick",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0,
			require = 1
	)
	private BlockPos triwasa$recordBrokerTarget(BlockPos target)
	{
		PrinterPacketAnchor.recordTarget(target);
		return target;
	}
}
