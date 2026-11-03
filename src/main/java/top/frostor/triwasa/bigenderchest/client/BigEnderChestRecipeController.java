package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.network.chat.Component;

/**
 * Protocol v2 has no authoritative crafting-grid capability. This controller deliberately
 * never creates a local recipe result.
 */
public final class BigEnderChestRecipeController
{
	public boolean isAvailable()
	{
		return false;
	}

	public boolean tryCraft()
	{
		return false;
	}

	public Component unavailableMessage()
	{
		return Component.translatable("triwasa.big_ender_chest.crafting_unsupported");
	}
}
