package top.frostor.triwasa.mixin.rei;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Keeps REI config sub-menus from clipping all text and icons through an empty scissor stack.
 */
@Mixin(targets = "me.shedaniel.rei.api.client.gui.compat.GuiGraphics")
public class ReiGuiGraphicsMixin
{
	@Redirect(
			method = "withFreshScissorStack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/navigation/ScreenRectangle;empty()Lnet/minecraft/client/gui/navigation/ScreenRectangle;"
			),
			require = 0
	)
	private ScreenRectangle triwasa$useFullGuiArea()
	{
		Window window = Minecraft.getInstance().getWindow();
		return new ScreenRectangle(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight());
	}
}
