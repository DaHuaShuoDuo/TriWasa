package top.frostor.triwasa.bigenderchest;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTerminalScreen;
import top.frostor.triwasa.bigenderchest.menu.BigEnderChestTerminalMenu;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestClientNetwork;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Registration entry point for the optional FCA terminal client.
 */
public final class BigEnderChestClient
{
	private static MenuType<? extends AbstractContainerMenu> terminalMenuType;
	private static Object terminalScreenConstructor;
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
		registerCompatibleMenuType();
	}

	public static MenuType<? extends AbstractContainerMenu> terminalMenuType()
	{
		return terminalMenuType;
	}

	public static Screen createScreen(
			MenuType<?> type,
			net.minecraft.client.Minecraft client,
			int syncId,
			net.minecraft.network.chat.Component title
	)
	{
		if(!isTerminalMenuType(type) || client.player == null)
		{
			return null;
		}
		MenuType rawType = type;
		AbstractContainerMenu menu = rawType.create(syncId, client.player.getInventory());
		return new BigEnderChestTerminalScreen(
				menu,
				client.player.getInventory(),
				title
		);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Object screenConstructor(MenuType<?> type)
	{
		if(!isTerminalMenuType(type))
		{
			return null;
		}
		if(terminalScreenConstructor != null)
		{
			return terminalScreenConstructor;
		}
		try
		{
			Class<?> constructorType = Class.forName(
					"net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"
			);
			InvocationHandler handler = (proxy, method, args) -> {
				if(method.getName().equals("create"))
				{
					AbstractContainerMenu menu = (AbstractContainerMenu)args[0];
					Inventory inventory = (Inventory)args[1];
					Component title = (Component)args[2];
					return new BigEnderChestTerminalScreen(menu, inventory, title);
				}
				if(method.isDefault())
				{
					return InvocationHandler.invokeDefault(proxy, method, args);
				}
				if(method.getName().equals("toString"))
				{
					return "TriWasa BigEnderChest terminal screen constructor";
				}
				if(method.getName().equals("hashCode"))
				{
					return System.identityHashCode(proxy);
				}
				if(method.getName().equals("equals"))
				{
					return proxy == args[0];
				}
				throw new UnsupportedOperationException(method.toString());
			};
			terminalScreenConstructor = Proxy.newProxyInstance(
					constructorType.getClassLoader(),
					new Class<?>[]{constructorType},
					handler
			);
			return terminalScreenConstructor;
		}
		catch(ReflectiveOperationException | IllegalArgumentException exception)
		{
			TriWasa.LOGGER.error(
					"Unable to create TriWasa terminal ScreenConstructor",
					exception
			);
			return null;
		}
	}

	private static boolean isTerminalMenuType(MenuType<?> type)
	{
		return type == terminalMenuType
				|| BigEnderChestProtocol.TERMINAL_MENU_ID.equals(
						BuiltInRegistries.MENU.getKey(type)
				);
	}

	private static void registerCompatibleMenuType()
	{
		MenuType<?> existing = BuiltInRegistries.MENU.getValue(
				BigEnderChestProtocol.TERMINAL_MENU_ID
		);
		if(existing != null)
		{
			terminalMenuType = existing;
			TriWasa.LOGGER.info(
					"[BigEnderChest] Reusing {} menu registration",
					BigEnderChestProtocol.MOD_ID
			);
			return;
		}
		terminalMenuType = BigEnderChestTerminalMenu.createMenuType();
		TriWasa.LOGGER.info(
				"[BigEnderChest] Registered standalone compatible terminal menu"
		);
	}
}
