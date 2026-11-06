package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VerifierPositionOverlayFilterTest
{
	private static int checks;

	private VerifierPositionOverlayFilterTest()
	{
	}

	public static void run()
	{
		BlockPos first = new BlockPos(12, 64, -7);
		BlockPos second = new BlockPos(-3, 70, 8);
		List<MismatchRenderPos> renderPositions = new ArrayList<>(List.of(
				new MismatchRenderPos(MismatchType.WRONG_STATE, new BlockPos(0, 64, 0)),
				new MismatchRenderPos(MismatchType.MISSING, first),
				new MismatchRenderPos(MismatchType.WRONG_BLOCK, second)
		));
		List<BlockPos> blockPositions = new ArrayList<>(List.of(
				new BlockPos(0, 64, 0),
				first,
				second
		));

		VerifierPositionOverlayFilter.retainPinnedPositions(
				renderPositions,
				blockPositions,
				Map.of(
						first, MismatchType.MISSING,
						second, MismatchType.WRONG_BLOCK
				)
		);

		check(renderPositions.size() == 2, "position filter must retain selections from multiple mismatch groups");
		check(blockPositions.size() == 2, "block position list must retain every selected position");
		check(renderPositions.stream().map(MismatchRenderPos::pos).collect(Collectors.toSet())
				.equals(Set.of(first, second)), "render entries must match every selected position");
		check(renderPositions.stream().filter(entry -> entry.pos().equals(first))
				.allMatch(entry -> entry.type() == MismatchType.MISSING),
				"first position must retain its mismatch group type");
		check(renderPositions.stream().filter(entry -> entry.pos().equals(second))
				.allMatch(entry -> entry.type() == MismatchType.WRONG_BLOCK),
				"second position must retain its mismatch group type");
		check(new java.util.HashSet<>(blockPositions).equals(Set.of(first, second)),
				"block position list must match every selected position");
		System.out.printf("VerifierPositionOverlayFilterTest passed %d checks%n", checks);
	}

	private static void check(boolean condition, String message)
	{
		checks++;
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}