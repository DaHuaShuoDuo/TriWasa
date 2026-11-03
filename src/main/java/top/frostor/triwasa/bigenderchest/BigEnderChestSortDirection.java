package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestSortDirection implements IConfigOptionListEntry
{
	ASCENDING("ascending"),
	DESCENDING("descending");

	private final String value;

	BigEnderChestSortDirection(String value)
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
		return StringUtils.translate("triwasa.config.big_ender_chest.sort_direction." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		BigEnderChestSortDirection[] values = values();
		return values[(this.ordinal() + (forward ? 1 : values.length - 1)) % values.length];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestSortDirection direction : values())
		{
			if(direction.value.equalsIgnoreCase(value))
			{
				return direction;
			}
		}
		return ASCENDING;
	}
}
