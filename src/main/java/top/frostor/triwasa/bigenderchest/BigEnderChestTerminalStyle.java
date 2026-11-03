package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestTerminalStyle implements IConfigOptionListEntry
{
	COMPACT("compact"),
	STANDARD("standard"),
	EXPANDED("expanded"),
	FULL_HEIGHT("fullHeight");

	private final String value;

	BigEnderChestTerminalStyle(String value)
	{
		this.value = value;
	}

	@Override
	public String getStringValue()
	{
		return this.value;
	}

	@Override
	public String getDisplayName()
	{
		return StringUtils.translate("triwasa.config.big_ender_chest.style." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		BigEnderChestTerminalStyle[] values = values();
		return values[(this.ordinal() + (forward ? 1 : values.length - 1)) % values.length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestTerminalStyle style : values())
		{
			if(style.value.equalsIgnoreCase(value))
			{
				return style;
			}
		}
		return STANDARD;
	}
}
