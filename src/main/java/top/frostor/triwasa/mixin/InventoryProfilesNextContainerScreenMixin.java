package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTerminalScreen;

/**
 * Prevents Inventory Profiles Next from injecting its container buttons into
 * the terminal, whose item list is virtual and sorted by TriWasa itself.
 */
@Mixin(targets = "org.anti_ad.mc.ipnext.gui.inject.ContainerScreenEventHandler")
public abstract class InventoryProfilesNextContainerScreenMixin
{
	@Inject(method = "onScreenInit", at = @At("HEAD"), cancellable = true)
	private void triwasa$skipTerminalWidgets(CallbackInfo ci)
	{
		if(Minecraft.getInstance().gui.screen() instanceof BigEnderChestTerminalScreen)
		{
			ci.cancel();
		}
	}
}
