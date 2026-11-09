package top.frostor.triwasa.mixin.bettereasyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "fi.dy.masa.litematica.util.EasyPlaceUtils$PlacementProtocolData")
public interface BetterEasyPlacePlacementProtocolDataAccessor
{
	@Accessor("handled")
	void triwasa$setHandled(boolean handled);

	@Accessor("mustFail")
	void triwasa$setMustFail(boolean mustFail);

	@Accessor("pos")
	void triwasa$setPos(BlockPos pos);

	@Accessor("side")
	void triwasa$setSide(Direction side);

	@Accessor("hitVec")
	void triwasa$setHitVec(Vec3 hitVec);
}
