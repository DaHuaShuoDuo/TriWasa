package top.frostor.triwasa.bigenderchest.protocol;

import java.util.List;

/**
 * Capabilities understood by TriWasa's independent FCA protocol v2 client.
 */
public final class BigEnderChestCapabilities
{
	public static final int SNAPSHOT = 1;
	public static final int DELTA = 1 << 1;
	public static final int ACTIONS = 1 << 2;
	public static final int SEARCH_LOCAL = 1 << 3;
	public static final int PAGE_CATALOG = 1 << 4;
	public static final int TERMINAL_MENU = 1 << 5;
	public static final int BATCH_ACTIONS = 1 << 6;
	public static final int REGION_ACTIONS = 1 << 7;
	public static final int DROP_ACTIONS = 1 << 8;
	public static final int SEQUENCED_ACTIONS = 1 << 9;
	public static final int SNAPSHOT_ACK = 1 << 10;
	public static final int SESSION_CLOSE = 1 << 11;

	public static final int REQUIRED = SNAPSHOT
			| DELTA
			| ACTIONS
			| SEARCH_LOCAL
			| PAGE_CATALOG
			| TERMINAL_MENU
			| BATCH_ACTIONS
			| REGION_ACTIONS
			| DROP_ACTIONS
			| SEQUENCED_ACTIONS
			| SNAPSHOT_ACK
			| SESSION_CLOSE;

	private BigEnderChestCapabilities()
	{
	}

	public static boolean hasAll(int capabilities, int required)
	{
		return (capabilities & required) == required;
	}

	public static List<String> names(int capabilities)
	{
		List<String> supported = new java.util.ArrayList<>();
		append(supported, capabilities, SNAPSHOT, "snapshot");
		append(supported, capabilities, DELTA, "delta");
		append(supported, capabilities, ACTIONS, "actions");
		append(supported, capabilities, SEARCH_LOCAL, "search_local");
		append(supported, capabilities, PAGE_CATALOG, "page_catalog");
		append(supported, capabilities, TERMINAL_MENU, "terminal_menu");
		append(supported, capabilities, BATCH_ACTIONS, "batch_actions");
		append(supported, capabilities, REGION_ACTIONS, "region_actions");
		append(supported, capabilities, DROP_ACTIONS, "drop_actions");
		append(supported, capabilities, SEQUENCED_ACTIONS, "sequenced_actions");
		append(supported, capabilities, SNAPSHOT_ACK, "snapshot_ack");
		append(supported, capabilities, SESSION_CLOSE, "session_close");
		return List.copyOf(supported);
	}

	private static void append(
			List<String> names,
			int capabilities,
			int capability,
			String name
	)
	{
		if((capabilities & capability) != 0)
		{
			names.add(name);
		}
	}
}
