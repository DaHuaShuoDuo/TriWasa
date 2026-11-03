package top.frostor.triwasa.bigenderchest;

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
	}
}
