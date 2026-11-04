package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.bigenderchest.BigEnderChestClient;

@Mixin(MenuScreens.class)
public abstract class BigEnderChestMenuScreensMixin
{
	/*
	 * Keep this hook: Fabric opens ExtendedMenuType screens by calling this private method
	 * directly, so a MenuScreens.create-only injection is bypassed.
	 */
	@Inject(method = "getConstructor", at = @At("HEAD"), cancellable = true)
	private static void triwasa$provideTerminalConstructor(
			MenuType<?> type,
			CallbackInfoReturnable<Object> callback
	)
	{
		Object constructor = BigEnderChestClient.screenConstructor(type);
		if(constructor != null)
		{
			callback.setReturnValue(constructor);
		}
	}

	@Inject(method = "create", at = @At("HEAD"), cancellable = true)
	private static void triwasa$openTerminalScreen(
			MenuType<?> type,
			Minecraft client,
			int syncId,
			Component title,
			CallbackInfo callback
	)
	{
		Screen screen = BigEnderChestClient.createScreen(type, client, syncId, title);
		if(screen != null)
		{
			client.gui.setScreen(screen);
			callback.cancel();
		}
	}
}
