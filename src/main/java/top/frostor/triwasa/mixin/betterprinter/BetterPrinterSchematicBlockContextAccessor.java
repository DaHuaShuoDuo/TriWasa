package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "me.aleksilassila.litematica.printer.printer.SchematicBlockContext")
public interface BetterPrinterSchematicBlockContextAccessor
{
	@Accessor("level")
	ClientLevel triwasa$getLevel();

	@Accessor("blockPos")
	BlockPos triwasa$getBlockPos();

	@Accessor("requiredState")
	BlockState triwasa$getRequiredState();
}
