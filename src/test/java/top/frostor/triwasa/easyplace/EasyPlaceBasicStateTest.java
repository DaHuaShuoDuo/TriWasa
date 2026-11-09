package top.frostor.triwasa.easyplace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class EasyPlaceBasicStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceBasicStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceBasicStateTest test = new EasyPlaceBasicStateTest();
		BlockState topSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.TOP);
		test.check(
				EasyPlaceStateCodec.applyGeneric(topSlab, 0, null).getValue(SlabBlock.TYPE)
						== SlabType.BOTTOM,
				"a projected bottom slab must override a top placement state"
		);
		BlockState bottomSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		test.check(
				EasyPlaceStateCodec.applyGeneric(bottomSlab, 0x10, null).getValue(SlabBlock.TYPE)
						== SlabType.TOP,
				"a projected top slab must override a bottom placement state"
		);
		BlockState doubleSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
		test.check(
				EasyPlaceStateCodec.applyGeneric(doubleSlab, 0, null).getValue(SlabBlock.TYPE)
						== SlabType.DOUBLE,
				"double slabs must retain their placement state"
		);
		BlockState cake = Blocks.CAKE.defaultBlockState()
				.setValue(BlockStateProperties.BITES, 5);
		test.check(EasyPlaceStateCodec.encode(cake) == 5, "cake bites must use the low three bits");
		test.check(
				EasyPlaceStateCodec.apply(Blocks.CAKE, Blocks.CAKE.defaultBlockState(), null, 5)
						.getValue(BlockStateProperties.BITES) == 5,
				"cake bites must round trip"
		);
		BlockState upperDoor = Blocks.OAK_DOOR.defaultBlockState()
				.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
		test.check(
				(EasyPlaceStateCodec.encode(upperDoor) & 0x10) != 0,
				"door upper half must use bit four"
		);
		test.check(
				EasyPlaceStateCodec.apply(
						Blocks.OAK_DOOR,
						Blocks.OAK_DOOR.defaultBlockState(),
						null,
						EasyPlaceStateCodec.encode(upperDoor)
				).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER,
				"door upper half must round trip"
		);
		Block copperGolemBlock = Blocks.COPPER_GOLEM_STATUE.asList().getFirst();
		CopperGolemStatueBlock.Pose pose = CopperGolemStatueBlock.Pose.STAR;
		BlockState copperGolem = copperGolemBlock.defaultBlockState()
				.setValue(BlockStateProperties.COPPER_GOLEM_POSE, pose);
		test.check(
				EasyPlaceStateCodec.apply(
						copperGolemBlock,
						copperGolemBlock.defaultBlockState(),
						null,
						EasyPlaceStateCodec.encode(copperGolem)
				).getValue(BlockStateProperties.COPPER_GOLEM_POSE) == pose,
				"copper golem pose must round trip"
		);
		Block copperBulbBlock = Blocks.COPPER_BULB.asList().getFirst();
		BlockState litCopperBulb = copperBulbBlock.defaultBlockState()
				.setValue(CopperBulbBlock.LIT, true)
				.setValue(CopperBulbBlock.POWERED, true);
		test.check(
				EasyPlaceStateCodec.encode(litCopperBulb) == 0x3,
				"copper bulb state must use bits zero and one"
		);
		BlockState appliedCopperBulb = EasyPlaceStateCodec.apply(
				copperBulbBlock,
				copperBulbBlock.defaultBlockState(),
				null,
				EasyPlaceStateCodec.encode(litCopperBulb)
		);
		test.check(
				appliedCopperBulb.getValue(CopperBulbBlock.LIT)
						&& appliedCopperBulb.getValue(CopperBulbBlock.POWERED),
				"copper bulb lit and powered states must round trip"
		);

		return test.checks();
	}
}
