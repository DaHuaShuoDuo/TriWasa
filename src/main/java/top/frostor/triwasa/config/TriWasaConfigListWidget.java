package top.frostor.triwasa.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class TriWasaConfigListWidget extends WidgetListConfigOptions
{
	TriWasaConfigListWidget(
			int x,
			int y,
			int width,
			int height,
			int configWidth,
			float zLevel,
			boolean useKeybindSearch,
			GuiConfigsBase parent
	)
	{
		super(x, y, width, height, configWidth, zLevel, useKeybindSearch, parent);
	}

	@Override
	protected Collection<ConfigOptionWrapper> getAllEntries()
	{
		return super.getAllEntries().stream()
				.filter(entry -> entry.getConfig() != TriWasaConfigs.EASY_PLACE_EXPANDED)
				.filter(entry -> TriWasaConfigs.EASY_PLACE_EXPANDED.getBooleanValue()
						|| this.hasFilter()
						|| !TriWasaConfigs.EASY_PLACE_CHILDREN.contains(entry.getConfig()))
				.toList();
	}

	@Override
	protected List<String> getEntryStringsForFilter(ConfigOptionWrapper entry)
	{
		List<String> strings = new ArrayList<>(super.getEntryStringsForFilter(entry));
		IConfigBase config = entry.getConfig();
		if(config == null)
		{
			return strings;
		}

		if(TriWasaConfigs.EASY_PLACE_CHILDREN.contains(config))
		{
			addSearchStrings(strings, TriWasaConfigs.EASY_PLACE_V2_PROTOCOL_EXTRA);
		}
		return strings;
	}

	private static void addSearchStrings(List<String> strings, IConfigBase config)
	{
		String name = config.getName().toLowerCase();
		String translated = config.getConfigGuiDisplayName().toLowerCase();
		if(!strings.contains(name))
		{
			strings.add(name);
		}
		if(!name.equals(translated) && !strings.contains(translated))
		{
			strings.add(translated);
		}
	}

	@Override
	public int getMaxNameLengthWrapped(List<ConfigOptionWrapper> wrappers)
	{
		int width = 0;
		for(ConfigOptionWrapper wrapper : wrappers)
		{
			IConfigBase config = wrapper.getConfig();
			if(config == null)
			{
				continue;
			}

			int indent = 18;
			if(TriWasaConfigs.EASY_PLACE_CHILDREN.contains(config))
			{
				indent += TriWasaConfigOptionWidget.CHILD_INDENT;
			}
			width = Math.max(width, this.getStringWidth(config.getConfigGuiDisplayName()) + indent + 16);
		}
		return width;
	}

	@Override
	protected WidgetConfigOption createListEntryWidget(
			int x,
			int y,
			int listIndex,
			boolean isOdd,
			ConfigOptionWrapper entry
	)
	{
		return new TriWasaConfigOptionWidget(
				x,
				y,
				this.browserEntryWidth,
				22,
				this.maxLabelWidth,
				this.configWidth,
				entry,
				listIndex,
				this.parent,
				this,
				this
		);
	}

	void refreshExpandedConfigs()
	{
		int scrollPosition = this.getScrollbar().getValue();
		this.refreshEntries();
		this.getScrollbar().setValue(scrollPosition);
	}
}
