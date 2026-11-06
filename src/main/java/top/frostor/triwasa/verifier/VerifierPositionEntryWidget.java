package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

final class VerifierPositionEntryWidget extends WidgetListEntryBase<BlockPos>
{
	private final VerifierPositionPickerScreen screen;
	private final BlockMismatch mismatch;
	private final boolean isOdd;

	VerifierPositionEntryWidget(
			int x,
			int y,
			int width,
			int height,
			boolean isOdd,
			VerifierPositionPickerScreen screen,
			BlockMismatch mismatch,
			BlockPos pos,
			int listIndex
	)
	{
		super(x, y, width, height, pos, listIndex);
		this.screen = screen;
		this.mismatch = mismatch;
		this.isOdd = isOdd;
	}

	@Override
	public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected)
	{
		boolean isSelected = selected || this.screen.isPositionSelected(this.getEntry());
		int color = isSelected
				? 0x80606060
				: this.isMouseOver(mouseX, mouseY)
						? 0x70505050
						: this.isOdd ? 0xA0101010 : 0xA0303030;

		RenderUtils.drawRect(ctx, this.x, this.y, this.width, this.height, color);
		if(isSelected)
		{
			RenderUtils.drawOutline(ctx, this.x, this.y, this.width, this.height, 0xFFE0E0E0);
		}

		BlockState iconState = this.mismatch.stateExpected().isAir()
				? this.mismatch.stateFound()
				: this.mismatch.stateExpected();
		ItemStack stack = ItemUtils.getItemForState(iconState);
		ctx.renderItem(stack, this.x + 3, this.y + 3);
		ctx.renderItemDecorations(this.textRenderer, stack, this.x + 3, this.y + 3);

		String expected = ItemUtils.getItemForState(this.mismatch.stateExpected())
				.getHoverName()
				.getString();
		String found = ItemUtils.getItemForState(this.mismatch.stateFound())
				.getHoverName()
				.getString();
		String type = this.mismatch.mismatchType().getFormattingCode()
				+ this.mismatch.mismatchType().getDisplayname()
				+ GuiBase.TXT_RST;
		String states = expected + " -> " + found;
		String coords = "x: " + this.getEntry().getX()
				+ ", y: " + this.getEntry().getY()
				+ ", z: " + this.getEntry().getZ();

		this.drawString(ctx, this.x + 23, this.y + 7, 0xFFFFFFFF, type + "  " + states);
		int coordsX = this.x + this.width - this.getStringWidth(coords) - 18;
		this.drawString(ctx, coordsX, this.y + 7, 0xFFB0B0B0, coords);
		if(isSelected)
		{
			this.drawString(ctx, this.x + this.width - 12, this.y + 7, 0xFF55FF55, "✓");
		}
	}
}