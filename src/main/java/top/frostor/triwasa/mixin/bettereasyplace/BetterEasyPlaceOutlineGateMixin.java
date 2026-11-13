package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.SchematicSelectionOutline;

/**
 * Vanilla skips the block outline pass entirely when {@code Minecraft#hitResult} is not a block
 * hit. A schematic target cell is air in the client world, so the gate has to be opened for the
 * outline state that {@code BetterEasyPlaceOutlineMixin} installed.
 */
@Mixin(GameRenderer.class)
public abstract class BetterEasyPlaceOutlineGateMixin
{
	@Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
	private void triwasa$renderSchematicTargetOutline(CallbackInfoReturnable<Boolean> cir)
	{
		if(Boolean.TRUE.equals(cir.getReturnValue()) == false
				&& SchematicSelectionOutline.hasTargetOutline())
		{
			SchematicSelectionOutline.noteRenderPassForced();
			cir.setReturnValue(true);
		}
	}
}
