package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;

/**
 * Re-anchors the encoded V2-Extra payload to the block position the click is queued against.
 *
 * <p>The encoder runs against the schematic target, but a support placement clicks the block next
 * to it. Shifting the X and Y channels here keeps the wire payload consistent with
 * {@code BlockHitResult.blockPos}, which is the only anchor the server decodes.</p>
 */
@Mixin(targets = "me.aleksilassila.litematica.printer.printer.ActionManager")
public abstract class PrinterProtocolAnchorMixin
{
	@ModifyVariable(method = "useProtocolHitModifier", at = @At("HEAD"), argsOnly = true)
	private Vec3 triwasa$reanchorProtocolPayload(Vec3 hitModifier)
	{
		return PrinterPacketAnchor.apply(hitModifier);
	}
}
