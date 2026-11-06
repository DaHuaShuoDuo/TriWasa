package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.gui.Icons;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class VerifierPositionListWidget
		extends WidgetListBase<BlockPos, VerifierPositionEntryWidget>
{
	private final VerifierPositionPickerScreen screen;
	private final BlockMismatch mismatch;
	private final List<BlockPos> positions;

	VerifierPositionListWidget(
			int x,
			int y,
			int width,
			int height,
			VerifierPositionPickerScreen screen,
			BlockMismatch mismatch,
			List<BlockPos> positions
	)
	{
		super(x, y, width, height, screen);
		this.screen = screen;
		this.mismatch = mismatch;
		this.positions = positions;
		this.browserEntryHeight = 22;
		this.allowMultiSelection = true;
		this.widgetSearchBar = new WidgetSearchBar(
				x + 2,
				y + 4,
				width - 14,
				14,
				0,
				Icons.FILE_ICON_SEARCH,
				LeftRight.LEFT
		);
		this.browserEntriesOffsetY = this.widgetSearchBar.getHeight() + 3;

		Set<BlockPos> pinnedPositions = screen.getPinnedPositions();
		for(BlockPos pos : positions)
		{
			if(pinnedPositions.contains(pos))
			{
				this.selectedEntries.add(pos);
			}
		}
	}

	@Override
	protected Collection<BlockPos> getAllEntries()
	{
		return this.positions;
	}

	@Override
	protected List<String> getEntryStringsForFilter(BlockPos pos)
	{
		List<String> strings = new ArrayList<>();
		strings.add(pos.toShortString().toLowerCase(Locale.ROOT));
		strings.add(pos.toString().toLowerCase(Locale.ROOT));
		addStateStrings(strings, this.mismatch.stateExpected());
		addStateStrings(strings, this.mismatch.stateFound());
		strings.add(this.mismatch.mismatchType().getDisplayname().toLowerCase(Locale.ROOT));
		return strings;
	}

	@Override
	protected VerifierPositionEntryWidget createListEntryWidget(
			int x,
			int y,
			int listIndex,
			boolean isOdd,
			BlockPos entry
	)
	{
		return new VerifierPositionEntryWidget(
				x,
				y,
				this.browserEntryWidth,
				this.getBrowserEntryHeightFor(entry),
				isOdd,
				this.screen,
				this.mismatch,
				entry,
				listIndex
		);
	}

	Set<BlockPos> getSelectedPositions()
	{
		return Set.copyOf(this.selectedEntries);
	}

	void selectAllPositions()
	{
		this.selectedEntries.clear();
		this.selectedEntries.addAll(this.positions);
		this.refreshEntries();
	}

	private static void addStateStrings(List<String> strings, BlockState state)
	{
		strings.add(state.toString().toLowerCase(Locale.ROOT));
		strings.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT));
		ItemStack stack = ItemUtils.getItemForState(state);
		strings.add(stack.getHoverName().getString().toLowerCase(Locale.ROOT));
	}
}