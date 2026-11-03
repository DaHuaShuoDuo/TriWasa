package top.frostor.triwasa.bigenderchest.network;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestPayload;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Uses the server mod's payload on integrated clients so FCA remains authoritative.
 */
final class BigEnderChestPayloadBridge
{
	private static final String FCA_MOD_ID = "frostors-carpet-addition";
	private static final ExternalPayload FCA_PAYLOAD = loadFcaPayload();

	private BigEnderChestPayloadBridge()
	{
	}

	static CustomPacketPayload.Type<?> type()
	{
		return FCA_PAYLOAD == null ? BigEnderChestPayload.TYPE : FCA_PAYLOAD.type();
	}

	@SuppressWarnings("unchecked")
	static StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload> codec()
	{
		return FCA_PAYLOAD == null
				? BigEnderChestPayload.CODEC
				: FCA_PAYLOAD.codec();
	}

	static CustomPacketPayload create(int messageType, CompoundTag data)
	{
		if(FCA_PAYLOAD == null)
		{
			return new BigEnderChestPayload(messageType, data);
		}
		try
		{
			return (CustomPacketPayload)FCA_PAYLOAD.constructor().newInstance(messageType, data);
		}
		catch(InstantiationException | IllegalAccessException | InvocationTargetException exception)
		{
			throw new IllegalStateException("Unable to create FCA big ender chest payload", exception);
		}
	}

	static int messageType(CustomPacketPayload payload)
	{
		if(payload instanceof BigEnderChestPayload ownPayload)
		{
			return ownPayload.messageType();
		}
		return (int)invokeAccessor(FCA_PAYLOAD.messageTypeMethod(), payload);
	}

	static CompoundTag data(CustomPacketPayload payload)
	{
		if(payload instanceof BigEnderChestPayload ownPayload)
		{
			return ownPayload.data();
		}
		return (CompoundTag)invokeAccessor(FCA_PAYLOAD.dataMethod(), payload);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static ExternalPayload loadFcaPayload()
	{
		if(!FabricLoader.getInstance().isModLoaded(FCA_MOD_ID))
		{
			return null;
		}

		try
		{
			Class<?> payloadClass = Class.forName(
					"top.frostor.carpet.bigenderchest.api.BigEnderChestProtocol$Payload"
			);
			Field typeField = payloadClass.getField("TYPE");
			Field codecField = payloadClass.getField("STREAM_CODEC");
			ExternalPayload payload = new ExternalPayload(
					payloadClass.getConstructor(int.class, CompoundTag.class),
					(CustomPacketPayload.Type<?>)typeField.get(null),
					(StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload>)
							codecField.get(null),
					payloadClass.getMethod("messageType"),
					payloadClass.getMethod("data")
			);
			TriWasa.LOGGER.info("Using FCA's authoritative big ender chest payload and codec");
			return payload;
		}
		catch(ReflectiveOperationException | ClassCastException exception)
		{
			TriWasa.LOGGER.error(
					"FCA is loaded but its big ender chest payload API is incompatible",
					exception
			);
			return null;
		}
	}

	private static Object invokeAccessor(Method method, Object payload)
	{
		if(FCA_PAYLOAD == null)
		{
			throw new IllegalStateException("FCA big ender chest payload is unavailable");
		}
		try
		{
			return method.invoke(payload);
		}
		catch(IllegalAccessException | InvocationTargetException exception)
		{
			throw new IllegalStateException("Unable to read FCA big ender chest payload", exception);
		}
	}

	private record ExternalPayload(
			Constructor<?> constructor,
			CustomPacketPayload.Type<?> type,
			StreamCodec<? super RegistryFriendlyByteBuf, ? extends CustomPacketPayload> codec,
			Method messageTypeMethod,
			Method dataMethod
	)
	{
	}
}
