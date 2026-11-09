package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public final class EasyPlaceAttachmentStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceAttachmentStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceAttachmentStateTest test = new EasyPlaceAttachmentStateTest();
		Direction[] directions = {
				Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
		};
		for(int faceIndex = 0; faceIndex < AttachFace.values().length; faceIndex++)
		{
			for(Direction direction : directions)
			{
				BlockState lever = Blocks.LEVER.defaultBlockState()
						.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.values()[faceIndex])
						.setValue(FaceAttachedHorizontalDirectionalBlock.FACING, direction);
				int encoded = EasyPlaceStateCodec.encode(lever);
				BlockState applied = EasyPlaceStateCodec.apply(
						Blocks.LEVER,
						Blocks.LEVER.defaultBlockState(),
						null,
						encoded
				);
				test.check(
						applied != null
								&& applied.getValue(FaceAttachedHorizontalDirectionalBlock.FACE)
								== AttachFace.values()[faceIndex]
								&& applied.getValue(FaceAttachedHorizontalDirectionalBlock.FACING)
								== direction,
						"face-attached buttons and levers must round trip every face/facing combination"
				);
			}
		}

		BlockState poweredLever = Blocks.LEVER.defaultBlockState()
				.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.CEILING)
				.setValue(FaceAttachedHorizontalDirectionalBlock.FACING, Direction.EAST)
				.setValue(LeverBlock.POWERED, true);
		test.check(EasyPlaceStateCodec.encode(poweredLever) == 0x65, "lever powered state must use bit six");
		BlockState appliedPoweredLever = EasyPlaceStateCodec.apply(
				Blocks.LEVER,
				Blocks.LEVER.defaultBlockState(),
				null,
				EasyPlaceStateCodec.encode(poweredLever)
		);
		test.check(
				appliedPoweredLever != null && appliedPoweredLever.getValue(LeverBlock.POWERED),
				"lever protocol must preserve powered state"
		);
		return test.checks();
	}
}
