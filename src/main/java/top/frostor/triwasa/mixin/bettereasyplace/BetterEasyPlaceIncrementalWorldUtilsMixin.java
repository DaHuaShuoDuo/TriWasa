package top.frostor.triwasa.mixin.bettereasyplace;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.IncrementalBlockRules;

@Mixin(WorldUtils.class)
public abstract class BetterEasyPlaceIncrementalWorldUtilsMixin
{
	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;easyPlaceIsPositionCached(Lnet/minecraft/core/BlockPos;)Z"
			)
	)
	private static boolean triwasa$allowIncrementalQuantityPastPositionCache(
			boolean original,
			@Local(name = "pos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState requiredState
	)
	{
		BlockState currentState = triwasa$getClientState(pos);
		if(TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			return false;
		}
		return original;
	}

	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;easyPlaceIsTooFast()Z"
			)
	)
	private static boolean triwasa$allowIncrementalQuantityPastSwapInterval(
			boolean original,
			@Local(name = "pos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState requiredState
	)
	{
		BlockState currentState = triwasa$getClientState(pos);
		if(TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& IncrementalBlockRules.canIncrement(currentState, requiredState))
		{
			return false;
		}
		return original;
	}

	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;easyPlaceBlockChecksCancel(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/HitResult;Lnet/minecraft/world/item/ItemStack;)Z"
			)
	)
	private static boolean triwasa$allowIncrementalQuantityPastBlockCheck(
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
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/BlockHitResult;getLocation()Lnet/minecraft/world/phys/Vec3;",
					ordinal = 0
			)
	)
	private static Vec3 triwasa$useTopCenterForSnowIncrement(
			Vec3 original,
			@Local(name = "pos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState requiredState,
			@Local(name = "stateClient") BlockState currentState
	)
	{
		if(!triwasa$isSnowIncrement(requiredState, currentState))
		{
			return original;
		}

		Level level = Minecraft.getInstance().level;
		if(level == null)
		{
			return original;
		}

		double top = pos.getY() + currentState.getShape(level, pos).max(Direction.Axis.Y);
		return new Vec3(pos.getX() + 0.5, top, pos.getZ() + 0.5);
	}

	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;",
					ordinal = 0
			)
	)
	private static Direction triwasa$useUpFaceForSnowIncrement(
			Direction original,
			@Local(name = "stateSchematic") BlockState requiredState,
			@Local(name = "stateClient") BlockState currentState
	)
	{
		return triwasa$isSnowIncrement(requiredState, currentState) ? Direction.UP : original;
	}

	private static BlockState triwasa$getClientState(BlockPos pos)
	{
		Level level = Minecraft.getInstance().level;
		return level == null ? Blocks.AIR.defaultBlockState() : level.getBlockState(pos);
	}

	private static boolean triwasa$isSnowIncrement(
			BlockState requiredState,
			BlockState currentState
	)
	{
		return TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				&& currentState.getBlock() instanceof SnowLayerBlock
				&& IncrementalBlockRules.canIncrement(currentState, requiredState);
	}
}
