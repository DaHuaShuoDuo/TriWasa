package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.Vec3;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.registry.EasyPlaceEncodedState;

public final class EasyPlaceWallStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceWallStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceWallStateTest test = new EasyPlaceWallStateTest();
		test.checkAllSideCombinations();
		test.checkPostState();
		test.checkInvalidRawValues();
		test.checkWaterloggedStateIsPreserved();
		test.checkServerFacingValues();
		test.checkCrafterAuxiliaryIsolation();
		test.checkDisabledToggleUsesVanillaPath();
		return test.checks();
	}

	private void checkAllSideCombinations()
	{
		WallSide[] sides = WallSide.values();
		for(int rawValue = 0; rawValue <= 80; rawValue++)
		{
			BlockState projected = Blocks.STONE_BRICK_WALL.defaultBlockState()
					.setValue(WallBlock.UP, false)
					.setValue(WallBlock.NORTH, sides[rawValue % 3])
					.setValue(WallBlock.EAST, sides[(rawValue / 3) % 3])
					.setValue(WallBlock.SOUTH, sides[(rawValue / 9) % 3])
					.setValue(WallBlock.WEST, sides[(rawValue / 27) % 3]);
			EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(projected);
			check(encoded.rawValue() == rawValue, "all wall side combinations must encode");
			check(encoded.auxiliaryValue() == 0, "UP=false must encode auxiliary zero");

			BlockState restored = EasyPlaceStateCodec.apply(
					Blocks.STONE_BRICK_WALL,
					Blocks.STONE_BRICK_WALL.defaultBlockState().setValue(WallBlock.UP, true),
					null,
					rawValue,
					0
			);
			check(restored != null, "every valid wall side combination must decode");
			if(restored == null)
			{
				continue;
			}
			check(
					restored.getValue(WallBlock.NORTH) == sides[rawValue % 3]
							&& restored.getValue(WallBlock.EAST) == sides[(rawValue / 3) % 3]
							&& restored.getValue(WallBlock.SOUTH) == sides[(rawValue / 9) % 3]
							&& restored.getValue(WallBlock.WEST) == sides[(rawValue / 27) % 3],
					"wall side values must round trip for raw value " + rawValue
			);
			check(!restored.getValue(WallBlock.UP), "auxiliary zero must decode as UP=false");
		}
	}

	private void checkPostState()
	{
		BlockState wall = Blocks.STONE_BRICK_WALL.defaultBlockState().setValue(WallBlock.UP, true);
		EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(wall);
		check(encoded.auxiliaryValue() == 1, "UP=true must use auxiliary bit zero");

		BlockState restored = EasyPlaceStateCodec.apply(
				Blocks.STONE_BRICK_WALL,
				Blocks.STONE_BRICK_WALL.defaultBlockState(),
				null,
				encoded.rawValue(),
				encoded.auxiliaryValue()
		);
		check(restored != null && restored.getValue(WallBlock.UP), "UP=true must round trip");
	}

	private void checkInvalidRawValues()
	{
		for(int rawValue = 81; rawValue <= 0x7f; rawValue++)
		{
			check(
					EasyPlaceStateCodec.apply(
							Blocks.STONE_BRICK_WALL,
							Blocks.STONE_BRICK_WALL.defaultBlockState(),
							null,
							rawValue,
							0
					) == null,
					"wall raw value " + rawValue + " must be rejected"
			);
		}
	}

	private void checkWaterloggedStateIsPreserved()
	{
		BlockState waterlogged = Blocks.STONE_BRICK_WALL.defaultBlockState()
				.setValue(WallBlock.WATERLOGGED, true)
				.setValue(WallBlock.UP, false);
		EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(waterlogged);
		check(
				encoded.auxiliaryValue() == 0,
				"waterlogged walls with no post must not write WATERLOGGED into auxiliary data"
		);
		BlockState restored = EasyPlaceStateCodec.apply(
				Blocks.STONE_BRICK_WALL,
				waterlogged,
				null,
				encoded.rawValue(),
				encoded.auxiliaryValue()
		);
		check(
				restored != null && restored.getValue(WallBlock.WATERLOGGED),
				"wall decoding must preserve the placement-state WATERLOGGED value"
		);
	}

	private void checkServerFacingValues()
	{
		BlockPos pos = new BlockPos(12, 70, -9);
		Vec3 hitVec = new Vec3(12.5, 70.5, -8.5);
		Vec3 encoded = EasyPlaceExtraProtocol.encodeX(pos, hitVec, 80);
		encoded = EasyPlaceExtraProtocol.encodeZ(pos, encoded, 1);
		check(
				EasyPlaceExtraProtocol.toRawValue(EasyPlaceExtraProtocol.decode(encoded, pos)) == 80,
				"wall raw values must survive the server-facing X channel"
		);
		check(
				EasyPlaceExtraProtocol.decodeZ(encoded, pos) == 1,
				"wall auxiliary values must survive the server-facing Z channel"
		);
	}

	private void checkCrafterAuxiliaryIsolation()
	{
		BlockState crafter = Blocks.CRAFTER.defaultBlockState();
		EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(crafter);
		check(encoded.auxiliaryValue() == 0, "crafters must not emit wall auxiliary state");

		BlockState withoutAuxiliary = EasyPlaceStateCodec.apply(
				Blocks.CRAFTER,
				crafter,
				null,
				1,
				0
		);
		BlockState withCrafterMask = EasyPlaceStateCodec.apply(
				Blocks.CRAFTER,
				crafter,
				null,
				1,
				0x1ab
		);
		check(
				withoutAuxiliary != null
						&& withCrafterMask != null
						&& withoutAuxiliary.getValue(BlockStateProperties.ORIENTATION)
						== FrontAndTop.values()[1]
						&& withCrafterMask.getValue(BlockStateProperties.ORIENTATION)
						== FrontAndTop.values()[1],
				"crafter slot-mask bits must not affect crafter orientation decoding"
		);
	}

	private void checkDisabledToggleUsesVanillaPath()
	{
		boolean previousWall = TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		boolean rail = TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(
					Blocks.STONE_BRICK_WALL.defaultBlockState()
			);
			check(
					encoded.rawValue() == -1,
					"disabling wall protection must disable projected wall state encoding"
			);
			check(
					encoded.auxiliaryValue() == 0,
					"disabling wall protection must clear wall auxiliary state"
			);
			check(
					EasyPlaceUpdateFlags.current() == EasyPlaceUpdateFlags.encode(
							rail,
							false,
							TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue(),
							TriWasaConfigs.EASY_PLACE_TNT_PROTECTION.getBooleanValue()
					),
					"disabling wall protection must remove only the wall suppression flag"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(previousWall);
		}
	}
}
