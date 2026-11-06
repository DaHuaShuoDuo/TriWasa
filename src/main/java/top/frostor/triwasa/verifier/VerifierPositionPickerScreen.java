package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.gui.GuiSchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class VerifierPositionPickerScreen
		extends GuiListBase<BlockPos, VerifierPositionEntryWidget, VerifierPositionListWidget>
		implements ISelectionListener<BlockPos>
{
	private final GuiSchematicVerifier parent;
	private final SchematicVerifier verifier;
	private final BlockMismatch mismatch;
	private final List<BlockPos> positions;

	public VerifierPositionPickerScreen(
			GuiSchematicVerifier parent,
			SchematicVerifier verifier,
			BlockMismatch mismatch
	)
	{
		super(10, 50);
		this.parent = parent;
		this.verifier = verifier;
		this.mismatch = mismatch;
		this.positions = new ArrayList<>(getAccess().triwasa$getPositionsFor(mismatch));
		this.positions.sort(createDistanceComparator());
		this.title = StringUtils.translate(
				"triwasa.gui.verifier_position_picker.title",
				this.positions.size()
		);
	}

	@Override
	protected int getBrowserWidth()
	{
		return this.getScreenWidth() - 20;
	}

	@Override
	protected int getBrowserHeight()
	{
		return this.getScreenHeight() - 94;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int y = this.getScreenHeight() - 26;
		ButtonGeneric showAll = new ButtonGeneric(
				12,
				y,
				-1,
				true,
				StringUtils.translate("triwasa.gui.verifier_position_picker.show_all")
		);
		this.addButton(showAll, (button, mouseButton) -> showAllForMismatch());

		String backLabel = StringUtils.translate("triwasa.gui.verifier_position_picker.back");
		int backWidth = this.getStringWidth(backLabel) + 20;
		ButtonGeneric back = new ButtonGeneric(
				this.getScreenWidth() - backWidth - 10,
				y,
				backWidth,
				20,
				backLabel
		);
		this.addButton(back, (button, mouseButton) -> openParent());
	}

	@Override
	protected VerifierPositionListWidget createListWidget(int listX, int listY)
	{
		return new VerifierPositionListWidget(
				listX,
				listY,
				this.getBrowserWidth(),
				this.getBrowserHeight(),
				this,
				this.mismatch,
				this.positions
		);
	}

	@Override
	protected ISelectionListener<BlockPos> getSelectionListener()
	{
		return this;
	}

	@Override
	public void onSelectionChange(BlockPos pos)
	{
		if(pos == null)
		{
			return;
		}

		BlockMismatch current = this.verifier.getMismatchForPosition(pos);
		if(current == null || !current.equals(this.mismatch))
		{
			this.addMessage(MessageType.WARNING, "triwasa.gui.verifier_position_picker.changed");
			return;
		}

		Set<BlockPos> selectedPositions = this.getListWidget().getSelectedPositions();
		getAccess().triwasa$setSelectedPositions(this.mismatch, selectedPositions);
	}

	public BlockMismatch getMismatch()
	{
		return this.mismatch;
	}

	public boolean isPositionSelected(BlockPos pos)
	{
		return getAccess().triwasa$getPinnedPositions().contains(pos);
	}

	Set<BlockPos> getPinnedPositions()
	{
		return getAccess().triwasa$getPinnedPositions();
	}

	List<BlockPos> getPositions()
	{
		return this.positions;
	}

	private void showAllForMismatch()
	{
		getAccess().triwasa$selectAllForMismatch(this.mismatch);
		this.getListWidget().clearAllSelections();
		this.getListWidget().refreshEntries();
	}

	private void openParent()
	{
		this.parent.initGui();
		GuiBase.openGui(this.parent);
	}

	private SchematicVerifierPositionPickerAccess getAccess()
	{
		return (SchematicVerifierPositionPickerAccess) (Object) this.verifier;
	}

	private static Comparator<BlockPos> createDistanceComparator()
	{
		Minecraft minecraft = Minecraft.getInstance();
		BlockPos center = minecraft.player != null
				? minecraft.player.blockPosition()
				: BlockPos.ZERO;

		return Comparator
				.comparingDouble((BlockPos pos) -> pos.distSqr(center))
				.thenComparingInt(BlockPos::getY)
				.thenComparingInt(BlockPos::getX)
				.thenComparingInt(BlockPos::getZ);
	}
}