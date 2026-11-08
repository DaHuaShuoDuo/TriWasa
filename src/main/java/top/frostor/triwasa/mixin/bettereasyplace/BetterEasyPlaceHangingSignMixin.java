package top.frostor.triwasa.mixin.bettereasyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.HangingSignRules;
import top.frostor.triwasa.config.TriWasaConfigs;

@Mixin(EasyPlaceUtils.class)
public abstract class BetterEasyPlaceHangingSignMixin
{
	@Inject(method = "applyPlacementProtocolAll", at = @At("RETURN"))
	private static void triwasa$fixHangingSignSupport(
			BlockPos pos,
			BlockState state,
			Vec3 hitVec,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(!TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue()
				|| !(cir.getReturnValue() instanceof BetterEasyPlacePlacementProtocolDataAccessor data))
		{
			return;
		}

		Block block = state.getBlock();
		if(!(block instanceof HangingSignBlock))
		{
			return;
		}

		ClientLevel level = Minecraft.getInstance().level;
		if(level == null)
		{
			return;
		}

		if(block instanceof WallHangingSignBlock)
		{
			Direction clickFace = state.getValue(WallHangingSignBlock.FACING).getOpposite();
			data.triwasa$setHandled(true);
			data.triwasa$setMustFail(false);
			data.triwasa$setPos(pos);
			data.triwasa$setSide(clickFace);
			data.triwasa$setHitVec(Vec3.atCenterOf(pos));
			return;
		}

		if(!HangingSignRules.hasRequiredCeilingHangingSignSupport(level, pos, state))
		{
			data.triwasa$setHandled(true);
			data.triwasa$setMustFail(true);
			return;
		}

		BlockPos supportPos = pos.above();
		data.triwasa$setHandled(true);
		data.triwasa$setMustFail(false);
		data.triwasa$setPos(supportPos);
		data.triwasa$setSide(Direction.DOWN);
		data.triwasa$setHitVec(
				Vec3.atCenterOf(supportPos).relative(Direction.DOWN, 0.5)
		);
	}
}
