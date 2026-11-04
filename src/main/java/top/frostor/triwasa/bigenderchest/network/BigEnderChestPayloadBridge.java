package top.frostor.triwasa.bigenderchest.network;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestPayload;

/**
 * Uses the server mod's payload on integrated clients so FCA remains authoritative.
 */
final class BigEnderChestPayloadBridge
{
	private static final String FCA_MOD_ID = "frostors-carpet-addition";
	private static final boolean FCA_LOADED = FabricLoader.getInstance().isModLoaded(FCA_MOD_ID);

	private BigEnderChestPayloadBridge()
	{
	}

	static CustomPacketPayload.Type<?> type()
	{
		return FCA_LOADED
				? FcaBigEnderChestPayloadAdapter.type()
				: BigEnderChestPayload.TYPE;
	}

	@SuppressWarnings("unchecked")
	static StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload> codec()
	{
		return FCA_LOADED
				? FcaBigEnderChestPayloadAdapter.codec()
				: BigEnderChestPayload.CODEC;
	}

	static CustomPacketPayload create(int messageType, CompoundTag data)
	{
		return FCA_LOADED
				? FcaBigEnderChestPayloadAdapter.create(messageType, data)
				: new BigEnderChestPayload(messageType, data);
	}

	static int messageType(CustomPacketPayload payload)
	{
		if(payload instanceof BigEnderChestPayload ownPayload)
		{
			return ownPayload.messageType();
		}
		return FcaBigEnderChestPayloadAdapter.messageType(payload);
	}

	static CompoundTag data(CustomPacketPayload payload)
	{
		if(payload instanceof BigEnderChestPayload ownPayload)
		{
			return ownPayload.data();
		}
		return FcaBigEnderChestPayloadAdapter.data(payload);
	}
}
