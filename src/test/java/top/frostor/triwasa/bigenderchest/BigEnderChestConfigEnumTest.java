package top.frostor.triwasa.bigenderchest;

public final class BigEnderChestConfigEnumTest
{
	private static int checks;

	private BigEnderChestConfigEnumTest()
	{
	}

	public static void run()
	{
		check(
				BigEnderChestAmountStyle.SMALL == BigEnderChestAmountStyle.SMALL
						.fromString("small"),
				"small amount style must remain loadable"
		);
		check(
				BigEnderChestAmountStyle.LARGE == BigEnderChestAmountStyle.SMALL
						.fromString("large"),
				"large amount style must remain loadable"
		);
		check(
				BigEnderChestAmountStyle.SMALL == BigEnderChestAmountStyle.SMALL
						.fromString("normal"),
				"the obsolete normal value must keep its previous small fallback"
		);
		check(
				BigEnderChestFeedbackMode.ACTION_BAR == BigEnderChestFeedbackMode.SILENT
						.fromString("action_bar"),
				"action-bar feedback must remain loadable"
		);
		check(
				BigEnderChestFeedbackMode.ACTION_BAR == BigEnderChestFeedbackMode.SILENT
						.fromString("unknown"),
				"unknown feedback must retain the action-bar fallback"
		);
		System.out.println("BigEnderChestConfigEnumTest passed " + checks + " checks");
	}

	private static void check(boolean condition, String message)
	{
		checks++;
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}
