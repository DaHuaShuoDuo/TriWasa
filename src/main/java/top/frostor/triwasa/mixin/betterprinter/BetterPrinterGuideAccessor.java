package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.Guide")
public interface BetterPrinterGuideAccessor
{
	@Accessor("requiredBlock")
	Block triwasa$getRequiredBlock();

	@Accessor("currentState")
	BlockState triwasa$getCurrentState();

	@Accessor("level")
	ClientLevel triwasa$getLevel();

	@Accessor("blockPos")
	BlockPos triwasa$getBlockPos();
}