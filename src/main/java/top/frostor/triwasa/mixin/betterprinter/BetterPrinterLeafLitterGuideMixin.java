package top.frostor.triwasa.mixin.betterprinter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

import java.util.Optional;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.guides.FlowerBedGuide")
public abstract class BetterPrinterLeafLitterGuideMixin
{
	@WrapOperation(
			method = "onBuildActionWrongState",
			at = @At(
					value = "INVOKE",
					target = "Lme/aleksilassila/litematica/printer/guide/guides/FlowerBedGuide;getProperty(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/util/Optional;"
			)
	)
	private Optional<Integer> triwasa$useLeafLitterAmount(
			BlockState state,
			Property<Integer> property,
			Operation<Optional<Integer>> original
	)
	{
		BetterPrinterGuideAccessor accessor = (BetterPrinterGuideAccessor) (Object) this;
		if(EasyPlaceRules.isPrinterEnabled()
				&& accessor.triwasa$getRequiredBlock() instanceof LeafLitterBlock)
		{
			return original.call(state, SegmentableBlock.AMOUNT);
		}
		return original.call(state, property);
	}
}
