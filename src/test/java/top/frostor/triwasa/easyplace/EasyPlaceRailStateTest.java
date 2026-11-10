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
		boolean previous = TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(true);
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

			BlockState poweredRail = Blocks.POWERED_RAIL.defaultBlockState()
					.setValue(PoweredRailBlock.SHAPE, RailShape.NORTH_SOUTH)
					.setValue(PoweredRailBlock.POWERED, true);
			int poweredValue = EasyPlaceStateCodec.encode(poweredRail);
			check(
					(poweredValue & 0x8) != 0
							&& (poweredValue & 0x7) == (RailShape.NORTH_SOUTH.ordinal() & 0x7)
							&& (poweredValue & EasyPlaceExtraProtocol.RAIL_POWERED_MASK) != 0,
					"powered rails must encode shape and redstone state"
			);
			BlockState appliedPoweredRail = EasyPlaceStateCodec.apply(
					Blocks.POWERED_RAIL,
					Blocks.POWERED_RAIL.defaultBlockState(),
					null,
					poweredValue
			);
			check(
					appliedPoweredRail != null
							&& appliedPoweredRail.getValue(PoweredRailBlock.POWERED)
							&& appliedPoweredRail.getValue(PoweredRailBlock.SHAPE)
							== RailShape.NORTH_SOUTH,
					"powered rail shape and redstone state must round trip"
			);

			check(
					Blocks.ACTIVATOR_RAIL instanceof PoweredRailBlock,
					"activator rails must use the shared PoweredRailBlock implementation"
			);
			BlockState activatorRail = Blocks.ACTIVATOR_RAIL.defaultBlockState()
					.setValue(PoweredRailBlock.SHAPE, RailShape.EAST_WEST)
					.setValue(PoweredRailBlock.POWERED, true);
			int activatorValue = EasyPlaceStateCodec.encode(activatorRail);
			check(
					(activatorValue & 0x8) != 0
							&& (activatorValue & 0x7) == (RailShape.EAST_WEST.ordinal() & 0x7)
							&& (activatorValue
							& EasyPlaceExtraProtocol.RAIL_POWERED_MASK) != 0,
					"activator rails must encode shape and redstone state"
			);
			BlockState appliedActivatorRail = EasyPlaceStateCodec.apply(
					Blocks.ACTIVATOR_RAIL,
					Blocks.ACTIVATOR_RAIL.defaultBlockState(),
					null,
					activatorValue
			);
			check(
					appliedActivatorRail != null
							&& appliedActivatorRail.getValue(PoweredRailBlock.POWERED)
							&& appliedActivatorRail.getValue(PoweredRailBlock.SHAPE)
							== RailShape.EAST_WEST,
					"activator rail shape and redstone state must round trip"
			);

			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(false);
			int disabledPoweredValue = EasyPlaceStateCodec.encode(poweredRail);
			int disabledActivatorValue = EasyPlaceStateCodec.encode(activatorRail);
			check(
					(disabledPoweredValue & 0x8) == 0
							&& (disabledPoweredValue
							& EasyPlaceExtraProtocol.RAIL_POWERED_MASK) == 0
							&& (disabledActivatorValue & 0x8) == 0
							&& (disabledActivatorValue
							& EasyPlaceExtraProtocol.RAIL_POWERED_MASK) == 0,
					"disabling rail protection must omit powered and activator rail protection bits"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(previous);
		}
	}

	private void checkDetectorRailState()
	{
		boolean previous = TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue();
		BlockState poweredDetectorRail = Blocks.DETECTOR_RAIL.defaultBlockState()
				.setValue(DetectorRailBlock.SHAPE, RailShape.NORTH_SOUTH)
				.setValue(DetectorRailBlock.POWERED, true);
		try
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(true);
			int poweredValue = EasyPlaceStateCodec.encode(poweredDetectorRail);
			check(
					((poweredValue & 0x8) != 0)
							&& ((poweredValue
							& EasyPlaceExtraProtocol.RAIL_POWERED_MASK) != 0),
					"detector rails must encode shape protection and initial active state"
			);
			BlockState restored = EasyPlaceStateCodec.apply(
					Blocks.DETECTOR_RAIL,
					Blocks.DETECTOR_RAIL.defaultBlockState(),
					null,
					poweredValue
			);
			check(
					restored != null
							&& restored.getValue(DetectorRailBlock.POWERED)
							&& restored.getValue(DetectorRailBlock.SHAPE)
							== RailShape.NORTH_SOUTH,
					"detector rail shape and initial active state must round trip"
			);

			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(false);
			int disabledValue = EasyPlaceStateCodec.encode(poweredDetectorRail);
			check(
					(disabledValue & 0x8) == 0
							&& (disabledValue
							& EasyPlaceExtraProtocol.RAIL_POWERED_MASK) == 0,
					"disabling rail protection must omit detector protection bits"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.setBooleanValue(previous);
		}
	}
}
