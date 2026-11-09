package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class EasyPlaceHangingSignStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceHangingSignStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceHangingSignStateTest test = new EasyPlaceHangingSignStateTest();
		BlockPos pos = new BlockPos(-17, 70, 31);
		BlockState wallHangingSign = Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
		int wallHangingSignValue = EasyPlaceStateCodec.encode(wallHangingSign, null);
		BlockState wallHangingSignResult = EasyPlaceStateCodec.applyStandingOrWall(
				Blocks.OAK_HANGING_SIGN,
				Blocks.OAK_WALL_HANGING_SIGN,
				wallHangingSign,
				null,
				wallHangingSignValue
		);
		test.check(
				wallHangingSignValue == 0x7
						&& wallHangingSignResult.getValue(BlockStateProperties.HORIZONTAL_FACING)
						== Direction.EAST,
				"wall hanging signs must encode and restore facing without attachment support"
		);
		test.check(
				EasyPlaceStateCodec.apply(
						Blocks.OAK_WALL_HANGING_SIGN,
						wallHangingSign,
						null,
						0x10
				).getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST,
				"wall hanging signs must not receive ceiling-only properties"
		);
		BlockState attachedCeilingHangingSign = Blocks.OAK_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.ROTATION_16, 11)
				.setValue(BlockStateProperties.ATTACHED, true);
		int attachedCeilingValue = EasyPlaceStateCodec.encode(attachedCeilingHangingSign, null);
		BlockState attachedCeilingResult = EasyPlaceStateCodec.applyStandingOrWall(
				Blocks.OAK_HANGING_SIGN,
				Blocks.OAK_WALL_HANGING_SIGN,
				attachedCeilingHangingSign,
				null,
				attachedCeilingValue
		);
		test.check(
				attachedCeilingValue == 0x5c
						&& attachedCeilingResult.getValue(BlockStateProperties.ROTATION_16) == 11
						&& attachedCeilingResult.getValue(BlockStateProperties.ATTACHED),
				"attached ceiling hanging signs must encode and restore rotation and attachment"
		);
		BlockState ceilingHangingSign = Blocks.OAK_HANGING_SIGN.defaultBlockState()
				.setValue(BlockStateProperties.ROTATION_16, 2)
				.setValue(BlockStateProperties.ATTACHED, false);
		BlockState ceilingResult = EasyPlaceStateCodec.apply(
				Blocks.OAK_HANGING_SIGN,
				ceilingHangingSign,
				null,
				0x13
		);
		test.check(
				ceilingResult.getValue(BlockStateProperties.ROTATION_16) == 3
						&& ceilingResult.getValue(BlockStateProperties.ATTACHED),
				"ceiling hanging signs must retain rotation and attachment decoding"
		);
		HangingSignRules.ClickTarget wallClickTarget =
				HangingSignRules.getForcedWallClickTarget(pos, wallHangingSign);
		test.check(
				wallClickTarget != null
						&& wallClickTarget.pos().equals(pos)
						&& wallClickTarget.side().getAxis()
						== wallHangingSign.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
				"wall hanging signs must click the target position from the same horizontal axis"
		);
		return test.checks();
	}
}
