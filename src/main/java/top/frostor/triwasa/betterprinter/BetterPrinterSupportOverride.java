package top.frostor.triwasa.betterprinter;

public final class BetterPrinterSupportOverride
{
	private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

	private BetterPrinterSupportOverride()
	{
	}

	public static void begin()
	{
		DEPTH.set(DEPTH.get() + 1);
	}

	public static void end()
	{
		int depth = DEPTH.get() - 1;
		if(depth <= 0)
		{
			DEPTH.remove();
		}
		else
		{
			DEPTH.set(depth);
		}
	}

	public static boolean isActive()
	{
		return DEPTH.get() > 0;
	}
}
