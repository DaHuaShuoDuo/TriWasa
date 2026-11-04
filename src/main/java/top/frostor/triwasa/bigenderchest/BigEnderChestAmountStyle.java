package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestAmountStyle implements IConfigOptionListEntry
{
	SMALL("small", 2.0f / 3.0f, -1, 4, 10_000),
	LARGE("large", 0.85f, 0, 3, 1_000);

	private final String value;
	private final float scale;
	private final int pixelOffset;
	private final int maxCharacters;
	private final int abbreviationThreshold;

	BigEnderChestAmountStyle(
			String value,
			float scale,
			int pixelOffset,
			int maxCharacters,
			int abbreviationThreshold
	)
	{
		this.value = value;
		this.scale = scale;
		this.pixelOffset = pixelOffset;
		this.maxCharacters = maxCharacters;
		this.abbreviationThreshold = abbreviationThreshold;
	}

	public float scale()
	{
		return this.scale;
	}

	public int pixelOffset()
	{
		return this.pixelOffset;
	}

	public int maxCharacters()
	{
		return this.maxCharacters;
	}

	public int abbreviationThreshold()
	{
		return this.abbreviationThreshold;
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
		BigEnderChestAmountStyle[] values = values();
		return values[(this.ordinal() + (forward ? 1 : values.length - 1)) % values.length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestAmountStyle style : values())
		{
			if(style.value.equalsIgnoreCase(value))
			{
				return style;
			}
		}
		return SMALL;
	}
}
