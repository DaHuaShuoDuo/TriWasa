package top.frostor.triwasa.quickshulker;

import fi.dy.masa.malilib.util.InventoryUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigs;

import java.util.ArrayList;
import java.util.List;

/**
 * Restocks the selected hotbar item from carried shulker boxes.
 *
 * <p>This follows Tweakeroo's hand-restock target semantics: the item already held by
 * the player is replenished when its count reaches the configured low-water mark. It does
 * not consult a schematic material list. Quick Shulker is used only to open a matching
 * carried box; TriWasa owns the menu confirmation, hotbar swap, timeout and recovery.</p>
 */
public final class QuickShulkerHandRestock
{
	private static final int SHULKER_SLOT_COUNT = 27;
	private static final int OPEN_TIMEOUT_TICKS = 80;
	private static final int SWAP_TIMEOUT_TICKS = 40;
	private static final int RETRY_DELAY_TICKS = 20;
	private static final int MAX_AUTOMATIC_ATTEMPTS = 3;
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static RestockSession session;
	private static ItemStack previousMainHand = ItemStack.EMPTY;
	private static int previousHotbarSlot = -1;
	private static ItemStack pendingTarget = ItemStack.EMPTY;
	private static ItemStack pendingResidue = ItemStack.EMPTY;
	private static int pendingHotbarSlot = -1;
	private static int retryDelay;
	private static int automaticAttempts;

	private QuickShulkerHandRestock()
	{
	}

	public static void register()
	{
		ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
	}

	public static void onConfigChanged()
	{
		if(!TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK.getBooleanValue())
		{
			abortSession(false);
			resetTracking();
		}
	}

	public static boolean shouldSuppressScreen(Screen screen)
	{
		return screen != null && session != null && !session.waitingForBackpackMove;
	}

	public static boolean isRestocking()
	{
		return session != null;
	}

	private static void tick()
	{
		LocalPlayer player = CLIENT.player;
		if(player == null || CLIENT.level == null || CLIENT.gameMode == null)
		{
			reset();
			return;
		}

		if(retryDelay > 0)
		{
			retryDelay--;
		}

		if(session != null)
		{
			tickSession(player);
			return;
		}

		ItemStack current = player.getMainHandItem();
		int selectedHotbarSlot = player.getInventory().getSelectedSlot();
		int threshold = TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK_THRESHOLD.getIntegerValue();
		boolean shouldRestock = shouldRestock(current, selectedHotbarSlot, threshold);
		ItemStack target = previousHotbarSlot == selectedHotbarSlot ? previousMainHand : current;
		if(shouldRestock)
		{
			pendingTarget = target.copy();
			pendingResidue = QuickShulkerRestockLogic.isRemainderOf(target, current)
					? current.copy()
					: ItemStack.EMPTY;
			pendingHotbarSlot = selectedHotbarSlot;
			retryDelay = 0;
			automaticAttempts = 0;
		}
		previousMainHand = current.copy();
		previousHotbarSlot = selectedHotbarSlot;

		if(!canStart(player))
		{
			return;
		}
		tickPendingRestock(player, threshold);
	}

	private static boolean shouldRestock(ItemStack current, int selectedHotbarSlot, int threshold)
	{
		if(previousMainHand.isEmpty() || previousHotbarSlot != selectedHotbarSlot)
		{
			return false;
		}
		boolean reachesThreshold = previousMainHand.getCount() <= threshold;
		if(current.isEmpty())
		{
			return reachesThreshold;
		}
		if(QuickShulkerRestockLogic.isRemainderOf(previousMainHand, current))
		{
			return reachesThreshold;
		}
		if(!ItemStack.isSameItemSameComponents(previousMainHand, current))
		{
			return false;
		}
		return current.getCount() < previousMainHand.getCount() && current.getCount() <= threshold;
	}

	private static boolean canStart(LocalPlayer player)
	{
		if(!TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK.getBooleanValue()
				|| !QuickShulkerBridge.isLoaded()
				|| player.isSpectator()
				|| player.getAbilities().instabuild
				|| player.isUsingItem()
				|| CLIENT.gui.screen() != null
				|| player.containerMenu != player.inventoryMenu
				|| !player.containerMenu.getCarried().isEmpty())
		{
			return false;
		}
		return true;
	}

	private static boolean startRestock(
			LocalPlayer player,
			ItemStack target,
			ItemStack slotStack,
			int selectedHotbarSlot,
			ItemStack residue
	)
	{
		if(hasMatchingInventoryReserve(player, target, selectedHotbarSlot))
		{
			return false;
		}

		int currentCount = ItemStack.isSameItemSameComponents(target, slotStack)
				? slotStack.getCount()
				: 0;
		ShulkerCandidate candidate = findShulkerCandidate(
				player,
				target,
				currentCount,
				selectedHotbarSlot
		);
		if(candidate == null)
		{
			return false;
		}

		if(!residue.isEmpty() && hasBackpackCapacity(player, residue))
		{
			int residueMenuSlot = findInventoryMenuSlot(player.inventoryMenu, selectedHotbarSlot);
			if(residueMenuSlot < 0)
			{
				return false;
			}

			CLIENT.gameMode.handleContainerInput(
					player.inventoryMenu.containerId,
					residueMenuSlot,
					0,
					ContainerInput.QUICK_MOVE,
					player
			);
			session = new RestockSession(
					target.copy(),
					selectedHotbarSlot,
					candidate.expectedContents(),
					residue.copy(),
					candidate.shulkerStack(),
					candidate.inventoryMenuSlot(),
					true
			);
			return true;
		}

		if(!QuickShulkerBridge.open(candidate.shulkerStack(), candidate.inventoryMenuSlot()))
		{
			return false;
		}

		session = new RestockSession(
				target.copy(),
				selectedHotbarSlot,
				candidate.expectedContents(),
				residue.copy(),
				candidate.shulkerStack(),
				candidate.inventoryMenuSlot(),
				false
		);
		TriWasa.LOGGER.debug(
				"[QuickShulkerRestock] Opening shulker for {} in hotbar slot {}",
				target.getItem(),
				selectedHotbarSlot
		);
		return true;
	}

	private static void tickPendingRestock(LocalPlayer player, int threshold)
	{
		if(pendingTarget.isEmpty() || pendingHotbarSlot < 0 || retryDelay > 0)
		{
			return;
		}

		ItemStack slotStack = player.getInventory().getItem(pendingHotbarSlot);
		if(!slotStack.isEmpty())
		{
			boolean matchesTarget = ItemStack.isSameItemSameComponents(pendingTarget, slotStack);
			boolean matchesResidue = !pendingResidue.isEmpty()
					&& ItemStack.isSameItemSameComponents(pendingResidue, slotStack);
			if((!matchesTarget && !matchesResidue)
					|| (matchesTarget && slotStack.getCount() > threshold))
			{
				clearPendingRestock();
				return;
			}
		}

		boolean started = startRestock(
				player,
				pendingTarget,
				slotStack,
				pendingHotbarSlot,
				pendingResidue
		);
		if(started)
		{
			clearPendingRestock();
			return;
		}

		automaticAttempts++;
		if(automaticAttempts >= MAX_AUTOMATIC_ATTEMPTS)
		{
			clearPendingRestock();
		}
		else
		{
			retryDelay = RETRY_DELAY_TICKS;
		}
	}

	private static boolean hasMatchingInventoryReserve(
			LocalPlayer player,
			ItemStack target,
			int selectedHotbarSlot
	)
	{
		for(int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
		{
			if(slot == selectedHotbarSlot)
			{
				continue;
			}

			ItemStack stack = player.getInventory().getItem(slot);
			if(!stack.isEmpty() && ItemStack.isSameItemSameComponents(target, stack))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean hasBackpackCapacity(LocalPlayer player, ItemStack residue)
	{
		int remaining = residue.getCount();
		for(Slot slot : player.inventoryMenu.slots)
		{
			int inventorySlot = slot.getContainerSlot();
			if(slot.container == player.getInventory() && inventorySlot >= 9 && inventorySlot < 36)
			{
				remaining -= insertionCapacity(slot, residue);
			}
			if(remaining <= 0)
			{
				return true;
			}
		}
		return false;
	}

	private static boolean hasShulkerCapacity(AbstractContainerMenu menu, ItemStack residue)
	{
		int remaining = residue.getCount();
		for(int slot = 0; slot < SHULKER_SLOT_COUNT; slot++)
		{
			remaining -= insertionCapacity(menu.slots.get(slot), residue);
			if(remaining <= 0)
			{
				return true;
			}
		}
		return false;
	}

	private static int insertionCapacity(Slot slot, ItemStack stack)
	{
		if(!slot.mayPlace(stack))
		{
			return 0;
		}

		ItemStack existing = slot.getItem();
		int maxStackSize = slot.getMaxStackSize(stack);
		if(existing.isEmpty())
		{
			return maxStackSize;
		}
		if(!ItemStack.isSameItemSameComponents(existing, stack))
		{
			return 0;
		}
		return Math.max(0, maxStackSize - existing.getCount());
	}

	private static ShulkerCandidate findShulkerCandidate(
			LocalPlayer player,
			ItemStack target,
			int currentCount,
			int selectedHotbarSlot
	)
	{
		ShulkerCandidate best = null;
		int bestCount = 0;

		for(int inventorySlot = 0; inventorySlot < player.getInventory().getContainerSize(); inventorySlot++)
		{
			if(inventorySlot == selectedHotbarSlot)
			{
				continue;
			}

			ItemStack shulkerStack = player.getInventory().getItem(inventorySlot);
			if(!isShulkerBox(shulkerStack) || shulkerStack.getCount() != 1)
			{
				continue;
			}

			NonNullList<ItemStack> stored = InventoryUtils.getStoredItems(shulkerStack, SHULKER_SLOT_COUNT);
			int matchingCount = largestMatchingCount(stored, target);
			if(matchingCount <= currentCount || matchingCount <= bestCount)
			{
				continue;
			}

			int menuSlot = findInventoryMenuSlot(player.inventoryMenu, inventorySlot);
			if(menuSlot < 0)
			{
				continue;
			}

			bestCount = matchingCount;
			best = new ShulkerCandidate(
					shulkerStack.copy(),
					menuSlot,
					copyStoredContents(stored)
			);
		}
		return best;
	}

	private static int largestMatchingCount(List<ItemStack> stored, ItemStack target)
	{
		int count = 0;
		for(ItemStack stack : stored)
		{
			if(!stack.isEmpty() && ItemStack.isSameItemSameComponents(target, stack))
			{
				count = Math.max(count, stack.getCount());
			}
		}
		return count;
	}

	private static List<ItemStack> copyStoredContents(List<ItemStack> stored)
	{
		List<ItemStack> contents = new ArrayList<>(SHULKER_SLOT_COUNT);
		for(int slot = 0; slot < SHULKER_SLOT_COUNT; slot++)
		{
			contents.add(slot < stored.size() ? stored.get(slot).copy() : ItemStack.EMPTY);
		}
		return List.copyOf(contents);
	}

	private static boolean isShulkerBox(ItemStack stack)
	{
		return stack.getItem() instanceof BlockItem blockItem
				&& blockItem.getBlock() instanceof ShulkerBoxBlock;
	}

	private static int findInventoryMenuSlot(AbstractContainerMenu menu, int inventorySlot)
	{
		LocalPlayer player = CLIENT.player;
		if(player == null)
		{
			return -1;
		}

		for(Slot slot : menu.slots)
		{
			if(slot.container == player.getInventory() && slot.getContainerSlot() == inventorySlot)
			{
				return slot.index;
			}
		}
		return -1;
	}

	private static void tickBackpackMove(LocalPlayer player, RestockSession active)
	{
		if(player.getInventory().getSelectedSlot() != active.selectedHotbarSlot
				|| player.containerMenu != player.inventoryMenu
				|| !player.containerMenu.getCarried().isEmpty()
				|| CLIENT.gui.screen() != null)
		{
			abortSession(false);
			return;
		}

		// Do not open the shulker in the same tick as the backpack QUICK_MOVE. Otherwise the
		// first menu can open against unsettled state, fail to restock, and only work on retry.
		ItemStack selectedStack = player.getInventory().getItem(active.selectedHotbarSlot);
		if(!selectedStack.isEmpty())
		{
			if(!ItemStack.isSameItemSameComponents(active.residue, selectedStack))
			{
				abortSession(false);
				return;
			}
			if(++active.ticksWaiting > SWAP_TIMEOUT_TICKS)
			{
				abortSession(false);
				return;
			}
			return;
		}

		if(!QuickShulkerBridge.open(active.shulkerStack, active.shulkerMenuSlot))
		{
			queueRestockRetry(active.target, active.selectedHotbarSlot);
			session = null;
			return;
		}

		active.waitingForBackpackMove = false;
		active.residue = ItemStack.EMPTY;
		active.residueCleared = true;
		active.ticksWaiting = 0;
	}

	private static void queueRestockRetry(ItemStack target, int selectedHotbarSlot)
	{
		pendingTarget = target.copy();
		pendingResidue = ItemStack.EMPTY;
		pendingHotbarSlot = selectedHotbarSlot;
		retryDelay = RETRY_DELAY_TICKS;
		automaticAttempts = Math.max(1, automaticAttempts);
		resetTracking();
	}

	private static void tickSession(LocalPlayer player)
	{
		RestockSession active = session;
		if(active == null)
		{
			return;
		}
		if(!TriWasaConfigs.QUICK_SHULKER_HAND_RESTOCK.getBooleanValue()
				|| !QuickShulkerBridge.isLoaded())
		{
			abortSession(false);
			return;
		}

		if(active.waitingForBackpackMove)
		{
			tickBackpackMove(player, active);
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		if(menu == player.inventoryMenu)
		{
			if(++active.ticksWaiting > OPEN_TIMEOUT_TICKS)
			{
				TriWasa.LOGGER.debug("[QuickShulkerRestock] Timed out waiting for the shulker menu");
				abortSession(true);
			}
			return;
		}
		if(!active.menuConfirmed)
		{
			if(!confirmShulkerMenu(player, menu, active))
			{
				TriWasa.LOGGER.warn("[QuickShulkerRestock] Rejected an unexpected container menu");
				abortSession(true);
			}
			return;
		}
		if(menu.containerId != active.containerId || player.containerMenu != menu)
		{
			abortSession(true);
			return;
		}
		if(!active.residue.isEmpty() && !active.residueCleared)
		{
			tickResidueInShulker(menu, active);
			return;
		}

		if(!active.swapSent)
		{
			if(++active.ticksWaiting > OPEN_TIMEOUT_TICKS)
			{
				abortSession(true);
				return;
			}
			sendSwap(player, menu, active);
			return;
		}

		if(slotMatches(menu, active.targetMenuSlot, active.expectedTargetAfter)
				&& slotMatches(menu, active.sourceMenuSlot, active.expectedSourceAfter))
		{
			TriWasa.LOGGER.debug(
					"[QuickShulkerRestock] Restocked hotbar slot {} from a shulker box",
					active.selectedHotbarSlot
			);
			finishSession(player);
			return;
		}

		if(++active.ticksWaiting > SWAP_TIMEOUT_TICKS)
		{
			TriWasa.LOGGER.debug("[QuickShulkerRestock] Timed out waiting for the hotbar swap");
			abortSession(true);
		}
	}

	private static void tickResidueInShulker(AbstractContainerMenu menu, RestockSession active)
	{
		ItemStack current = menu.slots.get(active.targetMenuSlot).getItem();
		if(current.isEmpty())
		{
			active.residueCleared = true;
			active.ticksWaiting = 0;
			return;
		}
		if(!ItemStack.isSameItemSameComponents(active.residue, current))
		{
			abortSession(true);
			return;
		}

		if(!active.residueMoveSent)
		{
			ContainerInput input = hasShulkerCapacity(menu, active.residue)
					? ContainerInput.QUICK_MOVE
					: ContainerInput.THROW;
			int button = input == ContainerInput.THROW ? 1 : 0;
			active.residueMoveSent = true;
			active.ticksWaiting = 0;
			CLIENT.gameMode.handleContainerInput(
					menu.containerId,
					active.targetMenuSlot,
					button,
					input,
					CLIENT.player
			);
			return;
		}

		if(++active.ticksWaiting > SWAP_TIMEOUT_TICKS)
		{
			abortSession(true);
		}
	}

	private static boolean confirmShulkerMenu(
			LocalPlayer player,
			AbstractContainerMenu menu,
			RestockSession active
	)
	{
		if(menu.slots.size() < SHULKER_SLOT_COUNT || !menu.getCarried().isEmpty())
		{
			return false;
		}

		for(int slot = 0; slot < SHULKER_SLOT_COUNT; slot++)
		{
			if(!ItemStack.matches(active.expectedContents.get(slot), menu.slots.get(slot).getItem()))
			{
				return false;
			}
		}

		int sourceMenuSlot = findLargestMatchingSlot(menu, active.target);
		int targetMenuSlot = findInventoryMenuSlot(menu, active.selectedHotbarSlot);
		if(sourceMenuSlot < 0 || targetMenuSlot < 0)
		{
			return false;
		}

		ItemStack targetStack = menu.slots.get(targetMenuSlot).getItem();
		boolean matchesTarget = ItemStack.isSameItemSameComponents(active.target, targetStack);
		boolean matchesResidue = !active.residue.isEmpty()
				&& ItemStack.isSameItemSameComponents(active.residue, targetStack);
		if(!targetStack.isEmpty() && !matchesTarget && !matchesResidue)
		{
			return false;
		}
		active.containerId = menu.containerId;
		active.sourceMenuSlot = sourceMenuSlot;
		active.targetMenuSlot = targetMenuSlot;
		active.menuConfirmed = true;
		active.ticksWaiting = 0;
		return true;
	}

	private static int findLargestMatchingSlot(AbstractContainerMenu menu, ItemStack target)
	{
		int bestSlot = -1;
		int bestCount = 0;
		for(int slot = 0; slot < SHULKER_SLOT_COUNT; slot++)
		{
			ItemStack stack = menu.slots.get(slot).getItem();
			if(!stack.isEmpty()
					&& ItemStack.isSameItemSameComponents(target, stack)
					&& stack.getCount() > bestCount)
			{
				bestSlot = slot;
				bestCount = stack.getCount();
			}
		}
		return bestSlot;
	}

	private static void sendSwap(LocalPlayer player, AbstractContainerMenu menu, RestockSession active)
	{
		ItemStack slotStack = player.getInventory().getItem(active.selectedHotbarSlot);
		if(!menu.getCarried().isEmpty()
				|| (!slotStack.isEmpty()
				&& !ItemStack.isSameItemSameComponents(active.target, slotStack)))
		{
			abortSession(true);
			return;
		}

		active.swapSent = true;
		active.ticksWaiting = 0;
		active.expectedSourceAfter = slotStack.copy();
		active.expectedTargetAfter = menu.slots.get(active.sourceMenuSlot).getItem().copy();
		CLIENT.gameMode.handleContainerInput(
				menu.containerId,
				active.sourceMenuSlot,
				active.selectedHotbarSlot,
				ContainerInput.SWAP,
				player
		);
	}

	private static boolean slotMatches(AbstractContainerMenu menu, int slot, ItemStack expected)
	{
		return slot >= 0 && slot < menu.slots.size()
				&& ItemStack.matches(expected, menu.slots.get(slot).getItem());
	}

	private static void finishSession(LocalPlayer player)
	{
		session = null;
		clearPendingRestock();
		if(player.containerMenu != player.inventoryMenu)
		{
			player.closeContainer();
		}
		resetTracking();
	}

	private static void abortSession(boolean retry)
	{
		LocalPlayer player = CLIENT.player;
		if(session != null && player != null && player.containerMenu != player.inventoryMenu)
		{
			player.closeContainer();
		}
		if(retry && session != null && !session.target.isEmpty())
		{
			pendingTarget = session.target.copy();
			pendingResidue = ItemStack.EMPTY;
			pendingHotbarSlot = session.selectedHotbarSlot;
			retryDelay = RETRY_DELAY_TICKS;
			automaticAttempts = Math.max(1, automaticAttempts);
		}
		else if(!retry)
		{
			clearPendingRestock();
		}
		session = null;
		resetTracking();
	}

	private static void clearPendingRestock()
	{
		pendingTarget = ItemStack.EMPTY;
		pendingResidue = ItemStack.EMPTY;
		pendingHotbarSlot = -1;
		retryDelay = 0;
		automaticAttempts = 0;
	}

	private static void resetTracking()
	{
		previousMainHand = ItemStack.EMPTY;
		previousHotbarSlot = -1;
	}

	private static void reset()
	{
		session = null;
		clearPendingRestock();
		resetTracking();
	}

	private record ShulkerCandidate(
			ItemStack shulkerStack,
			int inventoryMenuSlot,
			List<ItemStack> expectedContents
	)
	{
	}

	private static final class RestockSession
	{
		private final ItemStack target;
		private final int selectedHotbarSlot;
		private final List<ItemStack> expectedContents;
		private ItemStack residue;
		private final ItemStack shulkerStack;
		private final int shulkerMenuSlot;
		private int ticksWaiting;
		private int containerId = -1;
		private int sourceMenuSlot = -1;
		private int targetMenuSlot = -1;
		private ItemStack expectedSourceAfter = ItemStack.EMPTY;
		private ItemStack expectedTargetAfter = ItemStack.EMPTY;
		private boolean waitingForBackpackMove;
		private boolean menuConfirmed;
		private boolean residueMoveSent;
		private boolean residueCleared;
		private boolean swapSent;

		private RestockSession(
				ItemStack target,
				int selectedHotbarSlot,
				List<ItemStack> expectedContents,
				ItemStack residue,
				ItemStack shulkerStack,
				int shulkerMenuSlot,
				boolean waitingForBackpackMove
		)
		{
			this.target = target;
			this.selectedHotbarSlot = selectedHotbarSlot;
			this.expectedContents = expectedContents;
			this.residue = residue;
			this.shulkerStack = shulkerStack;
			this.shulkerMenuSlot = shulkerMenuSlot;
			this.waitingForBackpackMove = waitingForBackpackMove;
			this.residueCleared = residue.isEmpty();
		}
	}
}
