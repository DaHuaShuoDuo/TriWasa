package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;

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
						TriWasaConfigs.EASY_PLACE_EXPANDED,
						TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_EXPANDED,
						TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_EXPANDED,
						TriWasaConfigs.PICKUP_FILTER_EXPANDED
				)),
				"expansion-state ordering changed"
		);
		checkParent(
				TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA,
				TriWasaConfigs.EASY_PLACE_EXPANDED,
				TriWasaConfigs.EASY_PLACE_CHILDREN
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
						&& TriWasaConfigs.UTILITY_FILE_OPTIONS.contains(TriWasaConfigs.BETTER_EASY_PLACE),
				"betterEasyPlace must remain in the auxiliary feature list and file section"
		);
		check(
				TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue(),
				"betterEasyPlace must default to enabled"
		);
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_EASY_PLACE)
						&& TriWasaConfigs.getExpansionState(TriWasaConfigs.BETTER_EASY_PLACE) == null,
				"betterEasyPlace must remain a standalone parent switch"
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
		check(
				!TriWasaConfigs.isChildConfig(TriWasaConfigs.BETTER_PRINTER)
						&& TriWasaConfigs.getExpansionState(TriWasaConfigs.BETTER_PRINTER) == null,
				"betterPrinter must remain a standalone parent switch"
		);
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
