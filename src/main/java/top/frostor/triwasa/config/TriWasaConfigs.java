package top.frostor.triwasa.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.BigEnderChestAmountStyle;
import top.frostor.triwasa.bigenderchest.BigEnderChestFeedbackMode;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortDirection;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortField;
import top.frostor.triwasa.bigenderchest.BigEnderChestTerminalStyle;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestClientNetwork;
import top.frostor.triwasa.easyplace.EasyPlaceClientNetwork;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;
import top.frostor.triwasa.pickupfilter.PickupFilterFeedback;
import top.frostor.triwasa.pickupfilter.PickupFilterMode;
import top.frostor.triwasa.quickshulker.QuickShulkerHandRestock;
import top.frostor.triwasa.verifier.VerifierOverlayRefresh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class TriWasaConfigs implements IConfigHandler
{
	public static final String GENERAL_KEY = "General";
	public static final String LITEMATICA_KEY = "Litematica";
	public static final String UTILITIES_KEY = "Utilities";
	public static final String HOTKEYS_KEY = "Hotkeys";
	public static final String CONFIG_FILE_NAME = TriWasa.MOD_ID + ".json";
	public static final String GENERAL_TRANSLATION_PREFIX = "triwasa.config.general";
	public static final String LITEMATICA_TRANSLATION_PREFIX = "triwasa.config.litematica";
	public static final String UTILITIES_TRANSLATION_PREFIX = "triwasa.config.utilities";
	public static final String BIG_ENDER_CHEST_TRANSLATION_PREFIX =
			"triwasa.config.big_ender_chest";
	public static final String PICKUP_FILTER_TRANSLATION_PREFIX = "triwasa.config.pickup_filter";

	public static final ConfigHotkey OPEN_CONFIG_GUI =
			new ConfigHotkey("openConfigGui", "C,T")
					.apply(GENERAL_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_V2_PROTOCOL_EXTRA =
			new ConfigBoolean("easyPlaceV2ProtocolExtra", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigHotkey EASY_PLACE_V2_PROTOCOL_EXTRA_HOTKEY =
			booleanHotkey("easyPlaceV2ProtocolExtra");
	public static final ConfigBoolean EASY_PLACE_EXPANDED =
			new ConfigBoolean("easyPlaceExpanded", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_RAIL_PROTECTION =
			new ConfigBoolean("easyPlaceRailProtection", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE =
			new ConfigBoolean("easyPlaceWallBlockNoShapeUpdate", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_OBSERVER_PROTECTION =
			new ConfigBoolean("easyPlaceObserverProtection", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_TNT_PROTECTION =
			new ConfigBoolean("easyPlaceTntProtection", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_PISTON_PROTECTION =
			new ConfigBoolean("easyPlacePistonProtection", false).apply(LITEMATICA_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_CRAFTER_LOCK_SLOT =
			new ConfigBoolean("easyPlaceCrafterLockSlot", false).apply(LITEMATICA_TRANSLATION_PREFIX);

	public static final ConfigBoolean QUICK_SHULKER_HAND_RESTOCK =
			new ConfigBoolean("quickShulkerHandRestock", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean QUICK_SHULKER_OFFHAND_RESTOCK =
			new ConfigBoolean("quickShulkerOffhandRestock", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean BETTER_EASY_PLACE =
			new ConfigBoolean("betterEasyPlace", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean BETTER_EASY_PLACE_EXPANDED =
			new ConfigBoolean("betterEasyPlaceExpanded", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean BETTER_PRINTER =
			new ConfigBoolean("betterPrinter", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_FULL_BLOCK_SELECTION =
			new ConfigBoolean("easyPlaceFullBlockSelection", false)
					.apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_SELECTION_OUTLINE =
			new ConfigBoolean("easyPlaceSelectionOutline", false)
					.apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean QUICK_SHULKER_HAND_RESTOCK_EXPANDED =
			new ConfigBoolean("quickShulkerHandRestockExpanded", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigInteger QUICK_SHULKER_HAND_RESTOCK_THRESHOLD =
			new ConfigInteger("quickShulkerHandRestockThreshold", 8, 1, 63, true)
					.apply(UTILITIES_TRANSLATION_PREFIX);

	public static final ConfigBoolean BIG_ENDER_CHEST_TERMINAL_ENABLED =
			new ConfigBoolean("bigEnderChestTerminalEnabled", false)
					.apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigBoolean BIG_ENDER_CHEST_TERMINAL_EXPANDED =
			new ConfigBoolean("bigEnderChestTerminalExpanded", false)
					.apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_FEEDBACK =
			new ConfigOptionList(
					"bigEnderChestFeedback",
					BigEnderChestFeedbackMode.ACTION_BAR
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_TERMINAL_STYLE =
			new ConfigOptionList(
					"bigEnderChestTerminalStyle",
					BigEnderChestTerminalStyle.STANDARD
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_TERMINAL_AMOUNT_FONT_SIZE =
			new ConfigOptionList(
					"bigEnderChestTerminalAmountFontSize",
					BigEnderChestAmountStyle.SMALL
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_SORT_FIELD =
			new ConfigOptionList(
					"bigEnderChestSortField",
					BigEnderChestSortField.NAME
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_SORT_DIRECTION =
			new ConfigOptionList(
					"bigEnderChestSortDirection",
					BigEnderChestSortDirection.ASCENDING
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigBoolean BIG_ENDER_CHEST_REMEMBER_SEARCH =
			new ConfigBoolean("bigEnderChestRememberSearch", false)
					.apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);

	public static final ConfigBoolean PROJECTION_MATERIAL_STATS_FIX =
			new ConfigBoolean("projectionMaterialStatsFix", false)
					.apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean VERIFIER_POSITION_PICKER =
			new ConfigBoolean("verifierPositionPicker", false)
					.apply(UTILITIES_TRANSLATION_PREFIX);

	public static final ConfigBoolean PICKUP_FILTER_ENABLED =
			new ConfigBoolean("itemPickupFilterEnabled", false).apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigBoolean PICKUP_FILTER_EXPANDED =
			new ConfigBoolean("itemPickupFilterExpanded", false).apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigOptionList PICKUP_FILTER_MODE =
			new ConfigOptionList("itemPickupFilterMode", PickupFilterMode.BLACKLIST)
					.apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigStringList PICKUP_FILTER_ITEMS =
			new ConfigStringList("itemPickupFilterItems", ImmutableList.of())
					.apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigStringList PICKUP_FILTER_RARITIES =
			new ConfigStringList("itemPickupFilterRarities", ImmutableList.of())
					.apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigOptionList PICKUP_FILTER_FEEDBACK =
			new ConfigOptionList("itemPickupFilterFeedback", PickupFilterFeedback.ACTION_BAR)
					.apply(PICKUP_FILTER_TRANSLATION_PREFIX);
	public static final ConfigBoolean PICKUP_FILTER_PAUSE_WHILE_SNEAKING =
			new ConfigBoolean("itemPickupFilterPauseWhileSneaking", true)
					.apply(PICKUP_FILTER_TRANSLATION_PREFIX);

	/*
	 * One empty-by-default hotkey per visible true/false option. They are stored in their own
	 * "Hotkeys" section, are not part of any display list, and are rendered inside the matching
	 * boolean row (switch on the left, keybind on the right).
	 */
	public static final ConfigHotkey EASY_PLACE_RAIL_PROTECTION_HOTKEY =
			booleanHotkey("easyPlaceRailProtection");
	public static final ConfigHotkey EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE_HOTKEY =
			booleanHotkey("easyPlaceWallBlockNoShapeUpdate");
	public static final ConfigHotkey EASY_PLACE_OBSERVER_PROTECTION_HOTKEY =
			booleanHotkey("easyPlaceObserverProtection");
	public static final ConfigHotkey EASY_PLACE_TNT_PROTECTION_HOTKEY =
			booleanHotkey("easyPlaceTntProtection");
	public static final ConfigHotkey EASY_PLACE_PISTON_PROTECTION_HOTKEY =
			booleanHotkey("easyPlacePistonProtection");
	public static final ConfigHotkey EASY_PLACE_CRAFTER_LOCK_SLOT_HOTKEY =
			booleanHotkey("easyPlaceCrafterLockSlot");
	public static final ConfigHotkey PICKUP_FILTER_ENABLED_HOTKEY =
			booleanHotkey("itemPickupFilterEnabled");
	public static final ConfigHotkey PICKUP_FILTER_PAUSE_WHILE_SNEAKING_HOTKEY =
			booleanHotkey("itemPickupFilterPauseWhileSneaking");
	public static final ConfigHotkey BETTER_EASY_PLACE_HOTKEY = booleanHotkey("betterEasyPlace");
	public static final ConfigHotkey BETTER_PRINTER_HOTKEY = booleanHotkey("betterPrinter");
	public static final ConfigHotkey EASY_PLACE_FULL_BLOCK_SELECTION_HOTKEY =
			booleanHotkey("easyPlaceFullBlockSelection");
	public static final ConfigHotkey EASY_PLACE_SELECTION_OUTLINE_HOTKEY =
			booleanHotkey("easyPlaceSelectionOutline");
	public static final ConfigHotkey QUICK_SHULKER_HAND_RESTOCK_HOTKEY =
			booleanHotkey("quickShulkerHandRestock");
	public static final ConfigHotkey QUICK_SHULKER_OFFHAND_RESTOCK_HOTKEY =
			booleanHotkey("quickShulkerOffhandRestock");
	public static final ConfigHotkey BIG_ENDER_CHEST_TERMINAL_ENABLED_HOTKEY =
			booleanHotkey("bigEnderChestTerminalEnabled");
	public static final ConfigHotkey BIG_ENDER_CHEST_REMEMBER_SEARCH_HOTKEY =
			booleanHotkey("bigEnderChestRememberSearch");
	public static final ConfigHotkey PROJECTION_MATERIAL_STATS_FIX_HOTKEY =
			booleanHotkey("projectionMaterialStatsFix");
	public static final ConfigHotkey VERIFIER_POSITION_PICKER_HOTKEY =
			booleanHotkey("verifierPositionPicker");

	/**
	 * Boolean options that own a hotkey, in display order. Hidden {@code ...Expanded} switches
	 * are deliberately absent: they are UI state, not user facing toggles.
	 */
	public static final ImmutableMap<IConfigBase, ConfigHotkey> BOOLEAN_HOTKEYS =
			ImmutableMap.<IConfigBase, ConfigHotkey>builder()
					.put(EASY_PLACE_V2_PROTOCOL_EXTRA, EASY_PLACE_V2_PROTOCOL_EXTRA_HOTKEY)
					.put(EASY_PLACE_RAIL_PROTECTION, EASY_PLACE_RAIL_PROTECTION_HOTKEY)
					.put(EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE, EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE_HOTKEY)
					.put(EASY_PLACE_OBSERVER_PROTECTION, EASY_PLACE_OBSERVER_PROTECTION_HOTKEY)
					.put(EASY_PLACE_TNT_PROTECTION, EASY_PLACE_TNT_PROTECTION_HOTKEY)
					.put(EASY_PLACE_PISTON_PROTECTION, EASY_PLACE_PISTON_PROTECTION_HOTKEY)
					.put(EASY_PLACE_CRAFTER_LOCK_SLOT, EASY_PLACE_CRAFTER_LOCK_SLOT_HOTKEY)
					.put(PICKUP_FILTER_ENABLED, PICKUP_FILTER_ENABLED_HOTKEY)
					.put(PICKUP_FILTER_PAUSE_WHILE_SNEAKING, PICKUP_FILTER_PAUSE_WHILE_SNEAKING_HOTKEY)
					.put(BETTER_EASY_PLACE, BETTER_EASY_PLACE_HOTKEY)
					.put(BETTER_PRINTER, BETTER_PRINTER_HOTKEY)
					.put(EASY_PLACE_FULL_BLOCK_SELECTION, EASY_PLACE_FULL_BLOCK_SELECTION_HOTKEY)
					.put(EASY_PLACE_SELECTION_OUTLINE, EASY_PLACE_SELECTION_OUTLINE_HOTKEY)
					.put(QUICK_SHULKER_HAND_RESTOCK, QUICK_SHULKER_HAND_RESTOCK_HOTKEY)
					.put(QUICK_SHULKER_OFFHAND_RESTOCK, QUICK_SHULKER_OFFHAND_RESTOCK_HOTKEY)
					.put(BIG_ENDER_CHEST_TERMINAL_ENABLED, BIG_ENDER_CHEST_TERMINAL_ENABLED_HOTKEY)
					.put(BIG_ENDER_CHEST_REMEMBER_SEARCH, BIG_ENDER_CHEST_REMEMBER_SEARCH_HOTKEY)
					.put(PROJECTION_MATERIAL_STATS_FIX, PROJECTION_MATERIAL_STATS_FIX_HOTKEY)
					.put(VERIFIER_POSITION_PICKER, VERIFIER_POSITION_PICKER_HOTKEY)
					.build();
	public static final ImmutableList<IConfigBase> HOTKEY_OPTIONS =
			ImmutableList.copyOf(BOOLEAN_HOTKEYS.values());

	public static final ImmutableList<IConfigBase> GENERAL_OPTIONS =
			ImmutableList.of(OPEN_CONFIG_GUI);
	public static final ImmutableList<IConfigBase> EASY_PLACE_OPTIONS = ImmutableList.of(
			EASY_PLACE_V2_PROTOCOL_EXTRA,
			EASY_PLACE_EXPANDED,
			EASY_PLACE_RAIL_PROTECTION,
			EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_OBSERVER_PROTECTION,
			EASY_PLACE_TNT_PROTECTION,
			EASY_PLACE_PISTON_PROTECTION,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);
	public static final ImmutableList<IConfigBase> EASY_PLACE_CHILDREN = ImmutableList.of(
			EASY_PLACE_RAIL_PROTECTION,
			EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_OBSERVER_PROTECTION,
			EASY_PLACE_TNT_PROTECTION,
			EASY_PLACE_PISTON_PROTECTION,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);
	public static final ImmutableList<IConfigBase> BETTER_EASY_PLACE_OPTIONS = ImmutableList.of(
			BETTER_EASY_PLACE,
			BETTER_EASY_PLACE_EXPANDED,
			BETTER_PRINTER
	);
	public static final ImmutableList<IConfigBase> BETTER_EASY_PLACE_CHILDREN = ImmutableList.of(
			BETTER_PRINTER
	);
	/**
	 * Standalone switches without child options. They are independent of betterEasyPlace, so they
	 * are deliberately absent from {@code BETTER_EASY_PLACE_CHILDREN} and from
	 * {@code PARENT_GROUPS}: no indentation and no expand button.
	 */
	public static final ImmutableList<IConfigBase> EASY_PLACE_SELECTION_OPTIONS = ImmutableList.of(
			EASY_PLACE_FULL_BLOCK_SELECTION,
			EASY_PLACE_SELECTION_OUTLINE
	);
	public static final ImmutableList<IConfigBase> QUICK_SHULKER_HAND_RESTOCK_OPTIONS = ImmutableList.of(
			QUICK_SHULKER_HAND_RESTOCK,
			QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
			QUICK_SHULKER_HAND_RESTOCK_THRESHOLD,
			QUICK_SHULKER_OFFHAND_RESTOCK
	);
	public static final ImmutableList<IConfigBase> QUICK_SHULKER_HAND_RESTOCK_CHILDREN = ImmutableList.of(
			QUICK_SHULKER_HAND_RESTOCK_THRESHOLD,
			QUICK_SHULKER_OFFHAND_RESTOCK
	);
	public static final ImmutableList<IConfigBase> BIG_ENDER_CHEST_OPTIONS = ImmutableList.of(
			BIG_ENDER_CHEST_TERMINAL_ENABLED,
			BIG_ENDER_CHEST_TERMINAL_EXPANDED,
			BIG_ENDER_CHEST_FEEDBACK,
			BIG_ENDER_CHEST_TERMINAL_STYLE,
			BIG_ENDER_CHEST_TERMINAL_AMOUNT_FONT_SIZE,
			BIG_ENDER_CHEST_SORT_FIELD,
			BIG_ENDER_CHEST_SORT_DIRECTION,
			BIG_ENDER_CHEST_REMEMBER_SEARCH
	);
	public static final ImmutableList<IConfigBase> BIG_ENDER_CHEST_CHILDREN = ImmutableList.of(
			BIG_ENDER_CHEST_FEEDBACK,
			BIG_ENDER_CHEST_TERMINAL_STYLE,
			BIG_ENDER_CHEST_TERMINAL_AMOUNT_FONT_SIZE,
			BIG_ENDER_CHEST_SORT_FIELD,
			BIG_ENDER_CHEST_SORT_DIRECTION,
			BIG_ENDER_CHEST_REMEMBER_SEARCH
	);
	public static final ImmutableList<IConfigBase> PICKUP_FILTER_OPTIONS = ImmutableList.of(
			PICKUP_FILTER_ENABLED,
			PICKUP_FILTER_EXPANDED,
			PICKUP_FILTER_MODE,
			PICKUP_FILTER_ITEMS,
			PICKUP_FILTER_RARITIES,
			PICKUP_FILTER_FEEDBACK,
			PICKUP_FILTER_PAUSE_WHILE_SNEAKING
	);
	public static final ImmutableList<IConfigBase> PICKUP_FILTER_CHILDREN = ImmutableList.of(
			PICKUP_FILTER_MODE,
			PICKUP_FILTER_ITEMS,
			PICKUP_FILTER_RARITIES,
			PICKUP_FILTER_FEEDBACK,
			PICKUP_FILTER_PAUSE_WHILE_SNEAKING
	);
	/**
	 * Standalone switch without child options, so it is deliberately not part of
	 * {@code PARENT_GROUPS} (a parent group would render an expand button with nothing to show).
	 */
	public static final ImmutableList<IConfigBase> PROJECTION_MATERIAL_STATS_FIX_OPTIONS =
			ImmutableList.of(PROJECTION_MATERIAL_STATS_FIX);
	public static final ImmutableList<IConfigBase> VERIFIER_POSITION_PICKER_OPTIONS =
			ImmutableList.of(VERIFIER_POSITION_PICKER);
	private static final Map<IConfigBase, BooleanHotkeyGuiWrapper> GUI_BOOLEAN_WRAPPERS =
			new HashMap<>();
	private static final ImmutableList<ParentGroup> PARENT_GROUPS = ImmutableList.of(
			new ParentGroup(
					BETTER_EASY_PLACE,
					BETTER_EASY_PLACE_EXPANDED,
					BETTER_EASY_PLACE_CHILDREN
			),
			new ParentGroup(
					EASY_PLACE_V2_PROTOCOL_EXTRA,
					EASY_PLACE_EXPANDED,
					EASY_PLACE_CHILDREN
			),
			new ParentGroup(
					QUICK_SHULKER_HAND_RESTOCK,
					QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
					QUICK_SHULKER_HAND_RESTOCK_CHILDREN
			),
			new ParentGroup(
					BIG_ENDER_CHEST_TERMINAL_ENABLED,
					BIG_ENDER_CHEST_TERMINAL_EXPANDED,
					BIG_ENDER_CHEST_CHILDREN
			),
			new ParentGroup(
					PICKUP_FILTER_ENABLED,
					PICKUP_FILTER_EXPANDED,
					PICKUP_FILTER_CHILDREN
			)
	);
	public static final ImmutableList<IConfigBase> EXPANSION_STATE_OPTIONS =
			createExpansionStateOptions();
	/**
	 * "Litematica Assistance" tab: every option that changes Litematica behaviour, regardless of
	 * whether it was ported or written by TriWasa. Also the persisted "Litematica" section.
	 */
	public static final ImmutableList<IConfigBase> LITEMATICA_OPTIONS =
			ImmutableList.<IConfigBase>builder()
					.addAll(EASY_PLACE_OPTIONS)
					.addAll(BETTER_EASY_PLACE_OPTIONS)
					.addAll(EASY_PLACE_SELECTION_OPTIONS)
					.addAll(PROJECTION_MATERIAL_STATS_FIX_OPTIONS)
					.addAll(VERIFIER_POSITION_PICKER_OPTIONS)
					.build();
	/**
	 * "Auxiliary Features" tab: everything that is not Litematica related. Also the persisted
	 * "Utilities" section.
	 */
	public static final ImmutableList<IConfigBase> UTILITY_OPTIONS =
			ImmutableList.<IConfigBase>builder()
					.addAll(PICKUP_FILTER_OPTIONS)
					.addAll(QUICK_SHULKER_HAND_RESTOCK_OPTIONS)
					.addAll(BIG_ENDER_CHEST_OPTIONS)
					.build();
	public static final ImmutableList<IConfigBase> ALL_OPTIONS =
			ImmutableList.<IConfigBase>builder()
					.addAll(GENERAL_OPTIONS)
					.addAll(LITEMATICA_OPTIONS)
					.addAll(UTILITY_OPTIONS)
					.build();

	static
	{
		EASY_PLACE_V2_PROTOCOL_EXTRA.setValueChangeCallback(
				value -> EasyPlaceClientNetwork.onConfigChanged(value.getBooleanValue())
		);
		QUICK_SHULKER_HAND_RESTOCK.setValueChangeCallback(
				value -> QuickShulkerHandRestock.onConfigChanged()
		);
		BIG_ENDER_CHEST_TERMINAL_ENABLED.setValueChangeCallback(
				value -> BigEnderChestClientNetwork.onConfigChanged(value.getBooleanValue())
		);
		PICKUP_FILTER_ENABLED.setValueChangeCallback(value -> ItemPickupFilter.onConfigChanged());
		PICKUP_FILTER_MODE.setValueChangeCallback(value -> ItemPickupFilter.onConfigChanged());
		PICKUP_FILTER_ITEMS.setValueChangeCallback(value -> ItemPickupFilter.onConfigChanged());
		PICKUP_FILTER_RARITIES.setValueChangeCallback(value -> ItemPickupFilter.onConfigChanged());
		PROJECTION_MATERIAL_STATS_FIX.setValueChangeCallback(
				value -> MaterialCache.getInstance().clearCache()
		);
		VERIFIER_POSITION_PICKER.setValueChangeCallback(
				value -> VerifierOverlayRefresh.refreshAll()
		);
	}

	@Override
	public void load()
	{
		Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);

		if(Files.isRegularFile(configFile) && Files.isReadable(configFile))
		{
			JsonObject root = readObject(configFile);

			if(root != null)
			{
				if(root.has(GENERAL_KEY) && root.get(GENERAL_KEY).isJsonObject())
				{
					ConfigUtils.readConfigBase(root, GENERAL_KEY, GENERAL_OPTIONS);
				}
				if(root.has(LITEMATICA_KEY) && root.get(LITEMATICA_KEY).isJsonObject())
				{
					ConfigUtils.readConfigBase(root, LITEMATICA_KEY, LITEMATICA_OPTIONS);
				}
				if(root.has(UTILITIES_KEY) && root.get(UTILITIES_KEY).isJsonObject())
				{
					ConfigUtils.readConfigBase(root, UTILITIES_KEY, UTILITY_OPTIONS);
				}
				if(root.has(HOTKEYS_KEY) && root.get(HOTKEYS_KEY).isJsonObject())
				{
					ConfigUtils.readConfigBase(root, HOTKEYS_KEY, HOTKEY_OPTIONS);
				}
			}
		}

		ItemPickupFilter.onConfigChanged();
		BigEnderChestClientNetwork.onConfigChanged(
				BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue()
		);
	}

	@Override
	public void save()
	{
		Path configDir = FileUtils.getConfigDirectory();
		try
		{
			Files.createDirectories(configDir);
		}
		catch(IOException exception)
		{
			return;
		}

		JsonObject root = new JsonObject();
		ConfigUtils.writeConfigBase(root, GENERAL_KEY, GENERAL_OPTIONS);
		ConfigUtils.writeConfigBase(root, LITEMATICA_KEY, LITEMATICA_OPTIONS);
		ConfigUtils.writeConfigBase(root, UTILITIES_KEY, UTILITY_OPTIONS);
		ConfigUtils.writeConfigBase(root, HOTKEYS_KEY, HOTKEY_OPTIONS);
		JsonUtils.writeJsonToFile(root, configDir.resolve(CONFIG_FILE_NAME));
	}

	@Override
	public void onLanguageChanged(String languageCode)
	{
	}

	private static JsonObject readObject(Path path)
	{
		JsonElement element = JsonUtils.parseJsonFile(path);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	private static ConfigHotkey booleanHotkey(String name)
	{
		return new ConfigHotkey(name + "Hotkey", "");
	}

	/**
	 * Wraps a boolean option together with its hotkey so the config GUI renders one switch and
	 * one keybind button inside the same row. The wrapper delegates the value to the real option,
	 * so callbacks and persistence are unaffected.
	 */
	static IConfigBase getGuiConfig(IConfigBase config)
	{
		ConfigHotkey hotkey = BOOLEAN_HOTKEYS.get(config);

		if(hotkey == null || (config instanceof IConfigBoolean) == false)
		{
			return config;
		}

		return GUI_BOOLEAN_WRAPPERS.computeIfAbsent(
				config,
				key -> new BooleanHotkeyGuiWrapper(
						key.getName(),
						(IConfigBoolean) key,
						BOOLEAN_HOTKEYS.get(key).getKeybind()
				)
		);
	}

	static boolean isChildConfig(IConfigBase config)
	{
		for(ParentGroup group : PARENT_GROUPS)
		{
			if(group.children().contains(config))
			{
				return true;
			}
		}
		return false;
	}

	static boolean isExpandedParent(IConfigBase config)
	{
		ParentGroup group = findParentGroup(config);
		return group != null && group.expansion().getBooleanValue();
	}

	static ConfigBoolean getExpansionState(IConfigBase parent)
	{
		ParentGroup group = findParentGroup(parent);
		return group == null ? null : group.expansion();
	}

	static IConfigBase getParentConfig(IConfigBase child)
	{
		for(ParentGroup group : PARENT_GROUPS)
		{
			if(group.children().contains(child))
			{
				return group.parent();
			}
		}
		return null;
	}

	private static ParentGroup findParentGroup(IConfigBase parent)
	{
		for(ParentGroup group : PARENT_GROUPS)
		{
			if(group.parent() == parent)
			{
				return group;
			}
		}
		return null;
	}

	private static ImmutableList<IConfigBase> createExpansionStateOptions()
	{
		ImmutableList.Builder<IConfigBase> builder = ImmutableList.builder();
		for(ParentGroup group : PARENT_GROUPS)
		{
			builder.add(group.expansion());
		}
		return builder.build();
	}

	private record ParentGroup(
			IConfigBase parent,
			ConfigBoolean expansion,
			ImmutableList<IConfigBase> children
	)
	{
	}
}
