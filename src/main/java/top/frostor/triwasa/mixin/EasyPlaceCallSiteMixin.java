package top.frostor.triwasa.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceClientEncoder;
import top.frostor.triwasa.quickshulker.QuickShulkerHandRestock;

@Mixin(EasyPlaceUtils.class)
public abstract class EasyPlaceCallSiteMixin
{
	@Inject(method = "handleEasyPlace", at = @At("HEAD"), cancellable = true)
	private static void triwasa$pauseDuringShulkerRestock(CallbackInfoReturnable<InteractionResult> cir)
	{
		if(QuickShulkerHandRestock.isRestocking())
		{
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@ModifyExpressionValue(
			method = "handleEasyPlace",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/util/EasyPlaceUtils;applyCarpetProtocolHitVec(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
			)
	)
	private static Vec3 triwasa$encodeHandleEasyPlace(
			Vec3 original,
			@Local(name = "clickPos") BlockPos pos,
			@Local(name = "stateSchematic") BlockState state
	)
	{
		Vec3 encoded = EasyPlaceClientEncoder.encode(pos, state, original);
		return encoded == null ? original : encoded;
	}
}
