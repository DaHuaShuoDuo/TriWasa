package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
	private static final ThreadLocal<Integer> QUERY_DEPTH = ThreadLocal.withInitial(() -> 0);

	private SchematicSelectionBoost()
	{
	}

	/**
	 * Standalone feature switch. It deliberately does not depend on betterEasyPlace: the boost is
	 * a self contained ray trace change and must stay usable while the other placement
	 * corrections are off.
	 */
	public static boolean isEnabled()
	{
		return TriWasaConfigs.EASY_PLACE_FULL_BLOCK_SELECTION.getBooleanValue();
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

	/**
	 * Marks the read only preview trace used to draw the target outline. It does not depend on
	 * the easy place handling flag, which is only set while a click is being processed.
	 */
	public static void enterQuery()
	{
		if(isEnabled())
		{
			QUERY_DEPTH.set(QUERY_DEPTH.get() + 1);
		}
	}

	public static void exitQuery()
	{
		if(isEnabled() == false)
		{
			return;
		}

		int depth = QUERY_DEPTH.get() - 1;
		if(depth <= 0)
		{
			QUERY_DEPTH.remove();
		}
		else
		{
			QUERY_DEPTH.set(depth);
		}
	}

	public static VoxelShape expand(VoxelShape original, BlockPos pos, BlockState schematicState)
	{
		if(isScoped() == false || original.isEmpty())
		{
			return original;
		}

		return expandShape(
				original,
				true,
				isStateCorrect(pos, schematicState, Minecraft.getInstance().level)
		);
	}

	/**
	 * Empty shapes never become click targets, and blocks that already match the schematic keep
	 * their real outline: there is nothing to place there, so enlarging their hit box would only
	 * steal the click from neighbouring positions.
	 */
	public static VoxelShape expandShape(
			VoxelShape original,
			boolean active,
			boolean stateCorrect
	)
	{
		if(active == false || stateCorrect || original.isEmpty())
		{
			return original;
		}

		return Shapes.block();
	}

	public static boolean isStateCorrect(
			BlockPos pos,
			BlockState schematicState,
			Level clientLevel
	)
	{
		if(clientLevel == null)
		{
			return false;
		}

		return isSameBlockState(clientLevel.getBlockState(pos), schematicState);
	}

	/**
	 * Mirrors Litematica's own "nothing to place here" check in
	 * {@code EasyPlaceUtils#handleEasyPlace}: it compares the two {@code BlockState} references
	 * with {@code ==} ({@code if_acmpeq}). There is no public Litematica helper for it, and the
	 * property-loop comparison used by the wall neighbour protection must NOT be reused here:
	 * that one deliberately ignores WATERLOGGED for update suppression, which would classify a
	 * block Litematica still wants to place as already correct.
	 */
	public static boolean isSameBlockState(BlockState actual, BlockState projected)
	{
		return actual != null && projected != null && actual == projected;
	}

	private static boolean shouldBoost(Level level)
	{
		return isEnabled() && level instanceof WorldSchematic && isEasyPlaceContext();
	}

	private static boolean isEasyPlaceContext()
	{
		return EasyPlaceUtils.isHandling() || LEGACY_DEPTH.get() > 0 || QUERY_DEPTH.get() > 0;
	}
}
