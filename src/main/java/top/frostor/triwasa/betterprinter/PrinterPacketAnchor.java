package top.frostor.triwasa.betterprinter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import top.frostor.triwasa.TriWasa;

/**
 * Carries the encoder anchor of a printer placement over to the moment the wire payload is
 * attached to the queued click.
 *
 * <p>Litematica and the printer encode the V2-Extra payload against the schematic target, but a
 * support click is sent with the neighbouring support block as {@code BlockHitResult.blockPos}.
 * The packet codec stores the hit location as a binary32 float offset from that position, so the
 * server decodes one raw value low when the support block sits east or above the target.
 * Translating the X and Y channels by the queued target difference makes the payload
 * self-consistent with the packet position; the Z channel carries the fixed-origin auxiliary
 * payload and must not move.</p>
 *
 * <p>The whole record lives for one placement attempt: the encoder runs, the click is queued, and
 * the protocol payload is attached, all on the same thread within the same executor call.</p>
 */
public final class PrinterPacketAnchor
{
	private static final ThreadLocal<BlockPos> ENCODE_ANCHOR = new ThreadLocal<>();
	private static final ThreadLocal<Delta> PENDING = new ThreadLocal<>();

	private PrinterPacketAnchor()
	{
	}

	/** Marks the position the encoder just anchored its payload at. */
	public static void begin(BlockPos encodeAnchor)
	{
		PENDING.remove();
		ENCODE_ANCHOR.set(encodeAnchor.immutable());
	}

	/** Computes the translation for the click that is actually being queued. */
	public static void recordTarget(BlockPos packetTarget)
	{
		BlockPos encodeAnchor = ENCODE_ANCHOR.get();
		ENCODE_ANCHOR.remove();
		if(encodeAnchor == null)
		{
			return;
		}
		int dx = packetTarget.getX() - encodeAnchor.getX();
		int dy = packetTarget.getY() - encodeAnchor.getY();
		if(dx == 0 && dy == 0)
		{
			PENDING.remove();
			return;
		}
		PENDING.set(new Delta(dx, dy));
	}

	/** Applies the pending translation to an encoded payload and clears it. */
	public static Vec3 apply(Vec3 encodedHitVec)
	{
		Delta delta = PENDING.get();
		PENDING.remove();
		if(delta == null)
		{
			return encodedHitVec;
		}
		TriWasa.LOGGER.debug(
				"[EasyPlace] Re-anchored printer payload by x={}, y={}",
				delta.dx(),
				delta.dy()
		);
		return new Vec3(
				encodedHitVec.x + delta.dx(),
				encodedHitVec.y + delta.dy(),
				encodedHitVec.z
		);
	}

	public static void clear()
	{
		ENCODE_ANCHOR.remove();
		PENDING.remove();
	}

	private record Delta(int dx, int dy)
	{
	}
}
