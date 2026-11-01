package top.frostor.triwasa.easyplace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Wire-compatible with EarlyCompat and FCA's {@code earlycompat:network} channel.
 */
public record EasyPlacePayload(int packetId, CompoundTag data) implements CustomPacketPayload
{
	public static final int HELLO = 0;
	public static final int HELLO_ACK = 0;
	public static final Type<EasyPlacePayload> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath("earlycompat", "network")
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, EasyPlacePayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT,
					EasyPlacePayload::packetId,
					ByteBufCodecs.COMPOUND_TAG,
					EasyPlacePayload::data,
					EasyPlacePayload::new
			);

	@Override
	public Type<EasyPlacePayload> type()
	{
		return TYPE;
	}
}
