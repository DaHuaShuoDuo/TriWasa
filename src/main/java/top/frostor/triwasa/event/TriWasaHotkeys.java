package top.frostor.triwasa.event;

import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.Minecraft;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigGui;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.util.List;

public final class TriWasaHotkeys implements IKeybindProvider
{
	private static final TriWasaHotkeys INSTANCE = new TriWasaHotkeys();

	private TriWasaHotkeys()
	{
	}

	public static void register()
	{
		TriWasaConfigs.OPEN_CONFIG_GUI.getKeybind()
				.setCallback((action, keybind) -> openConfigScreen(action));
		InputEventHandler.getKeybindManager().registerKeybindProvider(INSTANCE);
	}

	private static boolean openConfigScreen(KeyAction action)
	{
		if(action != KeyAction.PRESS)
		{
			return false;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.gui.screen() != null || minecraft.player == null)
		{
			return false;
		}

		minecraft.gui.setScreen(new TriWasaConfigGui());
		return true;
	}

	@Override
	public void addKeysToMap(IKeybindManager manager)
	{
		manager.addKeybindToMap(TriWasaConfigs.OPEN_CONFIG_GUI.getKeybind());
	}

	@Override
	public void addHotkeys(IKeybindManager manager)
	{
		List<IHotkey> hotkeys = List.of(TriWasaConfigs.OPEN_CONFIG_GUI);
		manager.addHotkeysForCategory(
				TriWasa.MOD_NAME,
				"triwasa.hotkeys.category.general",
				hotkeys
		);
	}
}
