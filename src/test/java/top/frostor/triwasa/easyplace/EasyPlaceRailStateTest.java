package top.frostor.triwasa.easyplace;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceRailStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceRailStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceRailStateTest test = new EasyPlaceRailStateTest();
		test.checkRailStates();
		test.checkDetectorRailState();
		return test.checks();
	}

	private void checkRailStates()
	{
		boolean previous = TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
			RailShape shape = RailShape.SOUTH_EAST;
			BlockState rail = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, shape);
			int railValue = EasyPlaceStateCodec.encode(rail);
			check(
					(railValue & 0x10) != 0 && (railValue & 0xf) == (shape.ordinal() & 0xf),
					"normal rails must encode shape and no-update bits"
			);
			BlockState appliedRail = EasyPlaceStateCodec.apply(
					Blocks.RAIL,
					Blocks.RAIL.defaultBlockState(),
					null,
					railValue
			);
			check(
					appliedRail != null && appliedRail.getValue(RailBlock.SHAPE) == shape,
					"normal rail wire values must round trip"
			);

			RailShape poweredRailShape = RailShape.NORTH_SOUTH;
			BlockState poweredRail = Blocks.POWERED_RAIL.defaultBlockState()
					.setValue(PoweredRailBlock.SHAPE, poweredRailShape);
			int poweredValue = EasyPlaceStateCodec.encode(poweredRail);
			check(
					(poweredValue & 0x8) != 0
							&& (poweredValue & 0x7) == (poweredRailShape.ordinal() & 0x7),
					"powered rails must encode shape and no-update bits"
			);
			BlockState appliedPoweredRail = EasyPlaceStateCodec.apply(
					Blocks.POWERED_RAIL,
					Blocks.POWERED_RAIL.defaultBlockState(),
					null,
					poweredValue
			);
			check(
					appliedPoweredRail != null
							&& appliedPoweredRail.getValue(PoweredRailBlock.SHAPE)
							== poweredRailShape,
					"powered rail wire values must round trip"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(previous);
		}
	}

	private void checkDetectorRailState()
	{
		boolean previousDetector = TriWasaConfigs.EASY_PLACE_DETECTOR_RAIL_PROTECTION
				.getBooleanValue();
		boolean previousRail = TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE
				.getBooleanValue();
		BlockState state = Blocks.DETECTOR_RAIL.defaultBlockState();
		try
		{
			TriWasaConfigs.EASY_PLACE_DETECTOR_RAIL_PROTECTION.setBooleanValue(true);
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(false);
			int encoded = EasyPlaceStateCodec.encode(state, Blocks.AIR.defaultBlockState());
			check(encoded >= 0, Blocks.DETECTOR_RAIL + " must be owned by a client state adapter");
			check(
					EasyPlaceStateCodec.apply(Blocks.DETECTOR_RAIL, state, null, encoded) != null,
					Blocks.DETECTOR_RAIL + " must decode through its client state adapter"
			);

			BlockState poweredDetectorRail = state
					.setValue(DetectorRailBlock.SHAPE, RailShape.NORTH_SOUTH)
					.setValue(DetectorRailBlock.POWERED, true);
			int poweredValue = EasyPlaceStateCodec.encode(
					poweredDetectorRail,
					Blocks.AIR.defaultBlockState()
			);
			check(
					(poweredValue & EasyPlaceExtraProtocol.DETECTOR_RAIL_POWERED_MASK) != 0,
					"powered detector rails must encode the powered bit independently of rails"
			);
			check(
					(poweredValue & 0x8) == 0,
					"disabling the generic rail switch must omit the rail shape-update bit"
			);
			BlockState restoredPowered = EasyPlaceStateCodec.apply(
					Blocks.DETECTOR_RAIL,
					Blocks.DETECTOR_RAIL.defaultBlockState(),
					null,
					poweredValue
			);
			check(
					restoredPowered != null
							&& restoredPowered.getValue(DetectorRailBlock.POWERED)
							&& restoredPowered.getValue(DetectorRailBlock.SHAPE)
							== RailShape.NORTH_SOUTH,
					"powered detector rail shape and state must round trip"
			);

			TriWasaConfigs.EASY_PLACE_DETECTOR_RAIL_PROTECTION.setBooleanValue(false);
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(true);
			int disabledValue = EasyPlaceStateCodec.encode(
					poweredDetectorRail,
					Blocks.AIR.defaultBlockState()
			);
			check(
					(disabledValue & EasyPlaceExtraProtocol.DETECTOR_RAIL_POWERED_MASK) == 0,
					"disabling detector rail protection must omit the powered bit"
			);
			check(
					(disabledValue & 0x8) != 0,
					"the generic rail switch must remain independent of detector rail protection"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_DETECTOR_RAIL_PROTECTION.setBooleanValue(previousDetector);
			TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.setBooleanValue(previousRail);
		}
	}
}
