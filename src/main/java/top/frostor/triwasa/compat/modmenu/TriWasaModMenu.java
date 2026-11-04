package top.frostor.triwasa.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import top.frostor.triwasa.config.TriWasaConfigGui;

public final class TriWasaModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> {
			TriWasaConfigGui screen = new TriWasaConfigGui();
			screen.setParent(parent);
			return screen;
		};
	}
}
