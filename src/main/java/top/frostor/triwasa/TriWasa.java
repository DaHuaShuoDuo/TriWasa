package top.frostor.triwasa;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.frostor.triwasa.bigenderchest.BigEnderChestClient;
import top.frostor.triwasa.config.TriWasaConfigGui;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.EasyPlaceClientNetwork;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;
import top.frostor.triwasa.quickshulker.QuickShulkerHandRestock;

/**
 * Client entry point for TriWasa's Litematica integration.
 */
public final class TriWasa implements ClientModInitializer
{
	public static final String MOD_ID = "triwasa";
	public static final String MOD_NAME = "TriWasa";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient()
	{
		BigEnderChestClient.initialize();
		EasyPlaceClientNetwork.register();
		ItemPickupFilter.register();
		QuickShulkerHandRestock.register();
		InitializationHandler.getInstance().registerInitializationHandler(() -> {
			ConfigManager.getInstance().registerConfigHandler(MOD_ID, new TriWasaConfigs());
			Registry.CONFIG_SCREEN.registerConfigScreenFactory(
					new ModInfo(MOD_ID, MOD_NAME, TriWasaConfigGui::new)
			);
		});
	}
}
