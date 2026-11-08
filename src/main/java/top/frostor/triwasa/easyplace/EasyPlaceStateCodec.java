package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.PlacementHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.mixin.StandingAndWallBlockItemAccessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACHED;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.BITES;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.COPPER_GOLEM_POSE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST_REDSTONE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HALF;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH_REDSTONE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ORIENTATION;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SLAB_TYPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH_REDSTONE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.UP;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST_REDSTONE;

/**
 * Client-side V2-Extra state mapping, adapted from EarlyCompat and FCA (CC0-1.0).
 */
public final class EasyPlaceStateCodec
{
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};
	private EasyPlaceStateCodec()
	{
	}

	public static int encode(BlockState state)
	{
		return encode(state, null, null, null);
	}

	static int encode(BlockState state, @Nullable BlockState currentState)
	{
		return encode(state, currentState, null, null);
	}

	public static int encode(
			BlockState state,
			@Nullable BlockState currentState,
			@Nullable BlockGetter level,
			@Nullable BlockPos pos
	)
	{
		Block block = state.getBlock();
		if(MultiFaceBlockRules.isMultiFaceBlock(block))
		{
			Direction direction = level != null && pos != null
					? MultiFaceBlockRules.findAttachableMissingFace(
							currentState,
							state,
							level,
							pos
					)
					: MultiFaceBlockRules.findNextFace(currentState, state);
			return direction == null ? -1 : MultiFaceBlockRules.wireDirectionIndex(block, direction);
		}

		BlockItem item = state.getBlock().asItem() instanceof BlockItem blockItem ? blockItem : null;
		if(item instanceof StandingAndWallBlockItem standingAndWallItem)
		{
			int stateValue = encodeRegular(state, currentState);
			boolean hangingSign = state.getBlock() instanceof HangingSignBlock;
			if(stateValue < 0 || stateValue > (hangingSign ? 0x1f : 0xf))
			{
				return -1;
			}

			boolean wall = state.getBlock() != standingAndWallItem.getBlock();
			int facingIndex = verticalDirectionIndex(getFirstDirection(state));
			int encoded = ((stateValue & 0xf) << 3) | (facingIndex << 1) | (wall ? 1 : 0);
			if(hangingSign && (stateValue & 0x10) != 0)
			{
				encoded |= 0x4;
			}
			return encoded;
		}
		return encodeRegular(state, currentState);
	}

	public static @Nullable BlockState applyPlacementState(
			BlockState state,
			PlacementHandler.UseContext context,
			int rawValue
	)
	{
		BlockItem item = state.getBlock().asItem() instanceof BlockItem blockItem ? blockItem : null;
		if(item instanceof StandingAndWallBlockItem standingAndWallItem)
		{
			Block standingBlock = standingAndWallItem.getBlock();
			Block wallBlock = ((StandingAndWallBlockItemAccessor) standingAndWallItem)
					.triwasa$getWallBlock();
			BlockState standingState = state.getBlock() == standingBlock
					? state
					: standingBlock.getStateForPlacement(context.itemPlacementContext());
			if(standingState == null)
			{
				standingState = standingBlock.defaultBlockState();
			}
			return applyStandingOrWall(
					standingBlock,
					wallBlock,
					standingState,
					context.itemPlacementContext(),
					rawValue
			);
		}
		return apply(state.getBlock(), state, context.itemPlacementContext(), rawValue);
	}

	static int encodeRegular(BlockState state, @Nullable BlockState currentState)
	{
		Block block = state.getBlock();
		if(block instanceof AbstractBannerBlock)
		{
			return getInt(state, ROTATION_16) & 0xf;
		}
		if(block instanceof BellBlock)
		{
			int attachment = state.hasProperty(BellBlock.ATTACHMENT)
					? state.getValue(BellBlock.ATTACHMENT).ordinal()
					: BellAttachType.FLOOR.ordinal();
			return encodeGeneric(state) | ((attachment & 0x3) << 4);
		}
		if(block instanceof CampfireBlock)
		{
			return encodeGeneric(state) | (state.getValue(CampfireBlock.LIT) ? 0x10 : 0);
		}
		if(block instanceof CakeBlock)
		{
			return state.getValue(BITES) & 0x7;
		}
		if(block instanceof CandleBlock)
		{
			return (state.getValue(CandleBlock.CANDLES) - 1) & 0x3;
		}
		if(block instanceof ChestBlock)
		{
			int type = switch(state.getValue(ChestBlock.TYPE))
			{
				case SINGLE -> 0;
				case LEFT -> 1;
				case RIGHT -> 2;
			};
			return encodeGeneric(state) | 0x40 | (type << 4);
		}
		if(block instanceof CommandBlock)
		{
			return encodeGeneric(state) | (state.getValue(CommandBlock.CONDITIONAL) ? 0x10 : 0);
		}
		if(block instanceof CopperBulbBlock)
		{
			int value = state.getValue(CopperBulbBlock.LIT) ? 0x1 : 0;
			return value | (state.getValue(CopperBulbBlock.POWERED) ? 0x2 : 0);
		}
		if(block instanceof CopperGolemStatueBlock)
		{
			return encodeGeneric(state)
					| ((state.getValue(COPPER_GOLEM_POSE).ordinal() & 0x3) << 4);
		}
		if(block instanceof CrafterBlock)
		{
			return state.hasProperty(ORIENTATION) ? state.getValue(ORIENTATION).ordinal() & 0xf : 0;
		}
		if(block instanceof DaylightDetectorBlock)
		{
			return state.getValue(DaylightDetectorBlock.INVERTED) ? 1 : 0;
		}
		if(block instanceof DetectorRailBlock)
		{
			int value = state.getValue(DetectorRailBlock.SHAPE).ordinal() & 0x7;
			if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
			{
				value |= 0x8;
			}
			return value;
		}
		if(block instanceof DoorBlock)
		{
			int value = verticalDirectionIndex(state.getValue(HORIZONTAL_FACING));
			value |= (state.getValue(DoorBlock.HINGE).ordinal() & 0x1) << 2;
			if(!block.equals(Blocks.IRON_DOOR) && state.getValue(DoorBlock.OPEN))
			{
				value |= 0x8;
			}
			if(state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
			{
				value |= 0x10;
			}
			return value;
		}
		if(block instanceof FenceGateBlock)
		{
			return encodeGeneric(state) | (state.getValue(FenceGateBlock.OPEN) ? 0x10 : 0);
		}
		if(block instanceof FlowerBedBlock)
		{
			int value = verticalDirectionIndex(state.getValue(FlowerBedBlock.FACING)) << 2;
			return value | ((state.getValue(FlowerBedBlock.AMOUNT) - 1) & 0x3);
		}
		if(block instanceof HangingSignBlock)
		{
			int value = getInt(state, ROTATION_16) & 0xf;
			if(state.hasProperty(ATTACHED) && state.getValue(ATTACHED))
			{
				value |= 0x10;
			}
			return value;
		}
		if(block instanceof JigsawBlock)
		{
			return state.hasProperty(ORIENTATION) ? state.getValue(ORIENTATION).ordinal() & 0xf : 0;
		}
		if(block instanceof LanternBlock)
		{
			return state.getValue(LanternBlock.HANGING) ? 1 : 0;
		}
		if(block instanceof LeverBlock)
		{
			int value = encodeGeneric(state);
			value |= (state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE).ordinal() & 0x3) << 4;
			if(state.getValue(LeverBlock.POWERED))
			{
				value |= 0x40;
			}
			return value;
		}
		if(block instanceof LeafLitterBlock)
		{
			int value = verticalDirectionIndex(state.getValue(LeafLitterBlock.FACING)) << 2;
			return value | ((state.getValue(SegmentableBlock.AMOUNT) - 1) & 0x3);
		}
		if(block instanceof LightBlock)
		{
			return state.getValue(LightBlock.LEVEL) & 0xf;
		}
		if(block instanceof MushroomBlock)
		{
			return (state.getValue(NORTH) ? 0x01 : 0)
					| (state.getValue(EAST) ? 0x02 : 0)
					| (state.getValue(SOUTH) ? 0x04 : 0)
					| (state.getValue(WEST) ? 0x08 : 0)
					| (state.getValue(UP) ? 0x10 : 0)
					| (state.getValue(DOWN) ? 0x20 : 0);
		}
		if(block instanceof NoteBlock)
		{
			return state.getValue(NoteBlock.NOTE) & 0x1f;
		}
		if(block instanceof PoweredRailBlock)
		{
			int value = state.getValue(PoweredRailBlock.SHAPE).ordinal() & 0x7;
			if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
			{
				value |= 0x8;
			}
			return value;
		}
		if(block instanceof RailBlock)
		{
			int value = state.getValue(RailBlock.SHAPE).ordinal() & 0xf;
			if(TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
			{
				value |= 0x10;
			}
			return value;
		}
		if(block instanceof RedstoneWallTorchBlock)
		{
			return state.getValue(RedstoneWallTorchBlock.LIT) ? 1 : 0;
		}
		if(block instanceof RedstoneTorchBlock)
		{
			return state.getValue(RedstoneTorchBlock.LIT) ? 1 : 0;
		}
		if(block == Blocks.REDSTONE_WIRE)
		{
			boolean allDisconnected = state.getValue(NORTH_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(EAST_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(SOUTH_REDSTONE) == RedstoneSide.NONE
					&& state.getValue(WEST_REDSTONE) == RedstoneSide.NONE;
			return allDisconnected ? 1 : 0;
		}
		if(block instanceof SeaPickleBlock)
		{
			return (state.getValue(SeaPickleBlock.PICKLES) - 1) & 0x3;
		}
		if(block instanceof SignBlock)
		{
			return getInt(state, ROTATION_16) & 0xf;
		}
		if(block instanceof SkullBlock)
		{
			return getInt(state, SkullBlock.ROTATION) & 0xf;
		}
		if(block instanceof SnowLayerBlock)
		{
			return (state.getValue(SnowLayerBlock.LAYERS) - 1) & 0x7;
		}
		if(block instanceof StairBlock)
		{
			int value = verticalDirectionIndex(state.getValue(StairBlock.FACING));
			if(state.getValue(StairBlock.HALF) == Half.TOP)
			{
				value |= 0x4;
			}
			return value | ((state.getValue(StairBlock.SHAPE).ordinal() & 0x7) << 3);
		}
		if(block instanceof StructureBlock)
		{
			return state.getValue(StructureBlock.MODE).ordinal() & 0x3;
		}
		if(block instanceof TrapDoorBlock)
		{
			int value = verticalDirectionIndex(state.getValue(HORIZONTAL_FACING));
			if(state.getValue(TrapDoorBlock.HALF) == Half.TOP)
			{
				value |= 0x4;
			}
			if(!block.equals(Blocks.IRON_TRAPDOOR) && state.getValue(TrapDoorBlock.OPEN))
			{
				value |= 0x8;
			}
			return value;
		}
		if(block instanceof TurtleEggBlock)
		{
			return (state.getValue(TurtleEggBlock.EGGS) - 1) & 0x3;
		}
		if(block instanceof FaceAttachedHorizontalDirectionalBlock)
		{
			int value = encodeGeneric(state);
			return value | ((state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE).ordinal() & 0x3) << 4);
		}
		return encodeGeneric(state);
	}

	public static @Nullable BlockState apply(
			Block block,
			BlockState state,
			BlockPlaceContext context,
			int rawValue
	)
	{
		if(block instanceof AbstractBannerBlock)
		{
			return state.hasProperty(ROTATION_16) ? replaceSlot(state, ROTATION_16, rawValue & 0xf) : state;
		}
		if(block instanceof BellBlock)
		{
			BlockState generic = applyGeneric(state, rawValue, context);
			return replaceSlot(
					generic,
					BellBlock.ATTACHMENT,
					BellAttachType.values()[(rawValue & 0b0011_0000) >>> 4]
			);
		}
		if(block instanceof CampfireBlock)
		{
			return replaceSlot(applyGeneric(state, rawValue, context), CampfireBlock.LIT, (rawValue & 0x10) != 0);
		}
		if(block instanceof CakeBlock)
		{
			return replaceSlot(state, BITES, rawValue & 0x7);
		}
		if(block instanceof CandleBlock)
		{
			return validateIncremental(state, CandleBlock.CANDLES, (rawValue & 0x3) + 1);
		}
		if(block instanceof CommandBlock)
		{
			return replaceSlot(applyGeneric(state, rawValue, context), CommandBlock.CONDITIONAL, (rawValue & 0x10) != 0);
		}
		if(block instanceof CopperBulbBlock)
		{
			return state
					.setValue(CopperBulbBlock.LIT, (rawValue & 0x1) != 0)
					.setValue(CopperBulbBlock.POWERED, (rawValue & 0x2) != 0);
		}
		if(block instanceof CopperGolemStatueBlock)
		{
			BlockState generic = applyGeneric(state, rawValue, context);
			CopperGolemStatueBlock.Pose[] poses = CopperGolemStatueBlock.Pose.values();
			return replaceSlot(generic, COPPER_GOLEM_POSE, poses[(rawValue >>> 4) & 0x3]);
		}
		if(block instanceof CrafterBlock)
		{
			FrontAndTop[] values = FrontAndTop.values();
			return replaceSlot(state, ORIENTATION, values[Math.floorMod(rawValue, values.length)]);
		}
		if(block instanceof DaylightDetectorBlock)
		{
			return replaceSlot(state, DaylightDetectorBlock.INVERTED, (rawValue & 1) != 0);
		}
		if(block instanceof DetectorRailBlock)
		{
			return replaceSlot(state, DetectorRailBlock.SHAPE, RailShape.values()[(rawValue & 0x7) % 6]);
		}
		if(block instanceof DoorBlock)
		{
			BlockState result = state
					.setValue(HORIZONTAL_FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
					.setValue(DoorBlock.HINGE, DoorHingeSide.values()[(rawValue & 0x4) >>> 2])
					.setValue(DoorBlock.HALF, (rawValue & 0x10) != 0
							? DoubleBlockHalf.UPPER
							: DoubleBlockHalf.LOWER);
			if(!block.equals(Blocks.IRON_DOOR))
			{
				result = result.setValue(DoorBlock.OPEN, (rawValue & 0x8) != 0);
			}
			return result;
		}
		if(block instanceof FenceGateBlock)
		{
			return replaceSlot(applyGeneric(state, rawValue, context), FenceGateBlock.OPEN, (rawValue & 0x10) != 0);
		}
		if(block instanceof FlowerBedBlock)
		{
			Direction facing = VERTICAL_DIRECTIONS[(rawValue & 0xc) >>> 2];
			int amount = (rawValue & 0x3) + 1;
			BlockState current = context.getLevel().getBlockState(context.getClickedPos());
			BlockState result = current.is(block) ? state : state.setValue(FlowerBedBlock.FACING, facing);
			return validateIncremental(result, FlowerBedBlock.AMOUNT, amount);
		}
		if(block instanceof HangingSignBlock)
		{
			BlockState result = state;
			if(result.hasProperty(ROTATION_16))
			{
				result = result.setValue(ROTATION_16, rawValue & 0xf);
			}
			if(result.hasProperty(ATTACHED))
			{
				result = result.setValue(ATTACHED, (rawValue & 0x10) != 0);
			}
			return result;
		}
		if(block instanceof JigsawBlock)
		{
			FrontAndTop[] values = FrontAndTop.values();
			return replaceSlot(state, ORIENTATION, values[(rawValue & 0xf) % values.length]);
		}
		if(block instanceof LanternBlock)
		{
			return replaceSlot(state, LanternBlock.HANGING, (rawValue & 1) != 0);
		}
		if(block instanceof LeverBlock)
		{
			BlockState result = applyGeneric(state, rawValue, context);
			result = replaceSlot(
					result,
					FaceAttachedHorizontalDirectionalBlock.FACE,
					AttachFace.values()[((rawValue & 0x30) >>> 4) % 3]
			);
			return replaceSlot(result, LeverBlock.POWERED, (rawValue & 0x40) != 0);
		}
		if(block instanceof LeafLitterBlock)
		{
			Direction facing = VERTICAL_DIRECTIONS[(rawValue & 0xc) >>> 2];
			int amount = (rawValue & 0x3) + 1;
			BlockState current = context.getLevel().getBlockState(context.getClickedPos());
			BlockState result = current.is(block) ? state : state.setValue(LeafLitterBlock.FACING, facing);
			return validateIncremental(result, SegmentableBlock.AMOUNT, amount);
		}
		if(block instanceof LightBlock)
		{
			return replaceSlot(state, LightBlock.LEVEL, rawValue & 0xf);
		}
		if(MultiFaceBlockRules.isMultiFaceBlock(block))
		{
			return MultiFaceBlockRules.applyPlacementState(block, state, context, rawValue);
		}
		if(block instanceof MushroomBlock)
		{
			return state
					.setValue(NORTH, (rawValue & 0x01) != 0)
					.setValue(EAST, (rawValue & 0x02) != 0)
					.setValue(SOUTH, (rawValue & 0x04) != 0)
					.setValue(WEST, (rawValue & 0x08) != 0)
					.setValue(UP, (rawValue & 0x10) != 0)
					.setValue(DOWN, (rawValue & 0x20) != 0);
		}
		if(block instanceof NoteBlock)
		{
			return replaceSlot(state, NoteBlock.NOTE, (rawValue & 0x1f) % 25);
		}
		if(block instanceof PoweredRailBlock)
		{
			return replaceSlot(state, PoweredRailBlock.SHAPE, railShape(state, rawValue & 0x7));
		}
		if(block instanceof RailBlock)
		{
			return replaceSlot(state, RailBlock.SHAPE, railShape(state, rawValue & 0xf));
		}
		if(block instanceof RedstoneWallTorchBlock)
		{
			return replaceSlot(state, RedstoneWallTorchBlock.LIT, (rawValue & 1) != 0);
		}
		if(block instanceof RedstoneTorchBlock)
		{
			return replaceSlot(state, RedstoneTorchBlock.LIT, (rawValue & 1) != 0);
		}
		if(block == Blocks.REDSTONE_WIRE)
		{
			if((rawValue & 1) == 0)
			{
				return state;
			}
			return state
					.setValue(NORTH_REDSTONE, RedstoneSide.NONE)
					.setValue(EAST_REDSTONE, RedstoneSide.NONE)
					.setValue(SOUTH_REDSTONE, RedstoneSide.NONE)
					.setValue(WEST_REDSTONE, RedstoneSide.NONE);
		}
		if(block instanceof SeaPickleBlock)
		{
			return validateIncremental(state, SeaPickleBlock.PICKLES, (rawValue & 0x3) + 1);
		}
		if(block instanceof SignBlock)
		{
			return state.hasProperty(ROTATION_16) ? replaceSlot(state, ROTATION_16, rawValue & 0xf) : state;
		}
		if(block instanceof SkullBlock)
		{
			return state.hasProperty(SkullBlock.ROTATION)
					? replaceSlot(state, SkullBlock.ROTATION, rawValue & 0xf)
					: state;
		}
		if(block instanceof SnowLayerBlock)
		{
			return validateIncremental(state, SnowLayerBlock.LAYERS, (rawValue & 0x7) + 1);
		}
		if(block instanceof StairBlock)
		{
			return state
					.setValue(StairBlock.FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
					.setValue(StairBlock.HALF, halfFromBit(rawValue))
					.setValue(StairBlock.SHAPE, StairsShape.values()[(rawValue & 0x38) >>> 3]);
		}
		if(block instanceof StructureBlock)
		{
			return replaceSlot(state, StructureBlock.MODE, StructureMode.values()[rawValue & 0x3]);
		}
		if(block instanceof TrapDoorBlock)
		{
			BlockState result = state
					.setValue(HORIZONTAL_FACING, VERTICAL_DIRECTIONS[rawValue & 0x3])
					.setValue(TrapDoorBlock.HALF, halfFromBit(rawValue));
			if(!block.equals(Blocks.IRON_TRAPDOOR))
			{
				result = result.setValue(TrapDoorBlock.OPEN, (rawValue & 0x8) != 0);
			}
			return result;
		}
		if(block instanceof TurtleEggBlock)
		{
			return validateIncremental(state, TurtleEggBlock.EGGS, (rawValue & 0x3) + 1);
		}
		if(block instanceof FaceAttachedHorizontalDirectionalBlock)
		{
			return replaceSlot(
					applyGeneric(state, rawValue, context),
					FaceAttachedHorizontalDirectionalBlock.FACE,
					AttachFace.values()[((rawValue & 0x30) >>> 4) % 3]
			);
		}
		return applyGeneric(state, rawValue, context);
	}

	public static @Nullable BlockState applyStandingOrWall(
			Block standingBlock,
			Block wallBlock,
			BlockState standingState,
			BlockPlaceContext context,
			int rawValue
	)
	{
		boolean wall = (rawValue & 1) != 0;
		Direction facing = VERTICAL_DIRECTIONS[(rawValue >>> 1) & 0x3];
		int stateValue = rawValue >>> 3;
		if(standingBlock instanceof HangingSignBlock && (rawValue & 0x4) != 0)
		{
			stateValue |= 0x10;
		}
		Block block = wall ? wallBlock : standingBlock;
		BlockState state = standingState.is(block)
				? standingState
				: block.getStateForPlacement(context);
		if(state == null && wall)
		{
			state = wallBlock.defaultBlockState();
		}
		state = setFirstDirection(state, facing);
		return apply(block, state, context, stateValue);
	}

	static BlockState applyGeneric(BlockState state, int protocolValue, BlockPlaceContext context)
	{
		BlockState result = setFirstDirection(state, directionFromProtocol(state, protocolValue, context));
		if(result == state && state.hasProperty(AXIS))
		{
			result = result.setValue(AXIS, Direction.Axis.values()[Math.floorMod(protocolValue, 3)]);
		}

		if(result.hasProperty(SLAB_TYPE) && result.getValue(SLAB_TYPE) != SlabType.DOUBLE)
		{
			SlabType targetType = (protocolValue & 0x10) != 0 ? SlabType.TOP : SlabType.BOTTOM;
			result = result.setValue(SLAB_TYPE, targetType);
		}

		int highBits = protocolValue & ~0xf;
		if(result.hasProperty(RepeaterBlock.DELAY) && highBits >= 0x10)
		{
			int delay = highBits >>> 4;
			if(delay >= 1 && delay <= 4)
			{
				result = result.setValue(RepeaterBlock.DELAY, delay);
			}
		}
		else if(highBits == 0x10)
		{
			if(result.hasProperty(ComparatorBlock.MODE)
					&& result.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE)
			{
				result = result.setValue(ComparatorBlock.MODE, ComparatorMode.SUBTRACT);
			}
			else if(result.hasProperty(HALF) && result.getValue(HALF) == Half.BOTTOM)
			{
				result = result.setValue(HALF, Half.TOP);
			}
		}
		return result;
	}

	private static int encodeGeneric(BlockState state)
	{
		int value = 0;
		Optional<EnumProperty<Direction>> directionProperty = getFirstDirectionProperty(state);
		if(directionProperty.isPresent())
		{
			value = state.getValue(directionProperty.get()).get3DDataValue();
		}
		else if(state.hasProperty(AXIS))
		{
			value = state.getValue(AXIS).ordinal();
		}

		Block block = state.getBlock();
		if(block instanceof RepeaterBlock)
		{
			value |= state.getValue(RepeaterBlock.DELAY) << 4;
		}
		else if(block instanceof ComparatorBlock && state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT)
		{
			value |= 0x10;
		}
		else if(state.hasProperty(HALF) && state.getValue(HALF) == Half.TOP)
		{
			value |= 0x10;
		}
		else if(state.hasProperty(SLAB_TYPE) && state.getValue(SLAB_TYPE) == SlabType.TOP)
		{
			value |= 0x10;
		}
		return value;
	}

	private static Direction directionFromProtocol(
			BlockState state,
			int protocolValue,
			BlockPlaceContext context
	)
	{
		int index = protocolValue & 0xf;
		if(index <= 5)
		{
			return DIRECTIONS[index];
		}
		if(index == 6)
		{
			return context.getHorizontalDirection().getOpposite();
		}
		return state.hasProperty(HORIZONTAL_FACING) ? state.getValue(HORIZONTAL_FACING) : Direction.NORTH;
	}

	private static Optional<EnumProperty<Direction>> getFirstDirectionProperty(BlockState state)
	{
		for(Property<?> property : state.getProperties())
		{
			if(property instanceof EnumProperty<?> enumProperty
					&& enumProperty.getValueClass() == Direction.class)
			{
				@SuppressWarnings("unchecked")
				EnumProperty<Direction> directionProperty = (EnumProperty<Direction>) enumProperty;
				return Optional.of(directionProperty);
			}
		}
		return Optional.empty();
	}

	private static Direction getFirstDirection(BlockState state)
	{
		return getFirstDirectionProperty(state)
				.map(state::getValue)
				.filter(direction -> direction.getAxis().isHorizontal())
				.orElse(Direction.NORTH);
	}

	private static int verticalDirectionIndex(Direction direction)
	{
		for(int index = 0; index < VERTICAL_DIRECTIONS.length; index++)
		{
			if(VERTICAL_DIRECTIONS[index] == direction)
			{
				return index;
			}
		}
		return 0;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static BlockState setFirstDirection(BlockState state, Direction direction)
	{
		Optional<EnumProperty<Direction>> property = getFirstDirectionProperty(state);
		if(property.isEmpty() || !property.get().getPossibleValues().contains(direction))
		{
			return state;
		}
		return state.setValue((Property) property.get(), direction);
	}

	private static RailShape railShape(BlockState state, int index)
	{
		for(Property<?> property : state.getProperties())
		{
			Collection<?> values = property.getPossibleValues();
			if(!values.isEmpty() && values.iterator().next() instanceof RailShape)
			{
				List<RailShape> shapes = values.stream().map(RailShape.class::cast).toList();
				return shapes.get(Math.floorMod(index, shapes.size()));
			}
		}
		return RailShape.NORTH_SOUTH;
	}

	private static Half halfFromBit(int protocolValue)
	{
		return (protocolValue & 0x4) != 0 ? Half.TOP : Half.BOTTOM;
	}

	private static int getInt(BlockState state, Property<Integer> property)
	{
		return state.hasProperty(property) ? state.getValue(property) : 0;
	}

	private static <T extends Comparable<T>> @Nullable BlockState replaceSlot(
			BlockState state,
			Property<T> property,
			T value
	)
	{
		if(!state.hasProperty(property) || !property.getPossibleValues().contains(value))
		{
			return null;
		}
		return state.setValue(property, value);
	}

	static @Nullable BlockState validateIncremental(
			BlockState state,
			Property<Integer> property,
			int requestedMaximum
	)
	{
		if(!state.hasProperty(property))
		{
			return null;
		}
		return state.getValue(property) <= requestedMaximum ? state : null;
	}
}
