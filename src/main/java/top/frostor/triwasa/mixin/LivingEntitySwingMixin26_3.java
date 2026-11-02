package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;

@Mixin(LivingEntity.class)
public class LivingEntitySwingMixin26_3
{
	@Inject(
			method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void triwasa$suppressAutomatedDropSwing(
			InteractionHand hand,
			SwingAnimation animation,
			boolean fromClientPlayer,
			CallbackInfoReturnable<Boolean> callbackInfo
	)
	{
		if(ItemPickupFilter.shouldSuppressSwing()
				&& (Object) this == Minecraft.getInstance().player)
		{
			callbackInfo.setReturnValue(false);
		}
	}
}
