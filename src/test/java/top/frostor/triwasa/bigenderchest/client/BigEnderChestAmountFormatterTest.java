package top.frostor.triwasa.bigenderchest.client;

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
		System.out.println("BigEnderChestAmountFormatterTest passed " + checks + " checks");
	}

	private static void check(String expected, long amount)
	{
		checks++;
		String actual = BigEnderChestAmountFormatter.compact(amount);
		if(!expected.equals(actual))
		{
			throw new AssertionError("Expected compact amount " + expected + " for " + amount + ", got " + actual);
		}
	}
}
