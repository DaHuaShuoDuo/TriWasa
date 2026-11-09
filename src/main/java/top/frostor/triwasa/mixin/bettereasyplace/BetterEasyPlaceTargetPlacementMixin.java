package top.frostor.triwasa.mixin.bettereasyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.frostor.triwasa.easyplace.BellRules;
import top.frostor.triwasa.easyplace.CandleRules;
import top.frostor.triwasa.easyplace.EasyPlaceRules;
import top.frostor.triwasa.easyplace.HangingSignRules;
import top.frostor.triwasa.mixin.EasyPlaceUtilsInvoker;

@Mixin(EasyPlaceUtils.class)
public abstract class BetterEasyPlaceTargetPlacementMixin
{
	@Inject(method = "applyPlacementProtocolAll", at = @At("RETURN"))
	private static void triwasa$useTargetPositionForUnsupportedBlock(
			BlockPos pos,
			BlockState state,
			Vec3 hitVec,
			CallbackInfoReturnable<Object> cir
	)
	{
		if(!(cir.getReturnValue() instanceof BetterEasyPlacePlacementProtocolDataAccessor data))
		{
			return;
		}

		Block block = state.getBlock();
		if(block instanceof BellBlock)
		{
			if(!EasyPlaceRules.isEnabled())
			{
				return;
			}

			BellRules.BellPlacementPlan plan = BellRules.getPlacementPlan(pos, state);
			if(plan == null)
			{
				return;
			}

			ClientLevel level = Minecraft.getInstance().level;
			if(level == null)
			{
				return;
			}

			if(!BellRules.hasRequiredWallSupport(level, plan))
			{
				data.triwasa$setHandled(true);
				data.triwasa$setMustFail(true);
				return;
			}

			EasyPlaceUtilsInvoker.triwasa$cacheEasyPlacePosition(pos);
			BellRules.activatePlan(plan);
			data.triwasa$setHandled(true);
			data.triwasa$setMustFail(false);
			data.triwasa$setPos(plan.supportPos());
			data.triwasa$setSide(plan.side());
			data.triwasa$setHitVec(plan.hitVec());
			return;
		}

		if(block instanceof CandleBlock)
		{
			if(!EasyPlaceRules.isEnabled())
			{
				return;
			}
			triwasa$useTargetPosition(data, pos, Direction.UP);
			return;
		}

		if(!(block instanceof HangingSignBlock)
				|| !EasyPlaceRules.isEnabled())
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
			HangingSignRules.ClickTarget clickTarget =
					HangingSignRules.getForcedWallClickTarget(pos, state);
			if(clickTarget != null)
			{
				triwasa$useTargetPosition(data, clickTarget.pos(), clickTarget.side());
			}
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

	private static void triwasa$useTargetPosition(
			BetterEasyPlacePlacementProtocolDataAccessor data,
			BlockPos pos,
			Direction side
	)
	{
		data.triwasa$setHandled(true);
		data.triwasa$setMustFail(false);
		data.triwasa$setPos(pos);
		data.triwasa$setSide(side);
		data.triwasa$setHitVec(Vec3.atCenterOf(pos));
	}
}
