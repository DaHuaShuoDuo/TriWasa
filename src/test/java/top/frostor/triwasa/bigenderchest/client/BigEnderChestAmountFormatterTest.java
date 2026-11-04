package top.frostor.triwasa.bigenderchest.client;

import top.frostor.triwasa.bigenderchest.BigEnderChestAmountStyle;

public final class BigEnderChestAmountFormatterTest
{
	private static int checks;

	private BigEnderChestAmountFormatterTest()
	{
	}

	public static void run()
	{
		check("0", 0);
		check("9999", 9_999);
		check("10K", 10_000);
		check("999K", 999_999);
		check("1M", 1_000_000);
		check("1.2M", 1_234_567);
		check("999M", 999_999_999);
		check("1G", 1_000_000_000L);
		check("1T", 1_000_000_000_000L);
		check("1P", 1_000_000_000_000_000L);
		check("1E", 1_000_000_000_000_000_000L);
		check("9.2E", Long.MAX_VALUE);
		check("999", 999, BigEnderChestAmountStyle.LARGE);
		check("1K", 1_000, BigEnderChestAmountStyle.LARGE);
		check("1K", 1_234, BigEnderChestAmountStyle.LARGE);
		check("9K", 9_999, BigEnderChestAmountStyle.LARGE);
		check("10K", 10_000, BigEnderChestAmountStyle.LARGE);
		check("99K", 99_999, BigEnderChestAmountStyle.LARGE);
		check(".1M", 100_000, BigEnderChestAmountStyle.LARGE);
		check(".9M", 999_999, BigEnderChestAmountStyle.LARGE);
		check("1M", 1_000_000, BigEnderChestAmountStyle.LARGE);
		check("1M", 1_234_567, BigEnderChestAmountStyle.LARGE);
		System.out.println("BigEnderChestAmountFormatterTest passed " + checks + " checks");
	}

	private static void check(String expected, long amount)
	{
		check(expected, amount, BigEnderChestAmountStyle.SMALL);
	}

	private static void check(
			String expected,
			long amount,
			BigEnderChestAmountStyle style
	)
	{
		checks++;
		String actual = BigEnderChestAmountFormatter.compact(amount, style);
		if(!expected.equals(actual))
		{
			throw new AssertionError(
					"Expected compact amount " + expected + " for " + amount
							+ ", got " + actual
			);
		}
	}
}
