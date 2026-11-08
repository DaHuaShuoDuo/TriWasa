package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import top.frostor.triwasa.betterprinter.BetterPrinterGuideRules;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceProtocolTest
{
	private static int checks;

	private EasyPlaceProtocolTest()
	{
	}

	public static void main(String[] args)
	{
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		for(int rawValue = 0; rawValue <= 0x7f; rawValue++)
		{
			int extraValue = EasyPlaceExtraProtocol.toExtraValue(rawValue);
			check(EasyPlaceExtraProtocol.isExtra(extraValue), "encoded value must carry the marker");
			check(
					EasyPlaceExtraProtocol.toRawValue(extraValue) == rawValue,
					"raw protocol value must round trip"
			);
		}

		BlockPos pos = new BlockPos(-17, 70, 31);
		Vec3 encoded = EasyPlaceExtraProtocol.encodeX(pos, new Vec3(0.5, 70.5, 31.5), 0x5a);
		check(
				EasyPlaceExtraProtocol.toRawValue(EasyPlaceExtraProtocol.decode(encoded, pos)) == 0x5a,
				"encoded hit position must decode through the FCA wire layout"
		);
		Vec3 encodedCrafter = EasyPlaceExtraProtocol.encodeZ(pos, encoded, 0x1ab);
		check(
				EasyPlaceExtraProtocol.decodeZ(encodedCrafter, pos) == 0x1ab,
				"crafter slot masks must survive the hit Z encoding"
		);
		CompoundTag legacyCapability = new CompoundTag();
		check(
				EasyPlaceClientNetwork.acceptsCapability(legacyCapability),
				"legacy acknowledgements without capability fields must remain accepted"
		);
		CompoundTag currentCapability = new CompoundTag();
		currentCapability.putInt("protocol_version", 1);
		currentCapability.putBoolean("easy_place_v2_extra", true);
		check(
				EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an explicit V2-Extra capability must be accepted"
		);
		currentCapability.putBoolean("easy_place_v2_extra", false);
		check(
				!EasyPlaceClientNetwork.acceptsCapability(currentCapability),
				"an explicit capability rejection must be honored"
		);

		for(int mask = 0; mask <= 0x1ff; mask++)
		{
			int[] slots = EasyPlaceCrafterSlotLock.decodeSlots(mask);
			int rebuiltMask = 0;
			for(int slot : slots)
			{
				rebuiltMask |= 1 << slot;
			}
			check(rebuiltMask == mask, "crafter slot masks must round trip");
			check(Arrays.stream(slots).allMatch(slot -> slot >= 0 && slot < 9), "slot ids must be in range");
		}

		BlockState topSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.TOP);
		check(
				EasyPlaceStateCodec.applyGeneric(topSlab, 0, null).getValue(SlabBlock.TYPE) == SlabType.BOTTOM,
				"a projected bottom slab must override a top placement state"
		);
		BlockState bottomSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		check(
				EasyPlaceStateCodec.applyGeneric(bottomSlab, 0x10, null).getValue(SlabBlock.TYPE) == SlabType.TOP,
				"a projected top slab must override a bottom placement state"
		);
		BlockState doubleSlab = Blocks.STONE_SLAB.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
		check(
				EasyPlaceStateCodec.applyGeneric(doubleSlab, 0, null).getValue(SlabBlock.TYPE) == SlabType.DOUBLE,
				"double slabs must retain their placement state"
		);
		BlockState cake = Blocks.CAKE.defaultBlockState()
				.setValue(BlockStateProperties.BITES, 5);
		check(EasyPlaceStateCodec.encode(cake) == 5, "cake bites must use the low three bits");
		check(
				EasyPlaceStateCodec.apply(Blocks.CAKE, Blocks.CAKE.defaultBlockState(), null, 5)
						.getValue(BlockStateProperties.BITES) == 5,
				"cake bites must round trip"
		);
		BlockState upperDoor = Blocks.OAK_DOOR.defaultBlockState()
				.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
		check(
				(EasyPlaceStateCodec.encode(upperDoor) & 0x10) != 0,
				"door upper half must use bit four"
		);
		check(
				EasyPlaceStateCodec.apply(Blocks.OAK_DOOR, Blocks.OAK_DOOR.defaultBlockState(), null,
						EasyPlaceStateCodec.encode(upperDoor)).getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER,
				"door upper half must round trip"
		);
		Block copperGolemBlock = Blocks.COPPER_GOLEM_STATUE.asList().getFirst();
		CopperGolemStatueBlock.Pose pose = CopperGolemStatueBlock.Pose.STAR;
		BlockState copperGolem = copperGolemBlock.defaultBlockState()
				.setValue(BlockStateProperties.COPPER_GOLEM_POSE, pose);
		check(
				EasyPlaceStateCodec.apply(copperGolemBlock, copperGolemBlock.defaultBlockState(), null,
						EasyPlaceStateCodec.encode(copperGolem))
						.getValue(BlockStateProperties.COPPER_GOLEM_POSE) == pose,
				"copper golem pose must round trip"
		);
		Block copperBulbBlock = Blocks.COPPER_BULB.asList().getFirst();
		BlockState litCopperBulb = copperBulbBlock.defaultBlockState()
				.setValue(CopperBulbBlock.LIT, true)
				.setValue(CopperBulbBlock.POWERED, true);
		check(EasyPlaceStateCodec.encode(litCopperBulb) == 0x3, "copper bulb state must use bits zero and one");
		BlockState appliedCopperBulb = EasyPlaceStateCodec.apply(
				copperBulbBlock,
				copperBulbBlock.defaultBlockState(),
				null,
				EasyPlaceStateCodec.encode(litCopperBulb)
		);
		check(
				appliedCopperBulb.getValue(CopperBulbBlock.LIT)
						&& appliedCopperBulb.getValue(CopperBulbBlock.POWERED),
				"copper bulb lit and powered states must round trip"
		);
		BlockState leftChest = Blocks.CHEST.defaultBlockState()
				.setValue(ChestBlock.FACING, Direction.NORTH)
				.setValue(ChestBlock.TYPE, ChestType.LEFT);
		int chestProtocol = EasyPlaceStateCodec.encode(leftChest);
		check((chestProtocol & 0x40) != 0, "chest type must carry an explicit marker");
		check(
				((chestProtocol >>> 4) & 0x3) == 1,
				"chest LEFT type must use protocol value one"
		);

		Direction[] multifaceDirections = {
				Direction.DOWN, Direction.UP, Direction.NORTH,
				Direction.SOUTH, Direction.WEST, Direction.EAST
		};
		for(int index = 0; index < multifaceDirections.length; index++)
		{
			BlockState glowLichen = Blocks.GLOW_LICHEN.defaultBlockState()
					.setValue(MultifaceBlock.getFaceProperty(multifaceDirections[index]), true);
			check(
					EasyPlaceStateCodec.encode(glowLichen, Blocks.AIR.defaultBlockState()) == index,
					"glow lichen " + multifaceDirections[index]
							+ " must use the canonical six-face index"
			);
		}
		BlockState glowLichenUp = Blocks.GLOW_LICHEN.defaultBlockState()
				.setValue(BlockStateProperties.UP, true);
		check(
				EasyPlaceStateCodec.encode(glowLichenUp, glowLichenUp) == -1,
				"an already attached glow lichen face must not be sent again"
		);
		BlockState multifaceEmpty = Blocks.SCULK_VEIN.defaultBlockState();
		BlockState multifaceUpEast = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true)
				.setValue(MultifaceBlock.getFaceProperty(Direction.EAST), true);
		check(
				IncrementalBlockRules.findNextFace(multifaceEmpty, multifaceUpEast) == Direction.UP,
				"multiface increments must start with the first missing face"
		);
		BlockState multifaceUp = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true);
		check(
				IncrementalBlockRules.canIncrement(multifaceUp, multifaceUpEast),
				"multiface blocks must allow adding another required face"
		);
		check(
				IncrementalBlockRules.findNextFace(multifaceUp, multifaceUpEast) == Direction.EAST,
				"multiface increments must skip already attached faces"
		);
		check(
				EasyPlaceStateCodec.encode(multifaceUpEast, multifaceUp)
						== IncrementalBlockRules.multifaceDirectionIndex(Direction.EAST),
				"multiface protocol must encode the next missing face"
		);
		BlockState multifaceWrongNorth = multifaceEmpty
				.setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true);
		check(
				IncrementalBlockRules.findNextFace(multifaceWrongNorth, multifaceUp) == null
						&& !IncrementalBlockRules.canIncrement(multifaceWrongNorth, multifaceUp),
				"multiface increments must reject states with extra faces"
		);
		check(
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
			check(
					EasyPlaceStateCodec.encode(vine, Blocks.AIR.defaultBlockState()) == index,
					"vine " + vineDirections[index] + " must use the canonical five-face index"
			);
		}
		BlockState vineNorth = Blocks.VINE.defaultBlockState()
				.setValue(VineBlock.NORTH, true);
		check(
				VineBlockRules.hasFace(vineNorth, Direction.NORTH)
						&& !VineBlockRules.hasFace(vineNorth, Direction.SOUTH),
				"vine face lookup must follow the stored face properties"
		);
		check(
				!VineBlockRules.canAttach(
						net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
						pos,
						Direction.NORTH
				),
				"printer vine guidance must reject unsupported horizontal faces"
		);
		check(
				!VineBlockRules.canAttach(
						net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
						pos,
						Direction.DOWN
				),
				"printer vine guidance must reject the down face"
		);
		BlockState vineNorthEast = vineNorth.setValue(VineBlock.EAST, true);
		check(
				IncrementalBlockRules.canIncrement(vineNorth, vineNorthEast)
						&& VineBlockRules.findNextFace(vineNorth, vineNorthEast) == Direction.EAST,
				"vine easy place must allow adding another required face"
		);
		check(
				!IncrementalBlockRules.canIncrement(vineNorthEast, vineNorth),
				"vine easy place must reject states with extra attached faces"
		);
		check(
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
		check(
				MultiFaceBlockRules.findNextFace(vineCurrentEastUp, vineRequiredAll)
						== Direction.NORTH
						&& MultiFaceBlockRules.findAttachableMissingFace(
						vineCurrentEastUp,
						vineRequiredAll,
						net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
						pos
				) == null,
				"protocol selection must distinguish missing faces from attachable faces"
		);
		check(
				EasyPlaceStateCodec.encode(vineNorth, vineNorth) == -1,
				"an already attached vine face must not be sent again"
		);
		BlockState multipleVineFaces = vineNorthEast.setValue(VineBlock.UP, true);
		check(
				IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						multipleVineFaces
				),
				"multi-face vine placement must remain retryable after its first face"
		);
		check(
				IncrementalBlockRules.shouldRetryAfterPlacement(vineNorthEast, multipleVineFaces),
				"partially attached multi-face vines must remain retryable"
		);
		check(
				!IncrementalBlockRules.shouldRetryAfterPlacement(multipleVineFaces, multipleVineFaces),
				"complete multi-face vine states must not be retried as incremental placements"
		);
		check(
				!IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						vineNorth
				),
				"single-face vines must complete in one placement"
		);
		check(
				IncrementalBlockRules.shouldRetryAfterPlacement(
						Blocks.AIR.defaultBlockState(),
						multifaceUpEast
				),
				"all multi-face blocks must remain retryable after their first face"
		);
		check(
				IncrementalBlockRules.isMultiFaceBlock(multipleVineFaces)
						&& IncrementalBlockRules.isMultiFaceBlock(multifaceUpEast)
						&& !IncrementalBlockRules.isMultiFaceBlock(Blocks.CANDLE.defaultBlockState()),
				"incremental pending handling must apply only to vine and multiface blocks"
		);
		check(
				!MultiFaceBlockRules.hasFace(vineNorth, Direction.DOWN)
						&& MultiFaceBlockRules.hasFace(vineNorth, Direction.NORTH),
				"vine six-direction logic must leave DOWN permanently unavailable"
		);
		check(
				MultiFaceBlockRules.findNextFace(vineNorth, multipleVineFaces) == Direction.UP
						&& MultiFaceBlockRules.findNextFace(multifaceUp, multifaceUpEast)
						== Direction.EAST,
				"vine and multiface blocks must share missing-face selection"
		);
		check(
				MultiFaceBlockRules.findAttachableMissingFace(
						null,
						multipleVineFaces,
						net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
						pos
				) == null,
				"shared multiface guidance must reject unsupported vine faces"
		);
		BlockState singleVineFace = MultiFaceBlockRules.applyVineFace(
				Blocks.VINE,
				Blocks.AIR.defaultBlockState(),
				Direction.UP
		);
		check(
				singleVineFace != null && VineBlockRules.hasFace(singleVineFace, Direction.UP),
				"incremental vine state must start from vanilla defaults when the projected state is pre-expanded"
		);
		BlockState vineSouth = Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true);
		BlockState vineSouthUp = MultiFaceBlockRules.applyVineFace(Blocks.VINE, vineSouth, Direction.UP);
		check(
				vineSouthUp != null
						&& VineBlockRules.hasFace(vineSouthUp, Direction.SOUTH)
						&& VineBlockRules.hasFace(vineSouthUp, Direction.UP),
				"incremental vine state must preserve the current face set"
		);
		check(
				EasyPlaceStateCodec.encode(Blocks.VINE.defaultBlockState(), Blocks.AIR.defaultBlockState()) == -1,
				"faceless projected vines must not fall back to the UP face"
		);

		BlockState leafLitter = Blocks.LEAF_LITTER.defaultBlockState()
				.setValue(LeafLitterBlock.FACING, Direction.EAST)
				.setValue(SegmentableBlock.AMOUNT, 3);
		check(
				EasyPlaceStateCodec.encode(leafLitter, Blocks.AIR.defaultBlockState()) == 0xe,
				"leaf litter must encode facing and segment amount"
		);
		BlockState leafLitterOne = Blocks.LEAF_LITTER.defaultBlockState();
		check(
				EasyPlaceStateCodec.validateIncremental(
						leafLitterOne,
						SegmentableBlock.AMOUNT,
						3
				) != null,
				"leaf litter must allow incremental placement below the target amount"
		);
		check(
				EasyPlaceStateCodec.validateIncremental(
						leafLitter,
						SegmentableBlock.AMOUNT,
						2
				) == null,
				"leaf litter must reject placement beyond the projected amount"
		);

		BlockState wallHangingSign = Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
		int wallHangingSignValue = EasyPlaceStateCodec.encode(wallHangingSign, null);
		BlockState wallHangingSignResult = EasyPlaceStateCodec.applyStandingOrWall(
				Blocks.OAK_HANGING_SIGN,
				Blocks.OAK_WALL_HANGING_SIGN,
				wallHangingSign,
				null,
				wallHangingSignValue
		);
		check(
				wallHangingSignValue == 0x7
						&& wallHangingSignResult.getValue(BlockStateProperties.HORIZONTAL_FACING)
						== Direction.EAST,
				"wall hanging signs must encode and restore facing without attachment support"
		);
		check(
				EasyPlaceStateCodec.apply(Blocks.OAK_WALL_HANGING_SIGN, wallHangingSign, null, 0x10)
						.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST,
				"wall hanging signs must not receive ceiling-only properties"
		);
		BlockState attachedCeilingHangingSign = Blocks.OAK_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.ROTATION_16, 11)
				.setValue(BlockStateProperties.ATTACHED, true);
		int attachedCeilingValue = EasyPlaceStateCodec.encode(attachedCeilingHangingSign, null);
		BlockState attachedCeilingResult = EasyPlaceStateCodec.applyStandingOrWall(
				Blocks.OAK_HANGING_SIGN,
				Blocks.OAK_WALL_HANGING_SIGN,
				attachedCeilingHangingSign,
				null,
				attachedCeilingValue
		);
		check(
				attachedCeilingValue == 0x5c
						&& attachedCeilingResult.getValue(BlockStateProperties.ROTATION_16) == 11
						&& attachedCeilingResult.getValue(BlockStateProperties.ATTACHED),
				"attached ceiling hanging signs must encode and restore rotation and attachment"
		);
		BlockState ceilingHangingSign = Blocks.OAK_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.ROTATION_16, 2)
				.setValue(BlockStateProperties.ATTACHED, false);
		BlockState ceilingResult = EasyPlaceStateCodec.apply(
				Blocks.OAK_HANGING_SIGN,
				ceilingHangingSign,
				null,
				0x13
		);
		check(
				ceilingResult.getValue(BlockStateProperties.ROTATION_16) == 3
						&& ceilingResult.getValue(BlockStateProperties.ATTACHED),
				"ceiling hanging signs must retain rotation and attachment decoding"
		);
		HangingSignRules.ClickTarget wallClickTarget =
				HangingSignRules.getForcedWallClickTarget(pos, wallHangingSign);
		check(
				wallClickTarget != null
						&& wallClickTarget.pos().equals(pos)
						&& wallClickTarget.side().getAxis()
						== wallHangingSign.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
				"wall hanging signs must click the target position from the same horizontal axis"
		);

		checkQuantityIncrement(Blocks.CANDLE);
		checkQuantityIncrement(Blocks.PINK_PETALS);
		checkQuantityIncrement(Blocks.LEAF_LITTER);
		checkQuantityIncrement(Blocks.SEA_PICKLE);
		checkQuantityIncrement(Blocks.SNOW);
		checkQuantityIncrement(Blocks.TURTLE_EGG);
		checkForcedPlacementScope();
		checkPrinterMultifaceGuideMatching();

		System.out.println("EasyPlaceProtocolTest passed " + checks + " checks");
	}

	private static void checkPrinterMultifaceGuideMatching()
	{
		boolean easyPlace = TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
		boolean printer = TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
		try
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(true);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(true);
			Class<?>[] vineGuideBlocks = {net.minecraft.world.level.block.VineBlock.class,
					net.minecraft.world.level.block.GlowLichenBlock.class};
			Class<?>[] flowerBedGuideBlocks = {
					net.minecraft.world.level.block.FlowerBedBlock.class
			};
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.GLOW_LICHEN,
							vineGuideBlocks
					),
					"glow lichen must retain printer face guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.SCULK_VEIN,
							vineGuideBlocks
					),
					"sculk veins must reuse printer multiface guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.RESIN_CLUMP,
							vineGuideBlocks
					),
					"resin clumps must reuse printer multiface guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchLeafLitterGuide(
							Blocks.LEAF_LITTER,
							flowerBedGuideBlocks
					),
					"leaf litter must reuse the printer flower-bed quantity guide"
			);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(false);
			check(
					!BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.SCULK_VEIN,
							vineGuideBlocks
					) && !BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.RESIN_CLUMP,
							vineGuideBlocks
					),
					"printer multiface extension must be config-gated"
			);
			check(
					!BetterPrinterGuideRules.shouldMatchLeafLitterGuide(
							Blocks.LEAF_LITTER,
							flowerBedGuideBlocks
					),
					"printer leaf litter extension must be config-gated"
			);
			check(
					!BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.CANDLE,
							vineGuideBlocks
					),
					"non-multiface blocks must not reuse printer multiface guidance"
			);
		}
		finally
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(easyPlace);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(printer);
		}
	}

	private static void checkForcedPlacementScope()
	{
		EasyPlaceUtils.setHandling(false);
		check(
				!CandleRules.shouldAllowUnsupportedPlacement(Blocks.CANDLE.defaultBlockState()),
				"normal candle placement must retain vanilla support checks"
		);
		check(
				!HangingSignRules.shouldAllowUnsupportedWallPlacement(
						Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
				),
				"normal wall hanging sign placement must retain vanilla support checks"
		);
		check(
				!HangingSignRules.shouldSuppressEasyPlaceChaining(
						Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState(),
						Blocks.OAK_HANGING_SIGN.asItem()
				),
				"normal wall hanging sign interactions must retain vanilla chaining"
		);

		EasyPlaceUtils.setHandling(true);
		try
		{
			check(
					CandleRules.shouldAllowUnsupportedPlacement(Blocks.CANDLE.defaultBlockState()),
					"easy-place and printer candle interactions must allow unsupported placement"
			);
			check(
					HangingSignRules.shouldAllowUnsupportedWallPlacement(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
					),
					"easy-place and printer wall sign interactions must allow unsupported placement"
			);
			check(
					HangingSignRules.shouldSuppressEasyPlaceChaining(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState(),
							Blocks.OAK_HANGING_SIGN.asItem()
					),
					"easy-place wall sign interactions must not chain another sign above"
			);
		}
		finally
		{
			EasyPlaceUtils.setHandling(false);
		}
	}

	private static void checkQuantityIncrement(Block block)
	{
		IntegerProperty property = IncrementalBlockRules.getCountProperty(block);
		check(property != null, block + " must expose a supported quantity property");
		BlockState one = block.defaultBlockState().setValue(property, 1);
		BlockState two = one.setValue(property, 2);
		check(
				IncrementalBlockRules.canIncrement(one, two),
				block + " must allow moving from one to two"
		);
		check(
				!IncrementalBlockRules.canIncrement(two, one),
				block + " must reject a target below the current quantity"
		);
		check(
				IncrementalBlockRules.shouldRetryAfterPlacement(one, two),
				block + " must remain retryable until the target quantity is reached"
		);
		check(
				IncrementalBlockRules.isIncrementalBlock(one),
				block + " must use incremental printer pending handling"
		);
		check(
				!IncrementalBlockRules.canIncrement(one, one),
				block + " must reject an unchanged quantity"
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
