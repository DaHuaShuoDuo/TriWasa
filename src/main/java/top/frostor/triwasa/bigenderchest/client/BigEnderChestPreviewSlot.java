package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;

/**
 * Client-only slot used to expose terminal entries to slot and tooltip integrations.
 */
final class BigEnderChestPreviewSlot extends Slot
{
	private final int viewIndex;
	@Nullable
	private BigEnderChestMessages.EntryRecord entry;

	BigEnderChestPreviewSlot(int viewIndex, int x, int y)
	{
		super(new SimpleContainer(1), 0, x, y);
		this.viewIndex = viewIndex;
	}

	int viewIndex()
	{
		return this.viewIndex;
	}

	void updateEntry(@Nullable BigEnderChestMessages.EntryRecord entry)
	{
		this.entry = entry;
		this.container.setItem(
				0,
				entry == null ? ItemStack.EMPTY : entry.item().copyWithCount(1)
		);
	}

	@Nullable
	BigEnderChestMessages.EntryRecord entry()
	{
		return this.entry;
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
