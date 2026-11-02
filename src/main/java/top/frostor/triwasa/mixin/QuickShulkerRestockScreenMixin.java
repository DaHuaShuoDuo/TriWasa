package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.quickshulker.QuickShulkerHandRestock;

@Mixin(Minecraft.class)
public abstract class QuickShulkerRestockScreenMixin
{
	@Inject(method = "setScreenAndShow", at = @At("HEAD"), cancellable = true)
	private void triwasa$suppressTransientShulkerScreen(Screen screen, CallbackInfo ci)
	{
		if(QuickShulkerHandRestock.shouldSuppressScreen(screen))
		{
			ci.cancel();
		}
	}
}
