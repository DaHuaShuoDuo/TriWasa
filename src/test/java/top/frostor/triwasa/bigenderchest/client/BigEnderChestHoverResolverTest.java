package top.frostor.triwasa.bigenderchest.client;

public final class BigEnderChestHoverResolverTest
{
	private static int checks;

	private BigEnderChestHoverResolverTest()
	{
	}

	public static void run()
	{
		BigEnderChestLayout layout = BigEnderChestLayoutLoader.loadDefault();
		BigEnderChestLayout.Computed computed = layout.compute("standard", 400);
		BigEnderChestLayout.Bounds itemGrid = computed.bounds("itemSlot");
		BigEnderChestLayout.Bounds lastItem = computed.itemSlots().getLast().bounds();

		check(
				BigEnderChestHoverResolver.insideItemGrid(
						computed,
						itemGrid.x(),
						itemGrid.y()
				),
				"the first item-grid pixel must remain interactive"
		);
		check(
				BigEnderChestHoverResolver.insideItemGrid(
						computed,
						lastItem.x() + lastItem.w() - 1,
						lastItem.y() + lastItem.h() - 1
				),
				"the last item-grid pixel must remain interactive"
		);
		check(
				!BigEnderChestHoverResolver.insideItemGrid(
						computed,
						lastItem.x() + lastItem.w(),
						lastItem.y()
				),
				"the item grid must keep its existing right boundary"
		);

		BigEnderChestLayout.SideButtonBounds sideButton =
				computed.sideButtons().getFirst();
		BigEnderChestLayout.Bounds buttonBounds = sideButton.bounds();
		check(
				sideButton.id().equals(BigEnderChestHoverResolver.sideButtonAt(
						computed,
						buttonBounds.x(),
						buttonBounds.y()
				)),
				"side-button hit testing changed"
		);

		BigEnderChestLayout.Bounds search = computed.bounds("searchMiddle");
		check(
				BigEnderChestHoverResolver.insideAnySearchPart(
						computed,
						search.x(),
						search.y()
				),
				"search hit testing changed"
		);

		BigEnderChestLayout craftingLayout =
				BigEnderChestLayoutLoader.loadCraftingFallback();
		BigEnderChestLayout.Computed crafting =
				craftingLayout.compute("standard", 400);
		BigEnderChestLayout.Bounds matrix =
				crafting.bounds("craftingMatrixSlots");
		check(
				"craftingMatrixSlots".equals(BigEnderChestHoverResolver.craftingRegionAt(
						crafting,
						matrix.x(),
						matrix.y()
				)),
				"crafting-matrix hit testing changed"
		);
		check(
				BigEnderChestHoverResolver.craftingRegionAt(
						computed,
						itemGrid.x(),
						itemGrid.y()
				) == null,
				"the storage-only terminal must expose no crafting hitbox"
		);

		System.out.println("BigEnderChestHoverResolverTest passed " + checks + " checks");
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
