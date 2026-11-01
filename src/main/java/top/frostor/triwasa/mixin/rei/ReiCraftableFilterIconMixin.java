package top.frostor.triwasa.mixin.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.compat.rei.ReiFixIcons;

/**
 * Restores the crafting table icon on REI's craftable-items filter button.
 */
@Mixin(targets = "me.shedaniel.rei.impl.client.gui.widget.CraftableFilterButtonWidget")
public class ReiCraftableFilterIconMixin
{
	@Inject(method = "create", at = @At("RETURN"), cancellable = true, require = 0)
	private static void triwasa$addCraftingTableIcon(CallbackInfoReturnable<Widget> cir)
	{
		Widget filterButton = cir.getReturnValue();
		if(filterButton == null)
		{
			return;
		}

		Rectangle area = REIRuntimeImpl.getSearchField().getBounds().clone();
		area.setLocation(area.x + area.width + 4, area.y - 1);
		area.setSize(20, 20);

		Widget icon = Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
			graphics.pose().pushMatrix();
			graphics.pose().translate(area.x + 2, area.y + 2);
			graphics.renderItem(ReiFixIcons.CRAFTING_TABLE, 0, 0);
			graphics.pose().popMatrix();
		});

		cir.setReturnValue(Widgets.concat(filterButton, icon));
	}
}
