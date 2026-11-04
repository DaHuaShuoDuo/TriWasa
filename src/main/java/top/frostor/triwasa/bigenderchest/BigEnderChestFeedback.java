package top.frostor.triwasa.bigenderchest;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import top.frostor.triwasa.config.TriWasaConfigs;

public enum BigEnderChestFeedback implements IConfigOptionListEntry
{
	SILENT("silent"),
	ACTION_BAR("action_bar"),
	CHAT("chat");

	private final String value;

	BigEnderChestFeedback(String value)
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
		BigEnderChestFeedback[] values = values();
		int index = (this.ordinal() + (forward ? 1 : values.length - 1)) % values.length;
		return values[index];
	}

	@Override
	public IConfigOptionListEntry fromString(String value)
	{
		for(BigEnderChestFeedback feedback : values())
		{
			if(feedback.value.equalsIgnoreCase(value))
			{
				return feedback;
			}
		}

		return ACTION_BAR;
	}

	public static void showSelected(Component message)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
		{
			return;
		}

		BigEnderChestFeedback feedback = TriWasaConfigs.BIG_ENDER_CHEST_FEEDBACK
				.getOptionListValue() instanceof BigEnderChestFeedback selected
				? selected
				: ACTION_BAR;
		if(feedback == SILENT)
		{
			return;
		}
		if(feedback == CHAT)
		{
			player.sendSystemMessage(message);
		}
		else
		{
			player.sendOverlayMessage(message);
		}
	}
}
