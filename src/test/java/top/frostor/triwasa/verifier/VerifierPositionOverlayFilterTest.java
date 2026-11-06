package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
				new MismatchRenderPos(MismatchType.WRONG_STATE, first),
				new MismatchRenderPos(MismatchType.WRONG_STATE, second)
		));
		List<BlockPos> blockPositions = new ArrayList<>(List.of(
				new BlockPos(0, 64, 0),
				first,
				second
		));

		VerifierPositionOverlayFilter.retainPinnedPositions(
				renderPositions,
				blockPositions,
				Set.of(first, second),
				MismatchType.WRONG_STATE
		);

		check(renderPositions.size() == 2, "position filter must retain every selected render entry");
		check(blockPositions.size() == 2, "block position list must retain every selected position");
		check(renderPositions.stream().allMatch(entry -> entry.type() == MismatchType.WRONG_STATE),
				"render entries must retain the mismatch type");
		check(renderPositions.stream().map(MismatchRenderPos::pos).collect(java.util.stream.Collectors.toSet())
				.equals(Set.of(first, second)), "render entries must match the selected positions");
		check(new java.util.HashSet<>(blockPositions).equals(Set.of(first, second)),
				"block position list must match the selected positions");
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