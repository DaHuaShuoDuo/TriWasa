package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.util.StringUtils;

final class TriWasaConfigOptionWidget extends WidgetConfigOption
{
	static final int CHILD_INDENT = 28;
	private static final int EXPAND_BUTTON_WIDTH = 18;
	private static final int EXPAND_BUTTON_LEFT_OFFSET = -6;
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
}
