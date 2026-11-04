package top.frostor.triwasa.bigenderchest;

import top.frostor.triwasa.bigenderchest.client.BigEnderChestAmountFormatterTest;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestHoverResolverTest;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTextLayoutTest;
import top.frostor.triwasa.config.TriWasaConfigMetadataTest;
import top.frostor.triwasa.easyplace.EasyPlaceProtocolTest;

public final class BigEnderChestLogicTests
{
	private BigEnderChestLogicTests()
	{
	}

	public static void main(String[] args)
	{
		EasyPlaceProtocolTest.main(args);
		BigEnderChestProtocolTest.run();
		BigEnderChestSnapshotTest.run();
		BigEnderChestLayoutTest.run();
		BigEnderChestActionQueueTest.run();
		BigEnderChestConfigEnumTest.run();
		BigEnderChestAmountFormatterTest.run();
		BigEnderChestTextLayoutTest.run();
		BigEnderChestHoverResolverTest.run();
		TriWasaConfigMetadataTest.run();
	}
}
