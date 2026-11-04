package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestAmountFontSize implements IConfigOptionListEntry
{
	LARGE("large", 0.85f),
	NORMAL("normal", 2.0f / 3.0f);

	private final String value;
	private final float scale;

	BigEnderChestAmountFontSize(String value, float scale)
	{
		this.value = value;
		this.scale = scale;
	}

	public float scale()
	{
		return this.scale;
	}

	@Override
	public String getStringValue()
	{
		return this.value;
	}

	@Override
	public String getDisplayName()
	{
		return StringUtils.translate("triwasa.config.big_ender_chest.amount_font." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		BigEnderChestAmountFontSize[] values = values();
		return values[(this.ordinal() + (forward ? 1 : values.length - 1)) % values.length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestAmountFontSize fontSize : values())
		{
			if(fontSize.value.equalsIgnoreCase(value))
			{
				return fontSize;
			}
		}
		return LARGE;
	}
}
