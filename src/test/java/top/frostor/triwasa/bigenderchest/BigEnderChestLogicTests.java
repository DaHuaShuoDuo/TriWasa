package top.frostor.triwasa.bigenderchest;

import top.frostor.triwasa.bigenderchest.client.BigEnderChestPreviewSlotTest;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestAmountFormatterTest;
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
		BigEnderChestPreviewSlotTest.run();
		BigEnderChestAmountFormatterTest.run();
	}
}
