package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pixel-preserving renderer for the terminal's data-driven atlas slices.
 */
public final class BigEnderChestRenderer
{
	private static final Map<String, TextureSize> TEXTURES = Map.of(
			"crafting", new TextureSize(256, 256),
			"side_bottom", new TextureSize(24, 134),
			"sp_slots", new TextureSize(16, 80),
			"spirits", new TextureSize(128, 128),
			"terminal", new TextureSize(256, 256)
	);
	private static final Map<Identifier, Boolean> AVAILABLE_TEXTURES = new ConcurrentHashMap<>();

	public void drawBackground(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		drawRegion(graphics, layout, computed, "header", "normal");
		drawRegion(graphics, layout, computed, "rowFirst", "normal");
		drawRepeatedRows(graphics, layout, computed);
		drawRegion(graphics, layout, computed, "rowLast", "normal");
		drawRegion(graphics, layout, computed, "footer", "normal");
		drawSideBackdrop(graphics, layout, computed);

		drawGrid(graphics, layout, computed, "playerInventorySlots", computed.playerSlots(), mouseX, mouseY);
		drawGrid(graphics, layout, computed, "hotbarSlots", computed.hotbarSlots(), mouseX, mouseY);
		drawGrid(graphics, layout, computed, "craftingMatrixSlots", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "craftingResultSlot", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "armorHelmetSlot", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "armorChestSlot", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "armorLegsSlot", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "armorBootsSlot", mouseX, mouseY);
		drawGrid(graphics, layout, computed, "offhandSlot", mouseX, mouseY);
	}

	public void drawItemGrid(
			GuiGraphicsExtractor graphics,
			Font font,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			BigEnderChestViewController view,
			double mouseX,
			double mouseY
	)
	{
		for(BigEnderChestLayout.SlotHitbox hitbox : computed.itemSlots())
		{
			BigEnderChestLayout.Bounds bounds = hitbox.bounds();
			boolean hovered = bounds.contains(mouseX, mouseY);
			drawRegion(
					graphics,
					layout.region("itemSlot").material(hovered ? "hover" : "normal"),
					bounds
			);
			if(hovered && !layout.region("itemSlot").material("hover").hasTexture())
			{
				drawHoverFallback(graphics, bounds);
			}
			BigEnderChestMessages.EntryRecord entry = view.entryAtSlot(hitbox.index());
			if(entry != null)
			{
				drawEntry(graphics, font, entry, bounds);
			}
		}
	}

	public BigEnderChestMessages.EntryRecord hoveredEntry(
			BigEnderChestViewController view,
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		for(BigEnderChestLayout.SlotHitbox hitbox : computed.itemSlots())
		{
			if(hitbox.bounds().contains(mouseX, mouseY))
			{
				return view.entryAtSlot(hitbox.index());
			}
		}
		return null;
	}

	public void drawSideButtons(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			Map<String, String> stateByButton,
			double mouseX,
			double mouseY
	)
	{
		for(BigEnderChestLayout.SideButtonBounds buttonBounds : computed.sideButtons())
		{
			BigEnderChestLayout.SideButton button = layout.sideButtons().stream()
					.filter(candidate -> candidate.id().equals(buttonBounds.id()))
					.findFirst()
					.orElse(null);
			if(button == null)
			{
				continue;
			}
			String stateId = stateByButton.getOrDefault(
					button.id(),
					button.states().getFirst().id()
			);
			BigEnderChestLayout.SideButtonState state = button.states().stream()
					.filter(candidate -> candidate.id().equals(stateId))
					.findFirst()
					.orElse(button.states().getFirst());
			boolean hovered = buttonBounds.hitBounds().contains(mouseX, mouseY);
			drawRegion(
					graphics,
					hovered ? state.hover() : state.normal(),
					buttonBounds.bounds()
			);
		}
	}

	public void drawSearch(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			boolean focused,
			boolean disabled
	)
	{
		String state = disabled ? "disabled" : focused ? "focused" : "normal";
		drawRegion(graphics, layout, computed, "searchHead", state);
		drawRegion(graphics, layout, computed, "searchMiddle", state);
		drawRegion(graphics, layout, computed, "searchTail", state);
	}

	public void drawScrollThumb(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			BigEnderChestViewController view
	)
	{
		BigEnderChestLayout.Bounds thumb = computed.bounds("scrollThumb");
		if(thumb == null)
		{
			return;
		}
		int capacity = computed.itemCapacity();
		int maxRow = Math.max(
				1,
				Math.ceilDiv(Math.max(0, view.filteredCount() - capacity), 9)
		);
		int travel = Math.max(0, computed.rowCount() * 18 - thumb.h());
		int y = thumb.y() + travel * view.firstRow() / maxRow;
		drawRegion(
				graphics,
				layout.region("scrollThumb").material("normal"),
				new BigEnderChestLayout.Bounds(thumb.x(), y, thumb.w(), thumb.h())
		);
	}

	public void drawHoverFallback(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout.Bounds bounds
	)
	{
		graphics.fill(
				bounds.x() + 1,
				bounds.y() + 1,
				bounds.x() + bounds.w() - 1,
				bounds.y() + bounds.h() - 1,
				0x80ffffff
		);
	}

	public void drawUnavailableOverlay(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout.Bounds bounds
	)
	{
		graphics.fill(
				bounds.x(),
				bounds.y(),
				bounds.x() + bounds.w(),
				bounds.y() + bounds.h(),
				0x70000000
		);
	}

	private void drawGrid(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			String regionId,
			double mouseX,
			double mouseY
	)
	{
		BigEnderChestLayout.Region region = layout.region(regionId);
		BigEnderChestLayout.Bounds bounds = computed.bounds(regionId);
		if(region == null || bounds == null)
		{
			return;
		}
		int columns = region.columns();
		int rows = region.rows();
		for(int row = 0; row < rows; row++)
		{
			for(int column = 0; column < columns; column++)
			{
				BigEnderChestLayout.Bounds slot = new BigEnderChestLayout.Bounds(
						bounds.x() + column * region.pitchX(),
						bounds.y() + row * region.pitchY(),
						region.w(),
						region.h()
				);
				String state = slot.contains(mouseX, mouseY) ? "hover" : "normal";
				BigEnderChestLayout.Material material = region.material(state);
				drawRegion(graphics, material, slot);
				if("hover".equals(state)
						&& (material == null || !material.hasTexture()))
				{
					drawHoverFallback(graphics, slot);
				}
			}
		}
	}

	private void drawGrid(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			String regionId,
			List<BigEnderChestLayout.SlotHitbox> slots,
			double mouseX,
			double mouseY
	)
	{
		BigEnderChestLayout.Region region = layout.region(regionId);
		if(region == null)
		{
			return;
		}
		for(BigEnderChestLayout.SlotHitbox slot : slots)
		{
			String state = slot.bounds().contains(mouseX, mouseY) ? "hover" : "normal";
			BigEnderChestLayout.Material material = region.material(state);
			drawRegion(graphics, material, slot.bounds());
			if("hover".equals(state)
					&& (material == null || !material.hasTexture()))
			{
				drawHoverFallback(graphics, slot.bounds());
			}
		}
	}

	private void drawRepeatedRows(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed
	)
	{
		BigEnderChestLayout.Region row = layout.region("row");
		BigEnderChestLayout.Bounds bounds = computed.bounds("row");
		if(row == null || bounds == null)
		{
			return;
		}
		int repeats = Math.max(0, computed.rowCount() - 2);
		for(int index = 0; index < repeats; index++)
		{
			drawRegion(
					graphics,
					row.material("normal"),
					new BigEnderChestLayout.Bounds(
							bounds.x(),
							bounds.y() + index * layout.layoutPolicy().rowHeight(),
							bounds.w(),
							bounds.h()
					)
			);
		}
	}

	private void drawSideBackdrop(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed
	)
	{
		BigEnderChestLayout.Region side = layout.region("side");
		BigEnderChestLayout.Bounds bounds = computed.bounds("side");
		if(side == null || bounds == null)
		{
			return;
		}
		int bottom = computed.sideButtons().stream()
				.map(BigEnderChestLayout.SideButtonBounds::bounds)
				.mapToInt(button -> button.y() + button.h())
				.max()
				.orElse(bounds.y() + bounds.h());
		int height = Math.max(bounds.h(), bottom - bounds.y());
		drawRegion(
				graphics,
				side.material("normal"),
				new BigEnderChestLayout.Bounds(bounds.x(), bounds.y(), bounds.w(), height)
		);
	}

	private void drawEntry(
			GuiGraphicsExtractor graphics,
			Font font,
			BigEnderChestMessages.EntryRecord entry,
			BigEnderChestLayout.Bounds bounds
	)
	{
		ItemStack item = entry.item();
		graphics.item(item, bounds.x() + 1, bounds.y() + 1);
		graphics.itemDecorations(
				font,
				item,
				bounds.x() + 1,
				bounds.y() + 1,
				Long.toString(entry.totalAmount())
		);
	}

	private void drawRegion(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout layout,
			BigEnderChestLayout.Computed computed,
			String id,
			String state
	)
	{
		BigEnderChestLayout.Region region = layout.region(id);
		BigEnderChestLayout.Bounds bounds = computed.bounds(id);
		if(region != null && bounds != null)
		{
			drawRegion(graphics, region.material(state), bounds);
		}
	}

	private void drawRegion(
			GuiGraphicsExtractor graphics,
			BigEnderChestLayout.Material material,
			BigEnderChestLayout.Bounds target
	)
	{
		if(material == null || !material.hasTexture())
		{
			return;
		}
		TextureSize textureSize = TEXTURES.get(material.asset());
		if(textureSize == null)
		{
			drawHoverFallback(graphics, target);
			return;
		}
		Identifier texture = Identifier.fromNamespaceAndPath(
				"triwasa",
				"textures/gui/big_ender_chest/" + material.asset() + ".png"
		);
		boolean available = AVAILABLE_TEXTURES.computeIfAbsent(
				texture,
				key -> Minecraft.getInstance()
						.getResourceManager()
						.getResource(key)
						.isPresent()
		);
		if(!available)
		{
			graphics.fill(
					target.x(),
					target.y(),
					target.x() + target.w(),
					target.y() + target.h(),
					0xff2b2b2b
			);
			graphics.outline(
					target.x(),
					target.y(),
					target.x() + target.w(),
					target.y() + target.h(),
					0xffff00ff
			);
			return;
		}
		graphics.enableScissor(
				target.x(),
				target.y(),
				target.x() + target.w(),
				target.y() + target.h()
		);
		BigEnderChestLayout.Source source = material.source();
		int x = target.x() + (target.w() - source.w()) / 2;
		if("tileY".equals(material.fit()))
		{
			for(int y = target.y(); y < target.y() + target.h(); y += source.h())
			{
				graphics.blit(
						RenderPipelines.GUI_TEXTURED,
						texture,
						x,
						y,
						(float)source.x(),
						(float)source.y(),
						source.w(),
						source.h(),
						textureSize.width(),
						textureSize.height()
				);
			}
		}
		else
		{
			int y = target.y() + (target.h() - source.h()) / 2;
			graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					texture,
					x,
					y,
					(float)source.x(),
					(float)source.y(),
					source.w(),
					source.h(),
					textureSize.width(),
					textureSize.height()
			);
		}
		graphics.disableScissor();
	}

	private record TextureSize(int width, int height)
	{
	}
}
