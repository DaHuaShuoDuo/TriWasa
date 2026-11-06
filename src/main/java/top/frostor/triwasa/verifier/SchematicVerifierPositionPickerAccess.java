package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SchematicVerifierPositionPickerAccess
{
	List<BlockPos> triwasa$getPositionsFor(BlockMismatch mismatch);

	void triwasa$setSelectedPositions(BlockMismatch mismatch, Collection<BlockPos> positions);

	void triwasa$selectAllForMismatch(BlockMismatch mismatch);

	Set<BlockPos> triwasa$getPinnedPositions(BlockMismatch mismatch);
}