package top.frostor.triwasa.mixin.bettereasyplace;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.SchematicSelectionBoost;

/**
 * Covers the schematic world trace used by the easy place click. The scope is opened in
 * {@code rayTraceBlocks} only, so the block list traces of the area selection, the verifier and
 * the printer are untouched.
 */
@Mixin(RayTraceUtils.class)
public abstract class BetterEasyPlaceSchematicHitboxMixin
{
	@Inject(method = "rayTraceBlocks", at = @At("HEAD"))
	private static void triwasa$enterSchematicHitboxScope(
			Level level,
			Vec3 start,
			Vec3 end,
			ClipContext.Fluid fluidMode,
			boolean checkCollisionShape,
			boolean ignoreBlockState,
			boolean includeFluids,
			int maxSteps,
			CallbackInfoReturnable<BlockHitResult> cir
	)
	{
		SchematicSelectionBoost.enterTrace(level);
	}

	@Inject(method = "rayTraceBlocks", at = @At("RETURN"))
	private static void triwasa$exitSchematicHitboxScope(
			Level level,
			Vec3 start,
			Vec3 end,
			ClipContext.Fluid fluidMode,
			boolean checkCollisionShape,
			boolean ignoreBlockState,
			boolean includeFluids,
			int maxSteps,
			CallbackInfoReturnable<BlockHitResult> cir
	)
	{
		SchematicSelectionBoost.exitTrace(level);
	}

	@ModifyExpressionValue(
			method = "traceFirstStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
			)
	)
	private static VoxelShape triwasa$expandFirstStepShape(VoxelShape original)
	{
		return SchematicSelectionBoost.expand(original);
	}

	@ModifyExpressionValue(
			method = "traceLoopSteps",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
			)
	)
	private static VoxelShape triwasa$expandLoopStepShape(VoxelShape original)
	{
		return SchematicSelectionBoost.expand(original);
	}
}
