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
		CompoundTag legacyCapability = new CompoundTag();
		test.check(
				EasyPlaceClientNetwork.acceptsCapability(legacyCapability),
				"legacy acknowledgements without capability fields must remain accepted"
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
