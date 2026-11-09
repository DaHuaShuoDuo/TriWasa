package top.frostor.triwasa.mixin.bettereasyplace;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.IncrementalBlockRules;

@Mixin(EasyPlaceUtils.class)
public abstract class BetterEasyPlaceIncrementalCheckMixin
{
	@Inject(method = "getClickPosition", at = @At("HEAD"), cancellable = true)
	private static void triwasa$useTopFaceForSnowIncrement(
			BlockHitResult targetPosition,
			BlockState requiredState,
			BlockState currentState,
			CallbackInfoReturnable<BlockHitResult> cir
	)
	{
		if(!TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				|| !(currentState.getBlock() instanceof SnowLayerBlock)
				|| !IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			return;
		}

		Level level = Minecraft.getInstance().level;
		if(level == null)
		{
			return;
		}

		BlockPos pos = targetPosition.getBlockPos();
		double top = pos.getY() + currentState.getShape(level, pos).max(Direction.Axis.Y);
		cir.setReturnValue(new BlockHitResult(
				new Vec3(pos.getX() + 0.5, top, pos.getZ() + 0.5),
				Direction.UP,
				pos,
				false
		));
	}

	@Inject(method = "canPlaceBlock", at = @At("HEAD"), cancellable = true)
	private static void triwasa$allowIncrementalQuantityBlock(
			BlockPos pos,
			Level level,
			BlockState requiredState,
			BlockState currentState,
			CallbackInfoReturnable<Boolean> cir
	)
	{
		if(TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			cir.setReturnValue(true);
		}
	}

	@ModifyExpressionValue(
			method = "handleEasyPlace",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;easyPlaceIsPositionCached(Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private static boolean triwasa$allowIncrementalQuantityPastPositionCache(
			boolean original,
			@Local(name = "stateSchematic") BlockState requiredState,
			@Local(name = "stateClient") BlockState currentState
	)
	{
		if(TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			return false;
		}
		return original;
	}

	@ModifyExpressionValue(
			method = "handleEasyPlace",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;easyPlaceIsTooFast()Z"
			)
	)
	private static boolean triwasa$allowIncrementalQuantityPastSwapInterval(
			boolean original,
			@Local(name = "stateSchematic") BlockState requiredState,
			@Local(name = "stateClient") BlockState currentState
	)
	{
		if(TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			return false;
		}
		return original;
	}
}
