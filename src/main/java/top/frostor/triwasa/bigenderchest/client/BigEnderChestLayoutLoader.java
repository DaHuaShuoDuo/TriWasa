package top.frostor.triwasa.bigenderchest.client;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import top.frostor.triwasa.TriWasa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * Loads and validates the immutable terminal JSON shipped in TriWasa's assets.
 */
public final class BigEnderChestLayoutLoader
{
	public static final String NORMAL_RESOURCE_PATH =
			"/assets/triwasa/gui/big_ender_chest/normal_terminal.json";
	public static final String CRAFTING_FALLBACK_RESOURCE_PATH =
			"/assets/triwasa/gui/big_ender_chest/crafting_terminal.json";
	private static final Gson GSON = new Gson();
	private static final Map<String, AssetSize> ASSET_SIZES = Map.of(
			"crafting", new AssetSize(256, 256),
			"side_bottom", new AssetSize(24, 134),
			"sp_slots", new AssetSize(16, 80),
			"spirits", new AssetSize(128, 128),
			"terminal", new AssetSize(256, 256)
	);
	private static final Set<String> TERMINAL_REGIONS = Set.of(
			"header",
			"rowFirst",
			"row",
			"rowLast",
			"itemSlot",
			"footer",
			"playerInventorySlots",
			"hotbarSlots",
			"side",
			"searchHead",
			"searchMiddle",
			"searchTail",
			"scrollThumb",
			"hoverOverlay"
	);
	private static final Set<String> EQUIPMENT_REGIONS = Set.of(
			"header",
			"rowFirst",
			"row",
			"rowLast",
			"itemSlot",
			"footer",
			"playerInventorySlots",
			"hotbarSlots",
			"craftingMatrixSlots",
			"craftingResultSlot",
			"armorHelmetSlot",
			"armorChestSlot",
			"armorLegsSlot",
			"armorBootsSlot",
			"offhandSlot",
			"side",
			"searchHead",
			"searchMiddle",
			"searchTail",
			"scrollThumb",
			"hoverOverlay",
			"player"
	);
	private static final Set<String> REQUIRED_BUTTONS = Set.of(
			"sideButton1",
			"sideButton2",
			"sideButton3",
			"sideButton4",
			"sideButton5",
			"sideButton6"
	);
	private static BigEnderChestLayout cachedNormal;
	private static BigEnderChestLayout cachedCraftingFallback;

	private BigEnderChestLayoutLoader()
	{
	}

	public static synchronized BigEnderChestLayout loadDefault()
	{
		if(cachedNormal == null)
		{
			cachedNormal = loadResource(NORMAL_RESOURCE_PATH);
		}
		return cachedNormal;
	}

	/**
	 * Kept for the future authoritative crafting terminal. The normal terminal never
	 * falls back to this layout automatically.
	 */
	public static synchronized BigEnderChestLayout loadCraftingFallback()
	{
		if(cachedCraftingFallback == null)
		{
			cachedCraftingFallback = loadResource(CRAFTING_FALLBACK_RESOURCE_PATH);
		}
		return cachedCraftingFallback;
	}

	public static BigEnderChestLayout parse(java.io.Reader reader)
	{
		try
		{
			BigEnderChestLayout layout = GSON.fromJson(
					new BufferedReader(reader),
					BigEnderChestLayout.class
			);
			validate(layout);
			return layout;
		}
		catch(JsonParseException | IllegalArgumentException exception)
		{
			TriWasa.LOGGER.error("Invalid big ender chest terminal layout", exception);
			throw new IllegalArgumentException("Invalid terminal layout", exception);
		}
	}

	public static void validate(BigEnderChestLayout layout)
	{
		if(layout == null
				|| !BigEnderChestLayout.FORMAT.equals(layout.format())
				|| layout.version() != 1)
		{
			throw new IllegalArgumentException("Unsupported terminal layout format");
		}
		if(layout.screen() == null
				|| layout.layoutPolicy() == null
				|| layout.canonicalDesign() == null
				|| layout.sideButtons().size() != 6)
		{
			throw new IllegalArgumentException("Incomplete terminal layout");
		}
		if(layout.screen().width() != layout.canonicalDesign().width())
		{
			throw new IllegalArgumentException("Screen and canonical widths differ");
		}
		if(layout.layoutPolicy().rowHeight() <= 0
				|| layout.layoutPolicy().slotsPerRow() <= 0
				|| layout.layoutPolicy().minRows() < 1
				|| layout.layoutPolicy().styles().size() != 4)
		{
			throw new IllegalArgumentException("Invalid row policy");
		}
		if(layout.layoutPolicy().rowHeight() != layout.canonicalDesign().rowHeight()
				|| layout.canonicalDesign().headerHeight()
						!= layout.layoutPolicy().headerHeight()
				|| !layout.canonicalDesign().footerHeights().containsKey(layout.content()))
		{
			throw new IllegalArgumentException("Canonical dimensions do not match policy");
		}
		Set<String> styleIds = layout.layoutPolicy().styles().stream()
				.map(BigEnderChestLayout.Style::id)
				.collect(java.util.stream.Collectors.toSet());
		if(!styleIds.equals(Set.of("compact", "standard", "expanded", "fullHeight")))
		{
			throw new IllegalArgumentException("Missing or unexpected terminal styles");
		}
		if(!layout.canonicalDesign().backgroundOffsetOrder().equals(
				List.of("header", "rowFirst", "row", "rowLast", "footer")
		))
		{
			throw new IllegalArgumentException("Unexpected background offset order");
		}
		Set<String> expectedRegions = switch(layout.content())
		{
			case "terminal" -> TERMINAL_REGIONS;
			case "equipment" -> EQUIPMENT_REGIONS;
			default -> throw new IllegalArgumentException(
					"Unsupported terminal content: " + layout.content()
			);
		};
		Set<String> regionIds = layout.regions().stream()
				.map(BigEnderChestLayout.Region::id)
				.collect(java.util.stream.Collectors.toSet());
		if(regionIds.size() != layout.regions().size()
				|| !regionIds.equals(expectedRegions))
		{
			throw new IllegalArgumentException("Missing or unexpected terminal regions");
		}
		Set<String> buttonIds = layout.sideButtons().stream()
				.map(BigEnderChestLayout.SideButton::id)
				.collect(java.util.stream.Collectors.toSet());
		if(buttonIds.size() != layout.sideButtons().size()
				|| !buttonIds.equals(REQUIRED_BUTTONS))
		{
			throw new IllegalArgumentException("Missing or unexpected side buttons");
		}

		Map<String, BigEnderChestLayout.Material> materials = new HashMap<>();
		layout.regions().forEach(region ->
		{
			if(region.states().isEmpty() || !region.states().containsKey("normal"))
			{
				throw new IllegalArgumentException("Region has no normal material: " + region.id());
			}
			region.states().values().forEach(material ->
					materials.put(region.id() + "/" + material.sliceId(), material));
		});
		layout.sideButtons().forEach(button -> button.states().forEach(state -> {
			materials.put(button.id() + "/normal/" + state.normal().sliceId(), state.normal());
			materials.put(button.id() + "/hover/" + state.hover().sliceId(), state.hover());
		}));
		Map<String, Integer> expectedStateCounts = Map.of(
				"sideButton1", 1,
				"sideButton2", 2,
				"sideButton3", 1,
				"sideButton4", 2,
				"sideButton5", 1,
				"sideButton6", 4
		);
		layout.sideButtons().forEach(button -> {
			int expected = expectedStateCounts.getOrDefault(button.id(), 0);
			if(button.states().size() != expected)
			{
				throw new IllegalArgumentException(
						"Unexpected side button state count: " + button.id()
				);
			}
		});
		materials.values().forEach(BigEnderChestLayoutLoader::validateMaterial);
	}

	private static BigEnderChestLayout loadResource(String path)
	{
		try(InputStream input = BigEnderChestLayoutLoader.class.getResourceAsStream(path))
		{
			if(input == null)
			{
				throw new IllegalStateException("Missing terminal layout " + path);
			}
			return parse(new InputStreamReader(input, StandardCharsets.UTF_8));
		}
		catch(IOException exception)
		{
			throw new IllegalStateException("Failed to read terminal layout " + path, exception);
		}
	}

	private static void validateMaterial(BigEnderChestLayout.Material material)
	{
		material.validate();
		if(material.source() == null)
		{
			return;
		}
		AssetSize assetSize = ASSET_SIZES.get(material.asset());
		if(assetSize == null)
		{
			throw new IllegalArgumentException("Unknown material asset " + material.asset());
		}
		BigEnderChestLayout.Source source = material.source();
		if(source.x() + source.w() > assetSize.width()
				|| source.y() + source.h() > assetSize.height())
		{
			throw new IllegalArgumentException(
					"Material source exceeds " + material.asset()
			);
		}
	}

	private record AssetSize(int width, int height)
	{
	}
}
