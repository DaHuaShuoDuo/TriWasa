package top.frostor.triwasa.bigenderchest;

import top.frostor.triwasa.bigenderchest.network.BigEnderChestSnapshotAssembler;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestSessionState;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;
import net.minecraft.nbt.CompoundTag;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;

public final class BigEnderChestSnapshotTest
{
	private static int checks;

	private BigEnderChestSnapshotTest()
	{
	}

	public static void run()
	{
		BigEnderChestSnapshotAssembler assembler = new BigEnderChestSnapshotAssembler();
		byte[] first = "first".getBytes(StandardCharsets.UTF_8);
		byte[] second = "second".getBytes(StandardCharsets.UTF_8);
		assembler.begin(11, 22, 7, 2, 2);
		check(
				assembler.acceptChunk(11, 22, 0, checksum(first), first)
						== BigEnderChestSnapshotAssembler.ChunkResult.ACCEPTED,
				"valid first snapshot chunk must be accepted"
		);
		check(
				assembler.acceptChunk(11, 22, 1, checksum(second) ^ 1, second)
						== BigEnderChestSnapshotAssembler.ChunkResult.CHECKSUM_MISMATCH,
				"bad CRC must be rejected"
		);
		BigEnderChestSnapshotAssembler.Completion incomplete =
				assembler.complete(11, 22, 7, 2, 2);
		check(!incomplete.complete(), "incomplete snapshot must not commit");
		check(
				incomplete.missingChunks().equals(List.of(1)),
				"missing chunk index must be returned to the server"
		);
		check(
				assembler.acceptChunk(11, 22, 1, checksum(second), second)
						== BigEnderChestSnapshotAssembler.ChunkResult.ACCEPTED,
				"retransmitted chunk must repair the snapshot"
		);
		BigEnderChestSnapshotAssembler.Completion complete =
				assembler.complete(11, 22, 7, 2, 2);
		check(complete.complete(), "fully verified snapshot must complete");
		check(
				complete.chunks().size() == 2
						&& java.util.Arrays.equals(complete.chunks().get(0), first)
						&& java.util.Arrays.equals(complete.chunks().get(1), second),
				"snapshot chunks must retain server order"
		);
		check(
				!assembler.complete(99, 22, 7, 2, 2).complete(),
				"a snapshot from another session must be ignored"
		);
		BigEnderChestMessages.SnapshotAck ack =
				new BigEnderChestMessages.SnapshotAck(11, 22, List.of(0, 3));
		CompoundTag ackTag = ack.toTag();
		check(
				ackTag.getLongOr("session_id", 0) == 11
						&& ackTag.getLongOr("snapshot_id", 0) == 22
						&& java.util.Arrays.equals(
								ackTag.getIntArray("missing_chunks").orElseThrow(),
								new int[]{0, 3}
						),
				"SNAPSHOT_ACK must preserve session, snapshot, and missing chunk ids"
		);

		BigEnderChestSessionState state = activeSession();
		state.beginSnapshot(new BigEnderChestMessages.SnapshotBegin(
				55,
				66,
				9,
				1,
				0,
				1
		));
		check(
				state.revision() == 9
						&& state.entriesBySerial().isEmpty()
						&& state.state()
						== BigEnderChestSessionState.State.SYNCING,
				"an incomplete snapshot must not replace the active index or revision"
		);
		BigEnderChestSessionState.DeltaApplyResult applied = state.applyDelta(
				new BigEnderChestMessages.Delta(
						55,
						9,
						10,
						List.of(),
						List.of(),
						List.of(new BigEnderChestMessages.PageInfo(
								1,
								"READY",
								true,
								0,
								2
						)),
						List.of(1),
						true,
						2,
						false
				)
		);
		check(
				applied == BigEnderChestSessionState.DeltaApplyResult.APPLIED
						&& state.revision() == 10,
				"a contiguous DELTA must update revision and page capacity"
		);
		BigEnderChestSessionState.DeltaApplyResult stale = state.applyDelta(
				new BigEnderChestMessages.Delta(
						55,
						9,
						11,
						List.of(),
						List.of(),
						List.of(),
						List.of(),
						false,
						3,
						false
				)
		);
		check(
				stale == BigEnderChestSessionState.DeltaApplyResult.RESYNC_REQUIRED
						&& state.state() == BigEnderChestSessionState.State.RESYNCING,
				"a non-contiguous DELTA must require resync"
		);
		System.out.println("BigEnderChestSnapshotTest passed " + checks + " checks");
	}

	private static BigEnderChestSessionState activeSession()
	{
		BigEnderChestSessionState state = new BigEnderChestSessionState();
		state.helloPending();
		state.acceptHello(new BigEnderChestMessages.HelloAck(
				BigEnderChestProtocol.VERSION,
				BigEnderChestCapabilities.REQUIRED,
				BigEnderChestProtocol.MAX_CHUNK_BYTES,
				true
		));
		state.openSession(new BigEnderChestMessages.SessionOpen(
				55,
				8,
				9,
				-1,
				1,
				1,
				false,
				BigEnderChestCapabilities.REQUIRED,
				8,
				0
		));
		if(!state.installSnapshot(55, 9, List.of()))
		{
			throw new AssertionError("failed to initialize session state");
		}
		return state;
	}

	private static long checksum(byte[] data)
	{
		CRC32 crc = new CRC32();
		crc.update(data);
		return crc.getValue();
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
