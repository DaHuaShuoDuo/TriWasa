package top.frostor.triwasa.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTerminalScreen;

/**
 * The terminal's own JSON hover material replaces Minecraft's blue-gray slot
 * highlight for the real player inventory slots.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class BigEnderChestSlotHighlightMixin
{
	@Inject(method = "extractSlotHighlightBack", at = @At("HEAD"), cancellable = true)
	private void triwasa$hideTerminalHighlightBack(
			GuiGraphicsExtractor graphics,
			CallbackInfo callback
	)
	{
		if((Object)this instanceof BigEnderChestTerminalScreen)
		{
			callback.cancel();
		}
	}

	@Inject(method = "extractSlotHighlightFront", at = @At("HEAD"), cancellable = true)
	private void triwasa$hideTerminalHighlightFront(
			GuiGraphicsExtractor graphics,
			CallbackInfo callback
	)
	{
		if((Object)this instanceof BigEnderChestTerminalScreen)
		{
			callback.cancel();
		}
	}
}
