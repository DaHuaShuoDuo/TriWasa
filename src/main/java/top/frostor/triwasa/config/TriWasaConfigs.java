package top.frostor.triwasa.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.easyplace.EasyPlaceClientNetwork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TriWasaConfigs implements IConfigHandler
{
	public static final String PORTING_KEY = "Porting";
	public static final String CONFIG_FILE_NAME = TriWasa.MOD_ID + ".json";
	public static final String PORTING_TRANSLATION_PREFIX = "triwasa.config.porting";
	private static final String LEGACY_CONFIG_FILE_NAME = "earlycompat.json";

	public static final ConfigBoolean EASY_PLACE_V2_PROTOCOL_EXTRA =
			new ConfigBoolean("easyPlaceV2ProtocolExtra", true).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_EXPANDED =
			new ConfigBoolean("easyPlaceExpanded", false).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE =
			new ConfigBoolean("easyPlaceRailBlockNoShapeUpdate", true).apply(PORTING_TRANSLATION_PREFIX);
	public static final ConfigBoolean EASY_PLACE_CRAFTER_LOCK_SLOT =
			new ConfigBoolean("easyPlaceCrafterLockSlot", true).apply(PORTING_TRANSLATION_PREFIX);

	public static final ImmutableList<IConfigBase> PORTING_OPTIONS = ImmutableList.of(
			EASY_PLACE_V2_PROTOCOL_EXTRA,
			EASY_PLACE_EXPANDED,
			EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);
	public static final ImmutableList<IConfigBase> EASY_PLACE_CHILDREN = ImmutableList.of(
			EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE,
			EASY_PLACE_CRAFTER_LOCK_SLOT
	);

	static
	{
		EASY_PLACE_V2_PROTOCOL_EXTRA.setValueChangeCallback(
				value -> EasyPlaceClientNetwork.onConfigChanged(value.getBooleanValue())
		);
	}

	@Override
	public void load()
	{
		Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME);
		boolean loaded = false;

		if(Files.isRegularFile(configFile) && Files.isReadable(configFile))
		{
			JsonObject root = readObject(configFile);
			if(root != null && root.has(PORTING_KEY) && root.get(PORTING_KEY).isJsonObject())
			{
				ConfigUtils.readConfigBase(root, PORTING_KEY, PORTING_OPTIONS);
				loaded = true;
			}
		}

		if(!loaded)
		{
			migrateLegacyOptions();
		}
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
		ConfigUtils.writeConfigBase(root, PORTING_KEY, PORTING_OPTIONS);
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
		for(IConfigBase config : PORTING_OPTIONS)
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
}
