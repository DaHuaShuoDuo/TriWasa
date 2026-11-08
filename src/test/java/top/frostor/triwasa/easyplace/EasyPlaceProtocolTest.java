package top.frostor.triwasa.easyplace;

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
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

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
		BlockState vineNorthEast = vineNorth.setValue(VineBlock.EAST, true);
		check(
				EasyPlaceStateCodec.encode(vineNorthEast, vineNorth) == 4,
				"vine encoding must select the next missing projected face"
		);
		check(
				EasyPlaceStateCodec.encode(vineNorth, vineNorth) == -1,
				"an already attached vine face must not be sent again"
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

		System.out.println("EasyPlaceProtocolTest passed " + checks + " checks");
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
