package top.frostor.triwasa.pickupfilter;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Client-side pickup filter ported from Item Pickup Filter.
 *
 * <p>Pickup packets are only used to enqueue the item type. The inventory is not scanned
 * until a matching pickup is pending, and all matching stacks are dropped as whole stacks
 * before player input and rendering run for that client tick.</p>
 */
public final class ItemPickupFilter
{
	private static final Map<Item, PendingPickup> PENDING_PICKUPS = new LinkedHashMap<>();
	private static Set<Item> filteredItems = Set.of();
	private static Set<TagKey<Item>> filteredTags = Set.of();
	private static Set<Rarity> filteredRarities = Set.of();
	private static boolean automatedDrop;

	private ItemPickupFilter()
	{
	}

	public static void register()
	{
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PENDING_PICKUPS.clear());
	}

	public static boolean onItemPickup(ItemEntity itemEntity, int amount)
	{
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		ItemStack stack = itemEntity.getItem();

		if(player == null || client.level == null || stack.isEmpty()
				|| !TriWasaConfigs.PICKUP_FILTER_ENABLED.getBooleanValue()
				|| shouldPauseForSneaking()
				|| !shouldDrop(stack))
		{
			return false;
		}

		PendingPickup pending = PENDING_PICKUPS.computeIfAbsent(
				stack.getItem(),
				item -> new PendingPickup(stack.copyWithCount(1))
		);
		pending.remainingAmount += Math.max(1, amount);
		int removed = Math.min(amount, stack.getCount());
		stack.shrink(removed);
		if(stack.isEmpty())
		{
			client.level.removeEntity(itemEntity.getId(), Entity.RemovalReason.DISCARDED);
		}
		return true;
	}

	public static void onContainerSlotUpdated(
			Minecraft client,
			int containerId,
			int menuSlotIndex
	)
	{
		AbstractContainerMenu menu = getUpdatedMenu(client, containerId);
		if(menu == null || menuSlotIndex < 0 || menuSlotIndex >= menu.slots.size())
		{
			return;
		}

		LocalPlayer player = client.player;
		Slot slot = menu.getSlot(menuSlotIndex);
		ItemStack stack = slot.getItem();
		if(slot.container == player.getInventory()
				&& slot.getContainerSlot() < player.getInventory().getNonEquipmentItems().size()
				&& shouldDrop(stack))
		{
			queuePendingPickup(stack);
		}
	}

	public static void onContainerContentUpdated(Minecraft client, int containerId)
	{
		AbstractContainerMenu menu = getUpdatedMenu(client, containerId);
		if(menu == null)
		{
			return;
		}

		LocalPlayer player = client.player;
		int mainInventorySize = player.getInventory().getNonEquipmentItems().size();
		for(Slot slot : menu.slots)
		{
			ItemStack stack = slot.getItem();
			if(slot.container == player.getInventory()
					&& slot.getContainerSlot() < mainInventorySize
					&& shouldDrop(stack))
			{
				queuePendingPickup(stack);
			}
		}
	}

	public static void processPending(Minecraft client)
	{
		if(PENDING_PICKUPS.isEmpty())
		{
			return;
		}
		if(!TriWasaConfigs.PICKUP_FILTER_ENABLED.getBooleanValue())
		{
			PENDING_PICKUPS.clear();
			return;
		}
		if(client.player == null || client.gameMode == null || client.level == null
				|| client.getConnection() == null)
		{
			return;
		}

		Set<Item> pendingItems = new LinkedHashSet<>();
		Iterator<Map.Entry<Item, PendingPickup>> cleanupIterator = PENDING_PICKUPS.entrySet().iterator();
		while(cleanupIterator.hasNext())
		{
			Map.Entry<Item, PendingPickup> entry = cleanupIterator.next();
			PendingPickup pending = entry.getValue();
			if(!shouldDrop(pending.template))
			{
				cleanupIterator.remove();
				continue;
			}
			pendingItems.add(entry.getKey());
		}

		List<DropTarget> targets = findDropTargets(client.player, pendingItems);
		if(targets.isEmpty())
		{
			return;
		}

		float originalPitch = client.player.getXRot();
		boolean rotationSent = false;
		automatedDrop = true;
		try
		{
			client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
					client.player.getYRot(),
					90.0F,
					client.player.onGround(),
					false
			));
			rotationSent = true;

			for(DropTarget target : targets)
			{
				ItemStack feedbackStack = target.slot().getItem().copy();
				int droppedCount = feedbackStack.getCount();
				client.gameMode.handleContainerInput(
						target.menu().containerId,
						target.slot().index,
						1,
						ContainerInput.THROW,
						client.player
				);

				if(target.slot().getItem().isEmpty())
				{
					PendingPickup pending = PENDING_PICKUPS.get(feedbackStack.getItem());
					if(pending != null)
					{
						pending.remainingAmount -= droppedCount;
					}
					showFeedback(client.player, feedbackStack);
				}
			}
		}
		finally
		{
			automatedDrop = false;
			if(rotationSent)
			{
				client.getConnection().send(new ServerboundMovePlayerPacket.Rot(
						client.player.getYRot(),
						originalPitch,
						client.player.onGround(),
						false
				));
			}
		}

		PENDING_PICKUPS.entrySet().removeIf(entry -> entry.getValue().remainingAmount <= 0);
	}

	public static boolean shouldSuppressSwing()
	{
		return automatedDrop;
	}

	public static void onConfigChanged()
	{
		rebuildMatchers();
		if(!TriWasaConfigs.PICKUP_FILTER_ENABLED.getBooleanValue())
		{
			PENDING_PICKUPS.clear();
			return;
		}

		PENDING_PICKUPS.entrySet().removeIf(entry -> !shouldDrop(entry.getValue().template));
	}

	private static void queuePendingPickup(ItemStack stack)
	{
		PendingPickup pending = PENDING_PICKUPS.computeIfAbsent(
				stack.getItem(),
				item -> new PendingPickup(stack.copyWithCount(1))
		);
		pending.remainingAmount = Math.max(pending.remainingAmount, stack.getCount());
	}

	private static AbstractContainerMenu getUpdatedMenu(Minecraft client, int containerId)
	{
		LocalPlayer player = client.player;
		if(player == null
				|| !TriWasaConfigs.PICKUP_FILTER_ENABLED.getBooleanValue()
				|| shouldPauseForSneaking())
		{
			return null;
		}

		if(containerId == 0)
		{
			return player.inventoryMenu;
		}
		if(player.containerMenu != null && player.containerMenu.containerId == containerId)
		{
			return player.containerMenu;
		}
		return null;
	}

	private static List<DropTarget> findDropTargets(LocalPlayer player, Set<Item> itemTypes)
	{
		Inventory inventory = player.getInventory();
		AbstractContainerMenu menu = player.containerMenu;
		List<DropTarget> targets = new ArrayList<>();

		if(menu == null)
		{
			return targets;
		}

		int mainInventorySize = inventory.getNonEquipmentItems().size();
		for(Slot slot : menu.slots)
		{
			if(slot.container != inventory
					|| slot.getContainerSlot() >= mainInventorySize
					|| slot.getItem().isEmpty())
			{
				continue;
			}

			ItemStack stack = slot.getItem();
			if(itemTypes.contains(stack.getItem()) && shouldDrop(stack))
			{
				targets.add(new DropTarget(menu, slot));
			}
		}

		return targets;
	}

	private static void showFeedback(LocalPlayer player, ItemStack stack)
	{
		PickupFilterFeedback feedback = getFeedback();
		if(feedback == PickupFilterFeedback.SILENT)
		{
			return;
		}

		Component message = Component.translatable(
				"triwasa.pickup_filter.dropped",
				stack.getHoverName()
		);
		if(feedback == PickupFilterFeedback.CHAT)
		{
			player.sendSystemMessage(message);
		}
		else
		{
			player.sendOverlayMessage(message);
		}
	}

	private static boolean shouldPauseForSneaking()
	{
		ConfigBoolean pauseSetting = TriWasaConfigs.PICKUP_FILTER_PAUSE_WHILE_SNEAKING;
		Minecraft client = Minecraft.getInstance();
		return pauseSetting.getBooleanValue()
				&& client.options != null
				&& client.options.keyShift.isDown();
	}

	private static boolean shouldDrop(ItemStack stack)
	{
		if(stack.isEmpty())
		{
			return false;
		}

		boolean listed = filteredItems.contains(stack.getItem())
				|| filteredTags.stream().anyMatch(tag -> BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).is(tag))
				|| filteredRarities.contains(stack.getRarity());
		return getMode() == PickupFilterMode.WHITELIST ? !listed : listed;
	}

	private static PickupFilterMode getMode()
	{
		return (PickupFilterMode) TriWasaConfigs.PICKUP_FILTER_MODE.getOptionListValue();
	}

	private static PickupFilterFeedback getFeedback()
	{
		return (PickupFilterFeedback) TriWasaConfigs.PICKUP_FILTER_FEEDBACK.getOptionListValue();
	}

	private static void rebuildMatchers()
	{
		Set<Item> items = new LinkedHashSet<>();
		Set<TagKey<Item>> tags = new LinkedHashSet<>();
		Set<Rarity> rarities = new LinkedHashSet<>();

		for(String entry : TriWasaConfigs.PICKUP_FILTER_ITEMS.getStrings())
		{
			String normalized = normalizeEntry(entry);
			if(normalized.isEmpty())
			{
				continue;
			}

			if(normalized.charAt(0) == '#')
			{
				Identifier id = Identifier.tryParse(normalized.substring(1));
				if(id != null)
				{
					tags.add(TagKey.create(Registries.ITEM, id));
				}
				continue;
			}

			Identifier id = Identifier.tryParse(normalized);
			if(id == null)
			{
				TriWasa.LOGGER.warn("[Pickup Filter] Ignoring invalid item id '{}'", entry);
				continue;
			}

			BuiltInRegistries.ITEM.getOptional(id).ifPresentOrElse(
					items::add,
					() -> TriWasa.LOGGER.warn("[Pickup Filter] Unknown item id '{}'", id)
			);
		}

		for(String entry : TriWasaConfigs.PICKUP_FILTER_RARITIES.getStrings())
		{
			String normalized = normalizeEntry(entry);
			try
			{
				rarities.add(Rarity.valueOf(normalized.toUpperCase(Locale.ROOT)));
			}
			catch(IllegalArgumentException exception)
			{
				TriWasa.LOGGER.warn("[Pickup Filter] Ignoring invalid rarity '{}'", entry);
			}
		}

		filteredItems = Set.copyOf(items);
		filteredTags = Set.copyOf(tags);
		filteredRarities = Set.copyOf(rarities);
	}

	private static String normalizeEntry(String entry)
	{
		String normalized = entry.trim().toLowerCase(Locale.ROOT);
		if(normalized.isEmpty() || normalized.charAt(0) == '#')
		{
			return normalized;
		}
		return normalized.indexOf(':') >= 0 ? normalized : "minecraft:" + normalized;
	}

	private record DropTarget(AbstractContainerMenu menu, Slot slot)
	{
	}

	private static final class PendingPickup
	{
		private final ItemStack template;
		private int remainingAmount;

		private PendingPickup(ItemStack template)
		{
			this.template = template;
		}
	}
}
