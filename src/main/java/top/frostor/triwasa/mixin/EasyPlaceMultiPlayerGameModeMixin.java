package top.frostor.triwasa.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.BellRules;

@Mixin(MultiPlayerGameMode.class)
public abstract class EasyPlaceMultiPlayerGameModeMixin
{
	@ModifyVariable(method = "useItemOn", at = @At("HEAD"), argsOnly = true, index = 3)
	private BlockHitResult triwasa$restoreBellClickSide(BlockHitResult hitResult)
	{
		BellRules.BellPlacementPlan plan = BellRules.takeClickPlan(hitResult);
		if(plan != null)
		{
			return new BlockHitResult(
					plan.hitVec(),
					plan.side(),
					plan.supportPos(),
					false
			);
		}
		return hitResult;
	}
}
