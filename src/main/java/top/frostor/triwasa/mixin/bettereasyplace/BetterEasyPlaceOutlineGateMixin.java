package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.SchematicSelectionOutline;

/**
 * Vanilla skips the block outline pass entirely when {@code Minecraft#hitResult} is not a block
 * hit. A schematic target cell is air in the client world, so the gate has to be opened for the
 * outline state that {@code BetterEasyPlaceOutlineMixin} installed.
 *
 * <p>The vanilla method folds several gates into its result: the {@code renderBlockOutline}
 * field (cleared around screenshots and panoramas), {@code gui.hud.isHidden()} (F1) and the
 * adventure-mode break/place check. The first two are re-checked here so forcing the result can
 * never revive a pass that the game deliberately disabled.</p>
 */
@Mixin(GameRenderer.class)
public abstract class BetterEasyPlaceOutlineGateMixin
{
	@Shadow
	private boolean renderBlockOutline;

	@Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
	private void triwasa$renderSchematicTargetOutline(CallbackInfoReturnable<Boolean> cir)
	{
		Minecraft client = Minecraft.getInstance();

		if(client == null || client.gui.hud.isHidden() || this.renderBlockOutline == false)
		{
			return;
		}

		if(Boolean.TRUE.equals(cir.getReturnValue()) == false
				&& SchematicSelectionOutline.hasTargetOutline())
		{
			SchematicSelectionOutline.noteRenderPassForced();
			cir.setReturnValue(true);
		}
	}
}
