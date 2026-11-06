package top.frostor.triwasa.bigenderchest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTerminalScreen;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestClientNetwork;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestSessionState;

/**
 * Registration and screen selection for the optional FCA terminal client.
 */
public final class BigEnderChestClient
{
	private static boolean initialized;

	private BigEnderChestClient()
	{
	}

	public static void initialize()
	{
		if(initialized)
		{
			return;
		}
		initialized = true;
		BigEnderChestClientNetwork.register();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Screen createScreen(
			MenuType<?> type,
			Minecraft client,
			int syncId,
			Component title
	)
	{
		BigEnderChestSessionState session = BigEnderChestClientNetwork.state();
		if(type != MenuType.GENERIC_9x6
				|| client.player == null
				|| !session.isTerminalCarrierContainer(syncId))
		{
			return null;
		}
		MenuType rawType = type;
		AbstractContainerMenu menu = rawType.create(
				syncId,
				client.player.getInventory()
		);
		BigEnderChestTerminalScreen screen = new BigEnderChestTerminalScreen(
				menu,
				client.player.getInventory(),
				title
		);
		session.markCarrierScreenReplaced(true);
		return screen;
	}

	public static boolean tryReplaceCarrierScreen(Minecraft client)
	{
		if(client.player == null)
		{
			return false;
		}
		AbstractContainerMenu menu = client.player.containerMenu;
		BigEnderChestSessionState session = BigEnderChestClientNetwork.state();
		if(menu.getType() != MenuType.GENERIC_9x6
				|| !session.isTerminalCarrierContainer(menu.containerId))
		{
			return false;
		}
		if(client.gui.screen() instanceof BigEnderChestTerminalScreen terminalScreen
				&& terminalScreen.getMenu() == menu)
		{
			session.markCarrierScreenReplaced(true);
			return true;
		}
		if(session.carrierScreenReplaced())
		{
			return false;
		}
		try
		{
			BigEnderChestTerminalScreen screen = new BigEnderChestTerminalScreen(
					menu,
					client.player.getInventory(),
					Component.translatable("big_ender_chest.menu.title")
			);
			session.markCarrierScreenReplaced(true);
			client.gui.setScreen(screen);
			return true;
		}
		catch(RuntimeException exception)
		{
			TriWasa.LOGGER.error(
					"Failed to create the big ender chest terminal screen; keeping vanilla",
					exception
			);
			return false;
		}
	}
}
