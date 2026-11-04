package top.frostor.triwasa.bigenderchest.client;

import top.frostor.triwasa.bigenderchest.BigEnderChestAmountStyle;

public final class BigEnderChestTextLayoutTest
{
	private static int checks;

	private BigEnderChestTextLayoutTest()
	{
	}

	public static void run()
	{
		BigEnderChestLayout.Bounds slot = new BigEnderChestLayout.Bounds(7, 18, 18, 18);
		checkAmount(31, 45, 2.0f / 3.0f, slot, 6, BigEnderChestAmountStyle.SMALL);
		checkAmount(24, 35, 0.85f, slot, 6, BigEnderChestAmountStyle.LARGE);
		checkLegacyAmountFormula();

		BigEnderChestLayout.Bounds search =
				new BigEnderChestLayout.Bounds(82, 4, 86, 12);
		BigEnderChestLayout.Bounds inventory =
				new BigEnderChestLayout.Bounds(7, 212, 18, 18);
		BigEnderChestTextLayout.StatusPosition status =
				BigEnderChestTextLayout.status(search, inventory);
		check(status.titleX() == 8, "title x position changed");
		check(status.titleY() == 6, "title y position changed");
		check(status.inventoryTitleX() == 8, "inventory title x position changed");
		check(status.inventoryTitleY() == 201, "inventory title y position changed");
		check(status.craftingStatusX() == 92.0f, "crafting status x position changed");
		check(status.craftingStatusY() == 199.0f, "crafting status y position changed");

		System.out.println("BigEnderChestTextLayoutTest passed " + checks + " checks");
	}

	private static void checkLegacyAmountFormula()
	{
		for(BigEnderChestAmountStyle style : BigEnderChestAmountStyle.values())
		{
			float scale = style.scale();
			float inverseScale = 1.0f / scale;
			for(int slotX : new int[]{7, 97, 151})
			{
				for(int slotY : new int[]{18, 72, 108})
				{
					for(int textWidth = 1; textWidth <= 24; textWidth++)
					{
						BigEnderChestLayout.Bounds bounds =
								new BigEnderChestLayout.Bounds(slotX, slotY, 18, 18);
						BigEnderChestTextLayout.AmountPosition actual =
								BigEnderChestTextLayout.amount(bounds, textWidth, style);
						int expectedX = (int)((bounds.x() + 1
								+ style.pixelOffset() + 18.0f
								- textWidth * scale) * inverseScale);
						int expectedY = (int)((bounds.y() + 1
								+ style.pixelOffset() + 16.0f
								- 5.0f * scale - 0.5f) * inverseScale);
						check(
								actual.x() == expectedX && actual.y() == expectedY,
								"amount coordinates differ from the previous formula"
						);
					}
				}
			}
		}
	}

	private static void checkAmount(
			int expectedX,
			int expectedY,
			float expectedScale,
			BigEnderChestLayout.Bounds bounds,
			int textWidth,
			BigEnderChestAmountStyle style
	)
	{
		BigEnderChestTextLayout.AmountPosition position =
				BigEnderChestTextLayout.amount(bounds, textWidth, style);
		check(position.x() == expectedX, "amount x position changed for " + style);
		check(position.y() == expectedY, "amount y position changed for " + style);
		check(position.scale() == expectedScale, "amount scale changed for " + style);
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
