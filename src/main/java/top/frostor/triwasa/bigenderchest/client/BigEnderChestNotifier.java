package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import top.frostor.triwasa.bigenderchest.BigEnderChestFeedbackMode;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class BigEnderChestNotifier
{
	private BigEnderChestNotifier()
	{
	}

	public static void show(Component message)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
		{
			return;
		}

		BigEnderChestFeedbackMode feedback = TriWasaConfigs.BIG_ENDER_CHEST_FEEDBACK
				.getOptionListValue() instanceof BigEnderChestFeedbackMode selected
				? selected
				: BigEnderChestFeedbackMode.ACTION_BAR;
		if(feedback == BigEnderChestFeedbackMode.SILENT)
		{
			return;
		}
		if(feedback == BigEnderChestFeedbackMode.CHAT)
		{
			player.sendSystemMessage(message);
		}
		else
		{
			player.sendOverlayMessage(message);
		}
	}
}
