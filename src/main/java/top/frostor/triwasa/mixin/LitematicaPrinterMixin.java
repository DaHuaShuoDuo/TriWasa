package top.frostor.triwasa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.easyplace.EasyPlaceClientEncoder;

@Mixin(targets = "me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils")
public abstract class LitematicaPrinterMixin
{
	@ModifyExpressionValue(
			method = "usePrecisionPlacement",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;applyCarpetProtocolHitVec(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private static Vec3 triwasa$encodePrinter(
			Vec3 original,
			@Local(argsOnly = true) BlockPos pos,
			@Local(argsOnly = true) BlockState state
	)
	{
		Vec3 encoded = EasyPlaceClientEncoder.encode(pos, state, original);
		return encoded == null ? original : encoded;
	}
}
