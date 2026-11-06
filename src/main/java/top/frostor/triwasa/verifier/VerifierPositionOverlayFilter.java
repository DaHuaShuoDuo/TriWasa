package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.core.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class VerifierPositionOverlayFilter
{
	private VerifierPositionOverlayFilter()
	{
	}

	public static void retainPinnedPositions(
			List<MismatchRenderPos> renderPositions,
			List<BlockPos> blockPositions,
			Map<BlockPos, MismatchType> pinnedPositions
	)
	{
		renderPositions.clear();
		blockPositions.clear();

		pinnedPositions.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(
						Comparator.comparingInt((BlockPos pos) -> pos.getY())
								.thenComparingInt(BlockPos::getX)
								.thenComparingInt(BlockPos::getZ)
				))
				.forEach(entry -> {
					BlockPos pos = entry.getKey().immutable();
					renderPositions.add(new MismatchRenderPos(entry.getValue(), pos));
					blockPositions.add(pos);
				});
	}
}