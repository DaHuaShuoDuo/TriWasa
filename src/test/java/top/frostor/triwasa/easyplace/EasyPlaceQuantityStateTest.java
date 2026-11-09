package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class EasyPlaceQuantityStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceQuantityStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceQuantityStateTest test = new EasyPlaceQuantityStateTest();
		BlockState leafLitter = Blocks.LEAF_LITTER.defaultBlockState()
				.setValue(LeafLitterBlock.FACING, Direction.EAST)
				.setValue(SegmentableBlock.AMOUNT, 3);
		test.check(
				EasyPlaceStateCodec.encode(leafLitter, Blocks.AIR.defaultBlockState()) == 0xe,
				"leaf litter must encode facing and segment amount"
		);
		BlockState leafLitterOne = Blocks.LEAF_LITTER.defaultBlockState();
		test.check(
				EasyPlaceStateCodec.validateIncremental(
						leafLitterOne,
						SegmentableBlock.AMOUNT,
						3
				) != null,
				"leaf litter must allow incremental placement below the target amount"
		);
		test.check(
				EasyPlaceStateCodec.validateIncremental(
						leafLitter,
						SegmentableBlock.AMOUNT,
						2
				) == null,
				"leaf litter must reject placement beyond the projected amount"
		);

		test.checkSegmentableFacingRetention();
		Block[] quantityBlocks = {
				Blocks.CANDLE,
				Blocks.PINK_PETALS,
				Blocks.LEAF_LITTER,
				Blocks.SEA_PICKLE,
				Blocks.SNOW,
				Blocks.TURTLE_EGG
		};
		for(Block block : quantityBlocks)
		{
			test.checkQuantityIncrement(block);
		}

		return test.checks();
	}

	private void checkSegmentableFacingRetention()
	{
		BlockState flowerBedPlacement = Blocks.PINK_PETALS.defaultBlockState()
				.setValue(FlowerBedBlock.FACING, Direction.NORTH)
				.setValue(FlowerBedBlock.AMOUNT, 1);
		BlockState flowerBedResult = EasyPlaceStateCodec.apply(
				Blocks.PINK_PETALS,
				flowerBedPlacement,
				null,
				0x9
		);
		check(
				flowerBedResult != null
						&& flowerBedResult.getValue(FlowerBedBlock.FACING) == Direction.WEST,
				"flower bed increments must apply the projected facing"
		);

		BlockState leafLitterPlacement = Blocks.LEAF_LITTER.defaultBlockState()
				.setValue(LeafLitterBlock.FACING, Direction.NORTH)
				.setValue(SegmentableBlock.AMOUNT, 1);
		BlockState leafLitterResult = EasyPlaceStateCodec.apply(
				Blocks.LEAF_LITTER,
				leafLitterPlacement,
				null,
				0x9
		);
		check(
				leafLitterResult != null
						&& leafLitterResult.getValue(LeafLitterBlock.FACING) == Direction.WEST,
				"leaf litter increments must apply the projected facing"
		);
	}

	private void checkQuantityIncrement(Block block)
	{
		IntegerProperty property = IncrementalBlockRules.getCountProperty(block);
		check(property != null, block + " must expose a supported quantity property");
		BlockState one = block.defaultBlockState().setValue(property, 1);
		BlockState two = one.setValue(property, 2);
		check(IncrementalBlockRules.canIncrement(one, two), block + " must allow moving from one to two");
		check(!IncrementalBlockRules.canIncrement(two, one), block + " must reject a target below the current quantity");
		check(
				IncrementalBlockRules.shouldRetryAfterPlacement(one, two),
				block + " must remain retryable until the target quantity is reached"
		);
		check(
				IncrementalBlockRules.isIncrementalBlock(one),
				block + " must use incremental printer pending handling"
		);
		check(!IncrementalBlockRules.canIncrement(one, one), block + " must reject an unchanged quantity");
	}
}
