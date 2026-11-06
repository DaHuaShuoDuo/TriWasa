package top.frostor.triwasa.mixin;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SchematicVerifier.class)
public interface SchematicVerifierPickerInvoker
{
	@Invoker("getMapForMismatchType")
	ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> triwasa$invokeGetMapForMismatchType(
			MismatchType type
	);

	@Invoker("updateMismatchOverlays")
	void triwasa$invokeUpdateMismatchOverlays();
}