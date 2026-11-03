package top.frostor.triwasa.bigenderchest;

import top.frostor.triwasa.bigenderchest.client.BigEnderChestLayout;
import top.frostor.triwasa.bigenderchest.client.BigEnderChestLayoutLoader;

public final class BigEnderChestLayoutTest
{
	private static int checks;

	private BigEnderChestLayoutTest()
	{
	}

	public static void run()
	{
		BigEnderChestLayout layout = BigEnderChestLayoutLoader.loadDefault();
		check(
				"terminal".equals(layout.content())
						&& layout.regions().size() == 14
						&& layout.region("craftingMatrixSlots") == null,
				"the runtime layout must be the storage-only normal terminal"
		);
		BigEnderChestLayout.Computed standard = layout.compute("standard", 400);
		check(standard.rowCount() == 6, "standard style must use six rows at 400px GUI height");
		check(
				standard.windowHeight() == 224,
				"dynamic window height must include header, rows, and terminal footer"
		);
		check(
				standard.itemCapacity() == 54,
				"all item grid columns and rows must have independent hitboxes"
		);
		check(
				standard.itemSlots().stream()
						.map(slot -> slot.bounds().x() + "," + slot.bounds().y())
						.distinct()
						.count() == 54,
				"every item grid cell must have a unique hitbox"
		);
		BigEnderChestLayout.Bounds lastItem = standard.itemSlots().getLast().bounds();
		check(
				lastItem.x() == 151 && lastItem.y() == 108,
				"the final grid column and row must not reuse the first cell hitbox"
		);
		check(
				standard.bounds("itemSlot").x() == 7
						&& standard.bounds("itemSlot").y() == 18,
				"item grid must resolve from its content-top anchor"
		);
		check(
				standard.bounds("playerInventorySlots").y() == 140,
				"player inventory must stay anchored to the dynamic screen bottom"
		);
		check(
				standard.playerSlots().size() == 27
						&& standard.hotbarSlots().size() == 9,
				"the terminal must expose 36 player inventory hitboxes"
		);
		check(
				standard.backgroundOffsets().get("rowFirst") == 1,
				"background local offsets must accumulate in declared order"
		);
		check(
				standard.bounds("rowLast").y() == 108
						&& standard.bounds("footer").y() == 126,
				"repeated rows, final row, and footer must stay contiguous"
		);
		BigEnderChestLayout.Computed compact = layout.compute("compact", 400);
		BigEnderChestLayout.Computed full = layout.compute("fullHeight", 400);
		check(
				compact.rowCount() == 3 && full.rowCount() == 13,
				"all four size styles must derive row counts from actual GUI height"
		);
		BigEnderChestLayout craftingFallback =
				BigEnderChestLayoutLoader.loadCraftingFallback();
		check(
				"equipment".equals(craftingFallback.content())
						&& craftingFallback.regions().size() == 22,
				"the crafting layout must remain available as an inactive fallback"
		);
		System.out.println("BigEnderChestLayoutTest passed " + checks + " checks");
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
