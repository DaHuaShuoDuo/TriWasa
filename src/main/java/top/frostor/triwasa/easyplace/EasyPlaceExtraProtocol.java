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
	public static final int TNT_UPDATE_SUPPRESSION = 1 << 18;
	public static final int RAIL_NEIGHBOR_MATCH_BASE = 1 << 19;
	public static final int RAIL_NEIGHBOR_MATCH_MASK = 0x3f << 19;
	public static final int RAIL_STATE_UPDATE_SUPPRESSION = 1 << 25;
	public static final int WALL_AUXILIARY_SCHEMA = 1 << 26;
	public static final int WALL_UP = 1 << 27;
	public static final int RAIL_POWERED_MASK = 1 << 4;
	public static final int RAIL_PROTECTION_FLAG_POWERED = 1 << 3;
	public static final int RAIL_PROTECTION_FLAG_REGULAR = 1 << 4;
	public static final int Y_PRECISION_BITS = 8;
	public static final int Y_PRECISION_SCALE = 1 << Y_PRECISION_BITS;
	public static final int Y_PRECISION_MASK = Y_PRECISION_SCALE - 1;
	public static final int Y_PAYLOAD_SCHEMA = 1 << 15;
	public static final int Y_RAW_SHIFT = Y_PRECISION_BITS;
	public static final int Y_RAW_MASK = 0x7f;

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

	public static int decodeY(Vec3 hitPos, BlockPos clickedPos)
	{
		return decodeDimension(hitPos.y - clickedPos.getY());
	}

	public static int decodeYPayload(Vec3 hitPos)
	{
		if(hitPos.y < 2.0)
		{
			return -1;
		}
		return ((int) hitPos.y - 2) >>> 1;
	}

	public static Vec3 encodeY(BlockPos pos, Vec3 hitVec, int rawValue)
	{
		int precisionValue = quantizeRelativeY(hitVec.y - pos.getY());
		int payload = Y_PAYLOAD_SCHEMA
				| ((rawValue & Y_RAW_MASK) << Y_RAW_SHIFT)
				| precisionValue;
		return new Vec3(
				hitVec.x,
				2.0 + payload * 2.0,
				hitVec.z
		);
	}

	public static boolean isYPayload(int payload)
	{
		return payload >= 0 && (payload & Y_PAYLOAD_SCHEMA) != 0;
	}

	public static int rawValueFromYPayload(int payload)
	{
		if(!isYPayload(payload))
		{
			return -1;
		}
		return (payload >>> Y_RAW_SHIFT) & Y_RAW_MASK;
	}

	public static int quantizeRelativeY(double relativeHitY)
	{
		double clamped = Math.max(0.0, Math.min(1.0, relativeHitY));
		int value = Math.min(
				Y_PRECISION_MASK,
				(int) Math.floor(clamped * Y_PRECISION_SCALE)
		);
		if(clamped <= 0.5)
		{
			value = Math.min(value, (Y_PRECISION_SCALE / 2) - 1);
		}
		return value;
	}

	public static double restoreRelativeY(int precisionValue)
	{
		int clamped = Math.max(0, Math.min(Y_PRECISION_MASK, precisionValue));
		return (clamped + 0.5) / Y_PRECISION_SCALE;
	}

	public static boolean wallUp(int auxiliaryValue)
	{
		if(auxiliaryValue < 0)
		{
			return false;
		}
		if((auxiliaryValue & WALL_AUXILIARY_SCHEMA) != 0)
		{
			return (auxiliaryValue & WALL_UP) != 0;
		}
		return (auxiliaryValue & 0x1) != 0;
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
