package top.frostor.triwasa.easyplace;

import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceUpdateFlags
{
	private EasyPlaceUpdateFlags()
	{
	}

	public static int current()
	{
		return encode(
				TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue()
		);
	}

	public static int encode(boolean preserveRail, boolean preserveWall)
	{
		return (preserveRail ? EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION : 0)
				| (preserveWall ? EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION : 0);
	}
}
