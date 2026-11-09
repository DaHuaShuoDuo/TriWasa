package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.PlacementHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.easyplace.adapter.directional.DoorStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.FenceGateStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.StairStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.TrapDoorStateAdapter;
import top.frostor.triwasa.easyplace.adapter.attachment.FaceAttachedStateAdapter;
import top.frostor.triwasa.easyplace.adapter.cake.CakeStateAdapter;
import top.frostor.triwasa.easyplace.adapter.campfire.CampfireStateAdapter;
import top.frostor.triwasa.easyplace.adapter.container.ChestStateAdapter;
import top.frostor.triwasa.easyplace.adapter.copper.CopperBulbStateAdapter;
import top.frostor.triwasa.easyplace.adapter.copper.CopperGolemStateAdapter;
import top.frostor.triwasa.easyplace.adapter.crafter.CrafterStateAdapter;
import top.frostor.triwasa.easyplace.adapter.daylight.DaylightDetectorStateAdapter;
import top.frostor.triwasa.easyplace.adapter.generic.GenericFallbackStateAdapter;
import top.frostor.triwasa.easyplace.adapter.lantern.LanternStateAdapter;
import top.frostor.triwasa.easyplace.adapter.multiface.MultiFaceStateAdapter;
import top.frostor.triwasa.easyplace.adapter.mushroom.MushroomStateAdapter;
import top.frostor.triwasa.easyplace.adapter.quantity.QuantityStateAdapter;
import top.frostor.triwasa.easyplace.adapter.rail.DetectorRailStateAdapter;
import top.frostor.triwasa.easyplace.adapter.rail.RailStateAdapter;
import top.frostor.triwasa.easyplace.adapter.redstone.RedstoneStateAdapter;
import top.frostor.triwasa.easyplace.adapter.special.BellStateAdapter;
import top.frostor.triwasa.easyplace.adapter.sign.HangingSignStateAdapter;
import top.frostor.triwasa.easyplace.adapter.sign.SignStateAdapter;
import top.frostor.triwasa.easyplace.adapter.special.AdministrativeStateAdapter;
import top.frostor.triwasa.easyplace.adapter.wall.WallStateAdapter;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecRegistry;
import top.frostor.triwasa.easyplace.registry.EasyPlaceEncodedState;
import top.frostor.triwasa.mixin.StandingAndWallBlockItemAccessor;

public final class EasyPlaceStateCodec
{
	private static final Direction[] VERTICAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};
	private static final EasyPlaceCodecRegistry REGISTRY = createRegistry();

	private EasyPlaceStateCodec()
	{
	}

	public static int encode(BlockState state)
	{
		return encodeState(state).rawValue();
	}

	static int encode(BlockState state, @Nullable BlockState currentState)
	{
		return encodeState(state, currentState, null, null).rawValue();
	}

	public static int encode(
			BlockState state,
			@Nullable BlockState currentState,
			@Nullable BlockGetter level,
			@Nullable BlockPos pos
	)
	{
		return encodeState(state, currentState, level, pos).rawValue();
	}

	public static EasyPlaceEncodedState encodeState(BlockState state)
	{
		return encodeState(state, null, null, null);
	}

	public static EasyPlaceEncodedState encodeState(
			BlockState state,
			@Nullable BlockState currentState,
			@Nullable BlockGetter level,
			@Nullable BlockPos pos
	)
	{
		BlockItem item = state.getBlock().asItem() instanceof BlockItem blockItem ? blockItem : null;
		if(item instanceof StandingAndWallBlockItem standingAndWallItem)
		{
			EasyPlaceEncodedState encodedState = REGISTRY.encode(
					codecContext(state, currentState, level, pos)
			);
			int stateValue = encodedState.rawValue();
			boolean hangingSign = state.getBlock() instanceof HangingSignBlock;
			if(stateValue < 0 || stateValue > (hangingSign ? 0x1f : 0xf))
			{
				return new EasyPlaceEncodedState(-1, 0);
			}

			boolean wall = state.getBlock() != standingAndWallItem.getBlock();
			int facingIndex = EasyPlaceStateSupport.verticalDirectionIndex(
					EasyPlaceStateSupport.getFirstDirection(state)
			);
			int encoded = ((stateValue & 0xf) << 3) | (facingIndex << 1) | (wall ? 1 : 0);
			if(hangingSign && (stateValue & 0x10) != 0)
			{
				encoded |= 0x4;
			}
			return new EasyPlaceEncodedState(encoded, 0);
		}
		return REGISTRY.encode(new EasyPlaceCodecContext(
				state,
				currentState,
				level,
				pos,
				null,
				0,
				0
		));
	}

	public static @Nullable BlockState applyPlacementState(
			BlockState state,
			PlacementHandler.UseContext context,
			int rawValue
	)
	{
		return applyPlacementState(state, context, rawValue, 0);
	}

	public static @Nullable BlockState applyPlacementState(
			BlockState state,
			PlacementHandler.UseContext context,
			int rawValue,
			int auxiliaryValue
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
					rawValue,
					auxiliaryValue
			);
		}
		return apply(
				state.getBlock(),
				state,
				context.itemPlacementContext(),
				rawValue,
				auxiliaryValue
		);
	}

	public static @Nullable BlockState apply(
			Block block,
			BlockState state,
			BlockPlaceContext context,
			int rawValue
	)
	{
		return apply(block, state, context, rawValue, 0);
	}

	public static @Nullable BlockState apply(
			Block block,
			BlockState state,
			BlockPlaceContext context,
			int rawValue,
			int auxiliaryValue
	)
	{
		return REGISTRY.decode(new EasyPlaceCodecContext(
				state,
				null,
				null,
				null,
				context,
				rawValue,
				auxiliaryValue
		));
	}

	public static @Nullable BlockState applyStandingOrWall(
			Block standingBlock,
			Block wallBlock,
			BlockState standingState,
			BlockPlaceContext context,
			int rawValue
	)
	{
		return applyStandingOrWall(
				standingBlock,
				wallBlock,
				standingState,
				context,
				rawValue,
				0
		);
	}

	public static @Nullable BlockState applyStandingOrWall(
			Block standingBlock,
			Block wallBlock,
			BlockState standingState,
			BlockPlaceContext context,
			int rawValue,
			int auxiliaryValue
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
		if(state == null)
		{
			return null;
		}
		state = EasyPlaceStateSupport.setFirstDirection(state, facing);
		return apply(block, state, context, stateValue, auxiliaryValue);
	}

	static BlockState applyGeneric(BlockState state, int protocolValue, BlockPlaceContext context)
	{
		return EasyPlaceStateSupport.applyGeneric(state, protocolValue, context);
	}

	static @Nullable BlockState validateIncremental(
			BlockState state,
			net.minecraft.world.level.block.state.properties.Property<Integer> property,
			int requestedMaximum
	)
	{
		return EasyPlaceStateSupport.validateIncremental(state, property, requestedMaximum);
	}

	private static EasyPlaceCodecRegistry createRegistry()
	{
		EasyPlaceCodecRegistry registry = new EasyPlaceCodecRegistry();
		DoorStateAdapter door = new DoorStateAdapter();
		TrapDoorStateAdapter trapdoor = new TrapDoorStateAdapter();
		FenceGateStateAdapter fenceGate = new FenceGateStateAdapter();
		StairStateAdapter stair = new StairStateAdapter();
		SignStateAdapter sign = new SignStateAdapter();
		HangingSignStateAdapter hangingSign = new HangingSignStateAdapter();
		FaceAttachedStateAdapter faceAttached = new FaceAttachedStateAdapter();
		MultiFaceStateAdapter multiFace = new MultiFaceStateAdapter();
		QuantityStateAdapter quantity = new QuantityStateAdapter();
		RailStateAdapter rail = new RailStateAdapter();
		MushroomStateAdapter mushroom = new MushroomStateAdapter();
		ChestStateAdapter chest = new ChestStateAdapter();
		RedstoneStateAdapter redstone = new RedstoneStateAdapter();
		BellStateAdapter bell = new BellStateAdapter();
		CampfireStateAdapter campfire = new CampfireStateAdapter();
		CakeStateAdapter cake = new CakeStateAdapter();
		CopperBulbStateAdapter copperBulb = new CopperBulbStateAdapter();
		CopperGolemStateAdapter copperGolem = new CopperGolemStateAdapter();
		CrafterStateAdapter crafter = new CrafterStateAdapter();
		DaylightDetectorStateAdapter daylight = new DaylightDetectorStateAdapter();
		DetectorRailStateAdapter detectorRail = new DetectorRailStateAdapter();
		WallStateAdapter wall = new WallStateAdapter();
		LanternStateAdapter lantern = new LanternStateAdapter();
		GenericFallbackStateAdapter genericFallback = new GenericFallbackStateAdapter();
		AdministrativeStateAdapter administrative = new AdministrativeStateAdapter();
		registry.register("door", 700, DoorStateAdapter::matches, door, door);
		registry.register("trapdoor", 690, TrapDoorStateAdapter::matches, trapdoor, trapdoor);
		registry.register("fence_gate", 680, FenceGateStateAdapter::matches, fenceGate, fenceGate);
		registry.register("stair", 670, StairStateAdapter::matches, stair, stair);
		registry.register("sign", 660, SignStateAdapter::matches, sign, sign);
		registry.register(
				"hanging_sign",
				659,
				HangingSignStateAdapter::matches,
				hangingSign,
				hangingSign
		);
		registry.register(
				"face_attached",
				658,
				FaceAttachedStateAdapter::matches,
				faceAttached,
				faceAttached
		);
		registry.register(
				"multiface",
				657,
				MultiFaceStateAdapter::matches,
				multiFace,
				multiFace
		);
		registry.register(
				"quantity",
				656,
				QuantityStateAdapter::matches,
				quantity,
				quantity
		);
		registry.register("rail", 655, RailStateAdapter::matches, rail, rail);
		registry.register(
				"mushroom",
				654,
				MushroomStateAdapter::matches,
				mushroom,
				mushroom
		);
		registry.register("container", 653, ChestStateAdapter::matches, chest, chest);
		registry.register("redstone", 649, RedstoneStateAdapter::matches, redstone, redstone);
		registry.register("bell", 648, BellStateAdapter::matches, bell, bell);
		registry.register(
				"campfire",
				647,
				CampfireStateAdapter::matches,
				campfire,
				campfire
		);
		registry.register("cake", 646, CakeStateAdapter::matches, cake, cake);
		registry.register(
				"copper_bulb",
				645,
				CopperBulbStateAdapter::matches,
				copperBulb,
				copperBulb
		);
		registry.register(
				"copper_golem",
				644,
				CopperGolemStateAdapter::matches,
				copperGolem,
				copperGolem
		);
		registry.register("crafter", 643, CrafterStateAdapter::matches, crafter, crafter);
		registry.register(
				"daylight_detector",
				642,
				DaylightDetectorStateAdapter::matches,
				daylight,
				daylight
		);
		registry.register(
				"detector_rail",
				641,
				DetectorRailStateAdapter::matches,
				detectorRail,
				detectorRail
		);
		registry.register("wall", 652, WallStateAdapter::matches, wall, wall);
		registry.register("lantern", 640, LanternStateAdapter::matches, lantern, lantern);
		registry.register(
				"administrative",
				650,
				AdministrativeStateAdapter::matches,
				administrative,
				administrative
		);
		registry.register(
				"generic_fallback",
				50,
				GenericFallbackStateAdapter::matches,
				genericFallback,
				genericFallback
		);
		registry.freeze();
		return registry;
	}

	private static EasyPlaceCodecContext codecContext(
			BlockState state,
			@Nullable BlockState currentState,
			@Nullable BlockGetter level,
			@Nullable BlockPos pos
	)
	{
		return new EasyPlaceCodecContext(state, currentState, level, pos, null, 0, 0);
	}

}
