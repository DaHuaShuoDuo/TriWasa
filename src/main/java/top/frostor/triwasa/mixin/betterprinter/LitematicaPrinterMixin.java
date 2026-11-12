package top.frostor.triwasa.mixin.betterprinter;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.frostor.triwasa.betterprinter.PrinterPacketAnchor;
import top.frostor.triwasa.easyplace.EasyPlaceClientEncoder;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

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
		if(!EasyPlaceRules.isPrinterEnabled())
		{
			return original;
		}
		Vec3 encoded = EasyPlaceClientEncoder.encode(pos, state, original);
		if(encoded == null)
		{
			// A native V2/V3 payload is about to be attached; drop any state that a rejected
			// click may have left behind so it can never translate this placement.
			PrinterPacketAnchor.clear();
			return original;
		}
		// The payload is anchored at the schematic target; the queued click may target the
		// support block instead, so remember the anchor until the click is actually queued.
		PrinterPacketAnchor.begin(pos, encoded);
		return encoded;
	}
}
