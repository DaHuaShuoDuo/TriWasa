package top.frostor.triwasa.pickupfilter;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum PickupFilterMode implements IConfigOptionListEntry
{
	BLACKLIST("blacklist"),
	WHITELIST("whitelist");

	private final String value;

	PickupFilterMode(String value)
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
		return StringUtils.translate("triwasa.config.pickup_filter.mode." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		PickupFilterMode[] values = values();
		int index = (this.ordinal() + (forward ? 1 : values.length - 1)) % values.length;
		return values[index];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(PickupFilterMode mode : values())
		{
			if(mode.value.equalsIgnoreCase(value))
			{
				return mode;
			}
		}

		return BLACKLIST;
	}
}
