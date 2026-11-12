package top.frostor.triwasa.mixin;

import fi.dy.masa.litematica.materials.MaterialCache;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.materiallist.ProjectionMaterialStats;

/**
 * Completes the "items per block" factor Litematica stores on the material stack.
 *
 * <p>{@code overrideStackSize} only knows six block families, so flower beds, leaf litter,
 * resin clumps and vines keep a factor of 1 and the material list counts one item per block
 * no matter how many segments or faces the schematic state has. The upstream branches run
 * first; this hook only supplies the states they skipped, and writes the same value again for
 * glow lichen / sculk veins.</p>
 */
@Mixin(MaterialCache.class)
public abstract class MaterialCacheMixin
{
	@Inject(method = "overrideStackSize", at = @At("TAIL"))
	private void triwasa$applyMultiItemCount(BlockState state, ItemStack stack, CallbackInfo ci)
	{
		if(TriWasaConfigs.PROJECTION_MATERIAL_STATS_FIX.getBooleanValue() == false)
		{
			return;
		}

		int count = ProjectionMaterialStats.itemsPerBlock(state);

		// Never zero the stack: a state this class does not own keeps the upstream result.
		if(count > 0 && stack.isEmpty() == false)
		{
			stack.setCount(count);
		}
	}
}
