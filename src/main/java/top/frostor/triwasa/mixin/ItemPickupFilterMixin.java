package top.frostor.triwasa.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.pickupfilter.ItemPickupFilter;

@Mixin(ClientPacketListener.class)
public class ItemPickupFilterMixin
{
	@Inject(
			method = "handleTakeItemEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	private void triwasa$filterItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo callbackInfo)
	{
		Minecraft client = Minecraft.getInstance();
		if(client.player == null || client.level == null
				|| packet.getPlayerId() != client.player.getId())
		{
			return;
		}

		Entity entity = client.level.getEntity(packet.getItemId());
		if(entity instanceof ItemEntity itemEntity
				&& ItemPickupFilter.onItemPickup(itemEntity, packet.getAmount()))
		{
			callbackInfo.cancel();
		}
	}

	@Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
	private void triwasa$filterUpdatedSlot(
			ClientboundContainerSetSlotPacket packet,
			CallbackInfo callbackInfo
	)
	{
		ItemPickupFilter.onContainerSlotUpdated(
				Minecraft.getInstance(),
				packet.getContainerId(),
				packet.getSlot()
		);
	}

	@Inject(method = "handleContainerContent", at = @At("TAIL"))
	private void triwasa$filterUpdatedContent(
			ClientboundContainerSetContentPacket packet,
			CallbackInfo callbackInfo
	)
	{
		ItemPickupFilter.onContainerContentUpdated(
				Minecraft.getInstance(),
				packet.containerId()
		);
	}
}
