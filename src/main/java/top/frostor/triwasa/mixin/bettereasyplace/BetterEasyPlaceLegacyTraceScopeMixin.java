package top.frostor.triwasa.mixin.bettereasyplace;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.SchematicSelectionBoost;

/**
 * Covers Litematica's legacy easy-place path used when easyPlacePostRewrite is disabled. Unlike
 * the post-rewrite path it never sets the {@code EasyPlaceUtils} handling flag, so the boost scope
 * has to be opened around the trace calls of {@code doEasyPlaceAction}.
 */
@Mixin(WorldUtils.class)
public abstract class BetterEasyPlaceLegacyTraceScopeMixin
{
	@WrapOperation(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getGenericTrace(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DZZZ)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;"
			)
	)
	private static RayTraceUtils.RayTraceWrapper triwasa$boostLegacyGenericTrace(
			Level level,
			Entity entity,
			double range,
			boolean checkLayerRange,
			boolean includeFluids,
			boolean checkVerifierOverlay,
			Operation<RayTraceUtils.RayTraceWrapper> original
	)
	{
		SchematicSelectionBoost.enterLegacy();

		try
		{
			return original.call(
					level,
					entity,
					range,
					checkLayerRange,
					includeFluids,
					checkVerifierOverlay
			);
		}
		finally
		{
			SchematicSelectionBoost.exitLegacy();
		}
	}

	@WrapOperation(
			method = "doEasyPlaceAction",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/RayTraceUtils;getFurthestSchematicWorldTraceBeforeVanilla(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;D)Lfi/dy/masa/litematica/util/RayTraceUtils$RayTraceWrapper;"
			)
	)
	private static RayTraceUtils.RayTraceWrapper triwasa$boostLegacyFurthestTrace(
			Level level,
			Entity entity,
			double range,
			Operation<RayTraceUtils.RayTraceWrapper> original
	)
	{
		SchematicSelectionBoost.enterLegacy();

		try
		{
			return original.call(level, entity, range);
		}
		finally
		{
			SchematicSelectionBoost.exitLegacy();
		}
	}
}
