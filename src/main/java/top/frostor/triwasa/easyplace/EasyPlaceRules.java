package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceRules
{
	private EasyPlaceRules()
	{
	}

	public static boolean isEnabled()
	{
		return TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
	}

	public static boolean isPrinterEnabled()
	{
		return isEnabled() && TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
	}

	public static boolean isActive()
	{
		return isEnabled() && EasyPlaceUtils.isHandling();
	}
}
