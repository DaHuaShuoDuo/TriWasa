package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;

public final class EasyPlaceWallProtectionTest extends EasyPlaceTestSupport
{
	private EasyPlaceWallProtectionTest()
	{
	}

	public static int run()
	{
		EasyPlaceWallProtectionTest test = new EasyPlaceWallProtectionTest();
		test.checkProjectedStateComparison();
		test.checkNeighborMatchMask();
		test.checkMaskAuxiliaryRoundTrip();
		return test.checks();
	}

	private void checkProjectedStateComparison()
	{
		BlockState projected = Blocks.STONE_BRICK_WALL.defaultBlockState()
				.setValue(WallBlock.NORTH, WallSide.TALL)
				.setValue(WallBlock.UP, false)
				.setValue(WallBlock.WATERLOGGED, false);
		BlockState actualWaterlogged = projected.setValue(WallBlock.WATERLOGGED, true);
		check(
				EasyPlaceWallProtection.matchesProjectedWall(actualWaterlogged, projected),
				"waterlogged differences must not exclude wall protection"
		);
		check(
				!EasyPlaceWallProtection.matchesProjectedWall(
						projected.setValue(WallBlock.NORTH, WallSide.LOW),
						projected
				),
				"wall side differences must exclude wall protection"
		);
		check(
				!EasyPlaceWallProtection.matchesProjectedWall(
						projected.setValue(WallBlock.UP, true),
						projected
				),
				"wall post differences must exclude wall protection"
		);
		check(
				!EasyPlaceWallProtection.matchesProjectedWall(
						Blocks.COBBLESTONE_WALL.defaultBlockState(),
						projected
				),
				"different wall block variants must not match"
		);
	}

	private void checkNeighborMatchMask()
	{
		BlockPos origin = new BlockPos(4, 64, 8);
		BlockState matching = Blocks.STONE_BRICK_WALL.defaultBlockState()
				.setValue(WallBlock.UP, true);
		BlockState mismatch = matching.setValue(WallBlock.UP, false);
		int mask = EasyPlaceWallProtection.neighborMatchMask(
				pos -> {
					if(pos.equals(origin.above()))
					{
						return matching.setValue(WallBlock.WATERLOGGED, true);
					}
					if(pos.equals(origin.below()))
					{
						return mismatch;
					}
					if(pos.equals(origin.north()))
					{
						return matching;
					}
					return Blocks.AIR.defaultBlockState();
				},
				pos -> {
					if(pos.equals(origin.above())
							|| pos.equals(origin.below())
							|| pos.equals(origin.north()))
					{
						return matching;
					}
					return Blocks.AIR.defaultBlockState();
				},
				origin
		);
		int expected = (1 << Direction.UP.ordinal()) | (1 << Direction.NORTH.ordinal());
		check(mask == expected, "only matching wall neighbors must enter the protection mask");
	}

	private void checkMaskAuxiliaryRoundTrip()
	{
		int mask = (1 << Direction.UP.ordinal()) | (1 << Direction.DOWN.ordinal());
		int auxiliary = EasyPlaceWallProtection.applyNeighborMatchMask(
				EasyPlaceExtraProtocol.WALL_UPDATE_SUPPRESSION,
				mask
		);
		check(
				(auxiliary & EasyPlaceExtraProtocol.WALL_NEIGHBOR_MATCH_MASK)
						== (mask << 11),
				"wall neighbor match bits must pack into the auxiliary field"
		);
	}
}
