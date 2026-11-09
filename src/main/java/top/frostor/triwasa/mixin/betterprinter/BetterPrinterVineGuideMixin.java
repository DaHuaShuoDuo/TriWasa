package top.frostor.triwasa.mixin.betterprinter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.MultiFaceBlockRules;

@Mixin(targets = "me.aleksilassila.litematica.printer.guide.guides.VineGuide")
public abstract class BetterPrinterVineGuideMixin
{
	@WrapOperation(
			method = {"onBuildActionMissingBlock", "onBuildActionWrongState"},
			at = @At(
					value = "INVOKE",
					target = "Lme/aleksilassila/litematica/printer/printer/PrinterUtils;getPropertyByName(Lnet/minecraft/world/level/block/state/BlockState;Ljava/lang/String;)Ljava/lang/Comparable;"
			)
	)
	private Comparable<?> triwasa$filterUnsupportedMultiFaceFaces(
			BlockState state,
			String propertyName,
			Operation<Comparable<?>> original
	)
	{
		BetterPrinterGuideAccessor accessor = (BetterPrinterGuideAccessor) (Object) this;
		Block requiredBlock = accessor.triwasa$getRequiredBlock();
		if(!MultiFaceBlockRules.isMultiFaceBlock(requiredBlock) || !EasyPlaceRules.isPrinterEnabled())
		{
			return original.call(state, propertyName);
		}

		Direction direction = directionByName(propertyName);
		if(direction == null)
		{
			return original.call(state, propertyName);
		}
		BlockState currentState = accessor.triwasa$getCurrentState();
		boolean alreadyAttached = currentState.is(requiredBlock)
				&& MultiFaceBlockRules.hasFace(currentState, direction);
		boolean attachable = MultiFaceBlockRules.canAttach(
				requiredBlock,
				accessor.triwasa$getLevel(),
				accessor.triwasa$getBlockPos(),
				direction
		);
		Comparable<?> requiredValue = original.call(state, propertyName);
		if(alreadyAttached || !attachable)
		{
			return Boolean.FALSE;
		}
		return requiredValue;
	}

	private static Direction directionByName(String name)
	{
		for(Direction direction : Direction.values())
		{
			if(direction.name().equals(name))
			{
				return direction;
			}
		}
		return null;
	}
}
