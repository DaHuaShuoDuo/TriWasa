package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public final class EasyPlaceWireProtocolTest extends EasyPlaceTestSupport
{
	private EasyPlaceWireProtocolTest()
	{
	}

	public static int run()
	{
		EasyPlaceWireProtocolTest test = new EasyPlaceWireProtocolTest();
		for(int rawValue = 0; rawValue <= 0x7f; rawValue++)
		{
			int extraValue = EasyPlaceExtraProtocol.toExtraValue(rawValue);
			test.check(EasyPlaceExtraProtocol.isExtra(extraValue), "encoded value must carry the marker");
			test.check(
					EasyPlaceExtraProtocol.toRawValue(extraValue) == rawValue,
					"raw protocol value must round trip"
			);
		}

		BlockPos pos = new BlockPos(-17, 70, 31);
		Vec3 encoded = EasyPlaceExtraProtocol.encodeX(pos, new Vec3(0.5, 70.5, 31.5), 0x5a);
		test.check(
				EasyPlaceExtraProtocol.toRawValue(EasyPlaceExtraProtocol.decode(encoded, pos)) == 0x5a,
				"encoded hit position must decode through the FCA wire layout"
		);
		Vec3 encodedCrafter = EasyPlaceExtraProtocol.encodeZ(pos, encoded, 0x1ab);
		test.check(
				EasyPlaceExtraProtocol.decodeZ(encodedCrafter, pos) == 0x1ab,
				"crafter slot masks must survive the hit Z encoding"
		);
		int updateFlags = EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION
				| EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION;
		test.check(
				(updateFlags & EasyPlaceExtraProtocol.CRAFTER_SLOT_MASK) == 0,
				"update-suppression flags must not overlap crafter slot bits"
		);
		int combinedAuxiliary = updateFlags | 0x1ab;
		Vec3 encodedFlags = EasyPlaceExtraProtocol.encodeZ(pos, encoded, combinedAuxiliary);
		test.check(
				EasyPlaceExtraProtocol.decodeZ(encodedFlags, pos) == combinedAuxiliary,
				"update flags and crafter masks must survive the Z channel together"
		);
		int wallNeighborMask = (1 << net.minecraft.core.Direction.UP.ordinal())
				| (1 << net.minecraft.core.Direction.DOWN.ordinal());
		int wallAuxiliary = combinedAuxiliary
				| (wallNeighborMask << 11);
		test.check(
				EasyPlaceExtraProtocol.decodeZ(
						EasyPlaceExtraProtocol.encodeZ(pos, encoded, wallAuxiliary),
						pos
				) == wallAuxiliary,
				"wall neighbor match bits must survive the Z channel"
		);
		int observerAuxiliary = EasyPlaceExtraProtocol.OBSERVER_UPDATE_SUPPRESSION;
		test.check(
				EasyPlaceExtraProtocol.decodeZ(
						EasyPlaceExtraProtocol.encodeZ(pos, encoded, observerAuxiliary),
						pos
				) == observerAuxiliary,
				"observer protection flag must survive the Z channel"
		);
		int tntAuxiliary = EasyPlaceExtraProtocol.TNT_UPDATE_SUPPRESSION;
		test.check(
				EasyPlaceExtraProtocol.decodeZ(
						EasyPlaceExtraProtocol.encodeZ(pos, encoded, tntAuxiliary),
						pos
				) == tntAuxiliary,
				"TNT protection flag must survive the Z channel"
		);
		CompoundTag missingCapability = new CompoundTag();
		test.check(
				!EasyPlaceClientNetwork.acceptsCapability(missingCapability),
				"acknowledgements without the current protocol fields must be rejected"
		);
		CompoundTag currentCapability = new CompoundTag();
		currentCapability.putInt("protocol_version", 1);
		currentCapability.putBoolean("easy_place_v2_extra", true);
		test.check(
				EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an explicit V2-Extra capability must be accepted"
		);
		currentCapability.putBoolean("easy_place_v2_extra", false);
		test.check(
				!EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an explicit capability rejection must be honored"
		);
		currentCapability.putInt("protocol_version", 0);
		currentCapability.putBoolean("easy_place_v2_extra", true);
		test.check(
				!EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an unsupported protocol version must be rejected"
		);

		for(int mask = 0; mask <= 0x1ff; mask++)
		{
			int[] slots = EasyPlaceCrafterSlotLock.decodeSlots(mask);
			int rebuiltMask = 0;
			for(int slot : slots)
			{
				rebuiltMask |= 1 << slot;
			}
			test.check(rebuiltMask == mask, "crafter slot masks must round trip");
			test.check(
					Arrays.stream(slots).allMatch(slot -> slot >= 0 && slot < 9),
					"slot ids must be in range"
			);
		}

		return test.checks();
	}
}
