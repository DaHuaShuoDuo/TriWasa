package top.frostor.triwasa.easyplace;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
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

		int checks = EasyPlaceWireProtocolTest.run()
				+ EasyPlaceBasicStateTest.run()
				+ EasyPlaceContainerStateTest.run()
				+ EasyPlaceMultifaceStateTest.run()
				+ EasyPlaceQuantityStateTest.run()
				+ EasyPlaceHangingSignStateTest.run()
				+ EasyPlaceMushroomStateTest.run()
				+ EasyPlaceAttachmentStateTest.run()
				+ EasyPlaceRailStateTest.run()
				+ EasyPlaceWallStateTest.run()
				+ EasyPlaceUpdateFlagsTest.run()
				+ EasyPlaceSpecialStateTest.run()
				+ EasyPlacePrinterStateTest.run();

		System.out.println("EasyPlaceProtocolTest passed " + checks + " checks");
	}
}
