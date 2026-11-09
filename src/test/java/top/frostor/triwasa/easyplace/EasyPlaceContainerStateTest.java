package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class EasyPlaceContainerStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceContainerStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceContainerStateTest test = new EasyPlaceContainerStateTest();
		BlockState leftChest = Blocks.CHEST.defaultBlockState()
				.setValue(ChestBlock.FACING, Direction.NORTH)
				.setValue(ChestBlock.TYPE, ChestType.LEFT);
		int chestProtocol = EasyPlaceStateCodec.encode(leftChest);
		test.check((chestProtocol & 0x40) != 0, "chest type must carry an explicit marker");
		test.check(
				((chestProtocol >>> 4) & 0x3) == 1,
				"chest LEFT type must use protocol value one"
		);
		return test.checks();
	}
}
