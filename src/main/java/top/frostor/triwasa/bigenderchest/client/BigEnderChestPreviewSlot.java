package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only slot used to expose terminal entries to slot and tooltip integrations.
 */
final class BigEnderChestPreviewSlot extends Slot
{
	private final int viewIndex;

	BigEnderChestPreviewSlot(int viewIndex, int x, int y)
	{
		super(new SimpleContainer(1), 0, x, y);
		this.viewIndex = viewIndex;
	}

	int viewIndex()
	{
		return this.viewIndex;
	}

	void updateItem(ItemStack item)
	{
		this.container.setItem(0, item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1));
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean mayPickup(Player player)
	{
		return false;
	}

	@Override
	public void set(ItemStack stack)
	{
	}

	@Override
	public ItemStack remove(int amount)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getMaxStackSize()
	{
		return 0;
	}
}
