package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;

/**
 * Headless checks for the printer payload re-anchor.
 *
 * <p>The wire stores the hit location as a binary32 float offset from the packet
 * {@code BlockHitResult.blockPos}. A support click therefore has to translate the X/Y payload
 * from the schematic target to the block that is actually clicked; the Z channel carries the
 * fixed-origin auxiliary payload and must stay untouched.</p>
 */
public final class PrinterPacketAnchorTest
{
	private static final BlockPos TARGET = new BlockPos(118, 70, -23);
	private static final int Y_PRECISION = 0x53;

	private PrinterPacketAnchorTest()
	{
	}

	public static int run()
	{
		int checks = 0;
		checks += new PrinterPacketAnchorTest().checkIdentity();
		checks += new PrinterPacketAnchorTest().checkAxisTranslation();
		checks += new PrinterPacketAnchorTest().checkWireRoundTrip();
		checks += new PrinterPacketAnchorTest().checkOneShot();
		checks += new PrinterPacketAnchorTest().checkForeignPayload();
		return checks;
	}

	private int checkIdentity()
	{
		Vec3 encoded = payload(TARGET, 0x2a);
		Vec3 applied = PrinterPacketAnchor.apply(encoded);
		check(same(applied, encoded), "apply without a recorded anchor must be a no-op");

		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET);
		applied = PrinterPacketAnchor.apply(encoded);
		check(same(applied, encoded), "a click on the target must not translate the payload");
		return 2;
	}

	private int checkAxisTranslation()
	{
		Vec3 encoded = payload(TARGET, 0x2a);
		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.EAST));
		Vec3 east = PrinterPacketAnchor.apply(encoded);
		check(
				Math.abs(east.x - (encoded.x + 1.0)) < 1e-9
						&& Math.abs(east.y - encoded.y) < 1e-9
						&& Math.abs(east.z - encoded.z) < 1e-9,
				"an east support click must translate X only"
		);

		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.UP));
		Vec3 up = PrinterPacketAnchor.apply(encoded);
		check(
				Math.abs(up.y - (encoded.y + 1.0)) < 1e-9
						&& Math.abs(up.x - encoded.x) < 1e-9
						&& Math.abs(up.z - encoded.z) < 1e-9,
				"an up support click must translate Y only"
		);

		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.WEST));
		Vec3 west = PrinterPacketAnchor.apply(encoded);
		check(
				Math.abs(west.x - (encoded.x - 1.0)) < 1e-9
						&& Math.abs(west.z - encoded.z) < 1e-9,
				"a west support click must translate X backwards"
		);
		return 3;
	}

	/**
	 * The decoded raw must equal the requested raw on every side once the payload is translated,
	 * including the east/up sides that used to decode one value low.
	 */
	private int checkWireRoundTrip()
	{
		int checks = 0;
		for(int raw = 0; raw <= 0x7f; raw++)
		{
			Vec3 encoded = payload(TARGET, raw);
			for(Direction side : Direction.values())
			{
				BlockPos support = TARGET.relative(side);
				PrinterPacketAnchor.begin(TARGET, encoded);
				PrinterPacketAnchor.recordTarget(support);
				Vec3 reanchored = PrinterPacketAnchor.apply(encoded);
				Vec3 received = wire(support, reanchored);

				int protocolValue = EasyPlaceExtraProtocol.decode(received, support);
				check(
						EasyPlaceExtraProtocol.isExtra(protocolValue)
								&& EasyPlaceExtraProtocol.toRawValue(protocolValue) == raw,
						"raw " + raw + " must survive the support anchor on side " + side
				);
				check(
						EasyPlaceExtraProtocol.decodeY(received, support)
								== EasyPlaceExtraProtocol.quantizeRelativeY(0.42),
						"the Y precision must survive the support anchor on side " + side
				);
				checks += 2;
			}
		}
		return checks;
	}

	private int checkOneShot()
	{
		Vec3 encoded = payload(TARGET, 0x2a);
		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.EAST));
		PrinterPacketAnchor.apply(encoded);
		Vec3 second = PrinterPacketAnchor.apply(encoded);
		check(same(second, encoded), "the recorded anchor must be consumed after one payload");
		return 1;
	}

	/**
	 * A rejected click can leave a pending delta behind, and the client may then fall back to a
	 * native Litematica/Servux payload. That payload must pass through untouched, and the stale
	 * delta must not survive to the next attempt.
	 */
	private int checkForeignPayload()
	{
		Vec3 encoded = payload(TARGET, 0x2a);
		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.EAST));

		Vec3 nativePayload = new Vec3(
				TARGET.getX() + 2.5,
				TARGET.getY() + 0.5,
				TARGET.getZ() + 0.5
		);
		check(
				same(PrinterPacketAnchor.apply(nativePayload), nativePayload),
				"a native payload must never be translated by a printer delta"
		);
		check(
				same(PrinterPacketAnchor.apply(nativePayload), nativePayload),
				"the foreign attach must consume the stale delta"
		);

		PrinterPacketAnchor.begin(TARGET, encoded);
		PrinterPacketAnchor.recordTarget(TARGET.relative(Direction.EAST));
		PrinterPacketAnchor.clear();
		check(
				same(PrinterPacketAnchor.apply(encoded), encoded),
				"clear() must drop the pending delta and the remembered payload"
		);
		return 3;
	}

	/** Encodes X, Y and the fixed-origin Z the way the client encoder does. */
	private static Vec3 payload(BlockPos anchor, int raw)
	{
		int extraValue = EasyPlaceExtraProtocol.toExtraValue(raw);
		return new Vec3(
				anchor.getX() + (extraValue << 1) + 2.0,
				anchor.getY() + 2.0
						+ EasyPlaceExtraProtocol.quantizeRelativeY(0.42) * 2.0,
				0.5
		);
	}

	/** Mirrors the binary32 float offset both packet codecs write for the hit location. */
	private static Vec3 wire(BlockPos packetPos, Vec3 absoluteLocation)
	{
		return new Vec3(
				packetPos.getX() + (float)(absoluteLocation.x - packetPos.getX()),
				packetPos.getY() + (float)(absoluteLocation.y - packetPos.getY()),
				packetPos.getZ() + (float)(absoluteLocation.z - packetPos.getZ())
		);
	}

	private static boolean same(Vec3 left, Vec3 right)
	{
		return left.x == right.x && left.y == right.y && left.z == right.z;
	}

	private static void check(boolean condition, String message)
	{
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}
