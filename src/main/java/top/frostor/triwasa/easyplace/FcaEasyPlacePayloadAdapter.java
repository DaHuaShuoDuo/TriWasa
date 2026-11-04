package top.frostor.triwasa.easyplace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.carpet.easyplace.EasyPlaceExtraPayload;

/**
 * This class must only load when FCA is present; it directly uses FCA's public payload API.
 */
final class FcaEasyPlacePayloadAdapter
{
	private FcaEasyPlacePayloadAdapter()
	{
	}

	static CustomPacketPayload.Type<EasyPlaceExtraPayload> type()
	{
		return EasyPlaceExtraPayload.TYPE;
	}

	static StreamCodec<RegistryFriendlyByteBuf, EasyPlaceExtraPayload> codec()
	{
		return EasyPlaceExtraPayload.CODEC;
	}

	static CustomPacketPayload create(int packetId, CompoundTag data)
	{
		return new EasyPlaceExtraPayload(packetId, data);
	}

	static int packetId(CustomPacketPayload payload)
	{
		return ((EasyPlaceExtraPayload)payload).packetId();
	}

	static CompoundTag data(CustomPacketPayload payload)
	{
		return ((EasyPlaceExtraPayload)payload).data();
	}
}
