package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
		Vec3 encodedCrafter = encodeAuxiliary(encoded, 0x1ab);
		test.check(
				decodeAuxiliary(encodedCrafter) == 0x1ab,
				"crafter slot masks must survive the hit Z encoding"
		);
		int updateFlags = EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION
				| EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION;
		test.check(
				(updateFlags & EasyPlaceExtraProtocol.CRAFTER_SLOT_MASK) == 0,
				"update-suppression flags must not overlap crafter slot bits"
		);
		int combinedAuxiliary = updateFlags | 0x1ab;
		test.check(
				decodeAuxiliary(encodeAuxiliary(encoded, combinedAuxiliary)) == combinedAuxiliary,
				"update flags and crafter masks must survive the Z channel together"
		);
		int wallNeighborMask = (1 << net.minecraft.core.Direction.UP.ordinal())
				| (1 << net.minecraft.core.Direction.DOWN.ordinal());
		int wallAuxiliary = combinedAuxiliary | (wallNeighborMask << 11);
		test.check(
				decodeAuxiliary(encodeAuxiliary(encoded, wallAuxiliary)) == wallAuxiliary,
				"wall neighbor match bits must survive the Z channel"
		);
		int observerAuxiliary = EasyPlaceExtraProtocol.OBSERVER_UPDATE_SUPPRESSION;
		test.check(
				decodeAuxiliary(encodeAuxiliary(encoded, observerAuxiliary)) == observerAuxiliary,
				"observer protection flag must survive the Z channel"
		);
		int tntAuxiliary = EasyPlaceExtraProtocol.TNT_UPDATE_SUPPRESSION;
		test.check(
				decodeAuxiliary(encodeAuxiliary(encoded, tntAuxiliary)) == tntAuxiliary,
				"TNT protection flag must survive the Z channel"
		);
		int railNeighborMask = 0b1_0001;
		int railAuxiliary = EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION
				| (railNeighborMask
				<< Integer.numberOfTrailingZeros(
				EasyPlaceExtraProtocol.RAIL_NEIGHBOR_MATCH_BASE
		));
		test.check(
				decodeAuxiliary(encodeAuxiliary(encoded, railAuxiliary)) == railAuxiliary,
				"rail neighbor match bits must survive the Z channel"
		);
		Vec3 otherAnchor = EasyPlaceExtraProtocol.encodeX(
				new BlockPos(500, -20, -400),
				new Vec3(0.5, -19.5, -399.5),
				0x5a
		);
		test.check(
				decodeAuxiliary(encodeAuxiliary(otherAnchor, railAuxiliary)) == railAuxiliary,
				"the fixed-origin Z channel must not depend on the click position"
		);
		test.check(
				EasyPlaceExtraProtocol.stripAuxiliarySchema(
						EasyPlaceExtraProtocol.decodeAuxiliary(
								EasyPlaceExtraProtocol.encodeAuxiliary(encoded, railAuxiliary)
						)
				) == -1,
				"a Z value without the schema bit must be rejected"
		);
		CompoundTag missingCapability = new CompoundTag();
		test.check(
				!EasyPlaceClientNetwork.acceptsCapability(missingCapability),
				"acknowledgements without the current protocol fields must be rejected"
		);
		CompoundTag currentCapability = new CompoundTag();
		currentCapability.putInt("protocol_version", 1);
		currentCapability.putBoolean("easy_place_v2_extra", true);
		currentCapability.putBoolean("easy_place_y_precision", true);
		currentCapability.putInt("y_precision_bits", 8);
		ListTag capabilities = new ListTag();
		capabilities.add(StringTag.valueOf("easy_place_y_precision"));
		currentCapability.put("capabilities", capabilities);
		test.check(
				EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an explicit V2-Extra capability must be accepted"
		);
		test.check(
				EasyPlaceClientNetwork.acceptsYPrecisionCapability(currentCapability),
				"the eight-bit Y capability must be accepted"
		);
		currentCapability.put("capabilities", new ListTag());
		test.check(
				!EasyPlaceClientNetwork.acceptsYPrecisionCapability(currentCapability),
				"the Y capability must also be advertised in the capability list"
		);
		capabilities = new ListTag();
		capabilities.add(StringTag.valueOf("easy_place_y_precision"));
		currentCapability.put("capabilities", capabilities);
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

	private static Vec3 encodeAuxiliary(Vec3 hitVec, int auxiliaryValue)
	{
		return EasyPlaceExtraProtocol.encodeAuxiliary(
				hitVec,
				auxiliaryValue | EasyPlaceExtraProtocol.AUXILIARY_SCHEMA
		);
	}

	private static int decodeAuxiliary(Vec3 hitVec)
	{
		return EasyPlaceExtraProtocol.stripAuxiliarySchema(
				EasyPlaceExtraProtocol.decodeAuxiliary(hitVec)
		);
	}

}
