package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.SchematicSelectionBoost;
import top.frostor.triwasa.easyplace.SchematicSelectionOutline;

import java.util.Collections;
import java.util.HashSet;
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
				TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.getBooleanValue() == false
						&& TriWasaConfigs.EASY_PLACE_CRAFTER_LOCK_SLOT.getBooleanValue() == false,
				"V2-Extra easy place and all of its child switches must default to disabled"
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
		check(
				TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_CHILDREN.contains(
						TriWasaConfigs.QUICK_SHULKER_OFFHAND_RESTOCK)
						&& TriWasaConfigs.getParentConfig(
								TriWasaConfigs.QUICK_SHULKER_OFFHAND_RESTOCK)
						== TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK,
				"offhand restock must be a child of the hand restock switch"
		);
		check(
				TriWasaConfigs.QUICK_SHULKER_OFFHAND_RESTOCK.getBooleanValue() == false,
				"offhand restock must default to disabled"
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
				TriWasaConfigs.BETTER_EASY_PLACE_OPTIONS.contains(TriWasaConfigs.BETTER_PRINTER)
						&& TriWasaConfigs.LITEMATICA_OPTIONS.contains(
								TriWasaConfigs.BETTER_EASY_PLACE),
				"betterEasyPlace must remain the better-placement parent option"
		);
		check(
				TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue() == false,
				"betterEasyPlace must default to disabled"
		);
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_EASY_PLACE)
						&& TriWasaConfigs.getExpansionState(TriWasaConfigs.BETTER_EASY_PLACE)
						== TriWasaConfigs.BETTER_EASY_PLACE_EXPANDED,
				"betterEasyPlace must remain an expandable parent switch"
		);
		check(
				TriWasaConfigs.LITEMATICA_OPTIONS.contains(TriWasaConfigs.BETTER_PRINTER),
				"betterPrinter must remain on the Litematica tab"
		);
		check(
				TriWasaConfigs.BETTER_PRINTER.getBooleanValue() == false,
				"betterPrinter must default to disabled"
		);
		check(TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_PRINTER),
				"betterPrinter must be a child of betterEasyPlace");
		check(
				TriWasaConfigs.getParentConfig(TriWasaConfigs.BETTER_PRINTER)
						== TriWasaConfigs.BETTER_EASY_PLACE,
				"betterPrinter parent changed"
		);
		check(
				TriWasaConfigs.EASY_PLACE_SELECTION_OPTIONS.contains(
						TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION)
						&& TriWasaConfigs.LITEMATICA_OPTIONS.contains(
								TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION)
						&& TriWasaConfigs.BETTER_EASY_PLACE_OPTIONS.contains(
								TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION) == false,
				"easy place full block selection must be a standalone Litematica option"
		);
		check(
				TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.getBooleanValue() == false,
				"easy place full block selection must default to disabled"
		);
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION)
						&& TriWasaConfigs.getParentConfig(
								TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION) == null
						&& TriWasaConfigs.getExpansionState(
								TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION) == null,
				"easy place full block selection must not belong to betterEasyPlace"
		);
		check(
				TriWasaConfigs.EASY_PLACE_SELECTION_OPTIONS.contains(
						TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE)
						&& TriWasaConfigs.LITEMATICA_OPTIONS.contains(
								TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE)
						&& TriWasaConfigs.BETTER_EASY_PLACE_OPTIONS.contains(
								TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE) == false,
				"schematic target outline must be a standalone Litematica option"
		);
		check(
				TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE.getBooleanValue() == false,
				"schematic target outline must default to disabled"
		);
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE)
						&& TriWasaConfigs.getParentConfig(
								TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE) == null
						&& TriWasaConfigs.getExpansionState(
								TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE) == null,
				"schematic target outline must not belong to betterEasyPlace"
		);
		check(
				TriWasaConfigs.LITEMATICA_OPTIONS.contains(
						TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX)
						&& TriWasaConfigs.LITEMATICA_OPTIONS.contains(
								TriWasaConfigs.VERIFIER_POSITION_PICKER)
						&& TriWasaConfigs.LITEMATICA_OPTIONS.contains(
								TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA),
				"every Litematica related option must stay on the Litematica tab"
		);
		check(
				TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX.getBooleanValue() == false,
				"projection material stats fix must default to disabled"
		);
		check(
				TriWasaConfigs.VERIFIER_POSITION_PICKER.getBooleanValue() == false
						&& TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue() == false
						&& TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK.getBooleanValue() == false
						&& TriWasaConfigs.PICKUP_FILTER_ENABLED.getBooleanValue() == false,
				"every feature master switch must default to disabled"
		);
		check(
				TriWasaConfigs.getExpansionState(TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX) == null,
				"projection material stats fix must stay a standalone switch"
		);
		check(
				TriWasaConfigs.UTILITY_OPTIONS.contains(TriWasaConfigs.PICKUP_FILTER_ENABLED)
						&& TriWasaConfigs.UTILITY_OPTIONS.contains(
								TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK)
						&& TriWasaConfigs.UTILITY_OPTIONS.contains(
								TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED),
				"non-Litematica features must stay on the auxiliary features tab"
		);
		check(
				Collections.disjoint(
						TriWasaConfigs.LITEMATICA_OPTIONS,
						TriWasaConfigs.UTILITY_OPTIONS),
				"the Litematica and auxiliary tabs must not overlap"
		);
		check(
				new HashSet<>(TriWasaConfigs.ALL_OPTIONS).size()
						== TriWasaConfigs.ALL_OPTIONS.size(),
				"the All tab must not list an option twice"
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

		check(booleanOptions == 19, "unexpected number of visible boolean options");

		boolean easyPlace = TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
		boolean printer = TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
		boolean fullBlockSelection =
				TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.getBooleanValue();
		boolean outline = TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE.getBooleanValue();
		try
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.setBooleanValue(true);
			TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE.setBooleanValue(true);
			check(!EasyPlaceRules.isEnabled(), "betterEasyPlace master switch is not authoritative");
			check(!EasyPlaceRules.isPrinterEnabled(), "betterPrinter must be inactive when its parent is off");
			check(
					SchematicSelectionBoost.isEnabled(),
					"full block selection must stay independent of betterEasyPlace"
			);
			check(
					SchematicSelectionOutline.isOutlineEnabled(),
					"outline must stay independent of betterEasyPlace"
			);

			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(true);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.setBooleanValue(false);
			check(EasyPlaceRules.isEnabled(), "betterEasyPlace master switch must remain active");
			check(!EasyPlaceRules.isPrinterEnabled(), "betterPrinter child switch is not authoritative");
			check(
					!SchematicSelectionBoost.isEnabled(),
					"full block selection switch is not authoritative"
			);
			check(
					SchematicSelectionOutline.isOutlineEnabled(),
					"outline must stay active independently of full block selection"
			);
		}
		finally
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(easyPlace);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(printer);
			TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.setBooleanValue(fullBlockSelection);
			TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE.setBooleanValue(outline);
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
