package top.frostor.triwasa.bigenderchest.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed terminal layout and its dynamic row/anchor calculations.
 */
public record BigEnderChestLayout(
		String format,
		int version,
		String content,
		ScreenDefinition screen,
		LayoutPolicy layoutPolicy,
		CanonicalDesign canonicalDesign,
		List<SideButton> sideButtons,
		List<Slice> slices,
		List<Region> regions
)
{
	public static final String FORMAT = "triwasa-terminal-layout";

	public BigEnderChestLayout
	{
		sideButtons = List.copyOf(sideButtons);
		slices = List.copyOf(slices);
		regions = List.copyOf(regions);
	}

	public Region region(String id)
	{
		return this.regions.stream()
				.filter(region -> region.id().equals(id))
				.findFirst()
				.orElse(null);
	}

	public Computed compute(String requestedStyle, int guiScaledHeight)
	{
		Style style = this.layoutPolicy.styles().stream()
				.filter(candidate -> candidate.id().equals(requestedStyle))
				.findFirst()
				.orElseGet(() -> this.layoutPolicy.styles().stream()
						.filter(candidate -> candidate.id().equals("standard"))
						.findFirst()
						.orElseThrow());
		int footerHeight = this.canonicalDesign.footerHeights()
				.getOrDefault(this.content, 0);
		int availableHeight = Math.max(
				0,
				guiScaledHeight - 2 * this.layoutPolicy.verticalInset()
		);
		int possibleRows = Math.max(
				0,
				(availableHeight
						- this.layoutPolicy.headerHeight()
						- footerHeight
						- this.layoutPolicy.rowPadding())
						/ this.layoutPolicy.rowHeight()
		);
		int rowCount = Math.max(
				this.layoutPolicy.minRows(),
				possibleRows * style.rowWeight() / 4
		);
		int contentTop = this.layoutPolicy.headerHeight();
		int contentBottom = contentTop + rowCount * this.layoutPolicy.rowHeight();
		int windowHeight = contentBottom + footerHeight;

		// The exported anchors already include the cumulative local background offsets.
		// Keep the offset stack as a validated audit of that derived layout data.
		Map<String, Bounds> bounds = new LinkedHashMap<>();
		this.regions.forEach(region ->
				bounds.put(
						region.id(),
						region.resolve(contentTop, contentBottom, windowHeight)
				));

		List<SlotHitbox> itemSlots = slotHitboxes(
				region("itemSlot"),
				bounds.get("itemSlot"),
				this.layoutPolicy.slotsPerRow(),
				rowCount
		);
		List<SlotHitbox> playerSlots = slotHitboxes(
				region("playerInventorySlots"),
				bounds.get("playerInventorySlots"),
				9,
				3
		);
		List<SlotHitbox> hotbarSlots = slotHitboxes(
				region("hotbarSlots"),
				bounds.get("hotbarSlots"),
				9,
				1
		);
		List<SideButtonBounds> sideButtonBounds = new ArrayList<>();
		for(SideButton button : this.sideButtons.stream()
				.sorted(java.util.Comparator.comparingInt(SideButton::index))
				.toList())
		{
			sideButtonBounds.add(new SideButtonBounds(
					button.id(),
					button.index(),
					button.anchor().resolve(
							contentTop,
							contentBottom,
							windowHeight,
							button.w(),
							button.h()
					)
			));
		}

		Map<String, Integer> backgroundOffsets = new LinkedHashMap<>();
		int accumulated = 0;
		for(String id : this.canonicalDesign.backgroundOffsetOrder())
		{
			Region region = region(id);
			if(region != null)
			{
				accumulated += region.localOffsetY();
			}
			backgroundOffsets.put(id, accumulated);
		}

		return new Computed(
				style,
				rowCount,
				windowHeight,
				contentTop,
				contentBottom,
				Map.copyOf(bounds),
				List.copyOf(itemSlots),
				List.copyOf(playerSlots),
				List.copyOf(hotbarSlots),
				List.copyOf(sideButtonBounds),
				Map.copyOf(backgroundOffsets)
		);
	}

	private static List<SlotHitbox> slotHitboxes(
			Region region,
			Bounds origin,
			int columns,
			int rows
	)
	{
		List<SlotHitbox> result = new ArrayList<>(columns * rows);
		if(region == null || origin == null)
		{
			return result;
		}
		for(int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				result.add(new SlotHitbox(
						result.size(),
						new Bounds(
								origin.x() + column * region.pitchX(),
								origin.y() + row * region.pitchY(),
								region.w(),
								region.h()
						)
				));
			}
		}
		return result;
	}

	public record Bounds(int x, int y, int w, int h)
	{
		public boolean contains(double mouseX, double mouseY)
		{
			return mouseX >= this.x
					&& mouseY >= this.y
					&& mouseX < this.x + this.w
					&& mouseY < this.y + this.h;
		}
	}

	public record SlotHitbox(int index, Bounds bounds)
	{
	}

	public record SideButtonBounds(String id, int index, Bounds bounds)
	{
		public Bounds hitBounds()
		{
			return new Bounds(
					this.bounds.x() - 8,
					this.bounds.y(),
					this.bounds.w() + 16,
					this.bounds.h()
			);
		}
	}

	public record Computed(
			Style style,
			int rowCount,
			int windowHeight,
			int contentTop,
			int contentBottom,
			Map<String, Bounds> bounds,
			List<SlotHitbox> itemSlots,
			List<SlotHitbox> playerSlots,
			List<SlotHitbox> hotbarSlots,
			List<SideButtonBounds> sideButtons,
			Map<String, Integer> backgroundOffsets
	)
	{
		public int itemCapacity()
		{
			return this.itemSlots.size();
		}

		public Bounds bounds(String id)
		{
			return this.bounds.get(id);
		}
	}

	public record ScreenDefinition(int width)
	{
	}

	public record LayoutPolicy(
			int rowHeight,
			int slotsPerRow,
			int headerHeight,
			int verticalInset,
			int minRows,
			int rowPadding,
			List<Style> styles
	)
	{
		public LayoutPolicy
		{
			styles = List.copyOf(styles);
		}
	}

	public record Style(String id, String label, int rowWeight)
	{
	}

	public record CanonicalDesign(
			int width,
			int headerHeight,
			int rowHeight,
			Map<String, Integer> footerHeights,
			List<String> backgroundOffsetOrder
	)
	{
		public CanonicalDesign
		{
			footerHeights = Map.copyOf(footerHeights);
			backgroundOffsetOrder = List.copyOf(backgroundOffsetOrder);
		}
	}

	public record Anchor(
			String xOrigin,
			int xOffset,
			String yOrigin,
			int yOffset
	)
	{
		public Bounds resolve(
				int contentTop,
				int contentBottom,
				int windowHeight,
				int width,
				int height
		)
		{
			int x = this.xOffset;
			int y = switch(this.yOrigin)
			{
				case "screen.top" -> 0;
				case "content.top" -> contentTop;
				case "content.bottom" -> contentBottom;
				case "screen.bottom" -> windowHeight;
				default -> throw new IllegalArgumentException("Unknown y anchor: " + this.yOrigin);
			};
			return new Bounds(x, y + this.yOffset, width, height);
		}

		private Bounds resolve(
				Region region,
				int contentTop,
				int contentBottom,
				int windowHeight
		)
		{
			return resolve(contentTop, contentBottom, windowHeight, region.w(), region.h());
		}
	}

	public record Region(
			String id,
			String kind,
			Anchor anchor,
			int localOffsetY,
			int x,
			int y,
			int w,
			int h,
			String asset,
			String sliceId,
			String sliceName,
			Source source,
			String fit,
			float opacity,
			int columns,
			int rows,
			int pitchX,
			int pitchY,
			Map<String, Material> states
	)
	{
		public Region
		{
			states = states == null ? Map.of() : Map.copyOf(states);
		}

		public Bounds resolve(int contentTop, int contentBottom, int windowHeight)
		{
			return this.anchor.resolve(
					contentTop,
					contentBottom,
					windowHeight,
					this.w,
					this.h
			);
		}

		public Material material(String state)
		{
			Material material = this.states.get(state);
			return material != null ? material : this.states.get("normal");
		}
	}

	public record Material(
			String asset,
			String sliceId,
			String sliceName,
			Source source,
			String fit,
			float opacity
	)
	{
		public boolean hasTexture()
		{
			return this.asset != null && this.source != null;
		}

		public void validate()
		{
			if(this.source == null)
			{
				return;
			}
			if(this.source.x() < 0
					|| this.source.y() < 0
					|| this.source.w() <= 0
					|| this.source.h() <= 0)
			{
				throw new IllegalArgumentException("Invalid material source rect");
			}
			if(!"center".equals(this.fit) && !"tileY".equals(this.fit))
			{
				throw new IllegalArgumentException("Unsupported fit mode: " + this.fit);
			}
		}
	}

	public record Source(int x, int y, int w, int h)
	{
	}

	public record SideButton(
			String id,
			String label,
			int index,
			Anchor anchor,
			int x,
			int y,
			int w,
			int h,
			List<SideButtonState> states
	)
	{
		public SideButton
		{
			states = List.copyOf(states);
		}
	}

	public record SideButtonState(
			String id,
			String label,
			Material normal,
			Material hover
	)
	{
	}

	public record Slice(
			String asset,
			String id,
			String name,
			Source source
	)
	{
	}
}
