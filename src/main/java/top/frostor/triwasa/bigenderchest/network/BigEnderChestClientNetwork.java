package top.frostor.triwasa.bigenderchest.network;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FCA protocol v2 client. Standalone clients use TriWasa's wire-compatible payload;
 * integrated clients reuse FCA's server-owned payload through its public API.
 */
public final class BigEnderChestClientNetwork
{
	private static final String FCA_MOD_ID = "frostors-carpet-addition";
	private static final int RESYNC_COOLDOWN_TICKS = 20;
	private static final BigEnderChestSessionState SESSION = new BigEnderChestSessionState();
	private static final BigEnderChestActionDispatcher ACTIONS =
			new BigEnderChestActionDispatcher(
					SESSION,
					request -> send(
							BigEnderChestProtocol.ACTION_REQUEST,
							request.toTag()
					),
					BigEnderChestClientNetwork::showResult,
					BigEnderChestClientNetwork::resyncAfterActionFailure
			);
	private static boolean registered;
	private static boolean helloPending;
	private static boolean wasEnabled;
	private static long lastResyncRequestTick = Long.MIN_VALUE;
	private static long clientTick;
	private static String pendingResyncReason;
	private static boolean terminalDowngradeSent;
	private static boolean helloBlockedLogged;
	private static boolean downgradeAckExpected;

	private BigEnderChestClientNetwork()
	{
	}

	public static void register()
	{
		if(registered)
		{
			return;
		}
		registered = true;
		@SuppressWarnings("unchecked")
		CustomPacketPayload.Type<CustomPacketPayload> payloadType =
				(CustomPacketPayload.Type<CustomPacketPayload>)BigEnderChestPayloadBridge.type();
		if(!FabricLoader.getInstance().isModLoaded(FCA_MOD_ID))
		{
			registerPayloadType(payloadType);
		}
		ClientPlayNetworking.registerGlobalReceiver(
				payloadType,
				(payload, context) -> context.client().execute(() ->
						handlePayload(context.client(), payload))
		);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			reset();
			helloPending = TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue();
			helloBlockedLogged = false;
			trySendHello(client);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
		ClientTickEvents.END_CLIENT_TICK.register(BigEnderChestClientNetwork::tick);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerPayloadType(
			CustomPacketPayload.Type<CustomPacketPayload> payloadType
	)
	{
		PayloadTypeRegistry.serverboundPlay().register(
				(CustomPacketPayload.Type)payloadType,
				(net.minecraft.network.codec.StreamCodec)BigEnderChestPayloadBridge.codec()
		);
		PayloadTypeRegistry.clientboundPlay().register(
				(CustomPacketPayload.Type)payloadType,
				(net.minecraft.network.codec.StreamCodec)BigEnderChestPayloadBridge.codec()
		);
	}

	public static BigEnderChestSessionState state()
	{
		return SESSION;
	}

	public static BigEnderChestActionDispatcher actions()
	{
		return ACTIONS;
	}

	public static boolean ready()
	{
		return SESSION.canUseTerminal()
				&& SESSION.state() == BigEnderChestSessionState.State.READY;
	}

	public static void onConfigChanged(boolean enabled)
	{
		ACTIONS.clear();
		Minecraft client = Minecraft.getInstance();
		if(!enabled)
		{
			reset();
			if(client.player != null && client.getConnection() != null)
			{
				downgradeAckExpected = sendHello(
						client,
						BigEnderChestProtocol.CAPABILITIES
								& ~BigEnderChestProtocol.CAPABILITY_TERMINAL_MENU
				);
			}
			return;
		}
		downgradeAckExpected = false;
		if(client.player != null && client.getConnection() != null)
		{
			SESSION.helloPending();
			helloPending = true;
			trySendHello(client);
		}
	}

	public static boolean requestResync(String reasonKey)
	{
		if(SESSION.sessionId() == 0
				|| (lastResyncRequestTick != Long.MIN_VALUE
				&& clientTick - lastResyncRequestTick < RESYNC_COOLDOWN_TICKS))
		{
			if(SESSION.sessionId() != 0)
			{
				pendingResyncReason = reasonKey;
			}
			return false;
		}
		pendingResyncReason = null;
		lastResyncRequestTick = clientTick;
		SESSION.state(BigEnderChestSessionState.State.RESYNCING);
		send(
				BigEnderChestProtocol.REQUEST_RESYNC,
				new BigEnderChestMessages.RequestResync(SESSION.sessionId()).toTag()
		);
		if(reasonKey != null)
		{
			showMessage(reasonKey);
		}
		return true;
	}

	public static boolean resyncCoolingDown()
	{
		return lastResyncRequestTick != Long.MIN_VALUE
				&& clientTick - lastResyncRequestTick < RESYNC_COOLDOWN_TICKS;
	}

	private static void reset()
	{
		SESSION.reset();
		ACTIONS.clear();
		helloPending = false;
		wasEnabled = TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue();
		lastResyncRequestTick = Long.MIN_VALUE;
		pendingResyncReason = null;
		terminalDowngradeSent = false;
		helloBlockedLogged = false;
		downgradeAckExpected = false;
	}

	private static void tick(Minecraft client)
	{
		clientTick++;
		boolean enabled = TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue();
		if(enabled && !wasEnabled && client.player != null)
		{
			SESSION.helloPending();
			helloPending = true;
		}
		wasEnabled = enabled;
		if(enabled)
		{
			trySendHello(client);
			ACTIONS.tick();
			if(pendingResyncReason != null && !resyncCoolingDown())
			{
				requestResync(pendingResyncReason);
			}
		}
		else
		{
			ACTIONS.clear();
		}
	}

	private static void trySendHello(Minecraft client)
	{
		if(!helloPending
				|| client.player == null
				|| client.getConnection() == null
				|| !TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue())
		{
			return;
		}
		if(!ClientPlayNetworking.canSend(BigEnderChestPayloadBridge.type()))
		{
			if(!helloBlockedLogged)
			{
				helloBlockedLogged = true;
				TriWasa.LOGGER.warn(
						"[BigEnderChest] HELLO blocked: server does not advertise {}",
						BigEnderChestProtocol.CHANNEL
				);
			}
			return;
		}
		sendHello(client, BigEnderChestProtocol.CAPABILITIES);
		helloPending = false;
		TriWasa.LOGGER.debug("[BigEnderChest] Sent protocol v2 HELLO");
	}

	private static boolean sendHello(Minecraft client, int capabilities)
	{
		if(client.player == null
				|| client.getConnection() == null
				|| !ClientPlayNetworking.canSend(BigEnderChestPayloadBridge.type()))
		{
			return false;
		}
		send(
				BigEnderChestProtocol.HELLO,
				new BigEnderChestMessages.Hello(
						BigEnderChestProtocol.VERSION,
						capabilities,
						BigEnderChestProtocol.MAX_CHUNK_BYTES
				).toTag()
		);
		return true;
	}

	private static void handlePayload(
			Minecraft client,
			CustomPacketPayload payload
	)
	{
		int messageType = BigEnderChestPayloadBridge.messageType(payload);
		CompoundTag data = BigEnderChestPayloadBridge.data(payload);
		try
		{
			switch(messageType)
			{
				case BigEnderChestProtocol.HELLO_ACK -> handleHelloAck(
						client,
						BigEnderChestMessages.HelloAck.fromTag(data)
				);
				case BigEnderChestProtocol.SESSION_OPEN -> handleSessionOpen(
						BigEnderChestMessages.SessionOpen.fromTag(data)
				);
				case BigEnderChestProtocol.PAGE_CATALOG -> SESSION.applyPageCatalog(
						BigEnderChestMessages.PageCatalog.fromTag(data)
				);
				case BigEnderChestProtocol.SNAPSHOT_BEGIN -> SESSION.beginSnapshot(
						BigEnderChestMessages.SnapshotBegin.fromTag(data)
				);
				case BigEnderChestProtocol.SNAPSHOT_CHUNK -> handleSnapshotChunk(
						BigEnderChestMessages.SnapshotChunk.fromTag(data)
				);
				case BigEnderChestProtocol.SNAPSHOT_END -> handleSnapshotEnd(
						client,
						BigEnderChestMessages.SnapshotEnd.fromTag(data)
				);
				case BigEnderChestProtocol.DELTA -> handleDelta(
						BigEnderChestMessages.Delta.fromTag(
								data,
								requireRegistries(client)
						)
				);
				case BigEnderChestProtocol.ACTION_RESULT -> ACTIONS.onResult(
						BigEnderChestMessages.ActionResult.fromTag(data)
				);
				case BigEnderChestProtocol.SESSION_CLOSE -> handleSessionClose(
						BigEnderChestMessages.SessionClose.fromTag(data)
				);
				default -> {
				}
			}
		}
		catch(RuntimeException exception)
		{
			TriWasa.LOGGER.error(
					"Malformed big ender chest message type {}",
					messageType,
					exception
			);
			SESSION.errorKey("triwasa.big_ender_chest.error.malformed");
			showMessage(SESSION.errorKey());
			if(messageType == BigEnderChestProtocol.DELTA)
			{
				requestResync("triwasa.big_ender_chest.error.revision_stale");
			}
		}
	}

	private static void handleHelloAck(
			Minecraft client,
			BigEnderChestMessages.HelloAck ack
	)
	{
		if(!TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_ENABLED.getBooleanValue())
		{
			downgradeAckExpected = false;
			SESSION.reset();
			return;
		}
		if(downgradeAckExpected)
		{
			downgradeAckExpected = false;
			if(!BigEnderChestCapabilities.hasAll(
					ack.capabilities(),
					BigEnderChestCapabilities.REQUIRED
			))
			{
				SESSION.reset();
				return;
			}
		}
		boolean accepted = SESSION.acceptHello(ack);
		if(!accepted
				&& ack.accepted()
				&& ack.protocolVersion() == BigEnderChestProtocol.VERSION
				&& !terminalDowngradeSent
				&& (ack.capabilities()
				& (BigEnderChestCapabilities.SNAPSHOT
				| BigEnderChestCapabilities.ACTIONS))
				== (BigEnderChestCapabilities.SNAPSHOT
				| BigEnderChestCapabilities.ACTIONS))
		{
			terminalDowngradeSent = true;
			send(
					BigEnderChestProtocol.HELLO,
					new BigEnderChestMessages.Hello(
							BigEnderChestProtocol.VERSION,
							ack.capabilities()
									& ~BigEnderChestCapabilities.TERMINAL_MENU,
							BigEnderChestProtocol.MAX_CHUNK_BYTES
					).toTag()
			);
			if(client.player != null
					&& BigEnderChestProtocol.TERMINAL_MENU_ID.equals(
							BuiltInRegistries.MENU.getKey(
									client.player.containerMenu.getType()
							)
					))
			{
				client.player.closeContainer();
			}
		}
		if(!accepted && SESSION.errorKey() != null)
		{
			showMessage(SESSION.errorKey());
		}
	}

	private static void handleSessionOpen(BigEnderChestMessages.SessionOpen open)
	{
		SESSION.openSession(open);
		ACTIONS.clear();
		pendingResyncReason = null;
		lastResyncRequestTick = Long.MIN_VALUE;
		if(open.sessionId() == SESSION.sessionId()
				&& open.terminalMenuId() < 0
				&& BigEnderChestCapabilities.hasAll(
						SESSION.negotiatedCapabilities(),
						BigEnderChestCapabilities.TERMINAL_MENU
				))
		{
			send(
					BigEnderChestProtocol.OPEN_TERMINAL_REQUEST,
					new BigEnderChestMessages.OpenTerminalRequest(
							SESSION.allocateRequestId(),
							SESSION.sessionId(),
							0
					).toTag()
			);
		}
	}

	private static void handleSnapshotChunk(BigEnderChestMessages.SnapshotChunk chunk)
	{
		BigEnderChestSnapshotAssembler.ChunkResult result =
				SESSION.acceptSnapshotChunk(chunk);
		if(result == BigEnderChestSnapshotAssembler.ChunkResult.CHECKSUM_MISMATCH)
		{
			SESSION.errorKey("triwasa.big_ender_chest.error.snapshot_checksum");
		}
		else if(result == BigEnderChestSnapshotAssembler.ChunkResult.INVALID)
		{
			SESSION.errorKey("triwasa.big_ender_chest.error.snapshot_chunk");
		}
	}

	private static void handleSnapshotEnd(
			Minecraft client,
			BigEnderChestMessages.SnapshotEnd end
	)
	{
		BigEnderChestSnapshotAssembler.Completion completion =
				SESSION.completeSnapshot(end);
		if(!completion.complete())
		{
			if(!completion.missingChunks().isEmpty())
			{
				SESSION.errorKey("triwasa.big_ender_chest.error.snapshot_chunk");
			}
			sendSnapshotAck(end.sessionId(), end.snapshotId(), completion.missingChunks());
			return;
		}

		try
		{
			List<BigEnderChestMessages.EntryRecord> records = decodeSnapshot(
					completion.chunks(),
					requireRegistries(client)
			);
			if(records.size() != completion.entryCount()
					|| !SESSION.installSnapshot(
							end.sessionId(),
							completion.revision(),
							records
					))
			{
				throw new IllegalArgumentException("Snapshot entry count mismatch");
			}
			sendSnapshotAck(end.sessionId(), end.snapshotId(), List.of());
		}
		catch(RuntimeException | IOException exception)
		{
			TriWasa.LOGGER.error("Failed to decode big ender chest snapshot", exception);
			SESSION.errorKey("triwasa.big_ender_chest.error.snapshot_decode");
			requestResync("triwasa.big_ender_chest.error.snapshot_decode");
		}
	}

	private static List<BigEnderChestMessages.EntryRecord> decodeSnapshot(
			List<byte[]> chunks,
			HolderLookup.Provider registries
	) throws IOException
	{
		List<BigEnderChestMessages.EntryRecord> result = new ArrayList<>();
		Set<Long> serials = new HashSet<>();
		for(byte[] data : chunks)
		{
			CompoundTag root;
			try(DataInputStream input = new DataInputStream(new ByteArrayInputStream(data)))
			{
				root = NbtIo.read(
						input,
						NbtAccounter.create(BigEnderChestProtocol.MAX_CHUNK_BYTES * 2L)
				);
			}
			ListTag entries = root.getListOrEmpty("entries");
			for(int index = 0; index < entries.size(); index++)
			{
				BigEnderChestMessages.EntryRecord entry =
						BigEnderChestMessages.EntryRecord.fromTag(
								(CompoundTag)entries.get(index),
								registries
						);
				if(entry.templateSerial() <= 0
						|| entry.item().isEmpty()
						|| entry.totalAmount() <= 0
						|| !serials.add(entry.templateSerial()))
				{
					throw new IllegalArgumentException("Invalid snapshot entry");
				}
				result.add(entry);
			}
		}
		return result;
	}

	private static void handleDelta(BigEnderChestMessages.Delta delta)
	{
		BigEnderChestSessionState.DeltaApplyResult result = SESSION.applyDelta(delta);
		if(result == BigEnderChestSessionState.DeltaApplyResult.RESYNC_REQUIRED)
		{
			requestResync("triwasa.big_ender_chest.error.revision_stale");
		}
	}

	private static void handleSessionClose(BigEnderChestMessages.SessionClose close)
	{
		if(SESSION.isCurrentSession(close.sessionId()))
		{
			ACTIONS.clear();
			SESSION.clearSession();
			pendingResyncReason = null;
			showMessage("triwasa.big_ender_chest.error.session_closed");
		}
	}

	private static void sendSnapshotAck(
			long sessionId,
			long snapshotId,
			List<Integer> missingChunks
	)
	{
		send(
				BigEnderChestProtocol.SNAPSHOT_ACK,
				new BigEnderChestMessages.SnapshotAck(
						sessionId,
						snapshotId,
						missingChunks
				).toTag()
		);
	}

	private static void send(int messageType, CompoundTag data)
	{
		ClientPlayNetworking.send(BigEnderChestPayloadBridge.create(messageType, data));
	}

	private static HolderLookup.Provider requireRegistries(Minecraft client)
	{
		if(client.level == null)
		{
			throw new IllegalStateException("No client level registry access");
		}
		return client.level.registryAccess();
	}

	private static void showResult(int status)
	{
		if(status != BigEnderChestProtocol.RESULT_OK)
		{
			TriWasa.LOGGER.info("[BigEnderChest] Action result status={}", status);
		}
		if(status == BigEnderChestProtocol.RESULT_OK)
		{
			return;
		}
		showMessage(BigEnderChestProtocol.resultTranslationKey(status));
	}

	private static void resyncAfterActionFailure(String reasonKey)
	{
		requestResync(reasonKey);
	}

	private static void showMessage(String translationKey)
	{
		Minecraft client = Minecraft.getInstance();
		if(client.player != null && translationKey != null)
		{
			client.player.sendOverlayMessage(Component.translatable(translationKey));
		}
	}
}
