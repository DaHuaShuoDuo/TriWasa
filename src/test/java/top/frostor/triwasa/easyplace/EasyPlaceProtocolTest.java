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
import net.minecraft.world.level.block.SlabBlock;
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
