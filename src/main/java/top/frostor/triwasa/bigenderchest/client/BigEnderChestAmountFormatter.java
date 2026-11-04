package top.frostor.triwasa.bigenderchest.client;

final class BigEnderChestAmountFormatter
{
	private static final char[] UNITS = {'K', 'M', 'G', 'T', 'P', 'E'};

	private BigEnderChestAmountFormatter()
	{
	}

	static String compact(long amount)
	{
		return compact(amount, 4, 10_000);
	}

	static String compact(long amount, int maxCharacters, int abbreviationThreshold)
	{
		String full = Long.toString(amount);
		if(amount < abbreviationThreshold)
		{
			return full;
		}

		long divisor = 1;
		int unitIndex = -1;
		while(unitIndex + 1 < UNITS.length
				&& Long.toString(amount / divisor).length() + 1 > maxCharacters)
		{
			divisor *= 1000;
			unitIndex++;
		}

		long whole = amount / divisor;
		long tenths = amount % divisor / (divisor / 10);
		String number = Long.toString(whole);
		String fraction = tenths > 0 ? "." + tenths : "";
		String candidate = number + fraction + UNITS[unitIndex];
		if(candidate.length() <= maxCharacters)
		{
			return candidate;
		}
		if(whole == 0 && tenths > 0)
		{
			return "." + tenths + UNITS[unitIndex];
		}
		return number + UNITS[unitIndex];
	}
}
