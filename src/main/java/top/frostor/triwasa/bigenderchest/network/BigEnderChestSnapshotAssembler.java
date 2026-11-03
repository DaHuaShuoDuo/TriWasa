package top.frostor.triwasa.bigenderchest.network;

import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Collects an entire snapshot before it can replace the active index.
 */
public final class BigEnderChestSnapshotAssembler
{
	private PendingSnapshot pending;

	public void begin(
			long sessionId,
			long snapshotId,
			long revision,
			int entryCount,
			int chunkCount
	)
	{
		if(sessionId == 0
				|| snapshotId == 0
				|| revision < 0
				|| entryCount < 0
				|| chunkCount < 0)
		{
			throw new IllegalArgumentException("Invalid snapshot header");
		}
		this.pending = new PendingSnapshot(
				sessionId,
				snapshotId,
				revision,
				entryCount,
				chunkCount
		);
	}

	public ChunkResult acceptChunk(
			long sessionId,
			long snapshotId,
			int chunkIndex,
			long checksum,
			byte[] data
	)
	{
		if(this.pending == null
				|| this.pending.sessionId != sessionId
				|| this.pending.snapshotId != snapshotId)
		{
			return ChunkResult.WRONG_SNAPSHOT;
		}
		if(chunkIndex < 0
				|| chunkIndex >= this.pending.chunkCount
				|| data == null
				|| data.length > BigEnderChestProtocol.MAX_CHUNK_BYTES)
		{
			return ChunkResult.INVALID;
		}

		CRC32 crc = new CRC32();
		crc.update(data);
		if(crc.getValue() != checksum)
		{
			this.pending.chunks.remove(chunkIndex);
			this.pending.invalidChunks.add(chunkIndex);
			return ChunkResult.CHECKSUM_MISMATCH;
		}
			this.pending.invalidChunks.remove(Integer.valueOf(chunkIndex));
		this.pending.chunks.put(chunkIndex, data.clone());
		return ChunkResult.ACCEPTED;
	}

	public Completion complete(
			long sessionId,
			long snapshotId,
			long revision,
			int entryCount,
			int chunkCount
	)
	{
		if(this.pending == null
				|| this.pending.sessionId != sessionId
				|| this.pending.snapshotId != snapshotId)
		{
			return new Completion(false, revision, entryCount, List.of(), List.of(-1));
		}
		if(this.pending.revision != revision
				|| this.pending.entryCount != entryCount
				|| this.pending.chunkCount != chunkCount)
		{
			return new Completion(false, revision, entryCount, List.of(), this.missingChunks());
		}

		List<Integer> missing = this.missingChunks();
		if(!missing.isEmpty())
		{
			return new Completion(false, revision, entryCount, List.of(), missing);
		}
		List<byte[]> chunks = new ArrayList<>(chunkCount);
		for(int index = 0; index < chunkCount; index++)
		{
			chunks.add(this.pending.chunks.get(index).clone());
		}
		return new Completion(true, revision, entryCount, List.copyOf(chunks), List.of());
	}

	public List<Integer> missingChunks()
	{
		if(this.pending == null)
		{
			return List.of();
		}
		List<Integer> missing = new ArrayList<>(this.pending.invalidChunks);
		for(int index = 0; index < this.pending.chunkCount; index++)
		{
			if(!this.pending.chunks.containsKey(index))
			{
				missing.add(index);
			}
		}
		return missing.stream().distinct().sorted().toList();
	}

	public boolean isActive(long sessionId, long snapshotId)
	{
		return this.pending != null
				&& this.pending.sessionId == sessionId
				&& this.pending.snapshotId == snapshotId;
	}

	public void clear()
	{
		this.pending = null;
	}

	public enum ChunkResult
	{
		ACCEPTED,
		CHECKSUM_MISMATCH,
		WRONG_SNAPSHOT,
		INVALID
	}

	public record Completion(
			boolean complete,
			long revision,
			int entryCount,
			List<byte[]> chunks,
			List<Integer> missingChunks
	)
	{
		public Completion
		{
			chunks = List.copyOf(chunks);
			missingChunks = List.copyOf(missingChunks);
		}
	}

	private static final class PendingSnapshot
	{
		private final long sessionId;
		private final long snapshotId;
		private final long revision;
		private final int entryCount;
		private final int chunkCount;
		private final Map<Integer, byte[]> chunks = new HashMap<>();
		private final List<Integer> invalidChunks = new ArrayList<>();

		private PendingSnapshot(
				long sessionId,
				long snapshotId,
				long revision,
				int entryCount,
				int chunkCount
		)
		{
			this.sessionId = sessionId;
			this.snapshotId = snapshotId;
			this.revision = revision;
			this.entryCount = entryCount;
			this.chunkCount = chunkCount;
		}
	}
}
