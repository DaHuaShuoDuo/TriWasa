package top.frostor.triwasa.bigenderchest.client;

final class BigEnderChestAmountFormatter
{
	private static final char[] UNITS = {'K', 'M', 'G', 'T', 'P', 'E'};
	private static final int DECIMAL_THRESHOLD = 10_000;

	private BigEnderChestAmountFormatter()
	{
	}

	static String compact(long amount)
	{
		if(amount < DECIMAL_THRESHOLD)
		{
			return Long.toString(amount);
		}

		long divisor = 1;
		int unitIndex = -1;
		while(unitIndex + 1 < UNITS.length && amount / divisor >= 1000)
		{
			divisor *= 1000;
			unitIndex++;
		}

		long whole = amount / divisor;
		long tenths = amount % divisor / (divisor / 10);
		String number = whole < 10 && tenths > 0
				? whole + "." + tenths
				: Long.toString(whole);
		return number + UNITS[unitIndex];
	}
}
