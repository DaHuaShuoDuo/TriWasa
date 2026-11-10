package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class EasyPlaceRailProtectionTest extends EasyPlaceTestSupport
{
	private EasyPlaceRailProtectionTest()
	{
	}

	public static int run()
	{
		EasyPlaceRailProtectionTest test = new EasyPlaceRailProtectionTest();
		test.checkProjectedStateComparison();
		test.checkNeighborMatchMask();
		test.checkMaskAuxiliaryRoundTrip();
		return test.checks();
	}

	private void checkProjectedStateComparison()
	{
		BlockState powered = Blocks.POWERED_RAIL.defaultBlockState()
				.setValue(PoweredRailBlock.POWERED, true);
		check(
				EasyPlaceRailProtection.matchesProjectedRail(
						powered.setValue(BaseRailBlock.WATERLOGGED, true),
						powered
				),
				"waterlogged differences must not exclude rail protection"
		);
		check(
				EasyPlaceRailProtection.matchesProjectedRail(
						powered.setValue(PoweredRailBlock.POWERED, false),
						powered
				),
				"powered rail redstone state must not affect neighbor protection"
		);
		BlockState detector = Blocks.DETECTOR_RAIL.defaultBlockState()
				.setValue(DetectorRailBlock.POWERED, true);
		check(
				EasyPlaceRailProtection.matchesProjectedRail(
						detector.setValue(DetectorRailBlock.POWERED, false),
						detector
				),
				"detector rail redstone state must not affect neighbor protection"
		);
		BlockState activator = Blocks.ACTIVATOR_RAIL.defaultBlockState()
				.setValue(PoweredRailBlock.POWERED, true);
		check(
				EasyPlaceRailProtection.matchesProjectedRail(
						activator.setValue(PoweredRailBlock.POWERED, false),
						activator
				),
				"activator rail redstone state must not affect neighbor protection"
		);
		check(
				!EasyPlaceRailProtection.matchesProjectedRail(activator, powered),
				"powered and activator rails must remain distinct neighbor variants"
		);
		check(
				!EasyPlaceRailProtection.matchesProjectedRail(
						Blocks.RAIL.defaultBlockState(),
						powered
				),
				"different rail block variants must not match"
		);
	}

	private void checkNeighborMatchMask()
	{
		BlockPos origin = new BlockPos(4, 64, 8);
		BlockState matching = Blocks.POWERED_RAIL.defaultBlockState();
		BlockState mismatch = matching.setValue(PoweredRailBlock.POWERED, true);
		BlockState activator = Blocks.ACTIVATOR_RAIL.defaultBlockState();
		int mask = EasyPlaceRailProtection.neighborMatchMask(
				pos -> {
					if(pos.equals(origin.above()))
					{
						return matching.setValue(BaseRailBlock.WATERLOGGED, true);
					}
					if(pos.equals(origin.north()))
					{
						return matching;
					}
					if(pos.equals(origin.east()))
					{
						return activator.setValue(PoweredRailBlock.POWERED, true);
					}
					if(pos.equals(origin.south()))
					{
						return mismatch;
					}
					return Blocks.AIR.defaultBlockState();
				},
				pos -> {
					if(pos.equals(origin.above())
							|| pos.equals(origin.north())
							|| pos.equals(origin.east())
							|| pos.equals(origin.south()))
					{
						return pos.equals(origin.east()) ? activator : matching;
					}
					return Blocks.AIR.defaultBlockState();
				},
				origin
		);
		int expected = (1 << Direction.UP.ordinal())
				| (1 << Direction.NORTH.ordinal())
				| (1 << Direction.EAST.ordinal())
				| (1 << Direction.SOUTH.ordinal());
		check(
				mask == expected,
				"powered and activator rail redstone differences must remain in the rail protection mask"
		);
	}

	private void checkMaskAuxiliaryRoundTrip()
	{
		int mask = (1 << Direction.UP.ordinal()) | (1 << Direction.NORTH.ordinal());
		int auxiliary = EasyPlaceRailProtection.applyNeighborMatchMask(
				EasyPlaceExtraProtocol.RAIL_UPDATE_SUPPRESSION,
				mask
		);
		check(
				(auxiliary & EasyPlaceExtraProtocol.RAIL_NEIGHBOR_MATCH_MASK)
						== (mask << 19),
				"rail neighbor match bits must pack into the auxiliary field"
		);
	}
}
