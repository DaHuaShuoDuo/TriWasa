package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.frostor.triwasa.config.TriWasaConfigs;

/**
 * Gives every non-full schematic block a full 1x1x1 hit box while Litematica is tracing the
 * schematic world for an easy place click. The scope is opened by
 * {@code RayTraceUtils.rayTraceBlocks} and is therefore limited to the schematic world trace of
 * that one call: block list traces used by the area selection, the verifier overlay or the
 * printer keep the real outline shapes.
 */
public final class SchematicSelectionBoost
{
	private static final ThreadLocal<Integer> TRACE_DEPTH = ThreadLocal.withInitial(() -> 0);
	private static final ThreadLocal<Integer> LEGACY_DEPTH = ThreadLocal.withInitial(() -> 0);

	private SchematicSelectionBoost()
	{
	}

	public static boolean isEnabled()
	{
		return EasyPlaceRules.isEnabled()
				&& TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.getBooleanValue();
	}

	/**
	 * Opens the boost scope for one schematic world ray trace.
	 */
	public static void enterTrace(Level level)
	{
		if(shouldBoost(level))
		{
			TRACE_DEPTH.set(TRACE_DEPTH.get() + 1);
		}
	}

	/**
	 * Closes the scope opened by {@link #enterTrace(Level)}. The condition is re-evaluated
	 * instead of remembered so nested traces cannot unbalance the depth counter.
	 */
	public static void exitTrace(Level level)
	{
		if(shouldBoost(level) == false)
		{
			return;
		}

		int depth = TRACE_DEPTH.get() - 1;
		if(depth <= 0)
		{
			TRACE_DEPTH.remove();
		}
		else
		{
			TRACE_DEPTH.set(depth);
		}
	}

	/**
	 * Marks the legacy {@code WorldUtils.doEasyPlaceAction} trace, which does not set the
	 * {@code EasyPlaceUtils} handling flag used by the post-rewrite path.
	 */
	public static void enterLegacy()
	{
		LEGACY_DEPTH.set(LEGACY_DEPTH.get() + 1);
	}

	public static void exitLegacy()
	{
		int depth = LEGACY_DEPTH.get() - 1;
		if(depth <= 0)
		{
			LEGACY_DEPTH.remove();
		}
		else
		{
			LEGACY_DEPTH.set(depth);
		}
	}

	public static boolean isScoped()
	{
		return TRACE_DEPTH.get() > 0;
	}

	public static VoxelShape expand(VoxelShape original)
	{
		return expandShape(original, isScoped());
	}

	/**
	 * Air and other shapes without an outline stay untouchable, so the boost can never turn an
	 * empty schematic position into a valid click target.
	 */
	public static VoxelShape expandShape(VoxelShape original, boolean active)
	{
		if(active == false || original.isEmpty())
		{
			return original;
		}

		return Shapes.block();
	}

	private static boolean shouldBoost(Level level)
	{
		return isEnabled() && level instanceof WorldSchematic && isEasyPlaceContext();
	}

	private static boolean isEasyPlaceContext()
	{
		return EasyPlaceUtils.isHandling() || LEGACY_DEPTH.get() > 0;
	}
}
