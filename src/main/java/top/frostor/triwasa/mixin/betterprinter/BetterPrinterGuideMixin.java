package top.frostor.triwasa.mixin.betterprinter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.config.TriWasaConfigs;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.Guide")
public abstract class BetterPrinterGuideMixin
{
	@WrapOperation(
			method = "buildAction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private boolean triwasa$allowUnsupportedWallHangingSign(
			BlockState state,
			LevelReader level,
			BlockPos pos,
			Operation<Boolean> original
	)
	{
		if(TriWasaConfigs.BETTER_PRINTER.getBooleanValue()
				&& ((BetterPrinterGuideAccessor) (Object) this)
				.triwasa$getRequiredBlock() instanceof WallHangingSignBlock)
		{
			return true;
		}
		return original.call(state, level, pos);
	}
}
