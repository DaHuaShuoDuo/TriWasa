package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class EasyPlaceMultifaceStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceMultifaceStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceMultifaceStateTest test = new EasyPlaceMultifaceStateTest();
		BlockPos pos = new BlockPos(-17, 70, 31);
		Direction[] multifaceDirections = {
				Direction.DOWN, Direction.UP, Direction.NORTH,
				Direction.SOUTH, Direction.WEST, Direction.EAST
		};
		for(int index = 0; index < multifaceDirections.length; index++)
		{
			BlockState glowLichen = Blocks.GLOW_LICHEN.defaultBlockState()
					.setValue(MultifaceBlock.getFaceProperty(multifaceDirections[index]), true);
			test.check(
					EasyPlaceStateCodec.encode(glowLichen, Blocks.AIR.defaultBlockState()) == index,
					"glow lichen " + multifaceDirections[index]
							+ " must use the canonical six-face index"
			);
		}
		BlockState glowLichenUp = Blocks.GLOW_LICHEN.defaultBlockState()
				.setValue(BlockStateProperties.UP, true);
		test.check(
				EasyPlaceStateCodec.encode(glowLichenUp, glowLichenUp) == -1,
				"an already attached glow lichen face must not be sent again"
		);
		BlockState multifaceEmpty = Blocks.SCULK_VEIN.defaultBlockState();
		BlockState multifaceUpEast = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true)
				.setValue(MultifaceBlock.getFaceProperty(Direction.EAST), true);
		test.check(
				IncrementalBlockRules.findNextFace(multifaceEmpty, multifaceUpEast) == Direction.UP,
				"multiface increments must start with the first missing face"
		);
		BlockState multifaceUp = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true);
		test.check(
				IncrementalBlockRules.canIncrement(multifaceUp, multifaceUpEast),
				"multiface blocks must allow adding another required face"
		);
		test.check(
				IncrementalBlockRules.findNextFace(multifaceUp, multifaceUpEast) == Direction.EAST,
				"multiface increments must skip already attached faces"
		);
		test.check(
				EasyPlaceStateCodec.encode(multifaceUpEast, multifaceUp)
						== IncrementalBlockRules.multifaceDirectionIndex(Direction.EAST),
				"multiface protocol must encode the next missing face"
		);
		BlockState multifaceWrongNorth = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true);
		test.check(
				IncrementalBlockRules.findNextFace(multifaceWrongNorth, multifaceUp) == null
						&& !IncrementalBlockRules.canIncrement(multifaceWrongNorth, multifaceUp),
				"multiface increments must reject states with extra faces"
		);
		test.check(
				IncrementalBlockRules.canIncrement(
						Blocks.RESIN_CLUMP.defaultBlockState(),
						Blocks.RESIN_CLUMP.defaultBlockState()
								.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true)
				),
				"all multiface block variants must share incremental placement rules"
		);

		Direction[] vineDirections = {
				Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
		};
		for(int index = 0; index < vineDirections.length; index++)
		{
			BlockState vine = Blocks.VINE.defaultBlockState()
					.setValue(VineBlock.getPropertyForFace(vineDirections[index]), true);
			test.check(
					EasyPlaceStateCodec.encode(vine, Blocks.AIR.defaultBlockState()) == index,
					"vine " + vineDirections[index] + " must use the canonical five-face index"
			);
		}
		BlockState vineNorth = Blocks.VINE.defaultBlockState()
				.setValue(VineBlock.NORTH, true);
		test.check(
				VineBlockRules.hasFace(vineNorth, Direction.NORTH)
						&& !VineBlockRules.hasFace(vineNorth, Direction.SOUTH),
				"vine face lookup must follow the stored face properties"
		);
		test.check(
				!VineBlockRules.canAttach(EmptyBlockGetter.INSTANCE, pos, Direction.NORTH),
				"printer vine guidance must reject unsupported horizontal faces"
		);
		test.check(
				!VineBlockRules.canAttach(EmptyBlockGetter.INSTANCE, pos, Direction.DOWN),
				"printer vine guidance must reject the down face"
		);
		BlockState vineNorthEast = vineNorth.setValue(VineBlock.EAST, true);
		test.check(
				IncrementalBlockRules.canIncrement(vineNorth, vineNorthEast)
						&& VineBlockRules.findNextFace(vineNorth, vineNorthEast) == Direction.EAST,
				"vine easy place must allow adding another required face"
		);
		test.check(
				!IncrementalBlockRules.canIncrement(vineNorthEast, vineNorth),
				"vine easy place must reject states with extra attached faces"
		);
		test.check(
				EasyPlaceStateCodec.encode(vineNorthEast, vineNorth) == 4,
				"vine encoding must select the next missing projected face"
		);
		BlockState vineCurrentEastUp = Blocks.VINE.defaultBlockState()
				.setValue(VineBlock.EAST, true)
				.setValue(VineBlock.UP, true);
		BlockState vineRequiredAll = Blocks.VINE.defaultBlockState()
				.setValue(VineBlock.UP, true)
				.setValue(VineBlock.NORTH, true)
				.setValue(VineBlock.SOUTH, true)
				.setValue(VineBlock.WEST, true)
				.setValue(VineBlock.EAST, true);
		test.check(
				MultiFaceBlockRules.findNextFace(vineCurrentEastUp, vineRequiredAll)
						== Direction.NORTH
						&& MultiFaceBlockRules.findAttachableMissingFace(
						vineCurrentEastUp,
						vineRequiredAll,
						EmptyBlockGetter.INSTANCE,
						pos
				) == null,
				"protocol selection must distinguish missing faces from attachable faces"
		);
		test.check(
				EasyPlaceStateCodec.encode(vineNorth, vineNorth) == -1,
				"an already attached vine face must not be sent again"
		);
		BlockState multipleVineFaces = vineNorthEast.setValue(VineBlock.UP, true);
		test.check(
				IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						multipleVineFaces
				),
				"multi-face vine placement must remain retryable after its first face"
		);
		test.check(
				IncrementalBlockRules.shouldRetryAfterPlacement(vineNorthEast, multipleVineFaces),
				"partially attached multi-face vines must remain retryable"
		);
		test.check(
				!IncrementalBlockRules.shouldRetryAfterPlacement(multipleVineFaces, multipleVineFaces),
				"complete multi-face vine states must not be retried as incremental placements"
		);
		test.check(
				!IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						vineNorth
				),
				"single-face vines must complete in one placement"
		);
		test.check(
				IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						multifaceUpEast
				),
				"all multi-face blocks must remain retryable after their first face"
		);
		test.check(
				IncrementalBlockRules.isMultiFaceBlock(multipleVineFaces)
						&& IncrementalBlockRules.isMultiFaceBlock(multifaceUpEast)
						&& !IncrementalBlockRules.isMultiFaceBlock(Blocks.CANDLE.defaultBlockState()),
				"incremental pending handling must apply only to vine and multiface blocks"
		);
		test.check(
				!MultiFaceBlockRules.hasFace(vineNorth, Direction.DOWN)
						&& MultiFaceBlockRules.hasFace(vineNorth, Direction.NORTH),
				"vine six-direction logic must leave DOWN permanently unavailable"
		);
		test.check(
				MultiFaceBlockRules.findNextFace(vineNorth, multipleVineFaces) == Direction.UP
						&& MultiFaceBlockRules.findNextFace(multifaceUp, multifaceUpEast)
						== Direction.EAST,
				"vine and multiface blocks must share missing-face selection"
		);
		test.check(
				MultiFaceBlockRules.findAttachableMissingFace(
						null,
						multipleVineFaces,
						EmptyBlockGetter.INSTANCE,
						pos
				) == null,
				"shared multiface guidance must reject unsupported vine faces"
		);
		BlockState singleVineFace = MultiFaceBlockRules.applyVineFace(
				Blocks.VINE,
				Blocks.AIR.defaultBlockState(),
				Direction.UP
		);
		test.check(
				singleVineFace != null && VineBlockRules.hasFace(singleVineFace, Direction.UP),
				"incremental vine state must start from vanilla defaults when the projected state is pre-expanded"
		);
		BlockState vineSouth = Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true);
		BlockState vineSouthUp = MultiFaceBlockRules.applyVineFace(Blocks.VINE, vineSouth, Direction.UP);
		test.check(
				vineSouthUp != null
						&& VineBlockRules.hasFace(vineSouthUp, Direction.SOUTH)
						&& VineBlockRules.hasFace(vineSouthUp, Direction.UP),
				"incremental vine state must preserve the current face set"
		);
		test.check(
				EasyPlaceStateCodec.encode(
						Blocks.VINE.defaultBlockState(),
						Blocks.AIR.defaultBlockState()
				) == -1,
				"faceless projected vines must not fall back to the UP face"
		);

		return test.checks();
	}
}
