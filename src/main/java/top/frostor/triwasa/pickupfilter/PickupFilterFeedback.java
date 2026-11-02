package top.frostor.triwasa.pickupfilter;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum PickupFilterFeedback implements IConfigOptionListEntry
{
	SILENT("silent"),
	ACTION_BAR("action_bar"),
	CHAT("chat");

	private final String value;

	PickupFilterFeedback(String value)
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
		return StringUtils.translate("triwasa.config.pickup_filter.feedback." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		PickupFilterFeedback[] values = values();
		int index = (this.ordinal() + (forward ? 1 : values.length - 1)) % values.length;
		return values[index];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(PickupFilterFeedback feedback : values())
		{
			if(feedback.value.equalsIgnoreCase(value))
			{
				return feedback;
			}
		}

		return ACTION_BAR;
	}
}
