package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

import java.util.List;

public final class TriWasaConfigMetadataTest
{
	private static int checks;

	private TriWasaConfigMetadataTest()
	{
	}

	public static void run()
	{
		check(
				TriWasaConfigs.EXPANSION_STATE_OPTIONS.equals(List.of(
						TriWasaConfigs.BETTER_EASY_PLACE_EXPANDED,
						TriWasaConfigs.EASY_PLACE_EXPANDED,
						TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
						TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_EXPANDED,
						TriWasaConfigs.PICKUP_FILTER_EXPANDED
				)),
				"expansion-state ordering changed"
		);
		checkParent(
				TriWasaConfigs.BETTER_EASY_PLACE,
				TriWasaConfigs.BETTER_EASY_PLACE_EXPANDED,
				TriWasaConfigs.BETTER_EASY_PLACE_CHILDREN
		);
		checkParent(
				TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				TriWasaConfigs.EASY_PLACE_EXPANDED,
				TriWasaConfigs.EASY_PLACE_CHILDREN
		);
		check(
				TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue()
						&& TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue()
					&& TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue()
					&& TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.getBooleanValue()
					&& TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.getBooleanValue(),
			"easy-place protection child switches must default to enabled"
		);
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE)
						== TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				"wall update protection must be an Easy Place child"
		);
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION)
						== TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				"observer protection must be an Easy Place child"
		);
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.EASY_PLACE_TNT_PROTECTION)
						== TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				"TNT protection must be an Easy Place child"
		);
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION)
						== TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				"rail protection must be an Easy Place child"
		);
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION)
						== TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				"piston protection must be an Easy Place child"
		);
		check(
				TriWasaConfigOptionWidget.getTooltipWrapWidth(100) == 80
						&& TriWasaConfigOptionWidget.getTooltipWrapWidth(400) == 320
						&& TriWasaConfigOptionWidget.getTooltipWrapWidth(1000) == 320,
				"config tooltips must use bounded screen-aware wrapping"
		);
		checkParent(
				TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK,
				TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
				TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_CHILDREN
		);
		checkParent(
				TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED,
				TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_EXPANDED,
				TriWasaConfigs.BIG_ENDER_CHEST_CHILDREN
		);
		checkParent(
				TriWasaConfigs.PICKUP_FILTER_ENABLED,
				TriWasaConfigs.PICKUP_FILTER_EXPANDED,
				TriWasaConfigs.PICKUP_FILTER_CHILDREN
		);
		check(
				TriWasaConfigs.UTILITY_OPTIONS.contains(TriWasaConfigs.BETTER_EASY_PLACE)
						&& TriWasaConfigs.UTILITY_FILE_OPTIONS.contains(TriWasaConfigs.BETTER_EASY_PLACE)
						&& TriWasaConfigs.BETTER_EASY_PLACE_OPTIONS.contains(TriWasaConfigs.BETTER_PRINTER),
				"betterEasyPlace must remain the better-placement parent option"
		);
		check(
				TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue(),
				"betterEasyPlace must default to enabled"
		);
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_EASY_PLACE)
						&& TriWasaConfigs.getExpansionState(TriWasaConfigs.BETTER_EASY_PLACE)
						== TriWasaConfigs.BETTER_EASY_PLACE_EXPANDED,
				"betterEasyPlace must remain an expandable parent switch"
		);
		check(
				TriWasaConfigs.UTILITY_OPTIONS.contains(TriWasaConfigs.BETTER_PRINTER)
						&& TriWasaConfigs.UTILITY_FILE_OPTIONS.contains(TriWasaConfigs.BETTER_PRINTER),
				"betterPrinter must remain in the auxiliary feature list and file section"
		);
		check(
				TriWasaConfigs.BETTER_PRINTER.getBooleanValue(),
				"betterPrinter must default to enabled"
		);
		check(TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_PRINTER),
				"betterPrinter must be a child of betterEasyPlace");
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.BETTER_PRINTER)
						== TriWasaConfigs.BETTER_EASY_PLACE,
				"betterPrinter parent changed"
		);
		check(
				TriWasaConfigs.UTILITY_OPTIONS.contains(TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX)
						&& TriWasaConfigs.UTILITY_FILE_OPTIONS.contains(
								TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX),
				"projection material stats fix must remain a utility option"
		);
		check(
				TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX.getBooleanValue() == false,
				"projection material stats fix must default to disabled"
		);
		check(
				TriWasaConfigs.getExpansionState(TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX) == null,
				"projection material stats fix must stay a standalone switch"
		);

		int booleanOptions = 0;

		for(IConfigBase config : TriWasaConfigs.ALL_OPTIONS)
		{
			if((config instanceof IConfigBoolean) == false
					|| TriWasaConfigs.EXPANSION_STATE_OPTIONS.contains(config))
			{
				continue;
			}

			booleanOptions++;
			ConfigHotkey hotkey = TriWasaConfigs.BOOLEAN_HOTKEYS.get(config);
			check(hotkey != null, "every visible boolean option must own a hotkey");
			check(
					hotkey.getHotkeyStringValue().isEmpty(),
					"boolean hotkeys must default to unbound"
			);
			check(
					TriWasaConfigs.getGuiConfig(config) != config,
					"boolean options must render together with their hotkey"
			);
			check(
					TriWasaConfigs.HOTKEY_OPTIONS.contains(hotkey)
							&& TriWasaConfigs.ALL_OPTIONS.contains(hotkey) == false,
					"hotkeys must be persisted without becoming their own config row"
			);
		}

		check(booleanOptions == 16, "unexpected number of visible boolean options");

		boolean easyPlace = TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
		boolean printer = TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
		try
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(false);
			check(!EasyPlaceRules.isEnabled(), "betterEasyPlace master switch is not authoritative");
			check(!EasyPlaceRules.isPrinterEnabled(), "betterPrinter must be inactive when its parent is off");

			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(true);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(false);
			check(EasyPlaceRules.isEnabled(), "betterEasyPlace master switch must remain active");
			check(!EasyPlaceRules.isPrinterEnabled(), "betterPrinter child switch is not authoritative");
		}
		finally
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(easyPlace);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(printer);
		}
		System.out.println("TriWasaConfigMetadataTest passed " + checks + " checks");
	}

	private static void checkParent(
			IConfigBase parent,
			IConfigBase expansion,
			List<IConfigBase> children
	)
	{
		check(!TriWasaConfigs.isChildConfig(parent), "a parent must not be a child");
		check(
				TriWasaConfigs.getExpansionState(parent) == expansion,
				"parent expansion option changed"
		);
		for(IConfigBase child : children)
		{
			check(TriWasaConfigs.isChildConfig(child), "child visibility metadata changed");
			check(
					TriWasaConfigs.getParentConfig(child) == parent,
					"child parent metadata changed"
			);
		}
	}

	private static void check(boolean condition, String message)
	{
		checks++;
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}
