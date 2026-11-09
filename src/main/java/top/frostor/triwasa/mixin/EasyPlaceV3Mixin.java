package top.frostor.triwasa.mixin;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceClientEncoder;

@Mixin(EasyPlaceUtils.class)
public abstract class EasyPlaceV3Mixin
{
	@Inject(method = "applyPlacementProtocolV3", at = @At("RETURN"), cancellable = true)
	private static void triwasa$encodeExtraProtocolBeforeV3(
			BlockPos pos,
			BlockState state,
			Vec3 hitVec,
			CallbackInfoReturnable<Vec3> cir
	)
	{
		Vec3 encodedHitVec = EasyPlaceClientEncoder.encode(pos, state, cir.getReturnValue());
		if(encodedHitVec != null)
		{
			cir.setReturnValue(encodedHitVec);
		}
	}
}