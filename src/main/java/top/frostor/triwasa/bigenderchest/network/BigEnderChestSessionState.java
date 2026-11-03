package top.frostor.triwasa.bigenderchest.network;

import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full client-side replica state. Item amounts remain advisory until FCA confirms actions.
 */
public final class BigEnderChestSessionState
{
	private final BigEnderChestSnapshotAssembler snapshotAssembler =
			new BigEnderChestSnapshotAssembler();
	private State state = State.DISCONNECTED;
	private int negotiatedCapabilities;
	private boolean helloAccepted;
	private long sessionId;
	private int containerId = -1;
	private long revision = -1;
	private long pageRevision = -1;
	private int pageLimit;
	private int pageCount;
	private int totalSlots;
	private int occupiedSlots;
	private boolean readOnly;
	private Map<Long, BigEnderChestMessages.EntryRecord> entriesBySerial = Map.of();
	private List<BigEnderChestMessages.PageInfo> pages = List.of();
	private long nextRequestId = 1;
	private long nextActionSequence = 1;
	private String errorKey;

	public void reset()
	{
		this.state = State.DISCONNECTED;
		this.negotiatedCapabilities = 0;
		this.helloAccepted = false;
		this.sessionId = 0;
		this.containerId = -1;
		this.revision = -1;
		this.pageRevision = -1;
		this.pageLimit = 0;
		this.pageCount = 0;
		this.totalSlots = 0;
		this.occupiedSlots = 0;
		this.readOnly = false;
		this.entriesBySerial = Map.of();
		this.pages = List.of();
		this.nextRequestId = 1;
		this.nextActionSequence = 1;
		this.errorKey = null;
		this.snapshotAssembler.clear();
	}

	public void helloPending()
	{
		reset();
		this.state = State.HELLO_PENDING;
	}

	public boolean acceptHello(BigEnderChestMessages.HelloAck ack)
	{
		this.negotiatedCapabilities = ack.capabilities();
		this.helloAccepted = ack.accepted()
				&& ack.protocolVersion() == BigEnderChestProtocol.VERSION
				&& ack.chunkByteLimit() > 0
				&& BigEnderChestCapabilities.hasAll(
						ack.capabilities(),
						BigEnderChestCapabilities.REQUIRED
				);
		if(this.helloAccepted)
		{
			this.state = State.PROTOCOL_READY;
			this.errorKey = null;
		}
		else
		{
			this.state = State.PROTOCOL_READY;
			this.errorKey = ack.accepted()
					? "triwasa.big_ender_chest.error.capability_missing"
					: "triwasa.big_ender_chest.error.hello_rejected";
		}
		return this.helloAccepted;
	}

	public void openSession(BigEnderChestMessages.SessionOpen open)
	{
		if(!this.helloAccepted)
		{
			return;
		}
		if(open.sessionId() == 0
				|| open.containerId() < 0
				|| open.revision() < 0)
		{
			this.errorKey = "triwasa.big_ender_chest.error.session_invalid";
			return;
		}
		this.sessionId = open.sessionId();
		this.containerId = open.containerId();
		this.revision = open.revision();
		this.pageLimit = open.pageLimit();
		this.pageCount = open.pageCount();
		this.pageRevision = open.pageRevision();
		this.readOnly = open.readOnly();
		this.pages = List.of();
		this.totalSlots = 0;
		this.occupiedSlots = 0;
		this.nextRequestId = 1;
		this.nextActionSequence = 1;
		this.state = State.SESSION_PENDING;
		this.errorKey = null;
	}

	public void applyPageCatalog(BigEnderChestMessages.PageCatalog catalog)
	{
		if(!isCurrentSession(catalog.sessionId()))
		{
			return;
		}
		this.pageRevision = catalog.pageRevision();
		this.totalSlots = Math.max(0, catalog.totalSlots());
		this.occupiedSlots = Math.max(0, catalog.occupiedSlots());
		this.readOnly = catalog.readOnly();
		this.pages = List.copyOf(catalog.pages());
		this.pageCount = catalog.pages().size();
	}

	public void beginSnapshot(BigEnderChestMessages.SnapshotBegin begin)
	{
		if(!isCurrentSession(begin.sessionId()))
		{
			return;
		}
		this.snapshotAssembler.begin(
				begin.sessionId(),
				begin.snapshotId(),
				begin.revision(),
				begin.entryCount(),
				begin.chunkCount()
		);
		this.state = State.SYNCING;
	}

	public BigEnderChestSnapshotAssembler.ChunkResult acceptSnapshotChunk(
			BigEnderChestMessages.SnapshotChunk chunk
	)
	{
		return this.snapshotAssembler.acceptChunk(
				chunk.sessionId(),
				chunk.snapshotId(),
				chunk.chunkIndex(),
				chunk.checksum(),
				chunk.data()
		);
	}

	public BigEnderChestSnapshotAssembler.Completion completeSnapshot(
			BigEnderChestMessages.SnapshotEnd end
	)
	{
		return this.snapshotAssembler.complete(
				end.sessionId(),
				end.snapshotId(),
				end.revision(),
				end.entryCount(),
				end.chunkCount()
		);
	}

	public boolean installSnapshot(
			long sessionId,
			long revision,
			List<BigEnderChestMessages.EntryRecord> records
	)
	{
		if(sessionId != this.sessionId || revision < 0)
		{
			return false;
		}
		Map<Long, BigEnderChestMessages.EntryRecord> replacement = new LinkedHashMap<>();
		for(BigEnderChestMessages.EntryRecord record : records)
		{
			if(record.templateSerial() <= 0
					|| record.item().isEmpty()
					|| record.totalAmount() <= 0)
			{
				return false;
			}
			replacement.put(record.templateSerial(), record);
		}
		this.entriesBySerial = Map.copyOf(replacement);
		this.revision = revision;
		this.state = State.READY;
		this.errorKey = null;
		this.snapshotAssembler.clear();
		return true;
	}

	public DeltaApplyResult applyDelta(BigEnderChestMessages.Delta delta)
	{
		if(!isCurrentSession(delta.sessionId()))
		{
			return DeltaApplyResult.IGNORED;
		}
		if(delta.baseRevision() != this.revision)
		{
			this.state = State.RESYNCING;
			return DeltaApplyResult.RESYNC_REQUIRED;
		}

		Map<Long, BigEnderChestMessages.EntryRecord> replacement =
				new LinkedHashMap<>(this.entriesBySerial);
		for(BigEnderChestMessages.EntryRecord record : delta.entryChanges())
		{
			if(record.templateSerial() <= 0
					|| record.item().isEmpty()
					|| record.totalAmount() < 0)
			{
				this.state = State.RESYNCING;
				return DeltaApplyResult.RESYNC_REQUIRED;
			}
			if(record.totalAmount() == 0)
			{
				replacement.remove(record.templateSerial());
			}
			else
			{
				replacement.put(record.templateSerial(), record);
			}
		}
		for(long serial : delta.removedTemplateSerials())
		{
			replacement.remove(serial);
		}

		Map<Integer, BigEnderChestMessages.PageInfo> pageMap = new LinkedHashMap<>();
		this.pages.forEach(page -> pageMap.put(page.pageId(), page));
		for(BigEnderChestMessages.PageInfo change : delta.pageChanges())
		{
			pageMap.put(change.pageId(), change);
		}
		for(int pageId : delta.pageAdded())
		{
			pageMap.putIfAbsent(
					pageId,
					new BigEnderChestMessages.PageInfo(pageId, "READY", true, 0, 0)
			);
		}

		this.entriesBySerial = Map.copyOf(replacement);
		this.pages = List.copyOf(pageMap.values());
		this.pageCount = this.pages.size();
		this.pageRevision = delta.pageRevision();
		this.readOnly = delta.readOnly();
		this.occupiedSlots = this.pages.stream()
				.mapToInt(BigEnderChestMessages.PageInfo::occupiedSlots)
				.sum();
		if(delta.capacityChanged() || this.totalSlots <= 0)
		{
			this.totalSlots = this.pageCount * 27;
		}
		this.revision = delta.revision();
		this.state = State.READY;
		return DeltaApplyResult.APPLIED;
	}

	public boolean isCurrentSession(long candidateSessionId)
	{
		return this.sessionId != 0 && this.sessionId == candidateSessionId;
	}

	public boolean canUseTerminal()
	{
		return this.helloAccepted
				&& BigEnderChestCapabilities.hasAll(
						this.negotiatedCapabilities,
						BigEnderChestCapabilities.REQUIRED
				);
	}

	public long allocateRequestId()
	{
		return this.nextRequestId++;
	}

	public long allocateActionSequence()
	{
		return this.nextActionSequence++;
	}

	public State state()
	{
		return this.state;
	}

	public void state(State state)
	{
		this.state = state;
	}

	public boolean helloAccepted()
	{
		return this.helloAccepted;
	}

	public int negotiatedCapabilities()
	{
		return this.negotiatedCapabilities;
	}

	public long sessionId()
	{
		return this.sessionId;
	}

	public int containerId()
	{
		return this.containerId;
	}

	public long revision()
	{
		return this.revision;
	}

	public long pageRevision()
	{
		return this.pageRevision;
	}

	public int pageLimit()
	{
		return this.pageLimit;
	}

	public int pageCount()
	{
		return this.pageCount;
	}

	public int totalSlots()
	{
		return this.totalSlots;
	}

	public int occupiedSlots()
	{
		return this.occupiedSlots;
	}

	public boolean readOnly()
	{
		return this.readOnly;
	}

	public Map<Long, BigEnderChestMessages.EntryRecord> entriesBySerial()
	{
		return this.entriesBySerial;
	}

	public List<BigEnderChestMessages.PageInfo> pages()
	{
		return this.pages;
	}

	public BigEnderChestSnapshotAssembler snapshotAssembler()
	{
		return this.snapshotAssembler;
	}

	public String errorKey()
	{
		return this.errorKey;
	}

	public void errorKey(String errorKey)
	{
		this.errorKey = errorKey;
	}

	public void clearSession()
	{
		this.sessionId = 0;
		this.containerId = -1;
		this.revision = -1;
		this.pageRevision = -1;
		this.pageLimit = 0;
		this.pageCount = 0;
		this.totalSlots = 0;
		this.occupiedSlots = 0;
		this.readOnly = false;
		this.entriesBySerial = Map.of();
		this.pages = List.of();
		this.nextRequestId = 1;
		this.nextActionSequence = 1;
		this.errorKey = null;
		this.snapshotAssembler.clear();
		this.state = State.PROTOCOL_READY;
	}

	public enum State
	{
		DISCONNECTED,
		HELLO_PENDING,
		PROTOCOL_READY,
		SESSION_PENDING,
		SYNCING,
		READY,
		RESYNCING
	}

	public enum DeltaApplyResult
	{
		APPLIED,
		RESYNC_REQUIRED,
		IGNORED
	}
}
