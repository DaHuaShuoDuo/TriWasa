package top.frostor.triwasa.bigenderchest.protocol;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Wire-compatible with FCA's big ender chest protocol payload.
 */
public record BigEnderChestPayload(int messageType, CompoundTag data) implements CustomPacketPayload
{
	public static final Type<BigEnderChestPayload> TYPE = new Type<>(
			BigEnderChestProtocol.CHANNEL
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, BigEnderChestPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.VAR_INT,
					BigEnderChestPayload::messageType,
					ByteBufCodecs.compoundTagCodec(
							() -> NbtAccounter.create(BigEnderChestProtocol.MAX_CHUNK_BYTES * 2L)
					),
					BigEnderChestPayload::data,
					BigEnderChestPayload::new
			);

	@Override
	public Type<BigEnderChestPayload> type()
	{
		return TYPE;
	}
}
