package top.frostor.triwasa.devtools;

import top.frostor.triwasa.TriWasa;

public final class MixinPreflight
{
	private static final String[] TARGETS = {
			"net.minecraft.world.item.BlockItem",
			"net.minecraft.world.item.HangingSignItem",
			"net.minecraft.client.multiplayer.MultiPlayerGameMode",
			"net.minecraft.client.renderer.GameRenderer",
			"net.minecraft.client.renderer.extract.LevelExtractor",
			"net.minecraft.world.level.block.BaseRailBlock",
			"net.minecraft.world.level.block.DetectorRailBlock",
			"net.minecraft.world.level.block.FenceGateBlock",
			"net.minecraft.world.level.block.ObserverBlock",
			"net.minecraft.world.level.block.PoweredRailBlock",
			"net.minecraft.world.level.block.TntBlock",
			"net.minecraft.world.level.block.WallBlock",
			"net.minecraft.world.level.block.WallHangingSignBlock",
			"fi.dy.masa.litematica.util.EasyPlaceUtils",
			"fi.dy.masa.litematica.util.WorldUtils",
			"fi.dy.masa.litematica.util.PlacementHandler",
			"fi.dy.masa.litematica.util.RayTraceUtils",
			"fi.dy.masa.litematica.materials.MaterialCache",
			"fi.dy.masa.litematica.materials.MaterialListEntry",
			"me.aleksilassila.litematica.printer.guide.Guide",
			"me.aleksilassila.litematica.printer.guide.Guides",
			"me.aleksilassila.litematica.printer.guide.Guides$GuideRegistration",
			"me.aleksilassila.litematica.printer.guide.guides.FlowerBedGuide",
			"me.aleksilassila.litematica.printer.guide.guides.VineGuide",
			"me.aleksilassila.litematica.printer.guide.guides.SignGuide",
			"me.aleksilassila.litematica.printer.guide.guides.TurtleEggGuide",
			"me.aleksilassila.litematica.printer.printer.SchematicBlockContext",
			"me.aleksilassila.litematica.printer.printer.action.Action",
			"me.aleksilassila.litematica.printer.handler.HudStatsManager",
			"me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementExecutor",
			"me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementResult",
			"me.aleksilassila.litematica.printer.handler.handlers.print.PrintTargetScheduler",
			"me.aleksilassila.litematica.printer.handler.handlers.print.SortedSchematicTargetQueue",
			"me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils"
	};

	private MixinPreflight()
	{
	}

	public static void run()
	{
		ClassLoader loader = MixinPreflight.class.getClassLoader();
		try
		{
			for(String target : TARGETS)
			{
				Class.forName(target, false, loader);
			}
		}
		catch(Throwable throwable)
		{
			TriWasa.LOGGER.error("Mixin preflight failed", throwable);
			System.exit(1);
			return;
		}

		TriWasa.LOGGER.info("Mixin preflight passed");
		System.exit(0);
	}
}
