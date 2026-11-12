package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlacePistonProtectionTest extends EasyPlaceTestSupport
{
	private EasyPlacePistonProtectionTest()
	{
	}

	public static int run()
	{
		EasyPlacePistonProtectionTest test = new EasyPlacePistonProtectionTest();
		test.checkTargetMatching();
		test.checkNeighborMatching();
		test.checkMaskLayout();
		test.checkConfigSwitch();
		return test.checks();
	}

	private void checkTargetMatching()
	{
		BlockPos origin = new BlockPos(4, 64, 8);
		BlockState retracted = Blocks.PISTON.defaultBlockState()
				.setValue(PistonBaseBlock.EXTENDED, false);
		BlockState extended = retracted.setValue(PistonBaseBlock.EXTENDED, true);
		check(
				(EasyPlacePistonProtection.protectionMask(
						retracted,
						null,
						null,
						origin
				) & EasyPlaceExtraProtocol.PISTON_TARGET_MATCH) != 0,
				"an unextended projected piston must protect the placement target"
		);
		check(
				EasyPlacePistonProtection.protectionMask(
						extended,
						null,
						null,
						origin
				) == 0,
				"an extended projected piston must not request target state restoration"
		);
		check(
				EasyPlacePistonProtection.protectionMask(
						Blocks.STONE.defaultBlockState(),
						null,
						null,
						origin
				) == 0,
				"non-piston targets must not request piston protection"
		);
	}

	private void checkNeighborMatching()
	{
		BlockPos origin = new BlockPos(4, 64, 8);
		BlockState matching = Blocks.STICKY_PISTON.defaultBlockState()
				.setValue(PistonBaseBlock.FACING, Direction.EAST)
				.setValue(PistonBaseBlock.EXTENDED, true);
		BlockState mismatchedFacing = matching.setValue(
				PistonBaseBlock.FACING,
				Direction.WEST
		);
		BlockState mismatchedExtended = matching.setValue(
				PistonBaseBlock.EXTENDED,
				false
		);
		int mask = EasyPlacePistonProtection.protectionMask(
				Blocks.STONE.defaultBlockState(),
				pos -> {
					if(pos.equals(origin.north()))
					{
						return matching;
					}
					if(pos.equals(origin.south()))
					{
						return mismatchedFacing;
					}
					if(pos.equals(origin.west()))
					{
						return mismatchedExtended;
					}
					return matching;
				},
				pos -> pos.equals(origin.west())
						|| pos.equals(origin.above())
								? Blocks.PISTON.defaultBlockState()
								: matching,
				origin
		);
		int allDirections = 0;
		for(Direction direction : Direction.values())
		{
			allDirections |= 1 << (direction.ordinal() + 1);
		}
		int expectedMatches = allDirections
				& ~(1 << (Direction.SOUTH.ordinal() + 1))
				& ~(1 << (Direction.WEST.ordinal() + 1))
				& ~(1 << (Direction.UP.ordinal() + 1));
		check(
				(mask & EasyPlaceExtraProtocol.PISTON_NEIGHBOR_MATCH_MASK)
						== expectedMatches,
				"only exact projected piston neighbors must enter the protection mask"
		);
		check(
				EasyPlacePistonProtection.matchesProjectedPiston(
						Blocks.STICKY_PISTON.defaultBlockState(),
						Blocks.PISTON.defaultBlockState()
				) == false,
				"normal and sticky piston variants must not match"
		);
		check(
				allDirections == EasyPlaceExtraProtocol.PISTON_NEIGHBOR_MATCH_MASK,
				"piston directions must occupy exactly six neighbor bits"
		);
	}

	private void checkMaskLayout()
	{
		check(
				EasyPlaceExtraProtocol.PISTON_PROTECTION_MASK == 0xfff,
				"piston protection must include target, direct, and QC bits"
		);
		int packed = EasyPlacePistonProtection.protectionMask(
				Blocks.PISTON.defaultBlockState(),
				null,
				null,
				BlockPos.ZERO
		);
		check(
				packed == EasyPlaceExtraProtocol.PISTON_TARGET_MATCH,
				"target-only piston protection must stay in bit zero"
		);
		check(
				EasyPlaceExtraProtocol.pistonQcTargetOffsets().length == 5,
				"the QC shell must contain only positions around the block above"
		);
		int qcTargetMask = 0;
		for(BlockPos offset : EasyPlaceExtraProtocol.pistonQcTargetOffsets())
		{
			int bit = EasyPlaceExtraProtocol.pistonQcTargetMatchBit(
					BlockPos.ZERO,
					BlockPos.ZERO.offset(
							offset.getX(),
							offset.getY(),
							offset.getZ()
					)
			);
			check(bit != 0, "every QC target offset must map to a mask bit");
			check((qcTargetMask & bit) == 0, "QC target offsets must use unique mask bits");
			qcTargetMask |= bit;
		}
		check(
				qcTargetMask == EasyPlaceExtraProtocol.PISTON_QC_TARGET_MATCH_MASK,
				"the five QC target bits must fill the complete QC mask"
		);
		BlockPos qcPos = BlockPos.ZERO.offset(1, -1, 0);
		BlockState matching = Blocks.PISTON.defaultBlockState();
		int qcMask = EasyPlacePistonProtection.protectionMask(
				Blocks.STONE.defaultBlockState(),
				pos -> pos.equals(qcPos) ? matching : Blocks.AIR.defaultBlockState(),
				pos -> pos.equals(qcPos) ? matching : Blocks.AIR.defaultBlockState(),
				BlockPos.ZERO
		);
		check(
				(qcMask & EasyPlaceExtraProtocol.pistonQcTargetMatchBit(
						BlockPos.ZERO,
						qcPos
				)) != 0,
				"a matching QC piston must enter the protection mask"
		);
		int mismatchedQcMask = EasyPlacePistonProtection.protectionMask(
				Blocks.STONE.defaultBlockState(),
				pos -> pos.equals(qcPos)
						? matching.setValue(PistonBaseBlock.FACING, Direction.EAST)
						: Blocks.AIR.defaultBlockState(),
				pos -> pos.equals(qcPos) ? matching : Blocks.AIR.defaultBlockState(),
				BlockPos.ZERO
		);
		check(
				(mismatchedQcMask & EasyPlaceExtraProtocol.pistonQcTargetMatchBit(
						BlockPos.ZERO,
						qcPos
				)) == 0,
				"a mismatched QC piston must not enter the protection mask"
		);
	}

	private void checkConfigSwitch()
	{
		boolean previous = TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.setBooleanValue(false);
			check(
					!EasyPlacePistonProtection.isEnabled(),
					"disabling the piston child switch must disable protection requests"
			);
			TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.setBooleanValue(true);
			check(
					EasyPlacePistonProtection.isEnabled(),
					"enabling the piston child switch must enable protection requests"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_PISTON_PROTECTION.setBooleanValue(previous);
		}
	}
}
