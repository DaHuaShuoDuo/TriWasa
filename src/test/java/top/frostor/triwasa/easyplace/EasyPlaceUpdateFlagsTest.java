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
				EasyPlaceUpdateFlags.encode(true, true, true, true)
						== (EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION
						| EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION
						| EasyPlaceExtraProtocol.OBSERVER_UPDATE_SUPPRESSION
						| EasyPlaceExtraProtocol.TNT_UPDATE_SUPPRESSION),
				"all update-protection flags must encode together"
		);
		check(
				EasyPlaceUpdateFlags.encode(true, false, false, false)
						== EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION,
				"rail protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, true, false, false)
						== EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION,
				"wall protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, false, true, false)
						== EasyPlaceExtraProtocol.OBSERVER_UPDATE_SUPPRESSION,
				"observer protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, false, false, true)
						== EasyPlaceExtraProtocol.TNT_UPDATE_SUPPRESSION,
				"TNT protection must encode independently"
		);
		check(
				EasyPlaceUpdateFlags.encode(false, false, false, false) == 0,
				"disabled update protection must encode no flags"
		);
	}

	private void checkConfigSelection()
	{
		boolean rail = TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue();
		boolean wall = TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		boolean observer = TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue();
		boolean tnt = TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(false);
			check(EasyPlaceUpdateFlags.current() == 0, "config selection must disable all flags");

			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION,
					"wall config must control only the wall flag"
			);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(true);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.OBSERVER_UPDATE_SUPPRESSION,
					"observer config must control only the observer flag"
			);
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(true);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.TNT_UPDATE_SUPPRESSION,
					"TNT config must control only the TNT flag"
			);
			TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(true);
			check(
					EasyPlaceUpdateFlags.current()
							== EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION,
					"rail config must control only the rail flag"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(rail);
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(wall);
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(observer);
			TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(tnt);
		}
	}
}
