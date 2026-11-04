package top.frostor.triwasa.easyplace;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Reuses FCA's public payload on integrated clients and keeps a standalone fallback elsewhere.
 */
final class EasyPlacePayloadBridge
{
	private static final String FCA_MOD_ID = "frostors-carpet-addition";
	private static final boolean FCA_LOADED = FabricLoader.getInstance().isModLoaded(FCA_MOD_ID);

	private EasyPlacePayloadBridge()
	{
	}

	static CustomPacketPayload.Type<?> type()
	{
		return FCA_LOADED
				? FcaEasyPlacePayloadAdapter.type()
				: EasyPlacePayload.TYPE;
	}

	@SuppressWarnings("unchecked")
	static StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload> codec()
	{
		return FCA_LOADED
				? FcaEasyPlacePayloadAdapter.codec()
				: EasyPlacePayload.CODEC;
	}

	static CustomPacketPayload create(int packetId, CompoundTag data)
	{
		return FCA_LOADED
				? FcaEasyPlacePayloadAdapter.create(packetId, data)
				: new EasyPlacePayload(packetId, data);
	}

	static int packetId(CustomPacketPayload payload)
	{
		if(payload instanceof EasyPlacePayload ownPayload)
		{
			return ownPayload.packetId();
		}
		return FcaEasyPlacePayloadAdapter.packetId(payload);
	}

	static CompoundTag data(CustomPacketPayload payload)
	{
		if(payload instanceof EasyPlacePayload ownPayload)
		{
			return ownPayload.data();
		}
		return FcaEasyPlacePayloadAdapter.data(payload);
	}
}
