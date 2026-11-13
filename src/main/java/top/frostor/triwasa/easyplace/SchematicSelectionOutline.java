package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.tool.ToolMode;
import fi.dy.masa.litematica.util.EasyPlaceUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.util.HashSet;
import java.util.Set;

/**
 * Replaces the vanilla block outline with one around the schematic cell that easy place would
 * click. The outline state is render only data, so interactions, pick block and the protocol stay
 * untouched. It is an independent option: the box shape follows the currently active hit box
 * semantics (full block when the boost is on, real outline when it is off), so it never promises
 * a larger target than the player can actually click.
 */
public final class SchematicSelectionOutline
{
	private static final Set<String> NOTES = new HashSet<>();
	private static volatile boolean targetOutlineActive;

	private SchematicSelectionOutline()
	{
	}

	public static boolean hasTargetOutline()
	{
		return targetOutlineActive;
	}

	public static void noteRenderPassForced()
	{
		note("outline render pass forced");
	}

	public static BlockOutlineRenderState resolve(BlockOutlineRenderState original)
	{
		note("outline extractor hook active");
		BlockOutlineRenderState replacement = compute(original);
		targetOutlineActive = replacement != null;
		return replacement;
	}

	private static BlockOutlineRenderState compute(BlockOutlineRenderState original)
	{
		if(isOutlineEnabled() == false)
		{
			note("outline disabled by config or parent switch");
			return null;
		}

		if(isEasyPlaceArmed() == false)
		{
			note("easy place mode is off or tool mode is rebuild");
			return null;
		}

		Minecraft minecraft = Minecraft.getInstance();
		WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();

		if(minecraft.level == null || minecraft.player == null || schematic == null)
		{
			note("no level, player or schematic world");
			return null;
		}

		BlockPos pos = findTarget(minecraft);

		if(pos == null)
		{
			note("easy place trace has no schematic target");
			return null;
		}

		BlockState schematicState = schematic.getBlockState(pos);

		if(SchematicSelectionBoost.isStateCorrect(pos, schematicState, minecraft.level))
		{
			note("target block already matches the schematic, keeping the vanilla outline");
			return null;
		}

		note("outline moved to schematic target cell");

		VoxelShape shape = outlineShape(minecraft, schematic, pos, schematicState);

		if(shape.isEmpty())
		{
			note("target has no outline shape, skipping the box");
			return null;
		}

		boolean highContrast = original != null
				? original.highContrast()
				: minecraft.options.highContrastBlockOutline().get();
		boolean translucent = original != null && original.isTranslucent();

		return new BlockOutlineRenderState(pos, translucent, highContrast, shape);
	}

	/**
	 * The box always matches what can actually be clicked: a full cell while the hit box boost is
	 * enabled, otherwise the real outline of the projected block. {@code SchematicSelectionBoost}
	 * also decides whether the preview trace below is boosted, so both stay in sync.
	 */
	private static VoxelShape outlineShape(
			Minecraft minecraft,
			WorldSchematic schematic,
			BlockPos pos,
			BlockState schematicState
	)
	{
		if(SchematicSelectionBoost.isEnabled())
		{
			note("outline uses the full block shape");
			return Shapes.block();
		}

		note("outline uses the real block outline");
		return schematicState.getShape(schematic, pos, CollisionContext.of(minecraft.player));
	}

	private static BlockPos findTarget(Minecraft minecraft)
	{
		double range = EasyPlaceUtils.getValidBlockRange(minecraft);
		RayTraceUtils.RayTraceWrapper wrapper;

		// enterQuery only opens the boost scope while the hit box feature is enabled, so the
		// preview trace and the box shape always agree on what is clickable.
		SchematicSelectionBoost.enterQuery();

		try
		{
			wrapper = Configs.Generic.EASY_PLACE_FIRST.getBooleanValue()
					? RayTraceUtils.getGenericTrace(
							minecraft.level,
							minecraft.player,
							range,
							true,
							Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue(),
							false
					)
					: RayTraceUtils.getFurthestSchematicWorldTraceBeforeVanilla(
							minecraft.level,
							minecraft.player,
							range
					);
		}
		finally
		{
			SchematicSelectionBoost.exitQuery();
		}

		if(wrapper == null
				|| wrapper.getHitType() != RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK)
		{
			return null;
		}

		return wrapper.getBlockHitResult().getBlockPos();
	}

	/**
	 * Standalone feature switch. The outline is render only, so it does not depend on
	 * betterEasyPlace either. Whether the box is drawn as a full block or as the real outline
	 * still follows {@link SchematicSelectionBoost#isEnabled()}.
	 */
	public static boolean isOutlineEnabled()
	{
		return TriWasaConfigs.EASY_PLACE_SELECTION_OUTLINE.getBooleanValue();
	}

	private static boolean isEasyPlaceArmed()
	{
		return Configs.Generic.EASY_PLACE_MODE.getBooleanValue()
				&& DataManager.getToolMode() != ToolMode.REBUILD;
	}

	/**
	 * One-shot diagnostics so a failed outline can be diagnosed from latest.log without
	 * spamming it every frame.
	 */
	private static void note(String message)
	{
		if(NOTES.add(message))
		{
			TriWasa.LOGGER.info("[triwasa/easyPlaceOutline] {}", message);
		}
	}
}
