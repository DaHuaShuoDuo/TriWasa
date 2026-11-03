package top.frostor.triwasa.bigenderchest.protocol;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NBT records mirroring the stable FCA protocol v2 field names.
 */
public final class BigEnderChestMessages
{
	private BigEnderChestMessages()
	{
	}

	public record Hello(int protocolVersion, int capabilities, int maxChunkBytes)
	{
		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.putInt("protocol_version", this.protocolVersion);
			tag.putInt("capabilities", this.capabilities);
			tag.putInt("max_chunk_bytes", this.maxChunkBytes);
			return tag;
		}

		public static Hello fromTag(CompoundTag tag)
		{
			return new Hello(
					tag.getIntOr("protocol_version", -1),
					tag.getIntOr("capabilities", 0),
					tag.getIntOr("max_chunk_bytes", 0)
			);
		}
	}

	public record HelloAck(
			int protocolVersion,
			int capabilities,
			int chunkByteLimit,
			boolean accepted
	)
	{
		public static HelloAck fromTag(CompoundTag tag)
		{
			return new HelloAck(
					tag.getIntOr("protocol_version", -1),
					tag.getIntOr("capabilities", 0),
					tag.getIntOr("chunk_byte_limit", 0),
					tag.getBooleanOr("accepted", false)
			);
		}
	}

	public record SessionOpen(
			long sessionId,
			int containerId,
			long revision,
			int pageLimit,
			int pageCount,
			long pageRevision,
			boolean readOnly,
			int capabilities,
			int terminalMenuId,
			int flags
	)
	{
		public static SessionOpen fromTag(CompoundTag tag)
		{
			return new SessionOpen(
					tag.getLongOr("session_id", 0),
					tag.getIntOr("container_id", -1),
					tag.getLongOr("revision", -1),
					tag.getIntOr("page_limit", 0),
					tag.getIntOr("current_page_count", 1),
					tag.getLongOr("page_revision", -1),
					tag.getBooleanOr("read_only", false),
					tag.getIntOr("capabilities", 0),
					tag.getIntOr("terminal_menu_id", -1),
					tag.getIntOr("flags", 0)
			);
		}
	}

	public record PageInfo(
			int pageId,
			String state,
			boolean empty,
			int occupiedSlots,
			long contentRevision
	)
	{
		public static PageInfo fromTag(CompoundTag tag)
		{
			return new PageInfo(
					tag.getIntOr("page_id", -1),
					tag.getStringOr("state", "ERROR"),
					tag.getBooleanOr("empty", true),
					tag.getIntOr("occupied_slots", 0),
					tag.getLongOr("content_revision", 0)
			);
		}
	}

	public record PageCatalog(
			long sessionId,
			long pageRevision,
			int totalSlots,
			int occupiedSlots,
			boolean readOnly,
			List<PageInfo> pages
	)
	{
		public PageCatalog
		{
			pages = List.copyOf(pages);
		}

		public static PageCatalog fromTag(CompoundTag tag)
		{
			List<PageInfo> pages = new ArrayList<>();
			tag.getListOrEmpty("pages").forEach(element ->
					pages.add(PageInfo.fromTag((CompoundTag) element)));
			return new PageCatalog(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("page_revision", 0),
					tag.getIntOr("total_slots", 0),
					tag.getIntOr("occupied_slots", 0),
					tag.getBooleanOr("read_only", false),
					pages
			);
		}
	}

	public record SnapshotBegin(
			long sessionId,
			long snapshotId,
			long revision,
			int entryCount,
			int locationCount,
			int chunkCount
	)
	{
		public static SnapshotBegin fromTag(CompoundTag tag)
		{
			return new SnapshotBegin(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("snapshot_id", 0),
					tag.getLongOr("revision", -1),
					tag.getIntOr("entry_count", tag.getIntOr("template_count", 0)),
					tag.getIntOr("location_count", tag.getIntOr("slot_count", 0)),
					tag.getIntOr("chunk_count", 0)
			);
		}
	}

	public record EntryRecord(
			long templateSerial,
			ItemStack item,
			long totalAmount,
			List<SlotRecord> locations
	)
	{
		public EntryRecord
		{
			item = item.copyWithCount(1);
			locations = locations == null ? List.of() : List.copyOf(locations);
		}

		public static EntryRecord fromTag(CompoundTag tag, HolderLookup.Provider registries)
		{
			ItemStack item = tag.read(
					"item",
					ItemStack.OPTIONAL_CODEC,
					registries.createSerializationContext(NbtOps.INSTANCE)
			).orElse(ItemStack.EMPTY).copyWithCount(1);
			List<SlotRecord> locations = new ArrayList<>();
			tag.getListOrEmpty("locations").forEach(element ->
					locations.add(SlotRecord.fromTag((CompoundTag) element)));
			return new EntryRecord(
					tag.getLongOr("template_serial", 0),
					item,
					tag.getLongOr("total_amount", 0),
					locations
			);
		}
	}

	public record SlotRecord(int storagePage, int slot, long templateSerial, long amount)
	{
		public static SlotRecord fromTag(CompoundTag tag)
		{
			return new SlotRecord(
					tag.getIntOr("storage_page", -1),
					tag.getIntOr("slot", -1),
					tag.getLongOr("template_serial", 0),
					tag.getLongOr("amount", 0)
			);
		}
	}

	public record SnapshotChunk(
			long sessionId,
			long snapshotId,
			int chunkIndex,
			long checksum,
			byte[] data
	)
	{
		public SnapshotChunk
		{
			data = data.clone();
		}

		public static SnapshotChunk fromTag(CompoundTag tag)
		{
			return new SnapshotChunk(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("snapshot_id", 0),
					tag.getIntOr("chunk_index", -1),
					tag.getLongOr("checksum", 0),
					tag.getByteArray("data").orElse(new byte[0])
			);
		}
	}

	public record SnapshotEnd(
			long sessionId,
			long snapshotId,
			long revision,
			int entryCount,
			int chunkCount
	)
	{
		public static SnapshotEnd fromTag(CompoundTag tag)
		{
			return new SnapshotEnd(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("snapshot_id", 0),
					tag.getLongOr("revision", -1),
					tag.getIntOr("entry_count", 0),
					tag.getIntOr("chunk_count", 0)
			);
		}
	}

	public record SnapshotAck(long sessionId, long snapshotId, List<Integer> missingChunks)
	{
		public SnapshotAck
		{
			missingChunks = List.copyOf(missingChunks);
		}

		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong("session_id", this.sessionId);
			tag.putLong("snapshot_id", this.snapshotId);
			tag.put("missing_chunks", new IntArrayTag(
					this.missingChunks.stream().mapToInt(Integer::intValue).toArray()
			));
			return tag;
		}

	}

	public record Delta(
			long sessionId,
			long baseRevision,
			long revision,
			List<EntryRecord> entryChanges,
			List<Long> removedTemplateSerials,
			List<PageInfo> pageChanges,
			List<Integer> pageAdded,
			boolean capacityChanged,
			long pageRevision,
			boolean readOnly
	)
	{
		public Delta
		{
			entryChanges = List.copyOf(entryChanges);
			removedTemplateSerials = List.copyOf(removedTemplateSerials);
			pageChanges = List.copyOf(pageChanges);
			pageAdded = List.copyOf(pageAdded);
		}

		public static Delta fromTag(CompoundTag tag, HolderLookup.Provider registries)
		{
			List<EntryRecord> entries = new ArrayList<>();
			tag.getListOrEmpty("entry_changes").forEach(element ->
					entries.add(EntryRecord.fromTag((CompoundTag) element, registries)));
			List<Long> removed = new ArrayList<>();
			tag.getListOrEmpty("removed_template_serials").forEach(element ->
					removed.add(((CompoundTag) element).getLongOr("template_serial", 0)));
			List<PageInfo> pages = new ArrayList<>();
			tag.getListOrEmpty("page_changes").forEach(element ->
					pages.add(PageInfo.fromTag((CompoundTag) element)));
			List<Integer> pageAdded = new ArrayList<>();
			tag.getIntArray("page_added").ifPresent(array ->
					Arrays.stream(array).forEach(pageAdded::add));
			return new Delta(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("base_revision", -1),
					tag.getLongOr("revision", -1),
					entries,
					removed,
					pages,
					pageAdded,
					tag.getBooleanOr("capacity_changed", false),
					tag.getLongOr("page_revision", -1),
					tag.getBooleanOr("read_only", false)
			);
		}
	}

	public record ActionRequest(
			long sessionId,
			long requestId,
			int containerId,
			long knownRevision,
			int storagePage,
			int slot,
			long expectedTemplateSerial,
			int action,
			byte button,
			long amount,
			long actionSequence,
			List<Integer> playerSlots
	)
	{
		public ActionRequest
		{
			playerSlots = playerSlots == null ? List.of() : List.copyOf(playerSlots);
		}

		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong("session_id", this.sessionId);
			tag.putLong("request_id", this.requestId);
			tag.putInt("container_id", this.containerId);
			tag.putLong("known_revision", this.knownRevision);
			tag.putInt("storage_page", this.storagePage);
			tag.putInt("slot", this.slot);
			tag.putLong("expected_template_serial", this.expectedTemplateSerial);
			tag.putInt("action", this.action);
			tag.putByte("button", this.button);
			tag.putLong("amount", this.amount);
			tag.putLong("action_sequence", this.actionSequence);
			tag.put("player_slots", new IntArrayTag(
					this.playerSlots.stream().mapToInt(Integer::intValue).toArray()
			));
			return tag;
		}

		public static ActionRequest fromTag(CompoundTag tag)
		{
			List<Integer> slots = new ArrayList<>();
			tag.getIntArray("player_slots").ifPresent(array ->
					Arrays.stream(array).forEach(slots::add));
			return new ActionRequest(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("request_id", 0),
					tag.getIntOr("container_id", -1),
					tag.getLongOr("known_revision", -1),
					tag.getIntOr("storage_page", -1),
					tag.getIntOr("slot", -1),
					tag.getLongOr("expected_template_serial", 0),
					tag.getIntOr("action", 0),
					tag.getByteOr("button", (byte)0),
					tag.getLongOr("amount", 0),
					tag.getLongOr("action_sequence", 0),
					slots
			);
		}
	}

	public record ActionResult(
			long sessionId,
			long requestId,
			int status,
			long revision
	)
	{
		public static ActionResult fromTag(CompoundTag tag)
		{
			return new ActionResult(
					tag.getLongOr("session_id", 0),
					tag.getLongOr("request_id", 0),
					tag.getIntOr("status", BigEnderChestProtocol.RESULT_INTERNAL_ERROR),
					tag.getLongOr("revision", -1)
			);
		}
	}

	public record SessionClose(long sessionId, int reason)
	{
		public static SessionClose fromTag(CompoundTag tag)
		{
			return new SessionClose(
					tag.getLongOr("session_id", 0),
					tag.getIntOr("reason", 0)
			);
		}
	}

	public record RequestResync(long sessionId)
	{
		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong("session_id", this.sessionId);
			return tag;
		}
	}

	public record OpenTerminalRequest(
			long requestId,
			long sessionId,
			int sourceAction
	)
	{
		public CompoundTag toTag()
		{
			CompoundTag tag = new CompoundTag();
			tag.putLong("request_id", this.requestId);
			tag.putLong("session_id", this.sessionId);
			tag.putInt("source_action", this.sourceAction);
			return tag;
		}
	}
}
