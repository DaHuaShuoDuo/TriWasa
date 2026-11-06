package top.frostor.triwasa.bigenderchest;

import net.minecraft.nbt.CompoundTag;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestSessionState;

import java.util.List;

public final class BigEnderChestProtocolTest
{
	private static int checks;

	private BigEnderChestProtocolTest()
	{
	}

	public static void run()
	{
		check(BigEnderChestProtocol.VERSION == 3, "protocol version must be 3");
		check(
				BigEnderChestCapabilities.GENERIC_TERMINAL_CARRIER == 1 << 14,
				"generic terminal carrier must use the next capability bit"
		);
		check(
				BigEnderChestCapabilities.hasAll(
						BigEnderChestCapabilities.REQUIRED,
						BigEnderChestCapabilities.GENERIC_TERMINAL_CARRIER
				),
				"generic terminal carrier must be required by protocol v3"
		);
		check(
				BigEnderChestProtocol.TERMINAL_CARRIER_CONTAINER_SLOTS == 54
						&& BigEnderChestProtocol.TERMINAL_PLAYER_SLOT_START == 54
						&& BigEnderChestProtocol.TERMINAL_CARRIER_MAIN_SLOT_START == 54
						&& BigEnderChestProtocol.TERMINAL_CARRIER_HOTBAR_SLOT_START == 81,
				"generic carrier player slots must follow the 54 container slots"
		);

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
		ackTag.putInt("protocol_version", 3);
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
				"all required protocol v3 capabilities must be present"
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
						== 13,
				"all 13 required capabilities must be represented"
		);
		check(
				BigEnderChestCapabilities.names(BigEnderChestCapabilities.CAPABILITIES).size()
						== 15,
				"optional rule-state capabilities must be advertised"
		);

		BigEnderChestMessages.SessionOpen carrierOpen =
				new BigEnderChestMessages.SessionOpen(
						41,
						3,
						7,
						64,
						1,
						7,
						false,
						BigEnderChestCapabilities.CAPABILITIES,
						-1,
						BigEnderChestProtocol.SESSION_FLAG_GENERIC_TERMINAL_CARRIER
				);
		check(
				carrierOpen.isGenericTerminalCarrier(),
				"SESSION_OPEN must expose the generic carrier flag"
		);

		BigEnderChestSessionState session = new BigEnderChestSessionState();
		session.helloPending();
		int inactiveCapabilities = BigEnderChestCapabilities.CAPABILITIES
				& ~BigEnderChestCapabilities.TERMINAL_MENU_ACTIVE;
		check(
				session.acceptHello(new BigEnderChestMessages.HelloAck(
						BigEnderChestProtocol.VERSION,
						inactiveCapabilities,
						8192,
						true
				)),
				"HELLO_ACK must accept protocol support while the server rule is inactive"
		);
		check(!session.serverRuleEnabled(), "inactive server rule must not enable the terminal");
		check(!session.canUseTerminal(), "inactive server rule must block terminal use");
		session.updateCapabilities(
				inactiveCapabilities | BigEnderChestCapabilities.TERMINAL_MENU_ACTIVE
		);
		check(session.serverRuleEnabled(), "rule-state ACK must reactivate the terminal");
		check(session.canUseTerminal(), "updated capabilities must permit terminal use");

		session.openSession(carrierOpen);
		check(session.isTerminalCarrier(), "carrier flag must be retained by session state");
		check(
				session.isTerminalCarrierContainer(3),
				"matching carrier container id must permit screen replacement"
		);
		check(
				!session.isTerminalCarrierContainer(4),
				"mismatched container id must not permit screen replacement"
		);
		session.markCarrierScreenReplaced(true);
		check(session.carrierScreenReplaced(), "screen replacement must be deduplicated");

		BigEnderChestMessages.SessionOpen vanillaOpen =
				new BigEnderChestMessages.SessionOpen(
						42,
						4,
						8,
						64,
						1,
						8,
						false,
						BigEnderChestCapabilities.CAPABILITIES,
						-1,
						0
				);
		session.openSession(vanillaOpen);
		check(!session.isTerminalCarrier(), "flags-free session must stay vanilla");
		check(
				!session.isTerminalCarrierContainer(4),
				"flags-free session must never replace a generic chest screen"
		);

		session.openSession(carrierOpen);
		int downgradedCapabilities = inactiveCapabilities
				| BigEnderChestCapabilities.TERMINAL_MENU_ACTIVE;
		downgradedCapabilities &= ~BigEnderChestCapabilities.GENERIC_TERMINAL_CARRIER;
		session.updateCapabilities(downgradedCapabilities);
		check(
				!session.isTerminalCarrier(),
				"capability downgrade must clear carrier screen state"
		);

		BigEnderChestSessionState legacy = new BigEnderChestSessionState();
		legacy.helloPending();
		check(
				!legacy.acceptHello(new BigEnderChestMessages.HelloAck(
						2,
						BigEnderChestCapabilities.CAPABILITIES,
						8192,
						true
				)),
				"protocol v2 servers must be rejected instead of starting a terminal rewrite"
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
