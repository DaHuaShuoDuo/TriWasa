package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.BlockHitResult;

public final class EasyPlaceSpecialStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceSpecialStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceSpecialStateTest test = new EasyPlaceSpecialStateTest();
		Block[] blocks = {
				Blocks.BELL,
				Blocks.CAMPFIRE,
				Blocks.CAKE,
				Blocks.COPPER_BULB.asList().getFirst(),
				Blocks.COPPER_GOLEM_STATUE.asList().getFirst(),
				Blocks.CRAFTER,
				Blocks.DAYLIGHT_DETECTOR,
				Blocks.LANTERN,
				Blocks.REDSTONE_WALL_TORCH,
				Blocks.REDSTONE_TORCH,
				Blocks.REDSTONE_WIRE
		};
		for(Block block : blocks)
		{
			BlockState state = block.defaultBlockState();
			int encoded = EasyPlaceStateCodec.encode(state, Blocks.AIR.defaultBlockState());
			test.check(encoded >= 0, block + " must be owned by a client state adapter");
			test.check(
					EasyPlaceStateCodec.apply(block, state, null, encoded) != null,
					block + " must decode through its client state adapter"
			);
		}

		BlockState bell = Blocks.BELL.defaultBlockState()
				.setValue(BellBlock.ATTACHMENT, BellAttachType.values()[1])
				.setValue(BellBlock.FACING, Direction.NORTH);
		test.check(
				EasyPlaceStateCodec.encode(bell, Blocks.AIR.defaultBlockState()) == 0x12,
				"bells must encode facing and attachment"
		);
		BlockState wallBell = Blocks.BELL.defaultBlockState()
				.setValue(BellBlock.ATTACHMENT, BellAttachType.SINGLE_WALL)
				.setValue(BellBlock.FACING, Direction.NORTH);
		BlockPos bellPos = new BlockPos(4, 64, 4);
		BellRules.BellPlacementPlan bellPlan = BellRules.getPlacementPlan(bellPos, wallBell);
		test.check(
				bellPlan != null
						&& bellPlan.targetPos().equals(bellPos)
						&& bellPlan.supportPos().equals(bellPos.north())
						&& bellPlan.side() == Direction.SOUTH,
				"wall bells must click the solid support on the bell's facing side"
		);
		if(bellPlan != null)
		{
			BlockHitResult correctHit = new BlockHitResult(
					bellPlan.hitVec(),
					bellPlan.side(),
					bellPlan.supportPos(),
					false
			);
			BellRules.activatePlan(bellPlan);
			test.check(
					BellRules.takeClickPlan(correctHit) == null,
					"wall bell plans must be consumed when the click side is already correct"
			);

			BlockHitResult flippedHit = new BlockHitResult(
					bellPlan.hitVec(),
					bellPlan.side().getOpposite(),
					bellPlan.supportPos(),
					false
			);
			BellRules.activatePlan(bellPlan);
			test.check(
					bellPlan.equals(BellRules.takeClickPlan(flippedHit)),
					"wall bell plans must restore a flipped click side"
			);
			test.check(
					BellRules.takeClickPlan(flippedHit) == null,
					"wall bell plans must be consumed after restoring the click side"
			);
		}

		BlockState campfire = Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
		test.check(
				EasyPlaceStateCodec.apply(
						Blocks.CAMPFIRE,
						Blocks.CAMPFIRE.defaultBlockState(),
						null,
						EasyPlaceStateCodec.encode(campfire, Blocks.AIR.defaultBlockState())
				).getValue(CampfireBlock.LIT),
				"campfires must round trip lit state"
		);
		BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
		test.check(
				EasyPlaceStateCodec.apply(
						Blocks.LANTERN,
						Blocks.LANTERN.defaultBlockState(),
						null,
						EasyPlaceStateCodec.encode(lantern, Blocks.AIR.defaultBlockState())
				).getValue(LanternBlock.HANGING),
				"lanterns must round trip hanging state"
		);
		return test.checks();
	}
}
