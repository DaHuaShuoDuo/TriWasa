package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class VerifierPositionOverlayFilter
{
	private VerifierPositionOverlayFilter()
	{
	}

	public static void retainPinnedPositions(
			List<MismatchRenderPos> renderPositions,
			List<BlockPos> blockPositions,
			Collection<BlockPos> pinnedPositions,
			MismatchType mismatchType
	)
	{
		renderPositions.clear();
		blockPositions.clear();

		pinnedPositions.stream()
				.map(BlockPos::immutable)
				.sorted(Comparator.comparingInt((BlockPos pos) -> pos.getY())
						.thenComparingInt(BlockPos::getX)
						.thenComparingInt(BlockPos::getZ))
				.forEach(pos -> {
					renderPositions.add(new MismatchRenderPos(mismatchType, pos));
					blockPositions.add(pos);
				});
	}
}