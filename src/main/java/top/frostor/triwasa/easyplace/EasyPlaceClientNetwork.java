package top.frostor.triwasa.easyplace;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceClientNetwork
{
	private static final String FCA_MOD_ID = "frostors-carpet-addition";
	private static final int PROTOCOL_VERSION = 1;
	private static final String NBT_PROTOCOL_VERSION = "protocol_version";
	private static final String NBT_CAPABILITIES = "capabilities";
	private static final String NBT_Y_PRECISION_BITS = "y_precision_bits";
	private static final String NBT_PISTON_PROTECTION_BITS = "piston_protection_bits";
	private static final String CAPABILITY_EASY_PLACE_V2_EXTRA = "easy_place_v2_extra";
	private static final String CAPABILITY_PACKET_ANCHOR = "easy_place_packet_anchor";
	private static final String CAPABILITY_RELATIVE_AUX = "easy_place_relative_aux";
	private static final String CAPABILITY_Y_PRECISION = "easy_place_y_precision";
	private static final String CAPABILITY_PISTON_PROTECTION = "easy_place_piston_protection";
	private static boolean serverSupports;
	private static boolean serverSupportsYPrecision;
	private static boolean serverSupportsPistonProtection;
	private static boolean serverSupportsRelativeAux;
	private static boolean helloPending;
	private static boolean wasEnabled;

	private EasyPlaceClientNetwork()
	{
	}

	public static void register()
	{
		@SuppressWarnings("unchecked")
		CustomPacketPayload.Type<CustomPacketPayload> payloadType =
				(CustomPacketPayload.Type<CustomPacketPayload>)EasyPlacePayloadBridge.type();
		if(!FabricLoader.getInstance().isModLoaded(FCA_MOD_ID))
		{
			registerPayloadType(payloadType);
		}
		ClientPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
			context.client().execute(() -> {
				if(EasyPlacePayloadBridge.packetId(payload) == EasyPlacePayload.HELLO_ACK)
				{
					CompoundTag data = EasyPlacePayloadBridge.data(payload);
					serverSupports = acceptsCapability(data)
							&& acceptsPacketAnchorCapability(data);
					serverSupportsYPrecision = serverSupports
							&& acceptsYPrecisionCapability(data);
					serverSupportsPistonProtection = serverSupportsYPrecision
							&& acceptsPistonProtectionCapability(data);
					serverSupportsRelativeAux = serverSupports
							&& acceptsRelativeAuxCapability(data);
					TriWasa.LOGGER.info(
							"[EasyPlace] Client received V2-Extra acknowledgement (capability={}, y8={}, piston={})",
							serverSupports,
							serverSupportsYPrecision,
							serverSupportsPistonProtection
					);
				}
			});
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			reset();
			helloPending = true;
			trySendHello(client);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
		ClientTickEvents.END_CLIENT_TICK.register(EasyPlaceClientNetwork::tick);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerPayloadType(
			CustomPacketPayload.Type<CustomPacketPayload> payloadType
	)
	{
		PayloadTypeRegistry.serverboundPlay().register(
				(CustomPacketPayload.Type)payloadType,
				(net.minecraft.network.codec.StreamCodec)EasyPlacePayloadBridge.codec()
		);
		PayloadTypeRegistry.clientboundPlay().register(
				(CustomPacketPayload.Type)payloadType,
				(net.minecraft.network.codec.StreamCodec)EasyPlacePayloadBridge.codec()
		);
	}

	public static boolean isActive()
	{
		return TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue() && serverSupports;
	}

	public static boolean supportsYPrecision()
	{
		return isActive() && serverSupportsYPrecision;
	}

	/** The piston mask rides in the Y payload, so it is unavailable without the y8 capability. */
	public static boolean supportsPistonProtection()
	{
		return supportsYPrecision() && serverSupportsPistonProtection;
	}

	/**
	 * Whether the server splits the auxiliary value into a 24-bit relative Z payload plus three
	 * bits in X. Only an explicit server acknowledgement enables it: encoding the new layout for
	 * a server that still decodes the fixed-origin field would silently misplace state.
	 */
	public static boolean supportsRelativeAux()
	{
		return isActive() && serverSupportsRelativeAux;
	}

	public static void onConfigChanged(boolean enabled)
	{
		serverSupports = false;
		serverSupportsYPrecision = false;
		serverSupportsPistonProtection = false;
		serverSupportsRelativeAux = false;
		if(!enabled)
		{
			helloPending = false;
			wasEnabled = false;
			EasyPlaceCrafterSlotLock.clear();
			PrinterPacketAnchor.clear();
			return;
		}

		wasEnabled = true;
		Minecraft client = Minecraft.getInstance();

		if(client == null)
		{
			helloPending = false;
			return;
		}

		helloPending = client.player != null && client.getConnection() != null;
		if(helloPending)
		{
			trySendHello(client);
		}
	}

	public static void reset()
	{
		serverSupports = false;
		serverSupportsYPrecision = false;
		serverSupportsPistonProtection = false;
		serverSupportsRelativeAux = false;
		helloPending = false;
		wasEnabled = TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue();
		EasyPlaceCrafterSlotLock.clear();
		PrinterPacketAnchor.clear();
	}

	private static void tick(Minecraft client)
	{
		boolean enabled = TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue();
		if(enabled && !wasEnabled)
		{
			helloPending = client.player != null;
		}
		if(!enabled)
		{
			serverSupports = false;
			serverSupportsYPrecision = false;
			serverSupportsPistonProtection = false;
			serverSupportsRelativeAux = false;
			helloPending = false;
		}
		wasEnabled = enabled;

		if(helloPending)
		{
			trySendHello(client);
		}
		EasyPlaceCrafterSlotLock.clear();
	}

	private static void trySendHello(Minecraft client)
	{
		if(!helloPending
				|| client.player == null
				|| client.getConnection() == null
				|| !TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue())
		{
			return;
		}

		CompoundTag data = new CompoundTag();
		String version = FabricLoader.getInstance()
				.getModContainer(TriWasa.MOD_ID)
				.orElseThrow()
				.getMetadata()
				.getVersion()
				.getFriendlyString();
		data.putString("mod_version", version);
		data.putInt(NBT_PROTOCOL_VERSION, PROTOCOL_VERSION);
		data.putBoolean(CAPABILITY_EASY_PLACE_V2_EXTRA, true);
		data.putBoolean(CAPABILITY_PACKET_ANCHOR, true);
		data.putBoolean(CAPABILITY_RELATIVE_AUX, true);
		data.putBoolean(CAPABILITY_Y_PRECISION, true);
		data.putBoolean(CAPABILITY_PISTON_PROTECTION, true);
		data.putInt(NBT_Y_PRECISION_BITS, EasyPlaceExtraProtocol.Y_PRECISION_BITS);
		data.putInt(
				NBT_PISTON_PROTECTION_BITS,
				EasyPlaceExtraProtocol.PISTON_PROTECTION_BITS
		);
		ListTag capabilities = new ListTag();
		capabilities.add(StringTag.valueOf(CAPABILITY_EASY_PLACE_V2_EXTRA));
		capabilities.add(StringTag.valueOf(CAPABILITY_PACKET_ANCHOR));
		capabilities.add(StringTag.valueOf(CAPABILITY_RELATIVE_AUX));
		capabilities.add(StringTag.valueOf(CAPABILITY_Y_PRECISION));
		capabilities.add(StringTag.valueOf(CAPABILITY_PISTON_PROTECTION));
		data.put(NBT_CAPABILITIES, capabilities);
		ClientPlayNetworking.send(EasyPlacePayloadBridge.create(EasyPlacePayload.HELLO, data));
		TriWasa.LOGGER.info("[EasyPlace] Client sent V2-Extra hello");
		helloPending = false;
	}

	static boolean acceptsCapability(CompoundTag data)
	{
		return data.getIntOr(NBT_PROTOCOL_VERSION, -1) == PROTOCOL_VERSION
				&& data.getBooleanOr(CAPABILITY_EASY_PLACE_V2_EXTRA, false);
	}

	/**
	 * The server only decodes the packet anchor, so an ACK that does not advertise the same
	 * contract must not activate V2-Extra: the client would encode payloads the server reads one
	 * raw value off. Both the explicit boolean and the capabilities list have to agree.
	 */
	static boolean acceptsPacketAnchorCapability(CompoundTag data)
	{
		return data.getBooleanOr(CAPABILITY_PACKET_ANCHOR, false)
				&& advertisesCapability(data, CAPABILITY_PACKET_ANCHOR);
	}

	/** The split auxiliary layout is optional, but a half-declared ACK must not enable it. */
	static boolean acceptsRelativeAuxCapability(CompoundTag data)
	{
		return data.getBooleanOr(CAPABILITY_RELATIVE_AUX, false)
				&& advertisesCapability(data, CAPABILITY_RELATIVE_AUX);
	}

	static boolean acceptsYPrecisionCapability(CompoundTag data)
	{
		return data.getIntOr(NBT_Y_PRECISION_BITS, -1)
				== EasyPlaceExtraProtocol.Y_PRECISION_BITS
				&& data.getBooleanOr(CAPABILITY_Y_PRECISION, false)
				&& advertisesCapability(data, CAPABILITY_Y_PRECISION);
	}

	static boolean acceptsPistonProtectionCapability(CompoundTag data)
	{
		return data.getIntOr(NBT_PISTON_PROTECTION_BITS, -1)
				== EasyPlaceExtraProtocol.PISTON_PROTECTION_BITS
				&& data.getBooleanOr(CAPABILITY_PISTON_PROTECTION, false)
				&& advertisesCapability(data, CAPABILITY_PISTON_PROTECTION);
	}

	private static boolean advertisesCapability(CompoundTag data, String capability)
	{
		ListTag capabilities = data.getListOrEmpty(NBT_CAPABILITIES);
		for(int index = 0; index < capabilities.size(); index++)
		{
			if(capability.equals(capabilities.getStringOr(index, "")))
			{
				return true;
			}
		}
		return false;
	}
}
