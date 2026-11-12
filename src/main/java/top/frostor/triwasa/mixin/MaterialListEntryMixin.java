package top.frostor.triwasa.mixin;

import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.malilib.util.data.ItemType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.config.TriWasaConfigs;

/**
 * Keeps the "items per block" factor out of the entry's icon stack.
 *
 * <p>{@code countTotal} already contains the factor, but the recipe JSON export multiplies by
 * {@code entry.getStack().getCount()} again, so every multi-item block is counted twice (a
 * four-segment flower bed becomes 16). The icon stack is only used for the name, the icon and
 * the max stack size, so normalising it to a count of 1 fixes the export without changing what
 * the material list displays.</p>
 */
@Mixin(MaterialListEntry.class)
public abstract class MaterialListEntryMixin
{
	@Shadow @Final @Mutable private ItemType item;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void triwasa$normalizeIconStackCount(
			ItemStack stack,
			int countTotal,
			int countMissing,
			int countMismatched,
			int countAvailable,
			CallbackInfo ci
	)
	{
		if(TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX.getBooleanValue() == false)
		{
			return;
		}

		ItemStack icon = this.item.getStack().copy();

		if(icon.getCount() != 1)
		{
			icon.setCount(1);
			this.item = new ItemType(icon, true, false);
		}
	}
}
