package top.frostor.triwasa.quickshulker;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import top.frostor.triwasa.TriWasa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection-only boundary for Quick Shulker's public client helper.
 *
 * <p>Keeping the optional mod out of TriWasa's compile and runtime class paths means
 * disabling or removing Quick Shulker cannot break the rest of TriWasa.</p>
 */
final class QuickShulkerBridge
{
	private static Method openMethod;
	private static boolean openMethodTakesStack;
	private static boolean apiLookupFailed;

	private QuickShulkerBridge()
	{
	}

	static boolean isLoaded()
	{
		return FabricLoader.getInstance().isModLoaded("quickshulker");
	}

	static boolean open(ItemStack shulkerStack, int inventoryMenuSlot)
	{
		if(!isLoaded() || shulkerStack.isEmpty() || inventoryMenuSlot < 0)
		{
			return false;
		}

		Method method = getOpenMethod();
		if(method == null)
		{
			return false;
		}

		try
		{
			if(openMethodTakesStack)
			{
				return Boolean.TRUE.equals(method.invoke(null, shulkerStack, inventoryMenuSlot));
			}
			method.invoke(null, inventoryMenuSlot);
			return true;
		}
		catch(IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError exception)
		{
			TriWasa.LOGGER.warn("[QuickShulkerRestock] Failed to request a Quick Shulker open", exception);
			return false;
		}
	}

	private static Method getOpenMethod()
	{
		if(openMethod != null || apiLookupFailed)
		{
			return openMethod;
		}

		try
		{
			openMethod = Class.forName("net.kyrptonaught.quickshulker.client.ClientUtil")
					.getMethod("CheckAndSend", ItemStack.class, int.class);
			openMethodTakesStack = true;
		}
		catch(ClassNotFoundException | NoSuchMethodException | LinkageError exception)
		{
			try
			{
				// Quick Shulker 26.3 moved this helper to the packet class.
				openMethod = Class.forName("net.kyrptonaught.quickshulker.network.OpenShulkerPacket")
						.getMethod("sendOpenPacket", int.class);
				openMethodTakesStack = false;
			}
			catch(ClassNotFoundException | NoSuchMethodException | LinkageError fallbackException)
			{
				apiLookupFailed = true;
				fallbackException.addSuppressed(exception);
				TriWasa.LOGGER.warn(
						"[QuickShulkerRestock] Installed Quick Shulker API is not compatible",
						fallbackException
				);
			}
		}
		return openMethod;
	}
}
