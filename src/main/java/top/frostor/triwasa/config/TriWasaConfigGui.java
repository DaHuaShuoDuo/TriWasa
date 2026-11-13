package top.frostor.triwasa.config;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.util.StringUtils;
import top.frostor.triwasa.TriWasa;

import java.util.ArrayList;
import java.util.List;

public final class TriWasaConfigGui extends GuiConfigsBase
{
	private Tab currentTab = Tab.ALL;

	public TriWasaConfigGui()
	{
		super(10, 50, TriWasa.MOD_ID, null, "triwasa.gui.title");
	}

	@Override
	public void initGui()
	{
		super.initGui();
		clearOptions();

		int x = 10;
		for(Tab tab : Tab.values())
		{
			ButtonGeneric button = new ButtonGeneric(x, 26, -1, 20, tab.getDisplayName());
			button.setEnabled(currentTab != tab);
			this.addButton(button, (clickedButton, mouseButton) -> selectTab(tab));
			x += button.getWidth() + 2;
		}
	}

	@Override
	protected boolean useKeybindSearch()
	{
		return false;
	}

	@Override
	protected WidgetListConfigOptions createListWidget(int listX, int listY)
	{
		return new TriWasaConfigListWidget(
				listX,
				listY,
				this.getBrowserWidth(),
				this.getBrowserHeight(),
				this.getConfigWidth(),
				0.0F,
				this.useKeybindSearch(),
				this
		);
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs()
	{
		if(currentTab == Tab.ALL)
		{
			List<ConfigOptionWrapper> wrappers =
					new ArrayList<>(ConfigOptionWrapper.createFor(TriWasaConfigs.ALL_OPTIONS));
			wrappers.add(new ConfigOptionWrapper(
					StringUtils.translate("triwasa.fix.rei_submenu_text_fix")
			));
			return wrappers;
		}
		if(currentTab == Tab.LITEMATICA)
		{
			return ConfigOptionWrapper.createFor(TriWasaConfigs.LITEMATICA_OPTIONS);
		}
		if(currentTab == Tab.UTILITIES)
		{
			return ConfigOptionWrapper.createFor(TriWasaConfigs.UTILITY_OPTIONS);
		}

		return List.of(new ConfigOptionWrapper(
				StringUtils.translate("triwasa.fix.rei_submenu_text_fix")
		));
	}

	private void selectTab(Tab tab)
	{
		if(currentTab == tab)
		{
			return;
		}

		currentTab = tab;
		if(this.getListWidget() != null)
		{
			this.getListWidget().resetScrollbarPosition();
		}
		this.reCreateListWidget();
		this.initGui();
	}

	private enum Tab
	{
		ALL("triwasa.gui.config_category.all"),
		LITEMATICA("triwasa.gui.config_category.litematica"),
		UTILITIES("triwasa.gui.config_category.utilities"),
		OTHER_MOD_FIXES("triwasa.gui.config_category.other_mod_fixes");

		private final String translationKey;

		Tab(String translationKey)
		{
			this.translationKey = translationKey;
		}

		private String getDisplayName()
		{
			return StringUtils.translate(this.translationKey);
		}
	}
}
