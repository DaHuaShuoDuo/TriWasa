package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestSortField implements IConfigOptionListEntry
{
	NAME("name"),
	COUNT("count");

	private final String value;

	BigEnderChestSortField(String value)
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
		return StringUtils.translate("triwasa.config.big_ender_chest.sort_field." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		BigEnderChestSortField[] values = values();
		return values[(this.ordinal() + (forward ? 1 : values.length - 1)) % values.length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestSortField field : values())
		{
			if(field.value.equalsIgnoreCase(value))
			{
				return field;
			}
		}
		return NAME;
	}
}
