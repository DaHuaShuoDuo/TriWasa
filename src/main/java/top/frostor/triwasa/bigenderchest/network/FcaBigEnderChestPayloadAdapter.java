package top.frostor.triwasa.bigenderchest.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.carpet.bigenderchest.api.BigEnderChestProtocol;

/**
 * This class must only load when FCA is present; it directly uses FCA's public payload API.
 */
final class FcaBigEnderChestPayloadAdapter
{
	private FcaBigEnderChestPayloadAdapter()
	{
	}

	static CustomPacketPayload.Type<BigEnderChestProtocol.Payload> type()
	{
		return BigEnderChestProtocol.Payload.TYPE;
	}

	static StreamCodec<RegistryFriendlyByteBuf, BigEnderChestProtocol.Payload> codec()
	{
		return BigEnderChestProtocol.Payload.STREAM_CODEC;
	}

	static CustomPacketPayload create(int messageType, CompoundTag data)
	{
		return new BigEnderChestProtocol.Payload(messageType, data);
	}

	static int messageType(CustomPacketPayload payload)
	{
		return ((BigEnderChestProtocol.Payload)payload).messageType();
	}

	static CompoundTag data(CustomPacketPayload payload)
	{
		return ((BigEnderChestProtocol.Payload)payload).data();
	}
}
