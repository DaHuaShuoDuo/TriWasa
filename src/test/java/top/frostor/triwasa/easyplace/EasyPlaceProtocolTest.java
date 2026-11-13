package top.frostor.triwasa.easyplace;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecRegistryTest;

public final class EasyPlaceProtocolTest
{
	private EasyPlaceProtocolTest()
	{
	}

	public static void main(String[] args)
	{
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		EasyPlaceCodecRegistryTest.run();

		boolean[] saved = saveProtocolSwitches();

		try
		{
			enableProtocolSwitches();

			int checks = EasyPlaceWireProtocolTest.run()
					+ EasyPlaceBasicStateTest.run()
					+ EasyPlaceContainerStateTest.run()
					+ EasyPlaceMultifaceStateTest.run()
					+ EasyPlaceQuantityStateTest.run()
					+ EasyPlaceHangingSignStateTest.run()
					+ EasyPlaceMushroomStateTest.run()
					+ EasyPlaceAttachmentStateTest.run()
					+ EasyPlaceRailStateTest.run()
					+ EasyPlaceRailProtectionTest.run()
					+ EasyPlaceWallStateTest.run()
					+ EasyPlaceWallProtectionTest.run()
					+ EasyPlaceSelectionBoostTest.run()
					+ EasyPlaceObserverStateTest.run()
					+ EasyPlaceUpdateFlagsTest.run()
					+ EasyPlaceSpecialStateTest.run()
					+ EasyPlacePrinterStateTest.run()
					+ EasyPlaceYPrecisionTest.run()
					+ EasyPlacePistonProtectionTest.run()
					+ EasyPlaceYPistonPayloadTest.run()
					+ PrinterPacketAnchorTest.run();

			System.out.println("EasyPlaceProtocolTest passed " + checks + " checks");
		}
		finally
		{
			restoreProtocolSwitches(saved);
		}
	}

	/*
	 * These tests cover the protocol encoding paths themselves, so they must not depend on the
	 * user facing defaults, which are all disabled. The defaults themselves are asserted by
	 * TriWasaConfigMetadataTest, which shares this JVM, so the switches are restored afterwards.
	 */
	private static void enableProtocolSwitches()
	{
		TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.setBooleanValue(true);
		TriWasaConfigs.EASY_PLACE_CRAFTER_LOCK_SLOT.setBooleanValue(true);
	}

	private static boolean[] saveProtocolSwitches()
	{
		return new boolean[] {
				TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.getBooleanValue(),
				TriWasaConfigs.EASY_PLACE_CRAFTER_LOCK_SLOT.getBooleanValue()
		};
	}

	private static void restoreProtocolSwitches(boolean[] saved)
	{
		TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA.setBooleanValue(saved[0]);
		TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(saved[1]);
		TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(saved[2]);
		TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(saved[3]);
		TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.setBooleanValue(saved[4]);
		TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.setBooleanValue(saved[5]);
		TriWasaConfigs.EASY_PLACE_CRAFTER_LOCK_SLOT.setBooleanValue(saved[6]);
	}
}
