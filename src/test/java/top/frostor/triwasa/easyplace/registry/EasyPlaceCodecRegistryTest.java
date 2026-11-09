package top.frostor.triwasa.easyplace.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class EasyPlaceCodecRegistryTest
{
	private static int checks;

	private EasyPlaceCodecRegistryTest()
	{
	}

	public static void run()
	{
		checkPriorityOrder();
		checkDuplicateRegistration();
		checkFreeze();
		checkNotFrozenLookup();
		System.out.println("EasyPlaceCodecRegistryTest passed " + checks + " checks");
	}

	private static void checkPriorityOrder()
	{
		EasyPlaceCodecRegistry registry = new EasyPlaceCodecRegistry();
		registry.register(
				"generic",
				100,
				context -> true,
				context -> 1,
				context -> context.state().setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE)
		);
		registry.register(
				"family",
				500,
				context -> context.block() == Blocks.STONE_SLAB,
				context -> 2,
				context -> context.state().setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM)
		);
		registry.register(
				"exact",
				1000,
				context -> true,
				context -> 3,
				context -> context.state().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP)
		);
		registry.freeze();

		EasyPlaceCodecContext context = context();
		EasyPlaceEncodedState encoded = registry.encode(context);
		check(encoded.rawValue() == 3, "higher-priority exact encoders must win");
		check(encoded.auxiliaryValue() == 0, "adapters without auxiliary state must encode zero");
		BlockState result = registry.decode(context);
		check(result != null, "a matching decoder must produce a state");
		check(
				result.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.TOP,
				"higher-priority exact decoders must win over family and generic adapters"
		);
	}

	private static void checkDuplicateRegistration()
	{
		EasyPlaceCodecRegistry duplicatePriority = new EasyPlaceCodecRegistry();
		duplicatePriority.register("first", 500, context -> true, context -> 0, context -> context.state());
		checkThrows(
				() -> duplicatePriority.register(
						"second",
						500,
						context -> true,
						context -> 0,
						context -> context.state()
				),
				"duplicate priorities must be rejected"
		);

		EasyPlaceCodecRegistry duplicateId = new EasyPlaceCodecRegistry();
		duplicateId.register("same", 500, context -> true, context -> 0, context -> context.state());
		checkThrows(
				() -> duplicateId.register(
						"same",
						400,
						context -> true,
						context -> 0,
						context -> context.state()
				),
				"duplicate adapter ids must be rejected"
		);
	}

	private static void checkFreeze()
	{
		EasyPlaceCodecRegistry registry = new EasyPlaceCodecRegistry();
		registry.register("legacy", 0, context -> true, context -> 0, context -> context.state());
		registry.freeze();
		check(registry.isFrozen(), "the registry must expose its frozen state");
		checkThrows(
				() -> registry.register(
						"late",
						100,
						context -> true,
						context -> 0,
						context -> context.state()
				),
				"registration after freezing must be rejected"
		);
	}

	private static void checkNotFrozenLookup()
	{
		EasyPlaceCodecRegistry registry = new EasyPlaceCodecRegistry();
		registry.register("legacy", 0, context -> true, context -> 0, context -> context.state());
		checkThrows(
				() -> registry.encode(context()),
				"lookups before freezing must be rejected"
		);
	}

	private static EasyPlaceCodecContext context()
	{
		return new EasyPlaceCodecContext(
				Blocks.STONE_SLAB.defaultBlockState(),
				null,
				null,
				null,
				null,
				0,
				0
		);
	}

	private static void checkThrows(Runnable action, String message)
	{
		checks++;
		try
		{
			action.run();
		}
		catch(IllegalArgumentException | IllegalStateException expected)
		{
			return;
		}
		throw new AssertionError(message);
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
