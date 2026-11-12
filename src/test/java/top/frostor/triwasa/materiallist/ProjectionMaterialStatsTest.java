package top.frostor.triwasa.materiallist;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Verifies the per-block item factor used by the projection material stats fix.
 *
 * <p>Only blocks whose state accumulates one item per placement are taken over; shapes that are
 * derived from neighbours (moss carpets, walls) and blocks Litematica already handles (snow
 * layers) must keep returning {@code 0} so the upstream value is left alone.</p>
 */
public final class ProjectionMaterialStatsTest
{
	private static int checks;

	private ProjectionMaterialStatsTest()
	{
	}

	public static void run()
	{
		check(
				ProjectionMaterialStats.itemsPerBlock(withAmount(Blocks.PINK_PETALS, 4)) == 4,
				"a four-segment pink petal block must consume four items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(withAmount(Blocks.WILDFLOWERS, 3)) == 3,
				"a three-segment wildflower block must consume three items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(withAmount(Blocks.LEAF_LITTER, 2)) == 2,
				"a two-segment leaf litter block must consume two items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.VINE.defaultBlockState()
						.setValue(VineBlock.UP, true)
						.setValue(VineBlock.NORTH, true)
						.setValue(VineBlock.SOUTH, true)) == 3,
				"a three-face vine block must consume three items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.VINE.defaultBlockState()
						.setValue(VineBlock.UP, true)
						.setValue(VineBlock.NORTH, true)
						.setValue(VineBlock.SOUTH, true)
						.setValue(VineBlock.WEST, true)
						.setValue(VineBlock.EAST, true)) == 5,
				"a five-face vine block must consume five items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.RESIN_CLUMP.defaultBlockState()
						.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true)
						.setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true)
						.setValue(MultifaceBlock.getFaceProperty(Direction.WEST), true)) == 3,
				"a three-face resin clump must consume three items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.GLOW_LICHEN.defaultBlockState()
						.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true)
						.setValue(MultifaceBlock.getFaceProperty(Direction.EAST), true)) == 2,
				"a two-face glow lichen must consume two items"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.STONE.defaultBlockState()) == 0,
				"plain blocks must not be taken over"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.PALE_MOSS_CARPET.defaultBlockState()) == 0,
				"moss carpet sides are derived from neighbours and must not be counted"
		);
		check(
				ProjectionMaterialStats.itemsPerBlock(Blocks.SNOW.defaultBlockState()
						.setValue(SnowLayerBlock.LAYERS, 8)) == 0,
				"snow layers are already handled by Litematica and must not be taken over"
		);

		System.out.println("ProjectionMaterialStatsTest passed " + checks + " checks");
	}

	private static BlockState withAmount(Block block, int amount)
	{
		return block.defaultBlockState().setValue(
				((SegmentableBlock) block).getSegmentAmountProperty(),
				amount
		);
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
