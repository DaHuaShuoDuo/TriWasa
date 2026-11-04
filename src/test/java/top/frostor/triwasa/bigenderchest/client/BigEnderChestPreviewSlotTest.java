package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;

import java.util.List;
import java.util.Map;

public final class BigEnderChestPreviewSlotTest
{
	private static int checks;

	private BigEnderChestPreviewSlotTest()
	{
	}

	public static void run()
	{
		bindTestItem(Items.DIAMOND, "diamond", 64);
		bindTestItem(Items.DIRT, "dirt", 64);
		bindTestItem(Items.SHULKER_BOX, "shulker_box", 1);
		BigEnderChestViewController view = new BigEnderChestViewController();
		ItemContainerContents contents = ItemContainerContents.fromItems(
				List.of(new ItemStack(Items.DIAMOND, 3))
		);
		ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
		shulker.set(DataComponents.CONTAINER, contents);
		BigEnderChestMessages.EntryRecord entry = new BigEnderChestMessages.EntryRecord(
				7,
				shulker,
				1,
				List.of()
		);
		view.refresh(Map.of(7L, entry), 1L);

		BigEnderChestPreviewSlot slot = new BigEnderChestPreviewSlot(0, 10, 20);
		slot.updateEntry(entry);
		check(
				contents.equals(slot.getItem().get(DataComponents.CONTAINER)),
				"preview slots must retain container contents"
		);
		check(slot.entry() == entry, "preview slots must retain their aggregate entry");
		check(!slot.mayPlace(new ItemStack(Items.DIRT)), "preview slots must reject insertion");
		check(!slot.mayPickup(null), "preview slots must reject extraction");
		check(slot.remove(1).isEmpty(), "preview slots must not remove their display item");
		check(slot.getMaxStackSize() == 0, "preview slots must not expose a mutable stack limit");

		slot.set(new ItemStack(Items.DIRT));
		check(
				contents.equals(slot.getItem().get(DataComponents.CONTAINER)),
				"preview slots must ignore direct stack replacement"
		);
		slot.updateEntry(null);
		check(slot.entry() == null, "cleared preview slots must not retain an aggregate entry");
		check(slot.getItem().isEmpty(), "preview slots must support clearing after refresh");
		System.out.println("BigEnderChestPreviewSlotTest passed " + checks + " checks");
	}

	@SuppressWarnings("deprecation")
	private static void bindTestItem(Item item, String name, int maxStackSize)
	{
		item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
				.set(DataComponents.MAX_STACK_SIZE, maxStackSize)
				.set(DataComponents.ITEM_NAME, Component.literal(name))
				.build());
	}

	private static void check(boolean condition, String message)
	{
		checks++;
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}
