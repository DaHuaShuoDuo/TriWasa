package top.frostor.triwasa.compat.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestLayout;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestTerminalScreen;

import java.util.List;

/**
 * Keeps REI from consuming clicks in the terminal's left-side button strip.
 */
public final class TriWasaReiClientPlugin implements REIClientPlugin
{
	private static final DisplayBoundsProvider<BigEnderChestTerminalScreen> SCREEN_BOUNDS =
			new DisplayBoundsProvider<>()
			{
				@Override
				public Rectangle getScreenBounds(BigEnderChestTerminalScreen screen)
				{
					BigEnderChestLayout.Bounds bounds = screen.interactionBounds();
					return bounds == null
							? new Rectangle()
							: new Rectangle(
									bounds.x(),
									bounds.y(),
									bounds.w(),
									bounds.h()
							);
				}

				@Override
				public <R extends net.minecraft.client.gui.screens.Screen> boolean isHandingScreen(
						Class<R> screen
				)
				{
					return BigEnderChestTerminalScreen.class.isAssignableFrom(screen);
				}

				@Override
				public double getPriority()
				{
					return 100.0;
				}
			};

	@Override
	public void registerScreens(ScreenRegistry registry)
	{
		registry.registerDecider(SCREEN_BOUNDS);
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones)
	{
		zones.register(BigEnderChestTerminalScreen.class, screen -> {
			BigEnderChestLayout.Bounds bounds = screen.interactionBounds();
			return bounds == null
					? List.of()
					: List.of(new Rectangle(
							bounds.x(),
							bounds.y(),
							bounds.w(),
							bounds.h()
					));
		});
	}
}
