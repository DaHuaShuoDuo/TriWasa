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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.easyplace.adapter.directional.DoorStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.FenceGateStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.GenericDirectionalStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.StairStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.TrapDoorStateAdapter;
import top.frostor.triwasa.easyplace.adapter.attachment.FaceAttachedStateAdapter;
import top.frostor.triwasa.easyplace.adapter.legacy.LegacyStateAdapter;
import top.frostor.triwasa.easyplace.adapter.multiface.MultiFaceStateAdapter;
import top.frostor.triwasa.easyplace.adapter.sign.HangingSignStateAdapter;
import top.frostor.triwasa.easyplace.adapter.sign.SignStateAdapter;
import top.frostor.triwasa.easyplace.adapter.special.AdministrativeStateAdapter;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecRegistry;
import top.frostor.triwasa.mixin.StandingAndWallBlockItemAccessor;

import java.util.Optional;

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

		BlockItem item = state.getBlock().asItem() instanceof BlockItem blockItem ? blockItem : null;
		if(item instanceof StandingAndWallBlockItem standingAndWallItem)
		{
			int stateValue = REGISTRY.encode(codecContext(state, currentState, level, pos));
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
		return REGISTRY.encode(new EasyPlaceCodecContext(
				state,
				currentState,
				level,
				pos,
				null,
				0
		));
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

	public static @Nullable BlockState apply(
			Block block,
			BlockState state,
			BlockPlaceContext context,
			int rawValue
	)
	{
		return REGISTRY.decode(new EasyPlaceCodecContext(
				state,
				null,
				null,
				null,
				context,
				rawValue
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
		return LegacyEasyPlaceStateCodec.applyGeneric(state, protocolValue, context);
	}

	static @Nullable BlockState validateIncremental(
			BlockState state,
			net.minecraft.world.level.block.state.properties.Property<Integer> property,
			int requestedMaximum
	)
	{
		return LegacyEasyPlaceStateCodec.validateIncremental(state, property, requestedMaximum);
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
		AdministrativeStateAdapter administrative = new AdministrativeStateAdapter();
		GenericDirectionalStateAdapter generic = new GenericDirectionalStateAdapter();
		LegacyStateAdapter legacy = new LegacyStateAdapter();
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
				"administrative",
				650,
				AdministrativeStateAdapter::matches,
				administrative,
				administrative
		);
		registry.register(
				"generic_directional",
				100,
				GenericDirectionalStateAdapter::matches,
				generic,
				generic
		);
		registry.register("legacy", 0, context -> true, legacy, legacy);
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
		return new EasyPlaceCodecContext(state, currentState, level, pos, null, 0);
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

	private static Direction getFirstDirection(BlockState state)
	{
		return getFirstDirectionProperty(state)
				.map(state::getValue)
				.filter(direction -> direction.getAxis().isHorizontal())
				.orElse(Direction.NORTH);
	}

	private static Optional<EnumProperty<Direction>> getFirstDirectionProperty(BlockState state)
	{
		for(net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties())
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static BlockState setFirstDirection(BlockState state, Direction direction)
	{
		for(net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties())
		{
			if(property.getPossibleValues().stream().anyMatch(value -> value instanceof Direction)
					&& property.getPossibleValues().contains(direction))
			{
				return state.setValue((net.minecraft.world.level.block.state.properties.Property) property, direction);
			}
		}
		return state;
	}
}
