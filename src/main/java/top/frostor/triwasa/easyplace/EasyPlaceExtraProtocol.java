package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Wire-compatible V2-Extra layout used by Frostor's Carpet Addition.
 */
public final class EasyPlaceExtraProtocol
{
	private static final int EXTRA_MARKER = 0b0000_1000;
	public static final int CRAFTER_SLOT_MASK = 0x1ff;
	public static final int RAIL_UPDATE_SUPPRESSION = 1 << 9;
	public static final int WALL_UPDATE_SUPPRESSION = 1 << 10;
	public static final int WALL_NEIGHBOR_MATCH_BASE = 1 << 11;
	public static final int WALL_NEIGHBOR_MATCH_MASK = 0x3f << 11;
	public static final int OBSERVER_UPDATE_SUPPRESSION = 1 << 17;

	private EasyPlaceExtraProtocol()
	{
	}

	public static int decode(Vec3 hitPos, BlockPos clickedPos)
	{
		return decodeDimension(hitPos.x - clickedPos.getX());
	}

	public static int decodeZ(Vec3 hitPos, BlockPos clickedPos)
	{
		return decodeDimension(hitPos.z - clickedPos.getZ());
	}

	private static int decodeDimension(double relativeHit)
	{
		if(relativeHit < 2.0)
		{
			return -1;
		}
		return ((int) relativeHit - 2) >>> 1;
	}

	public static boolean isExtra(int protocolValue)
	{
		return protocolValue >= 0 && (protocolValue & EXTRA_MARKER) != 0;
	}

	public static int toRawValue(int extraProtocolValue)
	{
		return ((extraProtocolValue & 0b1111_0000) >>> 1) | (extraProtocolValue & 0b0000_0111);
	}

	public static int toExtraValue(int rawProtocolValue)
	{
		return ((rawProtocolValue & 0b0111_1000) << 1)
				| (rawProtocolValue & 0b0000_0111)
				| EXTRA_MARKER;
	}

	public static Vec3 encodeX(BlockPos pos, Vec3 hitVec, int rawProtocolValue)
	{
		int extraProtocolValue = toExtraValue(rawProtocolValue);
		return new Vec3(
				pos.getX() + (extraProtocolValue << 1) + 2.0,
				hitVec.y,
				hitVec.z
		);
	}

	public static Vec3 encodeZ(BlockPos pos, Vec3 hitVec, int protocolValue)
	{
		return new Vec3(
				hitVec.x,
				hitVec.y,
				pos.getZ() + (protocolValue << 1) + 2.0
		);
	}
}
