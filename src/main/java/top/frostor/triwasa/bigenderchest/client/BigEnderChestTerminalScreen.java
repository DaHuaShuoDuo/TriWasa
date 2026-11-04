package top.frostor.triwasa.bigenderchest.client;

import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.malilib.config.ConfigManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.bigenderchest.BigEnderChestAmountFontSize;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortDirection;
import top.frostor.triwasa.bigenderchest.BigEnderChestSortField;
import top.frostor.triwasa.bigenderchest.BigEnderChestTerminalStyle;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestActionDispatcher;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestClientNetwork;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestSessionState;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;
import top.frostor.triwasa.config.TriWasaConfigGui;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.mixin.SlotPositionAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dynamic, data-driven FCA big ender chest terminal.
 */
public final class BigEnderChestTerminalScreen
		extends AbstractContainerScreen<AbstractContainerMenu>
{
	private static final int TEXT_COLOR = 0xff000000;
	private static final SoundEvent TERMINAL_HELP_SOUND =
			SoundEvent.createVariableRangeEvent(
					Identifier.fromNamespaceAndPath("triwasa", "terminal_help")
			);
	private static final String[] SIDE_BUTTON_IDS = {
			"sideButton1",
			"sideButton2",
			"sideButton3",
			"sideButton4",
			"sideButton5",
			"sideButton6"
	};
	private static String rememberedSearch = "";

	private final BigEnderChestRenderer renderer = new BigEnderChestRenderer();
	private final BigEnderChestViewController view = new BigEnderChestViewController();
	private final BigEnderChestRecipeController recipeController =
			new BigEnderChestRecipeController();
	private BigEnderChestLayout layout;
	private BigEnderChestLayout.Computed computed;
	private EditBox searchBox;
	private int relativeMouseX;
	private int relativeMouseY;
	private boolean layoutErrorShown;
	private boolean draggingScrollbar;
	private int scrollbarGrabOffset;
	private int helpSoundPlayCount;

	public BigEnderChestTerminalScreen(
			AbstractContainerMenu menu,
			Inventory inventory,
			Component title
	)
	{
		super(menu, inventory, title, 195, 200);
		try
		{
			this.layout = BigEnderChestLayoutLoader.loadDefault();
			this.computed = this.layout.compute(
					this.selectedStyle().getStringValue(),
					this.height
			);
		}
		catch(RuntimeException exception)
		{
			TriWasa.LOGGER.error("Failed to prepare big ender chest terminal layout", exception);
			this.layout = null;
		}
	}

	@Override
	protected void init()
	{
		String initialSearch = this.searchBox != null
				? this.searchBox.getValue()
				: TriWasaConfigs.BIG_ENDER_CHEST_REMEMBER_SEARCH.getBooleanValue()
						? rememberedSearch
						: "";
		super.init();
		if(this.layout == null)
		{
			if(!this.layoutErrorShown)
			{
				this.layoutErrorShown = true;
				this.message("triwasa.big_ender_chest.error.layout_invalid");
			}
			return;
		}
		this.rebuildLayout();
		this.refreshView();
		this.searchBox = new EditBox(
				this.font,
				this.leftPos + this.computed.bounds("searchMiddle").x() + 1,
				this.topPos + this.computed.bounds("searchMiddle").y() + 2,
				Math.max(20, this.computed.bounds("searchMiddle").w() - 2),
				12,
				Component.translatable("triwasa.big_ender_chest.search")
		);
		this.searchBox.setBordered(false);
		this.searchBox.setMaxLength(120);
		this.searchBox.setTextColor(TEXT_COLOR);
		this.searchBox.setHint(
				Component.translatable("triwasa.big_ender_chest.search")
		);
		this.searchBox.setResponder(value -> {
			this.view.setQuery(value);
		});
		this.searchBox.setValue(initialSearch);
		this.addRenderableWidget(this.searchBox);
	}

	@Override
	protected void containerTick()
	{
		super.containerTick();
		this.refreshView();
	}

	private void refreshView()
	{
		BigEnderChestSessionState session = BigEnderChestClientNetwork.state();
		this.view.refresh(session.entriesBySerial(), session.revision());
		this.view.setSortField(this.selectedSortField()
				== BigEnderChestSortField.NAME
				? BigEnderChestViewController.SortField.NAME
				: BigEnderChestViewController.SortField.COUNT);
		this.view.setSortDirection(this.selectedSortDirection()
				== BigEnderChestSortDirection.ASCENDING
				? BigEnderChestViewController.SortDirection.ASCENDING
				: BigEnderChestViewController.SortDirection.DESCENDING);
		if(this.computed != null)
		{
			this.view.setViewportRows(this.computed.rowCount());
		}
	}

	@Override
	public void extractBackground(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTick
	)
	{
		this.extractTransparentBackground(graphics);
		if(this.layout == null || this.computed == null)
		{
			return;
		}
		this.updateRelativeMouse(mouseX, mouseY);
		graphics.pose().pushMatrix();
		graphics.pose().translate(this.leftPos, this.topPos);
		this.renderer.drawBackground(
				graphics,
				this.layout,
				this.computed,
				this.relativeMouseX,
				this.relativeMouseY
		);
		this.renderer.drawSearch(
				graphics,
				this.layout,
				this.computed,
				this.searchBox != null && this.searchBox.isFocused(),
				!BigEnderChestClientNetwork.ready()
		);
		graphics.pose().popMatrix();
	}

	@Override
	public void extractContents(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY,
			float partialTick
	)
	{
		super.extractContents(graphics, mouseX, mouseY, partialTick);
		if(this.layout == null || this.computed == null)
		{
			return;
		}
		this.updateRelativeMouse(mouseX, mouseY);
		this.view.refresh(
				BigEnderChestClientNetwork.state().entriesBySerial(),
				BigEnderChestClientNetwork.state().revision()
		);
		this.view.setViewportRows(this.computed.rowCount());

		graphics.pose().pushMatrix();
		graphics.pose().translate(this.leftPos, this.topPos);
		this.renderer.drawItemGrid(
				graphics,
				this.font,
				this.layout,
				this.computed,
				this.view,
				this.selectedAmountFontSize(),
				this.relativeMouseX,
				this.relativeMouseY
		);
		this.renderer.drawSideButtons(
				graphics,
				this.layout,
				this.computed,
				this.sideButtonStates(),
				this.relativeMouseX,
				this.relativeMouseY
		);
		if(!BigEnderChestClientNetwork.ready()
				|| BigEnderChestClientNetwork.resyncCoolingDown())
		{
			for(BigEnderChestLayout.SideButtonBounds button : this.computed.sideButtons())
			{
				if(button.id().equals("sideButton3"))
				{
					this.renderer.drawUnavailableOverlay(graphics, button.bounds());
				}
			}
		}
		this.renderer.drawScrollThumb(
				graphics,
				this.layout,
				this.computed,
				this.view
		);
		this.drawEquipmentItems(graphics);
		this.drawPlayer(graphics);
		this.drawStatus(graphics);
		graphics.pose().popMatrix();
	}

	@Override
	protected void extractTooltip(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY
	)
	{
		if(this.layout == null || this.computed == null)
		{
			super.extractTooltip(graphics, mouseX, mouseY);
			return;
		}
		BigEnderChestMessages.EntryRecord entry = this.renderer.hoveredEntry(
				this.view,
				this.computed,
				this.relativeMouseX,
				this.relativeMouseY
		);
		if(entry != null)
		{
			ItemStack item = entry.item();
			List<Component> tooltip = new ArrayList<>(getTooltipFromItem(this.minecraft, item));
			tooltip.add(Component.translatable(
					"triwasa.big_ender_chest.tooltip.total",
					entry.totalAmount()
			));
			graphics.setTooltipForNextFrame(
					this.font,
					tooltip,
					item.getTooltipImage(),
					mouseX,
					mouseY
			);
			return;
		}
		super.extractTooltip(graphics, mouseX, mouseY);
		String hoveredButton = this.hoveredSideButton();
		if(hoveredButton != null)
		{
			graphics.setTooltipForNextFrame(
					this.font,
					this.sideButtonTooltip(hoveredButton),
					mouseX,
					mouseY
			);
			return;
		}
		ItemStack hoveredEquipment = this.hoveredEquipment();
		if(!hoveredEquipment.isEmpty())
		{
			graphics.setTooltipForNextFrame(
					this.font,
					hoveredEquipment,
					mouseX,
					mouseY
			);
			return;
		}
		String hoveredCraftingRegion = this.hoveredCraftingRegion();
		if(hoveredCraftingRegion != null)
		{
			graphics.setTooltipForNextFrame(
					this.font,
					this.recipeController.unavailableMessage(),
					mouseX,
					mouseY
			);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		if(this.layout == null || this.computed == null)
		{
			return super.mouseClicked(event, doubleClick);
		}
		double localX = event.x() - this.leftPos;
		double localY = event.y() - this.topPos;
		this.updateRelativeMouse(event.x(), event.y());
		if(this.searchBox != null
				&& this.searchBox.isFocused()
				&& !this.isInsideAnySearchPart(localX, localY))
		{
			this.clearFocus();
		}
		if(event.button() == InputConstants.MOUSE_BUTTON_RIGHT
				&& this.isInsideAnySearchPart(localX, localY))
		{
			if(this.searchBox != null)
			{
				this.searchBox.setValue("");
			}
			return true;
		}
		String sideButton = this.sideButtonAt(localX, localY);
		if(sideButton != null)
		{
			this.handleSideButton(sideButton, event.button());
			return true;
		}
		if(this.hoveredCraftingRegion() != null)
		{
			this.message("triwasa.big_ender_chest.crafting_unsupported");
			return true;
		}
		if(this.startScrollbarDrag(localX, localY))
		{
			return true;
		}
		BigEnderChestMessages.EntryRecord entry =
				this.renderer.hoveredEntry(
						this.view,
						this.computed,
						localX,
						localY
				);
		if(entry != null || this.isInsideItemGrid(localX, localY))
		{
			this.handleStorageClick(entry, event);
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(
			MouseButtonEvent event,
			double dragX,
			double dragY
	)
	{
		if(this.draggingScrollbar)
		{
			this.updateScrollbar(event.y() - this.topPos);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event)
	{
		if(this.draggingScrollbar)
		{
			this.updateScrollbar(event.y() - this.topPos);
			this.draggingScrollbar = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(
			double mouseX,
			double mouseY,
			double scrollX,
			double scrollY
	)
	{
		if(this.layout != null
				&& this.computed != null
				&& this.isInsideItemGrid(
						mouseX - this.leftPos,
						mouseY - this.topPos
				)
				&& this.view.scrollRows(
						scrollY > 0 ? -1 : 1,
						this.computed.itemCapacity()
				))
		{
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	protected boolean hasClickedOutside(
			double mouseX,
			double mouseY,
			int guiLeft,
			int guiTop
	)
	{
		if(this.layout == null || this.computed == null)
		{
			return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop);
		}
		return mouseX < guiLeft
				|| mouseY < guiTop
				|| mouseX >= guiLeft + this.layout.screen().width()
				|| mouseY >= guiTop + this.computed.windowHeight();
	}

	@Override
	public boolean keyPressed(KeyEvent event)
	{
		if(this.searchBox != null && this.searchBox.isFocused())
		{
			return super.keyPressed(event);
		}
		if(this.minecraft.options.keyDrop.matches(event)
				&& this.layout != null
				&& this.computed != null)
		{
			BigEnderChestMessages.EntryRecord entry = this.renderer.hoveredEntry(
					this.view,
					this.computed,
					this.relativeMouseX,
					this.relativeMouseY
			);
			if(entry != null)
			{
				int action = event.hasControlDown()
						? BigEnderChestProtocol.ACTION_DROP_STACK
						: BigEnderChestProtocol.ACTION_DROP_ONE;
				this.enqueueEntryAction(action, entry, 0);
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public void removed()
	{
		BigEnderChestClientNetwork.actions().clear();
		if(TriWasaConfigs.BIG_ENDER_CHEST_REMEMBER_SEARCH.getBooleanValue()
				&& this.searchBox != null)
		{
			rememberedSearch = this.searchBox.getValue();
		}
		else
		{
			rememberedSearch = "";
		}
		super.removed();
	}

	@Override
	protected void extractLabels(
			GuiGraphicsExtractor graphics,
			int mouseX,
			int mouseY
	)
	{
		// The data-driven terminal uses its header status line instead.
	}

	private void rebuildLayout()
	{
		this.computed = this.layout.compute(
				this.selectedStyle().getStringValue(),
				this.height
		);
		this.leftPos = (this.width - this.layout.screen().width()) / 2;
		this.topPos = Math.max(
				this.layout.layoutPolicy().verticalInset(),
				(this.height - this.computed.windowHeight()) / 2
		);
		this.positionPlayerSlots();
		this.view.setViewportRows(this.computed.rowCount());
	}

	public BigEnderChestLayout.Bounds interactionBounds()
	{
		if(this.layout == null || this.computed == null)
		{
			return null;
		}
		int leftPadding = this.computed.sideButtons().stream()
				.mapToInt(button -> button.bounds().x())
				.min()
				.orElse(0);
		return new BigEnderChestLayout.Bounds(
				this.leftPos + Math.min(0, leftPadding),
				this.topPos,
				this.layout.screen().width() + Math.max(0, -leftPadding),
				this.computed.windowHeight()
		);
	}

	private void positionPlayerSlots()
	{
		int slotIndex = 0;
		for(BigEnderChestLayout.SlotHitbox hitbox : this.computed.playerSlots())
		{
			if(slotIndex >= Math.min(36, this.menu.slots.size())) return;
			Slot current = this.menu.slots.get(slotIndex);
			((SlotPositionAccessor)current).triwasa$setX(
					hitbox.bounds().x() + 1
			);
			((SlotPositionAccessor)current).triwasa$setY(
					hitbox.bounds().y() + 1
			);
			slotIndex++;
		}
		for(BigEnderChestLayout.SlotHitbox hitbox : this.computed.hotbarSlots())
		{
			if(slotIndex >= Math.min(36, this.menu.slots.size())) return;
			Slot current = this.menu.slots.get(slotIndex);
			((SlotPositionAccessor)current).triwasa$setX(
					hitbox.bounds().x() + 1
			);
			((SlotPositionAccessor)current).triwasa$setY(
					hitbox.bounds().y() + 1
			);
			slotIndex++;
		}
	}

	private void handleStorageClick(
			BigEnderChestMessages.EntryRecord entry,
			MouseButtonEvent event
	)
	{
		if(!BigEnderChestClientNetwork.ready())
		{
			this.message("triwasa.big_ender_chest.error.not_ready");
			return;
		}
		if(BigEnderChestClientNetwork.state().readOnly())
		{
			this.message("triwasa.big_ender_chest.error.read_only");
			return;
		}
		boolean carrying = !this.menu.getCarried().isEmpty();
		if(!carrying)
		{
			if(entry == null)
			{
				return;
			}
			if(event.button() == InputConstants.MOUSE_BUTTON_LEFT && hasShiftDown(event))
			{
				this.enqueueEntryAction(
						BigEnderChestProtocol.ACTION_QUICK_MOVE_TO_PLAYER,
						entry,
						0
				);
			}
			else if(event.button() == InputConstants.MOUSE_BUTTON_LEFT)
			{
				this.enqueueEntryAction(
						BigEnderChestProtocol.ACTION_TAKE_STACK,
						entry,
						0
				);
			}
			else if(event.button() == InputConstants.MOUSE_BUTTON_RIGHT)
			{
				this.enqueueEntryAction(
						BigEnderChestProtocol.ACTION_TAKE_AMOUNT,
						entry,
						Math.max(1, Math.ceilDiv(entry.item().getMaxStackSize(), 2))
				);
			}
			return;
		}
		if(event.button() == InputConstants.MOUSE_BUTTON_LEFT)
		{
			this.enqueueEntryAction(
					BigEnderChestProtocol.ACTION_PUT_STACK,
					entry,
					0
			);
		}
		else if(event.button() == InputConstants.MOUSE_BUTTON_RIGHT)
		{
			this.enqueueEntryAction(
					BigEnderChestProtocol.ACTION_PUT_ONE,
					entry,
					0
			);
		}
	}

	private boolean enqueueEntryAction(
			int action,
			BigEnderChestMessages.EntryRecord entry,
			long amount
	)
	{
		if(!BigEnderChestClientNetwork.ready())
		{
			this.message("triwasa.big_ender_chest.error.not_ready");
			return false;
		}
		if(BigEnderChestClientNetwork.state().readOnly())
		{
			this.message("triwasa.big_ender_chest.error.read_only");
			return false;
		}
		long serial = entry == null ? 0 : entry.templateSerial();
		return BigEnderChestClientNetwork.actions().enqueue(
				BigEnderChestActionDispatcher.ActionIntent.entry(
						action,
						serial,
						amount
				)
		);
	}

	private void handleSideButton(String id, int mouseButton)
	{
		if(mouseButton != InputConstants.MOUSE_BUTTON_LEFT
				&& mouseButton != InputConstants.MOUSE_BUTTON_RIGHT)
		{
			return;
		}
		this.playVanillaButtonSound();
		switch(id)
		{
			case "sideButton1" -> this.playTerminalHelpSound();
			case "sideButton2" -> {
				BigEnderChestSortField current = this.selectedSortField();
				TriWasaConfigs.BIG_ENDER_CHEST_SORT_FIELD.setOptionListValue(
						current == BigEnderChestSortField.NAME
								? BigEnderChestSortField.COUNT
								: BigEnderChestSortField.NAME
				);
				this.view.setSortField(this.selectedSortField()
						== BigEnderChestSortField.NAME
						? BigEnderChestViewController.SortField.NAME
						: BigEnderChestViewController.SortField.COUNT);
				this.saveConfigs();
			}
			case "sideButton3" -> BigEnderChestClientNetwork.requestResync(
					"triwasa.big_ender_chest.refresh_requested"
			);
			case "sideButton4" -> {
				BigEnderChestSortDirection current = this.selectedSortDirection();
				TriWasaConfigs.BIG_ENDER_CHEST_SORT_DIRECTION.setOptionListValue(
						current == BigEnderChestSortDirection.ASCENDING
								? BigEnderChestSortDirection.DESCENDING
								: BigEnderChestSortDirection.ASCENDING
				);
				this.view.setSortDirection(this.selectedSortDirection()
						== BigEnderChestSortDirection.ASCENDING
						? BigEnderChestViewController.SortDirection.ASCENDING
						: BigEnderChestViewController.SortDirection.DESCENDING);
				this.saveConfigs();
			}
			case "sideButton5" -> {
				if(this.minecraft.player != null)
				{
					this.minecraft.player.sendSystemMessage(Component.translatable(
							"triwasa.big_ender_chest.settings_closes_session"
					));
				}
				BigEnderChestClientNetwork.actions().clear();
				this.minecraft.player.closeContainer();
				this.minecraft.gui.setScreen(new TriWasaConfigGui());
			}
			case "sideButton6" -> {
				BigEnderChestTerminalStyle next = this.selectedStyle().cycle(true)
						instanceof BigEnderChestTerminalStyle style
						? style
						: BigEnderChestTerminalStyle.STANDARD;
				TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_STYLE.setOptionListValue(next);
				this.rebuildLayout();
				if(this.searchBox != null)
				{
					this.searchBox.setY(
							this.topPos + this.computed.bounds("searchMiddle").y() + 2
					);
				}
				this.saveConfigs();
			}
			default -> {
			}
		}
	}

	private Map<String, String> sideButtonStates()
	{
		return Map.of(
				"sideButton1", this.sideButtonStateId("sideButton1", 0),
				"sideButton2", this.sideButtonStateId(
						"sideButton2",
						this.selectedSortField().ordinal()
				),
				"sideButton3", this.sideButtonStateId("sideButton3", 0),
				"sideButton4", this.sideButtonStateId(
						"sideButton4",
						this.selectedSortDirection().ordinal()
				),
				"sideButton5", this.sideButtonStateId("sideButton5", 0),
				"sideButton6", this.sideButtonStateId(
						"sideButton6",
						this.selectedStyle().ordinal()
				)
		);
	}

	private String sideButtonStateId(String buttonId, int stateIndex)
	{
		BigEnderChestLayout.SideButton button = this.layout.sideButtons().stream()
				.filter(candidate -> candidate.id().equals(buttonId))
				.findFirst()
				.orElseThrow();
		int index = Math.clamp(stateIndex, 0, button.states().size() - 1);
		return button.states().get(index).id();
	}

	private Component sideButtonTooltip(String id)
	{
		return switch(id)
		{
			case "sideButton1" -> Component.translatable("triwasa.big_ender_chest.button.what");
			case "sideButton2" -> Component.translatable(
					"triwasa.big_ender_chest.button.sort_by",
					this.selectedSortField().getDisplayName()
			);
			case "sideButton3" -> Component.translatable(
					"triwasa.big_ender_chest.button.refresh"
			);
			case "sideButton4" -> Component.translatable(
					"triwasa.big_ender_chest.button.queue_by",
					this.selectedSortDirection().getDisplayName()
			);
			case "sideButton5" -> Component.translatable(
					"triwasa.big_ender_chest.button.settings"
			);
			case "sideButton6" -> Component.translatable(
					"triwasa.big_ender_chest.button.list_size",
					this.selectedStyle().getDisplayName()
			);
			default -> Component.empty();
		};
	}

	private String sideButtonAt(double mouseX, double mouseY)
	{
		for(BigEnderChestLayout.SideButtonBounds button : this.computed.sideButtons())
		{
			if(button.bounds().contains(mouseX, mouseY))
			{
				return button.id();
			}
		}
		return null;
	}

	private String hoveredSideButton()
	{
		return this.sideButtonAt(this.relativeMouseX, this.relativeMouseY);
	}

	private boolean isInsideItemGrid(double mouseX, double mouseY)
	{
		BigEnderChestLayout.Bounds itemSlot = this.computed.bounds("itemSlot");
		return mouseX >= itemSlot.x()
				&& mouseY >= itemSlot.y()
				&& mouseX < itemSlot.x() + 9 * 18
				&& mouseY < itemSlot.y() + this.computed.rowCount() * 18;
	}

	private boolean isInsideAnySearchPart(double mouseX, double mouseY)
	{
		return this.computed.bounds("searchHead").contains(mouseX, mouseY)
				|| this.computed.bounds("searchMiddle").contains(mouseX, mouseY)
				|| this.computed.bounds("searchTail").contains(mouseX, mouseY);
	}

	private boolean startScrollbarDrag(double mouseX, double mouseY)
	{
		BigEnderChestLayout.Bounds track = this.scrollTrackBounds();
		if(track == null || !track.contains(mouseX, mouseY))
		{
			return false;
		}
		BigEnderChestLayout.Bounds thumb = this.scrollThumbBounds();
		this.scrollbarGrabOffset = thumb.contains(mouseX, mouseY)
				? (int)Math.round(mouseY - thumb.y())
				: thumb.h() / 2;
		this.draggingScrollbar = true;
		this.updateScrollbar(mouseY);
		return true;
	}

	private void updateScrollbar(double mouseY)
	{
		BigEnderChestLayout.Bounds track = this.scrollTrackBounds();
		BigEnderChestLayout.Bounds thumb = this.scrollThumbBounds();
		int travel = Math.max(0, track.h() - thumb.h());
		int maxRow = Math.max(
				1,
				Math.ceilDiv(
						Math.max(0, this.view.filteredCount() - this.computed.itemCapacity()),
						9
				)
		);
		if(travel <= 0)
		{
			this.view.setFirstRow(0);
			return;
		}
		double thumbY = Math.clamp(
				mouseY - this.scrollbarGrabOffset,
				track.y(),
				track.y() + travel
		);
		int row = (int)Math.round((thumbY - track.y()) / travel * maxRow);
		this.view.setFirstRow(row);
	}

	private BigEnderChestLayout.Bounds scrollTrackBounds()
	{
		BigEnderChestLayout.Bounds thumb = this.computed == null
				? null
				: this.computed.bounds("scrollThumb");
		if(thumb == null)
		{
			return null;
		}
		return new BigEnderChestLayout.Bounds(
				thumb.x(),
				thumb.y(),
				thumb.w(),
				this.computed.rowCount() * 18
		);
	}

	private BigEnderChestLayout.Bounds scrollThumbBounds()
	{
		BigEnderChestLayout.Bounds track = this.scrollTrackBounds();
		BigEnderChestLayout.Bounds base = this.computed.bounds("scrollThumb");
		int travel = Math.max(0, track.h() - base.h());
		int maxRow = Math.max(
				1,
				Math.ceilDiv(
						Math.max(0, this.view.filteredCount() - this.computed.itemCapacity()),
						9
				)
		);
		int y = track.y() + travel * this.view.firstRow() / maxRow;
		return new BigEnderChestLayout.Bounds(
				base.x(),
				y,
				base.w(),
				base.h()
		);
	}

	private void drawEquipmentItems(GuiGraphicsExtractor graphics)
	{
		if(!this.hasRegion("armorHelmetSlot"))
		{
			return;
		}
		this.drawPlayerItem(
				graphics,
				this.minecraft.player.getItemBySlot(EquipmentSlot.HEAD),
				"armorHelmetSlot"
		);
		this.drawPlayerItem(
				graphics,
				this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST),
				"armorChestSlot"
		);
		this.drawPlayerItem(
				graphics,
				this.minecraft.player.getItemBySlot(EquipmentSlot.LEGS),
				"armorLegsSlot"
		);
		this.drawPlayerItem(
				graphics,
				this.minecraft.player.getItemBySlot(EquipmentSlot.FEET),
				"armorBootsSlot"
		);
		this.drawPlayerItem(
				graphics,
				this.minecraft.player.getItemBySlot(EquipmentSlot.OFFHAND),
				"offhandSlot"
		);
	}

	private void drawPlayerItem(
			GuiGraphicsExtractor graphics,
			ItemStack stack,
			String regionId
	)
	{
		if(stack.isEmpty())
		{
			return;
		}
		BigEnderChestLayout.Bounds bounds = this.computed.bounds(regionId);
		graphics.item(stack, bounds.x() + 1, bounds.y() + 1);
	}

	private void drawPlayer(GuiGraphicsExtractor graphics)
	{
		BigEnderChestLayout.Bounds bounds = this.computed.bounds("player");
		if(bounds == null)
		{
			return;
		}
		InventoryScreen.extractEntityInInventoryFollowsMouse(
				graphics,
				bounds.x(),
				bounds.y(),
				bounds.x() + bounds.w(),
				bounds.y() + bounds.h(),
				35,
				0.0f,
				this.relativeMouseX,
				this.relativeMouseY,
				this.minecraft.player
		);
	}

	private void drawStatus(GuiGraphicsExtractor graphics)
	{
		BigEnderChestLayout.Bounds search =
				this.computed.bounds("searchMiddle");
		graphics.text(
				this.font,
				Component.translatable("triwasa.big_ender_chest.title"),
				7,
				search.y() + 4,
				TEXT_COLOR,
				false
		);
		BigEnderChestLayout.Bounds inventory =
				this.computed.bounds("playerInventorySlots");
		graphics.text(
				this.font,
				Component.translatable("triwasa.big_ender_chest.inventory_title"),
				inventory.x() + 1,
				inventory.y() - 10,
				TEXT_COLOR,
				false
		);
		if(this.hasRegion("craftingMatrixSlots") && !this.recipeController.isAvailable())
		{
			String crafting = this.recipeController.unavailableMessage().getString();
			graphics.pose().pushMatrix();
			graphics.pose().translate(
					92.0f,
					this.computed.bounds("playerInventorySlots").y() - 13.0f
			);
			graphics.pose().scale(0.75f);
			graphics.text(
					this.font,
					crafting,
					0,
					0,
					TEXT_COLOR,
					false
			);
			graphics.pose().popMatrix();
		}
	}

	private void playVanillaButtonSound()
	{
		this.minecraft.getSoundManager().play(
				SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)
		);
	}

	private void playTerminalHelpSound()
	{
		this.helpSoundPlayCount++;
		TriWasa.LOGGER.info("[BigEnderChest] Played terminal help sound");
		this.minecraft.getSoundManager().play(
				SimpleSoundInstance.forUI(TERMINAL_HELP_SOUND, 1.0f)
		);
	}

	private ItemStack hoveredEquipment()
	{
		if(!this.hasRegion("armorHelmetSlot"))
		{
			return ItemStack.EMPTY;
		}
		if(this.computed.bounds("armorHelmetSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return this.minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
		}
		if(this.computed.bounds("armorChestSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return this.minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
		}
		if(this.computed.bounds("armorLegsSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return this.minecraft.player.getItemBySlot(EquipmentSlot.LEGS);
		}
		if(this.computed.bounds("armorBootsSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return this.minecraft.player.getItemBySlot(EquipmentSlot.FEET);
		}
		if(this.computed.bounds("offhandSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return this.minecraft.player.getItemBySlot(EquipmentSlot.OFFHAND);
		}
		return ItemStack.EMPTY;
	}

	private String hoveredCraftingRegion()
	{
		if(!this.hasRegion("craftingMatrixSlots"))
		{
			return null;
		}
		if(this.computed.bounds("craftingMatrixSlots").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return "craftingMatrixSlots";
		}
		if(this.computed.bounds("craftingResultSlot").contains(
				this.relativeMouseX,
				this.relativeMouseY
		))
		{
			return "craftingResultSlot";
		}
		return null;
	}

	private boolean hasRegion(String id)
	{
		return this.computed != null && this.computed.bounds(id) != null;
	}

	private void updateRelativeMouse(double mouseX, double mouseY)
	{
		this.relativeMouseX = (int)(mouseX - this.leftPos);
		this.relativeMouseY = (int)(mouseY - this.topPos);
	}

	private boolean hasShiftDown(MouseButtonEvent event)
	{
		return event.hasShiftDown() || this.minecraft.hasShiftDown();
	}

	private BigEnderChestSortField selectedSortField()
	{
		return TriWasaConfigs.BIG_ENDER_CHEST_SORT_FIELD.getOptionListValue()
				instanceof BigEnderChestSortField field
				? field
				: BigEnderChestSortField.NAME;
	}

	private BigEnderChestSortDirection selectedSortDirection()
	{
		return TriWasaConfigs.BIG_ENDER_CHEST_SORT_DIRECTION.getOptionListValue()
				instanceof BigEnderChestSortDirection direction
				? direction
				: BigEnderChestSortDirection.ASCENDING;
	}

	private BigEnderChestTerminalStyle selectedStyle()
	{
		return TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_STYLE.getOptionListValue()
				instanceof BigEnderChestTerminalStyle style
				? style
				: BigEnderChestTerminalStyle.STANDARD;
	}

	private BigEnderChestAmountFontSize selectedAmountFontSize()
	{
		return TriWasaConfigs.BIG_ENDER_CHEST_TERMINAL_AMOUNT_FONT_SIZE
				.getOptionListValue()
				instanceof BigEnderChestAmountFontSize size
				? size
				: BigEnderChestAmountFontSize.LARGE;
	}

	private void saveConfigs()
	{
		((ConfigManager)ConfigManager.getInstance()).saveAllConfigs();
	}

	private void message(String translationKey)
	{
		if(this.minecraft.player != null)
		{
			this.minecraft.player.sendOverlayMessage(Component.translatable(translationKey));
		}
	}
}
