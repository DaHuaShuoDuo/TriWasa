package top.frostor.triwasa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.EasyPlaceClientEncoder;

/**
 * Covers Litematica's legacy easy-place path used when easyPlacePostRewrite is disabled.
 */
@Mixin(WorldUtils.class)
public abstract class EasyPlaceWorldUtilsCallSiteMixin
{
	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;applyCarpetProtocolHitVec(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private static Vec3 triwasa$encodeLegacyV2(
			Vec3 original,
			@Local(name = "pos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState state
	)
	{
		return triwasa$encode(pos, state, original);
	}

	@ModifyExpressionValue(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;applyPlacementProtocolV3(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private static Vec3 triwasa$encodeLegacyV3(
			Vec3 original,
			@Local(name = "pos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState state
	)
	{
		return triwasa$encode(pos, state, original);
	}

	private static Vec3 triwasa$encode(BlockPos pos, BlockState state, Vec3 original)
	{
		Vec3 encoded = EasyPlaceClientEncoder.encode(pos, state, original);
		return encoded == null ? original : encoded;
	}
}
