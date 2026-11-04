package top.frostor.triwasa.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.BigEnderChestAmountFontSize;
import top.frostor.triwasa.bigenderchest.BigEnderChestFeedback;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortDirection;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortField;
import top.frostor.triwasa.bigenderchest.BigEnderChestTerminalStyle;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestClientNetwork;
import top.frostor.triwasa.easyplace.EasyPlaceClientNetwork;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;
import top.frostor.triwasa.pickupfilter.PickupFilterFeedback;
import top.frostor.triwasa.pickupfilter.PickupFilterMode;
import top.frostor.triwasa.quickshulker.QuickShulkerHandRestock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TriWasaConfigs implements IConfigHandler
{
	public static final String GENERAL_KEY = "General";
	public static final String PORTING_KEY = "Porting";
	public static final String UTILITIES_KEY = "Utilities";
	public static final String BIG_ENDER_CHEST_KEY = "BigEnderChest";
	public static final String PICKUP_FILTER_KEY = "ItemPickupFilter";
	public static final String CONFIG_FILE_NAME = TriWasa.MOD_ID + ".json";
	public static final String GENERAL_TRANSLATION_PREFIX = "triwasa.config.general";
	public static final String PORTING_TRANSLATION_PREFIX = "triwasa.config.porting";
	public static final String UTILITIES_TRANSLATION_PREFIX = "triwasa.config.utilities";
	public static final String BIG_ENDER_CHEST_TRANSLATION_PREFIX =
			"triwasa.config.big_ender_chest";
	public static final String PICKUP_FILTER_TRANSLATION_PREFIX = "triwasa.config.pickup_filter";
	private static final String LEGACY_CONFIG_FILE_NAME = "earlycompat.json";

	public static final ConfigHotkey OPEN_CONFIG_GUI =
			new ConfigHotkey("openConfigGui", "T,C")
					.apply(GENERAL_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_V2_PROTOCOL_EXTRA =
			new ConfigBoolean("easyPlaceV2ProtocolExtra", true).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_EXPANDED =
			new ConfigBoolean("easyPlaceExpanded", false).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE =
			new ConfigBoolean("easyPlaceRailBlockNoShapeUpdate", true).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_CRAFTER_LOCK_SLOT =
			new ConfigBoolean("easyPlaceCrafterLockSlot", true).apply(PORTING_TRANSLATION_PREFIX);

	public static final ConfigBoolean QUICK_SHULKER_HAND_RESTOCK =
			new ConfigBoolean("quickShulkerHandRestock", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigBoolean QUICK_SHULKER_HAND_RESTOCK_EXPANDED =
			new ConfigBoolean("quickShulkerHandRestockExpanded", false).apply(UTILITIES_TRANSLATION_PREFIX);
	public static final ConfigInteger QUICK_SHULKER_HAND_RESTOCK_THRESHOLD =
			new ConfigInteger("quickShulkerHandRestockThreshold", 8, 1, 63, true)
					.apply(UTILITIES_TRANSLATION_PREFIX);

	public static final ConfigBoolean BIG_ENDER_CHEST_TERMINAL_ENABLED =
			new ConfigBoolean("bigEnderChestTerminalEnabled", true)
					.apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigBoolean BIG_ENDER_CHEST_TERMINAL_EXPANDED =
			new ConfigBoolean("bigEnderChestTerminalExpanded", false)
					.apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_FEEDBACK =
			new ConfigOptionList(
					"bigEnderChestFeedback",
					BigEnderChestFeedback.ACTION_BAR
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_TERMINAL_STYLE =
			new ConfigOptionList(
					"bigEnderChestTerminalStyle",
					BigEnderChestTerminalStyle.STANDARD
			).apply(BIG_ENDER_CHEST_TRANSLATION_PREFIX);
	public static final ConfigOptionList BIG_ENDER_CHEST_TERMINAL_AMOUNT_FONT_SIZE =
			new ConfigOptionList(
					"bigEnderChestTerminalAmountFontSize",
					BigEnderChestAmountFontSize.SMALL
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

	public static final ImmutableList<IConfigBase> GENERAL_OPTIONS =
			ImmutableList.of(OPEN_CONFIG_GUI);
	public static final ImmutableList<IConfigBase> EASY_PLACE_OPTIONS = ImmutableList.of(
			EASY_PLACE_V2_PROTOCOL_EXTRA,
			EASY_PLACE_EXPANDED,
			EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);
	public static final ImmutableList<IConfigBase> EASY_PLACE_CHILDREN = ImmutableList.of(
			EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);
	public static final ImmutableList<IConfigBase> QUICK_SHULKER_HAND_RESTOCK_OPTIONS = ImmutableList.of(
			QUICK_SHULKER_HAND_RESTOCK,
			QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
			QUICK_SHULKER_HAND_RESTOCK_THRESHOLD
	);
	public static final ImmutableList<IConfigBase> QUICK_SHULKER_HAND_RESTOCK_CHILDREN = ImmutableList.of(
			QUICK_SHULKER_HAND_RESTOCK_THRESHOLD
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
	public static final ImmutableList<IConfigBase> EXPANSION_STATE_OPTIONS = ImmutableList.of(
			EASY_PLACE_EXPANDED,
			QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
			BIG_ENDER_CHEST_TERMINAL_EXPANDED,
			PICKUP_FILTER_EXPANDED
	);
	public static final ImmutableList<IConfigBase> PORTING_OPTIONS = ImmutableList.<IConfigBase>builder()
			.addAll(EASY_PLACE_OPTIONS)
			.addAll(PICKUP_FILTER_OPTIONS)
			.build();
	public static final ImmutableList<IConfigBase> PORTING_FILE_OPTIONS = EASY_PLACE_OPTIONS;
	public static final ImmutableList<IConfigBase> UTILITY_OPTIONS =
			ImmutableList.<IConfigBase>builder()
					.addAll(QUICK_SHULKER_HAND_RESTOCK_OPTIONS)
					.addAll(BIG_ENDER_CHEST_OPTIONS)
					.build();
	public static final ImmutableList<IConfigBase> ALL_OPTIONS =
			ImmutableList.<IConfigBase>builder()
					.addAll(GENERAL_OPTIONS)
					.addAll(PORTING_OPTIONS)
					.addAll(UTILITY_OPTIONS)
					.build();
	public static final ImmutableList<IConfigBase> UTILITY_FILE_OPTIONS =
			QUICK_SHULKER_HAND_RESTOCK_OPTIONS;

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
	}

	@Override
	public void load()
	{
		Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);
		boolean loadedPorting = false;
		boolean loadedUtilities = false;

		if(Files.isRegularFile(configFile) && Files.isReadable(configFile))
		{
			JsonObject root = readObject(configFile);
			if(root != null && root.has(GENERAL_KEY) && root.get(GENERAL_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, GENERAL_KEY, GENERAL_OPTIONS);
			}
			if(root != null && root.has(PORTING_KEY) && root.get(PORTING_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, PORTING_KEY, PORTING_FILE_OPTIONS);
				loadedPorting = true;
			}
			if(root != null && root.has(PICKUP_FILTER_KEY) && root.get(PICKUP_FILTER_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, PICKUP_FILTER_KEY, PICKUP_FILTER_OPTIONS);
			}
			if(root != null && root.has(UTILITIES_KEY) && root.get(UTILITIES_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, UTILITIES_KEY, UTILITY_FILE_OPTIONS);
				loadedUtilities = true;
			}
			if(root != null
					&& root.has(BIG_ENDER_CHEST_KEY)
					&& root.get(BIG_ENDER_CHEST_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, BIG_ENDER_CHEST_KEY, BIG_ENDER_CHEST_OPTIONS);
			}
			if(!loadedUtilities && root != null
					&& root.has(PORTING_KEY) && root.get(PORTING_KEY).isJsonObject())
			{
				// Early development builds stored the original utility under Porting.
				ConfigUtils.readConfigBase(root, PORTING_KEY, UTILITY_FILE_OPTIONS);
			}
		}

		if(!loadedPorting)
		{
			migrateLegacyOptions();
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
		ConfigUtils.writeConfigBase(root, PORTING_KEY, PORTING_FILE_OPTIONS);
		ConfigUtils.writeConfigBase(root, UTILITIES_KEY, UTILITY_FILE_OPTIONS);
		ConfigUtils.writeConfigBase(root, PICKUP_FILTER_KEY, PICKUP_FILTER_OPTIONS);
		ConfigUtils.writeConfigBase(root, BIG_ENDER_CHEST_KEY, BIG_ENDER_CHEST_OPTIONS);
		JsonUtils.writeJsonToFile(root, configDir.resolve(CONFIG_FILE_NAME));
	}

	@Override
	public void onLanguageChanged(String languageCode)
	{
	}

	private static void migrateLegacyOptions()
	{
		Path legacyFile = FileUtils.getConfigDirectory().resolve(LEGACY_CONFIG_FILE_NAME);
		if(!Files.isRegularFile(legacyFile) || !Files.isReadable(legacyFile))
		{
			return;
		}

		JsonObject root = readObject(legacyFile);
		if(root == null || !root.has("options") || !root.get("options").isJsonObject())
		{
			return;
		}

		JsonObject options = root.getAsJsonObject("options");
		for(IConfigBase config : EASY_PLACE_OPTIONS)
		{
			if(options.has(config.getName()))
			{
				config.setValueFromJsonElement(options.get(config.getName()));
			}
		}
	}

	private static JsonObject readObject(Path path)
	{
		JsonElement element = JsonUtils.parseJsonFile(path);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	static boolean isChildConfig(IConfigBase config)
	{
		return EASY_PLACE_CHILDREN.contains(config)
				|| QUICK_SHULKER_HAND_RESTOCK_CHILDREN.contains(config)
				|| BIG_ENDER_CHEST_CHILDREN.contains(config)
				|| PICKUP_FILTER_CHILDREN.contains(config);
	}

	static boolean isExpandedParent(IConfigBase config)
	{
		if(config == EASY_PLACE_V2_PROTOCOL_EXTRA)
		{
			return EASY_PLACE_EXPANDED.getBooleanValue();
		}
		if(config == QUICK_SHULKER_HAND_RESTOCK)
		{
			return QUICK_SHULKER_HAND_RESTOCK_EXPANDED.getBooleanValue();
		}
		if(config == BIG_ENDER_CHEST_TERMINAL_ENABLED)
		{
			return BIG_ENDER_CHEST_TERMINAL_EXPANDED.getBooleanValue();
		}
		if(config == PICKUP_FILTER_ENABLED)
		{
			return PICKUP_FILTER_EXPANDED.getBooleanValue();
		}
		return false;
	}

	static ConfigBoolean getExpansionState(IConfigBase parent)
	{
		if(parent == EASY_PLACE_V2_PROTOCOL_EXTRA)
		{
			return EASY_PLACE_EXPANDED;
		}
		if(parent == QUICK_SHULKER_HAND_RESTOCK)
		{
			return QUICK_SHULKER_HAND_RESTOCK_EXPANDED;
		}
		if(parent == BIG_ENDER_CHEST_TERMINAL_ENABLED)
		{
			return BIG_ENDER_CHEST_TERMINAL_EXPANDED;
		}
		if(parent == PICKUP_FILTER_ENABLED)
		{
			return PICKUP_FILTER_EXPANDED;
		}
		return null;
	}

	static IConfigBase getParentConfig(IConfigBase child)
	{
		if(EASY_PLACE_CHILDREN.contains(child))
		{
			return EASY_PLACE_V2_PROTOCOL_EXTRA;
		}
		if(QUICK_SHULKER_HAND_RESTOCK_CHILDREN.contains(child))
		{
			return QUICK_SHULKER_HAND_RESTOCK;
		}
		if(BIG_ENDER_CHEST_CHILDREN.contains(child))
		{
			return BIG_ENDER_CHEST_TERMINAL_ENABLED;
		}
		if(PICKUP_FILTER_CHILDREN.contains(child))
		{
			return PICKUP_FILTER_ENABLED;
		}
		return null;
	}
}
