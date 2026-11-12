package top.frostor.triwasa.betterprinter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

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
	private static final ThreadLocal<Vec3> ENCODED_PAYLOAD = new ThreadLocal<>();
	private static final ThreadLocal<Delta> PENDING = new ThreadLocal<>();

	private PrinterPacketAnchor()
	{
	}

	/**
	 * Marks the position the encoder anchored its payload at.
	 *
	 * <p>The payload instance is remembered as well, so a later attach can prove it is still
	 * looking at this placement's payload instead of a leftover delta from a rejected click or a
	 * native Litematica/Servux payload.</p>
	 */
	public static void begin(BlockPos encodeAnchor, Vec3 encodedPayload)
	{
		clear();
		ENCODE_ANCHOR.set(encodeAnchor.immutable());
		ENCODED_PAYLOAD.set(encodedPayload);
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

	/**
	 * Applies the pending translation to this placement's payload and clears it.
	 *
	 * <p>Anything other than the payload recorded by {@link #begin} is passed through untouched:
	 * a rejected click can leave a pending delta behind, and a client that falls back to a native
	 * V3/Servux payload must never be shifted by it.</p>
	 */
	public static Vec3 apply(Vec3 encodedHitVec)
	{
		Delta delta = PENDING.get();
		PENDING.remove();
		Vec3 encoded = ENCODED_PAYLOAD.get();
		ENCODED_PAYLOAD.remove();
		ENCODE_ANCHOR.remove();
		if(delta == null || encoded == null || !encoded.equals(encodedHitVec))
		{
			return encodedHitVec;
		}
		return new Vec3(
				encodedHitVec.x + delta.dx(),
				encodedHitVec.y + delta.dy(),
				encodedHitVec.z
		);
	}

	public static void clear()
	{
		ENCODE_ANCHOR.remove();
		ENCODED_PAYLOAD.remove();
		PENDING.remove();
	}

	private record Delta(int dx, int dy)
	{
	}
}
