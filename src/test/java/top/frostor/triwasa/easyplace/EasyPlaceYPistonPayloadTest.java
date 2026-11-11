package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.phys.Vec3;

public final class EasyPlaceYPistonPayloadTest extends EasyPlaceTestSupport
{
	private static final BlockPos POS = new BlockPos(-17, 70, 31);

	private EasyPlaceYPistonPayloadTest()
	{
	}

	public static int run()
	{
		EasyPlaceYPistonPayloadTest test = new EasyPlaceYPistonPayloadTest();
		test.checkFloatBudget();
		test.checkWireRoundTrip();
		test.checkPlainEncodingUnchanged();
		test.checkSchemaValidation();
		test.checkCapabilityNegotiation();
		return test.checks();
	}

	private void checkFloatBudget()
	{
		long maxOffset = 2L + 2L * EasyPlaceExtraProtocol.Y_PAYLOAD_MASK;
		check(
				maxOffset == (1L << 22),
				"the maximum Y payload must encode to an offset of exactly 2^22"
		);
		check(
				maxOffset <= (1L << 24),
				"the Y payload must stay inside the exact-integer range of a binary32 float offset"
		);
		check(
				(EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK
						<< EasyPlaceExtraProtocol.Y_PISTON_MASK_SHIFT)
						< EasyPlaceExtraProtocol.Y_PISTON_SCHEMA,
				"the piston mask must not overlap the Y schema bit"
		);
	}

	private void checkWireRoundTrip()
	{
		Vec3 original = new Vec3(2.5, 70.73, 6.5);
		int expectedPrecision = EasyPlaceExtraProtocol.quantizeRelativeY(0.73);
		for(int mask : new int[]{
				0,
				1,
				0x5a3,
				EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK
		})
		{
			Vec3 encoded = EasyPlaceExtraProtocol.encodeY(POS, original, mask);
			check(
					encoded.x == original.x && encoded.z == original.z,
					"Y piston encoding must not touch the X or Z channels"
			);
			Vec3 wire = throughWire(encoded, POS);
			int packedY = EasyPlaceExtraProtocol.decodeY(wire, POS);
			check(
					(packedY & EasyPlaceExtraProtocol.Y_PRECISION_MASK) == expectedPrecision,
					"the 8-bit hit height must survive the binary32 network offset"
			);
			check(
					EasyPlaceExtraProtocol.decodeYPiston(packedY)
							== (mask & EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK),
					"the piston mask must survive the binary32 network offset"
			);
			check(
					EasyPlaceExtraProtocol.hasYPistonSchema(packedY) == (mask != 0),
					"the Y schema bit must only appear when piston data is present"
			);
		}
	}

	private void checkPlainEncodingUnchanged()
	{
		Vec3 original = new Vec3(2.5, 70.73, 6.5);
		check(
				EasyPlaceExtraProtocol.encodeY(POS, original, 0)
						.equals(EasyPlaceExtraProtocol.encodeY(POS, original)),
				"disabling piston protection must keep the legacy Y encoding bit-identical"
		);
		check(
				EasyPlaceExtraProtocol.encodeYPistonPayload(
						EasyPlaceExtraProtocol.quantizeRelativeY(0.73),
						0
				) == EasyPlaceExtraProtocol.quantizeRelativeY(0.73),
				"a zero piston mask must not add any high Y bits"
		);
		check(
				EasyPlaceExtraProtocol.decodeYPiston(-1) == 0,
				"an unencoded Y channel must not request piston protection"
		);
	}

	private void checkSchemaValidation()
	{
		int precision = EasyPlaceExtraProtocol.quantizeRelativeY(0.73);
		int payload = EasyPlaceExtraProtocol.encodeYPistonPayload(
				precision,
				EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK | 0x7000
		);
		check(
				EasyPlaceExtraProtocol.decodeYPiston(payload)
						== EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK,
				"piston mask bits outside the 12-bit field must be discarded"
		);
		check(
				(payload & ~EasyPlaceExtraProtocol.Y_PAYLOAD_MASK) == 0,
				"the Y payload must not use bits outside the declared layout"
		);
		int schemaOnly = EasyPlaceExtraProtocol.Y_PISTON_SCHEMA | precision;
		check(
				EasyPlaceExtraProtocol.hasYPistonSchema(schemaOnly)
						&& EasyPlaceExtraProtocol.decodeYPiston(schemaOnly) == 0,
				"a schema bit without mask bits must decode to an empty mask"
		);
	}

	private void checkCapabilityNegotiation()
	{
		CompoundTag data = new CompoundTag();
		data.putInt("piston_protection_bits", EasyPlaceExtraProtocol.PISTON_PROTECTION_BITS);
		data.putBoolean("easy_place_piston_protection", true);
		ListTag capabilities = new ListTag();
		capabilities.add(StringTag.valueOf("easy_place_piston_protection"));
		data.put("capabilities", capabilities);
		check(
				EasyPlaceClientNetwork.acceptsPistonProtectionCapability(data),
				"a complete piston capability ack must be accepted"
		);
		check(
				!EasyPlaceClientNetwork.acceptsPistonProtectionCapability(new CompoundTag()),
				"an ack without the piston capability must be rejected"
		);

		CompoundTag missingBits = data.copy();
		missingBits.remove("piston_protection_bits");
		check(
				!EasyPlaceClientNetwork.acceptsPistonProtectionCapability(missingBits),
				"an ack without the declared mask width must be rejected"
		);

		CompoundTag noListEntry = data.copy();
		noListEntry.put("capabilities", new ListTag());
		check(
				!EasyPlaceClientNetwork.acceptsPistonProtectionCapability(noListEntry),
				"the piston capability must also be advertised in the capabilities list"
		);
	}

	private static Vec3 throughWire(Vec3 encoded, BlockPos pos)
	{
		float wireX = (float)(encoded.x - pos.getX());
		float wireY = (float)(encoded.y - pos.getY());
		float wireZ = (float)(encoded.z - pos.getZ());
		return new Vec3(
				pos.getX() + (double)wireX,
				pos.getY() + (double)wireY,
				pos.getZ() + (double)wireZ
		);
	}
}
