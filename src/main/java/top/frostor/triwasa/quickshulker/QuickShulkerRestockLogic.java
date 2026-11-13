package top.frostor.triwasa.quickshulker;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.component.UseRemainder;

/**
 * Resolves the item left behind when a use consumes the selected stack.
 */
final class QuickShulkerRestockLogic
{
	private QuickShulkerRestockLogic()
	{
	}

	static ItemStack useRemainderOf(ItemStack target)
	{
		if(target.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		UseRemainder useRemainder = target.get(DataComponents.USE_REMAINDER);
		if(useRemainder != null)
		{
			return useRemainder.convertInto().create();
		}
		if(target.getItem() instanceof SolidBucketItem)
		{
			return new ItemStack(Items.BUCKET);
		}

		ItemStackTemplate craftingRemainder = target.getItem().getCraftingRemainder();
		return craftingRemainder == null ? ItemStack.EMPTY : craftingRemainder.create();
	}

	static boolean isRemainderOf(ItemStack target, ItemStack current)
	{
		if(target.isEmpty() || current.isEmpty())
		{
			return false;
		}

		ItemStack remainder = useRemainderOf(target);
		return !remainder.isEmpty() && ItemStack.isSameItemSameComponents(remainder, current);
	}

	/**
	 * Picks which hand to restock. The main hand always wins so the offhand option can never
	 * delay a main-hand restock; the offhand is only used when it needs one itself.
	 *
	 * @return the inventory slot to restock, or {@code -1} when neither hand needs one
	 */
	static int selectRestockSlot(
			boolean mainHandNeeds,
			int mainHandSlot,
			boolean offhandEnabled,
			boolean offhandNeeds
	)
	{
		if(mainHandNeeds)
		{
			return mainHandSlot;
		}
		if(offhandEnabled && offhandNeeds)
		{
			return Inventory.SLOT_OFFHAND;
		}
		return -1;
	}
}
