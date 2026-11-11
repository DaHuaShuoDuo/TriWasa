package top.frostor.triwasa.mixin;

import fi.dy.masa.litematica.util.PlacementHandler;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.EasyPlaceClientNetwork;
import top.frostor.triwasa.easyplace.EasyPlaceExtraProtocol;
import top.frostor.triwasa.easyplace.EasyPlaceStateCodec;

@Mixin(PlacementHandler.class)
public abstract class PlacementHandlerMixin
{
	@Inject(method = "getEffectiveProtocolVersion", at = @At("RETURN"), cancellable = true)
	private static void triwasa$forceV2Extra(
			CallbackInfoReturnable<EasyPlaceProtocol> cir
	)
	{
		if(EasyPlaceClientNetwork.isActive())
		{
			cir.setReturnValue(EasyPlaceProtocol.V2);
		}
	}

	@Inject(method = "applyPlacementProtocolToPlacementState", at = @At("HEAD"), cancellable = true)
	private static void triwasa$decodeExtraBeforeVersionDispatch(
			BlockState state,
			PlacementHandler.UseContext context,
			CallbackInfoReturnable<BlockState> cir
	)
	{
		triwasa$decodeExtraIfPresent(state, context, cir);
	}

	@Inject(method = "applyPlacementProtocolV2", at = @At("HEAD"), cancellable = true)
	private static void triwasa$decodeExtraProtocol(
			BlockState state,
			PlacementHandler.UseContext context,
			CallbackInfoReturnable<BlockState> cir
	)
	{
		triwasa$decodeExtraIfPresent(state, context, cir);
	}

	private static void triwasa$decodeExtraIfPresent(
			BlockState state,
			PlacementHandler.UseContext context,
			CallbackInfoReturnable<BlockState> cir
	)
	{
		if(!EasyPlaceClientNetwork.isActive())
		{
			return;
		}

		int protocolValue = EasyPlaceExtraProtocol.decode(context.hitVec(), context.pos());
		if(!EasyPlaceExtraProtocol.isExtra(protocolValue))
		{
			return;
		}

		int rawValue = EasyPlaceExtraProtocol.toRawValue(protocolValue);
		int auxiliaryValue = EasyPlaceExtraProtocol.stripAuxiliarySchema(
				EasyPlaceExtraProtocol.decodeAuxiliary(context.hitVec())
		);
		cir.setReturnValue(
				EasyPlaceStateCodec.applyPlacementState(
						state,
						context,
						rawValue,
						auxiliaryValue
				)
		);
	}
}
