package top.frostor.triwasa.bigenderchest;

import net.minecraft.nbt.CompoundTag;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.util.List;

public final class BigEnderChestProtocolTest
{
	private static int checks;

	private BigEnderChestProtocolTest()
	{
	}

	public static void run()
	{
		BigEnderChestMessages.Hello hello = new BigEnderChestMessages.Hello(
				BigEnderChestProtocol.VERSION,
				BigEnderChestCapabilities.REQUIRED,
				BigEnderChestProtocol.MAX_CHUNK_BYTES
		);
		check(
				hello.equals(BigEnderChestMessages.Hello.fromTag(hello.toTag())),
				"HELLO must round trip through protocol field names"
		);
		CompoundTag ackTag = new CompoundTag();
		ackTag.putInt("protocol_version", 2);
		ackTag.putInt("capabilities", BigEnderChestCapabilities.REQUIRED);
		ackTag.putInt("chunk_byte_limit", 8192);
		ackTag.putBoolean("accepted", true);
		BigEnderChestMessages.HelloAck ack =
				BigEnderChestMessages.HelloAck.fromTag(ackTag);
		check(ack.accepted(), "HELLO_ACK accepted flag must decode");
		check(
				BigEnderChestCapabilities.hasAll(
						ack.capabilities(),
						BigEnderChestCapabilities.REQUIRED
				),
				"all required protocol v2 capabilities must be present"
		);

		BigEnderChestMessages.ActionRequest action =
				new BigEnderChestMessages.ActionRequest(
						41,
						9,
						3,
						7,
						-1,
						-1,
						123,
						BigEnderChestProtocol.ACTION_QUICK_MOVE_TO_PLAYER,
						(byte)4,
						64,
						12,
						List.of(2, 7, 9)
				);
		check(
				action.equals(BigEnderChestMessages.ActionRequest.fromTag(action.toTag())),
				"ACTION_REQUEST must round trip all player inventory indices"
		);
		check(action.requestId() != 0, "request ids must be non-zero");
		check(action.actionSequence() > 0, "action sequence must be positive");
		check(
				BigEnderChestCapabilities.names(BigEnderChestCapabilities.REQUIRED).size()
						== 12,
				"all 12 required capabilities must be represented"
		);
		System.out.println("BigEnderChestProtocolTest passed " + checks + " checks");
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
