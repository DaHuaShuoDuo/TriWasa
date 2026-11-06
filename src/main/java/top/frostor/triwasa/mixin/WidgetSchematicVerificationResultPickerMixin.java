package top.frostor.triwasa.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fi.dy.masa.litematica.gui.GuiSchematicVerifier;
import fi.dy.masa.litematica.gui.GuiSchematicVerifier.BlockMismatchEntry;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicVerificationResult;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.verifier.VerifierPositionPickerScreen;

@Mixin(WidgetSchematicVerificationResult.class)
public abstract class WidgetSchematicVerificationResultPickerMixin
{
	@Shadow @Final private GuiSchematicVerifier guiSchematicVerifier;
	@Shadow @Final private BlockMismatchEntry mismatchEntry;
	@Shadow @Final @Nullable private ButtonGeneric buttonIgnore;
	@Unique @Nullable private ButtonGeneric triwasa$positionPickerButton;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void triwasa$addPositionPickerButton(CallbackInfo ci)
	{
		if(this.mismatchEntry.type != BlockMismatchEntry.Type.DATA
				|| this.mismatchEntry.blockMismatch == null
				|| this.mismatchEntry.mismatchType == MismatchType.CORRECT_STATE)
		{
			return;
		}

		WidgetListEntryBase<?> widget = (WidgetListEntryBase<?>) (Object) this;
		int right = this.buttonIgnore != null
				? this.buttonIgnore.getX()
				: widget.getX() + widget.getWidth();
		String label = StringUtils.translate("triwasa.gui.verifier_position_picker.button");
		ButtonGeneric button = new ButtonGeneric(0, widget.getY() + 1, -1, true, label);
		button.setX(right - button.getWidth() - 2);
		button.setActionListener((clickedButton, mouseButton) -> openPositionPicker());
		button.setHoverStrings(StringUtils.translate("triwasa.gui.verifier_position_picker.tooltip"));
		this.triwasa$positionPickerButton = button;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void triwasa$renderPositionPickerButton(
			GuiContext ctx,
			int mouseX,
			int mouseY,
			boolean selected,
			CallbackInfo ci
	)
	{
		ButtonGeneric button = this.triwasa$positionPickerButton;
		if(button != null)
		{
			button.render(ctx, mouseX, mouseY, false);
		}
	}

	@Inject(method = "onMouseClickedImpl", at = @At("HEAD"), cancellable = true)
	private void triwasa$clickPositionPickerButton(
			MouseButtonEvent click,
			boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir
	)
	{
		ButtonGeneric button = this.triwasa$positionPickerButton;
		if(button != null && button.isMouseOver((int) click.x(), (int) click.y()))
		{
			if(button.onMouseClicked(click, doubleClick))
			{
				cir.setReturnValue(true);
			}
		}
	}

	@ModifyReturnValue(method = "canSelectAt", at = @At("RETURN"))
	private boolean triwasa$excludePositionPickerButton(boolean original, MouseButtonEvent click)
	{
		ButtonGeneric button = this.triwasa$positionPickerButton;
		return button == null || !button.isMouseOver((int) click.x(), (int) click.y())
				? original
				: false;
	}

	@Inject(method = "postRenderHovered", at = @At("HEAD"), cancellable = true)
	private void triwasa$renderPositionPickerHover(
			GuiContext ctx,
			int mouseX,
			int mouseY,
			boolean selected,
			CallbackInfo ci
	)
	{
		ButtonGeneric button = this.triwasa$positionPickerButton;
		if(button != null && button.isMouseOver(mouseX, mouseY))
		{
			button.postRenderHovered(ctx, mouseX, mouseY, false);
			ci.cancel();
		}
	}

	@Unique
	private void openPositionPicker()
	{
		if(this.mismatchEntry.blockMismatch == null)
		{
			return;
		}

		VerifierPositionPickerScreen screen = new VerifierPositionPickerScreen(
				this.guiSchematicVerifier,
				this.guiSchematicVerifier.getPlacement().getSchematicVerifier(),
				this.mismatchEntry.blockMismatch
		);
		screen.setParent(this.guiSchematicVerifier);
		GuiBase.openGui(screen);
	}
}