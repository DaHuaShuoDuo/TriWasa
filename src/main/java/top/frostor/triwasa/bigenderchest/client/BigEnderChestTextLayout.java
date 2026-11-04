package top.frostor.triwasa.bigenderchest.client;

import top.frostor.triwasa.bigenderchest.BigEnderChestAmountStyle;

/**
 * Pure text placement shared by terminal rendering; the tests lock the previous pixel coordinates.
 */
final class BigEnderChestTextLayout
{
	private static final int AMOUNT_CONTENT_INSET = 1;
	private static final int AMOUNT_RIGHT_EDGE = 18;
	private static final int AMOUNT_BOTTOM_EDGE = 16;
	private static final float FONT_BASELINE_HEIGHT = 5.0f;
	private static final float STYLE_VERTICAL_ADJUSTMENT = 0.5f;
	private static final int TITLE_X_INSET = 1;
	private static final int TITLE_Y_FROM_SEARCH = 2;
	private static final int INVENTORY_TITLE_Y_OFFSET = -11;
	private static final float CRAFTING_STATUS_X = 92.0f;
	private static final float CRAFTING_STATUS_Y_OFFSET = -13.0f;

	private BigEnderChestTextLayout()
	{
	}

	static AmountPosition amount(
			BigEnderChestLayout.Bounds bounds,
			int textWidth,
			BigEnderChestAmountStyle style
	)
	{
		float scale = style.scale();
		float inverseScale = 1.0f / scale;
		int contentX = bounds.x() + AMOUNT_CONTENT_INSET;
		int contentY = bounds.y() + AMOUNT_CONTENT_INSET;
		int x = (int)((contentX + style.pixelOffset() + AMOUNT_RIGHT_EDGE
				- textWidth * scale) * inverseScale);
		int y = (int)((contentY + style.pixelOffset() + AMOUNT_BOTTOM_EDGE
				- FONT_BASELINE_HEIGHT * scale - STYLE_VERTICAL_ADJUSTMENT)
				* inverseScale);
		return new AmountPosition(x, y, scale);
	}

	static StatusPosition status(BigEnderChestLayout.Computed computed)
	{
		BigEnderChestLayout.Bounds search = computed.bounds("searchMiddle");
		BigEnderChestLayout.Bounds inventory = computed.bounds("playerInventorySlots");
		return status(search, inventory);
	}

	static StatusPosition status(
			BigEnderChestLayout.Bounds search,
			BigEnderChestLayout.Bounds inventory
	)
	{
		int inventoryX = inventory.x() + TITLE_X_INSET;
		return new StatusPosition(
				inventoryX,
				search.y() + TITLE_Y_FROM_SEARCH,
				inventoryX,
				inventory.y() + INVENTORY_TITLE_Y_OFFSET,
				CRAFTING_STATUS_X,
				inventory.y() + CRAFTING_STATUS_Y_OFFSET
		);
	}

	record AmountPosition(int x, int y, float scale)
	{
	}

	record StatusPosition(
			int titleX,
			int titleY,
			int inventoryTitleX,
			int inventoryTitleY,
			float craftingStatusX,
			float craftingStatusY
	)
	{
	}
}
