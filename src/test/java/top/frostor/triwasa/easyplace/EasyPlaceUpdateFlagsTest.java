package top.frostor.triwasa.easyplace;

import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceUpdateFlagsTest extends EasyPlaceTestSupport
{
	private EasyPlaceUpdateFlagsTest()
	{
	}

	public static int run()
	{
		EasyPlaceUpdateFlagsTest test = new EasyPlaceUpdateFlagsTest();
		test.checkEncoding();
		test.checkConfigSelection();
		return test.checks();
	}

	private void checkEncoding()
	{
		check(
				EasyPlaceUpdateFlags.encode(true, true)
						== (EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION
						| EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION),
				"both update-protection flags must encode together"
		);
		check(
				EasyPlaceUpdateFlags.encode(true, false)
						== EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION,
				"rail protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, true)
						== EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION,
				"wall protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, false) == 0,
				"disabled update protection must encode no flags"
		);
	}

	private void checkConfigSelection()
	{
		boolean rail = TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		boolean wall = TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			check(EasyPlaceUpdateFlags.current() == 0, "config selection must disable both flags");

			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION,
					"wall config must control only the wall flag"
			);

			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION,
					"rail config must control only the rail flag"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(rail);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(wall);
		}
	}
}
