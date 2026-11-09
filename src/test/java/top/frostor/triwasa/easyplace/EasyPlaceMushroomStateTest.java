package top.frostor.triwasa.easyplace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class EasyPlaceMushroomStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceMushroomStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceMushroomStateTest test = new EasyPlaceMushroomStateTest();
		Block[] hugeMushroomBlocks = {Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK};
		for(Block block : hugeMushroomBlocks)
		{
			test.check(block instanceof HugeMushroomBlock, block + " must use HugeMushroomBlock");
			for(int mask = 0; mask < 64; mask++)
			{
				BlockState expected = block.defaultBlockState()
						.setValue(BlockStateProperties.NORTH, (mask & 0x01) != 0)
						.setValue(BlockStateProperties.EAST, (mask & 0x02) != 0)
						.setValue(BlockStateProperties.SOUTH, (mask & 0x04) != 0)
						.setValue(BlockStateProperties.WEST, (mask & 0x08) != 0)
						.setValue(BlockStateProperties.UP, (mask & 0x10) != 0)
						.setValue(BlockStateProperties.DOWN, (mask & 0x20) != 0);
				test.check(EasyPlaceStateCodec.encode(expected) == mask, "huge mushroom masks must use all six bits");
				BlockState applied = EasyPlaceStateCodec.apply(
						block,
						block.defaultBlockState(),
						null,
						mask
				);
				test.check(applied != null, "huge mushroom protocol state must apply");
				test.check(
						applied.getValue(BlockStateProperties.NORTH)
								== expected.getValue(BlockStateProperties.NORTH)
								&& applied.getValue(BlockStateProperties.EAST)
								== expected.getValue(BlockStateProperties.EAST)
								&& applied.getValue(BlockStateProperties.SOUTH)
								== expected.getValue(BlockStateProperties.SOUTH)
								&& applied.getValue(BlockStateProperties.WEST)
								== expected.getValue(BlockStateProperties.WEST)
								&& applied.getValue(BlockStateProperties.UP)
								== expected.getValue(BlockStateProperties.UP)
								&& applied.getValue(BlockStateProperties.DOWN)
								== expected.getValue(BlockStateProperties.DOWN),
						"huge mushroom six-face state must round trip"
				);
			}
		}

		BlockState smallMushroom = Blocks.BROWN_MUSHROOM.defaultBlockState();
		test.check(
				EasyPlaceStateCodec.encode(smallMushroom) == 0,
				"small mushrooms must not use huge mushroom masks"
		);
		test.check(
				EasyPlaceStateCodec.apply(Blocks.BROWN_MUSHROOM, smallMushroom, null, 0)
						== smallMushroom,
				"small mushrooms must retain their placement state"
		);
		return test.checks();
	}
}
