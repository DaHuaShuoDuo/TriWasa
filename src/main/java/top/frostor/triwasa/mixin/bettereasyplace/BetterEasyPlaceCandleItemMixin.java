package top.frostor.triwasa.mixin.bettereasyplace;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.CandleRules;

@Mixin(BlockItem.class)
public abstract class BetterEasyPlaceCandleItemMixin
{
	@WrapOperation(
			method = "canPlace",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private boolean triwasa$allowUnsupportedCandlePlacement(
			BlockState state,
			LevelReader level,
			BlockPos pos,
			Operation<Boolean> original
	)
	{
		if(CandleRules.shouldAllowUnsupportedPlacement(state))
		{
			return true;
		}
		return original.call(state, level, pos);
	}
}
