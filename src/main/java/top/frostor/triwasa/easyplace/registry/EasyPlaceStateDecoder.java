package top.frostor.triwasa.easyplace.registry;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EasyPlaceStateDecoder
{
	@Nullable BlockState decode(EasyPlaceCodecContext context);
}
