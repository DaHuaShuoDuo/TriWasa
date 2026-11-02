package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;

@Mixin(PacketProcessor.class)
public class PacketProcessorMixin
{
	@Inject(method = "processQueuedPackets", at = @At("TAIL"))
	private void triwasa$processPickupFilterAfterPackets(CallbackInfo callbackInfo)
	{
		ItemPickupFilter.processPending(Minecraft.getInstance());
	}
}
