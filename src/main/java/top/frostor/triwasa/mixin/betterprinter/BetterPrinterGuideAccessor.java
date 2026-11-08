package top.frostor.triwasa.mixin.betterprinter;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.Guide")
public interface BetterPrinterGuideAccessor
{
	@Accessor("requiredBlock")
	Block triwasa$getRequiredBlock();
}
