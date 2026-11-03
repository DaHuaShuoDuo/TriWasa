package top.frostor.triwasa.bigenderchest.protocol;

import net.minecraft.resources.Identifier;

/**
 * Wire constants shared with FCA protocol v2.
 */
public final class BigEnderChestProtocol
{
	public static final String MOD_ID = "frostors-carpet-addition";
	public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(
			MOD_ID,
			"big_ender_chest"
	);
	public static final int VERSION = 2;
	public static final int MAX_CHUNK_BYTES = 256 * 1024;
	public static final Identifier TERMINAL_MENU_ID = Identifier.fromNamespaceAndPath(
			MOD_ID,
			"big_ender_chest_terminal"
	);
	public static final int PLAYER_SLOT_COUNT = 36;
	public static final int MAIN_INVENTORY_SLOT_COUNT = 27;
	public static final int HOTBAR_SLOT_COUNT = 9;

	public static final int CAPABILITY_SNAPSHOT = BigEnderChestCapabilities.SNAPSHOT;
	public static final int CAPABILITY_DELTA = BigEnderChestCapabilities.DELTA;
	public static final int CAPABILITY_ACTIONS = BigEnderChestCapabilities.ACTIONS;
	public static final int CAPABILITY_SEARCH_LOCAL = BigEnderChestCapabilities.SEARCH_LOCAL;
	public static final int CAPABILITY_PAGE_CATALOG = BigEnderChestCapabilities.PAGE_CATALOG;
	public static final int CAPABILITY_TERMINAL_MENU = BigEnderChestCapabilities.TERMINAL_MENU;
	public static final int CAPABILITY_BATCH_ACTIONS = BigEnderChestCapabilities.BATCH_ACTIONS;
	public static final int CAPABILITY_REGION_ACTIONS = BigEnderChestCapabilities.REGION_ACTIONS;
	public static final int CAPABILITY_DROP_ACTIONS = BigEnderChestCapabilities.DROP_ACTIONS;
	public static final int CAPABILITY_SEQUENCED_ACTIONS = BigEnderChestCapabilities.SEQUENCED_ACTIONS;
	public static final int CAPABILITY_SNAPSHOT_ACK = BigEnderChestCapabilities.SNAPSHOT_ACK;
	public static final int CAPABILITY_SESSION_CLOSE = BigEnderChestCapabilities.SESSION_CLOSE;
	public static final int CAPABILITIES = BigEnderChestCapabilities.REQUIRED;

	public static final int HELLO = 1;
	public static final int HELLO_ACK = 2;
	public static final int SESSION_OPEN = 3;
	public static final int SNAPSHOT_BEGIN = 4;
	public static final int SNAPSHOT_CHUNK = 5;
	public static final int DELTA = 6;
	public static final int ACTION_REQUEST = 7;
	public static final int ACTION_RESULT = 8;
	public static final int SESSION_CLOSE = 9;
	public static final int PAGE_CATALOG = 10;
	public static final int SNAPSHOT_END = 11;
	public static final int SNAPSHOT_ACK = 12;
	public static final int REQUEST_RESYNC = 13;
	public static final int OPEN_TERMINAL_REQUEST = 14;

	public static final int ACTION_TAKE_ONE = 1;
	public static final int ACTION_TAKE_STACK = 2;
	public static final int ACTION_PUT_ONE = 3;
	public static final int ACTION_PUT_STACK = 4;
	public static final int ACTION_QUICK_MOVE_TO_PLAYER = 5;
	public static final int ACTION_QUICK_MOVE_FROM_PLAYER = 6;
	public static final int ACTION_TAKE_AMOUNT = 7;
	public static final int ACTION_DROP_ONE = 8;
	public static final int ACTION_DROP_STACK = 9;
	public static final int ACTION_BATCH_PUT_PLAYER_SLOTS = 10;
	public static final int ACTION_BATCH_STORAGE_TO_PLAYER = 11;

	public static final int RESULT_OK = 0;
	public static final int RESULT_SESSION_EXPIRED = 1;
	public static final int RESULT_REVISION_STALE = 2;
	public static final int RESULT_PAGE_NOT_READY = 3;
	public static final int RESULT_SLOT_MISMATCH = 4;
	public static final int RESULT_CAPACITY_FULL = 5;
	public static final int RESULT_INVENTORY_FULL = 6;
	public static final int RESULT_PERMISSION_DENIED = 7;
	public static final int RESULT_INTERNAL_ERROR = 8;
	public static final int RESULT_PROTOCOL_REJECTED = 9;
	public static final int RESULT_PAGE_LOAD_FAILED = 10;
	public static final int RESULT_DISK_FULL = 11;
	public static final int RESULT_FEATURE_READ_ONLY = 12;
	public static final int RESULT_INDEX_INVALID = 13;
	public static final int RESULT_RESYNC_REQUIRED = 14;
	public static final int RESULT_RATE_LIMITED = 15;
	public static final int RESULT_DUPLICATE_REQUEST = 16;
	public static final int RESULT_ENTRY_EMPTY = 17;

	private BigEnderChestProtocol()
	{
	}

	public static String resultTranslationKey(int status)
	{
		return "triwasa.big_ender_chest.result." + status;
	}
}
