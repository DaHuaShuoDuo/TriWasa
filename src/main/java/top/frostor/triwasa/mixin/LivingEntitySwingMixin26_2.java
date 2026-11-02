package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;

@Mixin(LivingEntity.class)
public class LivingEntitySwingMixin26_2
{
	@Inject(
			method = "swing(Lnet/minecraft/world/InteractionHand;Z)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void triwasa$suppressAutomatedDropSwing(
			InteractionHand hand,
			boolean fromClientPlayer,
			CallbackInfo callbackInfo
	)
	{
		if(ItemPickupFilter.shouldSuppressSwing()
				&& (Object) this == Minecraft.getInstance().player)
		{
			callbackInfo.cancel();
		}
	}
}
