package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.network.chat.MutableComponent;

final class TriWasaConfigOptionWidget extends WidgetConfigOption
{
	static final int CHILD_INDENT = 28;
	private static final int EXPAND_BUTTON_WIDTH = 18;
	private static final int EXPAND_BUTTON_LEFT_OFFSET = -6;
	private static final int TOOLTIP_MIN_WIDTH = 80;
	private static final int TOOLTIP_MAX_WIDTH = 320;
	private static final int TOOLTIP_HORIZONTAL_MARGIN = 40;
	private final TriWasaConfigListWidget configList;

	TriWasaConfigOptionWidget(
			int x,
			int y,
			int width,
			int height,
			int labelWidth,
			int configWidth,
			ConfigOptionWrapper wrapper,
			int listIndex,
			IKeybindConfigGui host,
			WidgetListConfigOptionsBase<?, ?> parent,
			TriWasaConfigListWidget configList
	)
	{
		super(x, y, width, height, labelWidth, configWidth, wrapper, listIndex, host, parent);
		this.configList = configList;
	}

	@Override
	protected void addConfigOption(int x, int y, int labelWidth, int configWidth, IConfigBase config)
	{
		if(TriWasaConfigs.isChildConfig(config))
		{
			x += CHILD_INDENT;
			labelWidth = Math.max(20, labelWidth - CHILD_INDENT);
		}

		int expandButtonX = x + EXPAND_BUTTON_LEFT_OFFSET;
		x += EXPAND_BUTTON_WIDTH;
		labelWidth = Math.max(20, labelWidth - EXPAND_BUTTON_WIDTH);
		super.addConfigOption(x, y, labelWidth, configWidth, config);

		ConfigBoolean expansion = TriWasaConfigs.getExpansionState(config);
		if(expansion != null)
		{
			this.addExpandButton(expandButtonX, y, expansion);
		}
	}

	@Override
	protected void addConfigComment(int x, int y, int width, int height, String comment)
	{
		this.addWidget(new WrappedHoverInfo(
				x,
				y,
				width,
				height,
				StringUtils.translate(comment),
				getTooltipWrapWidth(this.mc.getWindow().getGuiScaledWidth())
		));
	}

	@Override
	protected void addConfigComment(
			int x,
			int y,
			int width,
			int height,
			MutableComponent comment
	)
	{
		this.addWidget(new WrappedHoverInfo(
				x,
				y,
				width,
				height,
				comment.getString(),
				getTooltipWrapWidth(this.mc.getWindow().getGuiScaledWidth())
		));
	}

	static int getTooltipWrapWidth(int screenWidth)
	{
		return Math.min(
				TOOLTIP_MAX_WIDTH,
				Math.max(TOOLTIP_MIN_WIDTH, screenWidth - TOOLTIP_HORIZONTAL_MARGIN)
		);
	}

	private void addExpandButton(int x, int y, ConfigBoolean expansion)
	{
		String label = expansion.getBooleanValue() ? "[-]" : "[+]";
		ButtonGeneric button = new ButtonGeneric(
				x,
				y + 2,
				EXPAND_BUTTON_WIDTH,
				18,
				label,
				StringUtils.translate("triwasa.gui.expand_settings")
		).setRenderDefaultBackground(false);
		this.addButton(button, (clickedButton, mouseButton) -> {
			expansion.setBooleanValue(!expansion.getBooleanValue());
			this.configList.refreshExpandedConfigs();
		});
	}

	private static final class WrappedHoverInfo extends WidgetHoverInfo
	{
		private WrappedHoverInfo(
				int x,
				int y,
				int width,
				int height,
				String text,
				int maxLineWidth
		)
		{
			super(x, y, width, height, "");
			this.lines.clear();
			StringUtils.splitTextToLines(this.lines, text, maxLineWidth);
			while(!this.lines.isEmpty() && this.lines.getLast().isEmpty())
			{
				this.lines.removeLast();
			}
		}
	}
}
