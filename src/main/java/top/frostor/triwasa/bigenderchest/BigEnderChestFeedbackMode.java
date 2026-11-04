package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum BigEnderChestFeedbackMode implements IConfigOptionListEntry
{
	SILENT("silent"),
	ACTION_BAR("action_bar"),
	CHAT("chat");

	private final String value;

	BigEnderChestFeedbackMode(String value)
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
		return StringUtils.translate("triwasa.config.big_ender_chest.feedback." + this.value);
	}

	@Override
	public IConfigOptionListEntry cycle(boolean forward)
	{
		BigEnderChestFeedbackMode[] values = values();
		int index = (this.ordinal() + (forward ? 1 : values.length - 1)) % values.length;
		return values[index];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestFeedbackMode feedback : values())
		{
			if(feedback.value.equalsIgnoreCase(value))
			{
				return feedback;
			}
		}

		return ACTION_BAR;
	}
}
