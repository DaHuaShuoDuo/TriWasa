package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.easyplace.SchematicSelectionOutline;

/**
 * Rewrites the extracted block outline state instead of {@code Minecraft#hitResult}, so the
 * selection box follows the boosted schematic target without touching any interaction path.
 */
@Mixin(LevelExtractor.class)
public abstract class BetterEasyPlaceOutlineMixin
{
	@Inject(method = "extractBlockOutline", at = @At("RETURN"))
	private void triwasa$showSchematicTargetOutline(
			Camera camera,
			LevelRenderState levelRenderState,
			CallbackInfo ci
	)
	{
		BlockOutlineRenderState replacement =
				SchematicSelectionOutline.resolve(levelRenderState.blockOutlineRenderState);

		if(replacement != null)
		{
			levelRenderState.blockOutlineRenderState = replacement;
		}
	}
}
