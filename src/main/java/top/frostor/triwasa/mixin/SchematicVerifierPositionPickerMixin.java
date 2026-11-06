package top.frostor.triwasa.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchRenderPos;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.frostor.triwasa.verifier.SchematicVerifierPositionPickerAccess;
import top.frostor.triwasa.verifier.VerifierPositionOverlayFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(SchematicVerifier.class)
public abstract class SchematicVerifierPositionPickerMixin
		implements SchematicVerifierPositionPickerAccess
{
	@Shadow @Final private Object2ObjectOpenHashMap<BlockPos, BlockMismatch> blockMismatches;
	@Shadow @Final private Set<MismatchType> selectedCategories;
	@Shadow @Final private HashMultimap<MismatchType, BlockMismatch> selectedEntries;
	@Shadow @Final private List<MismatchRenderPos> mismatchPositionsForRender;
	@Shadow @Final private List<BlockPos> mismatchBlockPositionsForRender;
	@Unique private final Set<BlockPos> triwasa$pinnedPositions = new HashSet<>();

	@Override
	public List<BlockPos> triwasa$getPositionsFor(BlockMismatch mismatch)
	{
		if(mismatch.mismatchType() == MismatchType.CORRECT_STATE)
		{
			return List.of();
		}

		ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> map =
				this.triwasa$invoker().triwasa$invokeGetMapForMismatchType(mismatch.mismatchType());
		Pair<BlockState, BlockState> pair = Pair.of(
				mismatch.stateExpected(),
				mismatch.stateFound()
		);
		return new ArrayList<>(map.get(pair));
	}

	@Override
	public void triwasa$setSelectedPositions(BlockMismatch mismatch, Collection<BlockPos> positions)
	{
		if(mismatch.mismatchType() == MismatchType.CORRECT_STATE)
		{
			return;
		}

		ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> map =
				this.triwasa$invoker().triwasa$invokeGetMapForMismatchType(mismatch.mismatchType());
		Pair<BlockState, BlockState> pair = Pair.of(
				mismatch.stateExpected(),
				mismatch.stateFound()
		);
		Collection<BlockPos> validPositions = map.get(pair);
		Set<BlockPos> selectedPositions = new HashSet<>();

		for(BlockPos pos : positions)
		{
			if(validPositions.contains(pos))
			{
				selectedPositions.add(pos.immutable());
			}
		}

		this.triwasa$selectOnly(mismatch);
		this.triwasa$pinnedPositions.clear();
		this.triwasa$pinnedPositions.addAll(selectedPositions);
		this.triwasa$invoker().triwasa$invokeUpdateMismatchOverlays();
	}

	@Override
	public void triwasa$selectAllForMismatch(BlockMismatch mismatch)
	{
		if(mismatch.mismatchType() == MismatchType.CORRECT_STATE)
		{
			return;
		}

		this.triwasa$selectOnly(mismatch);
		this.triwasa$pinnedPositions.clear();
		this.triwasa$invoker().triwasa$invokeUpdateMismatchOverlays();
	}

	@Override
	public Set<BlockPos> triwasa$getPinnedPositions()
	{
		return Set.copyOf(this.triwasa$pinnedPositions);
	}

	@Unique
	private SchematicVerifierPickerInvoker triwasa$invoker()
	{
		return (SchematicVerifierPickerInvoker) (Object) this;
	}

	@Unique
	private void triwasa$selectOnly(BlockMismatch mismatch)
	{
		this.selectedCategories.clear();
		this.selectedEntries.clear();
		this.selectedEntries.put(mismatch.mismatchType(), mismatch);
	}

	@Inject(method = "toggleMismatchCategorySelected", at = @At("HEAD"))
	private void triwasa$clearPinOnCategorySelection(MismatchType type, CallbackInfo ci)
	{
		this.triwasa$pinnedPositions.clear();
	}

	@Inject(method = "toggleMismatchEntrySelected", at = @At("HEAD"))
	private void triwasa$clearPinOnEntrySelection(BlockMismatch mismatch, CallbackInfo ci)
	{
		this.triwasa$pinnedPositions.clear();
	}

	@Inject(method = "clearData", at = @At("HEAD"))
	private void triwasa$clearPinOnReset(CallbackInfo ci)
	{
		this.triwasa$pinnedPositions.clear();
	}

	@Inject(method = "combineClosestPositions", at = @At("RETURN"))
	private void triwasa$retainPinnedPositions(BlockPos centerPos, int maxEntries, CallbackInfo ci)
	{
		if(this.triwasa$pinnedPositions.isEmpty())
		{
			return;
		}

		this.triwasa$pinnedPositions.removeIf(pos -> {
			BlockMismatch mismatch = this.blockMismatches.get(pos);
			return mismatch == null || !this.selectedEntries.containsValue(mismatch);
		});

		if(this.triwasa$pinnedPositions.isEmpty())
		{
			return;
		}

		BlockPos firstPos = this.triwasa$pinnedPositions.iterator().next();
		BlockMismatch mismatch = this.blockMismatches.get(firstPos);
		VerifierPositionOverlayFilter.retainPinnedPositions(
				this.mismatchPositionsForRender,
				this.mismatchBlockPositionsForRender,
				this.triwasa$pinnedPositions,
				mismatch.mismatchType()
		);
	}
}