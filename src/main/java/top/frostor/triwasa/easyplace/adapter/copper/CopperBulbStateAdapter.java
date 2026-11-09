package top.frostor.triwasa.easyplace.adapter.copper;

import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class CopperBulbStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CopperBulbBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		int value = context.state().getValue(CopperBulbBlock.LIT) ? 0x1 : 0;
		return value | (context.state().getValue(CopperBulbBlock.POWERED) ? 0x2 : 0);
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		int rawValue = context.rawValue();
		return context.state()
				.setValue(CopperBulbBlock.LIT, (rawValue & 0x1) != 0)
				.setValue(CopperBulbBlock.POWERED, (rawValue & 0x2) != 0);
	}
}
