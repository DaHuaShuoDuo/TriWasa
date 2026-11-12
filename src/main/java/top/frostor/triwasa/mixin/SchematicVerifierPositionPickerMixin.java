package top.frostor.triwasa.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.verifier.SchematicVerifierPositionPickerAccess;
import top.frostor.triwasa.verifier.VerifierOverlayRefresh;
import top.frostor.triwasa.verifier.VerifierPositionOverlayFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	@Unique private final Map<BlockMismatch, Set<BlockPos>> triwasa$pinnedSelections = new HashMap<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void triwasa$registerForOverlayRefresh(CallbackInfo ci)
	{
		VerifierOverlayRefresh.register((SchematicVerifier) (Object) this);
	}

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

		List<BlockPos> allPositions = this.triwasa$getPositionsFor(mismatch);
		Set<BlockPos> validPositions = new HashSet<>(allPositions);
		Set<BlockPos> selectedPositions = new HashSet<>();

		for(BlockPos pos : positions)
		{
			if(validPositions.contains(pos))
			{
				selectedPositions.add(pos.immutable());
			}
		}

		if(selectedPositions.isEmpty())
		{
			this.triwasa$pinnedSelections.remove(mismatch);
			this.selectedEntries.remove(mismatch.mismatchType(), mismatch);
		}
		else if(selectedPositions.containsAll(allPositions))
		{
			this.triwasa$pinnedSelections.remove(mismatch);
			this.selectedCategories.remove(mismatch.mismatchType());
			this.selectedEntries.put(mismatch.mismatchType(), mismatch);
		}
		else
		{
			this.triwasa$pinnedSelections.put(mismatch, new HashSet<>(selectedPositions));
		}

		this.triwasa$invoker().triwasa$invokeUpdateMismatchOverlays();
	}

	@Override
	public void triwasa$selectAllForMismatch(BlockMismatch mismatch)
	{
		this.triwasa$setSelectedPositions(mismatch, this.triwasa$getPositionsFor(mismatch));
	}

	@Override
	public Set<BlockPos> triwasa$getPinnedPositions(BlockMismatch mismatch)
	{
		if(TriWasaConfigs.VERIFIER_POSITION_PICKER.getBooleanValue() == false)
		{
			return Set.of();
		}

		Set<BlockPos> pinnedPositions = this.triwasa$pinnedSelections.get(mismatch);
		if(pinnedPositions != null)
		{
			return Set.copyOf(pinnedPositions);
		}

		if(this.triwasa$isCoarseSelected(mismatch))
		{
			return Set.copyOf(this.triwasa$getPositionsFor(mismatch));
		}

		return Set.of();
	}

	@ModifyReturnValue(method = "isMismatchEntrySelected", at = @At("RETURN"))
	private boolean triwasa$markPinnedEntrySelected(boolean original, BlockMismatch mismatch)
	{
		if(TriWasaConfigs.VERIFIER_POSITION_PICKER.getBooleanValue() == false)
		{
			return original;
		}

		return original || this.triwasa$pinnedSelections.containsKey(mismatch);
	}

	@Inject(method = "toggleMismatchEntrySelected", at = @At("HEAD"), cancellable = true)
	private void triwasa$clearPinnedEntryOnToggle(BlockMismatch mismatch, CallbackInfo ci)
	{
		if(!this.triwasa$pinnedSelections.containsKey(mismatch))
		{
			return;
		}

		this.triwasa$pinnedSelections.remove(mismatch);
		this.selectedCategories.remove(mismatch.mismatchType());
		this.selectedEntries.remove(mismatch.mismatchType(), mismatch);
		this.triwasa$invoker().triwasa$invokeUpdateMismatchOverlays();
		ci.cancel();
	}

	@Unique
	private boolean triwasa$isCoarseSelected(BlockMismatch mismatch)
	{
		return this.selectedCategories.contains(mismatch.mismatchType())
				|| this.selectedEntries.containsEntry(mismatch.mismatchType(), mismatch);
	}

	@Unique
	private SchematicVerifierPickerInvoker triwasa$invoker()
	{
		return (SchematicVerifierPickerInvoker) (Object) this;
	}

	@Inject(method = "clearData", at = @At("HEAD"))
	private void triwasa$clearPinnedSelectionsOnReset(CallbackInfo ci)
	{
		this.triwasa$pinnedSelections.clear();
	}

	@Inject(method = "combineClosestPositions", at = @At("RETURN"))
	private void triwasa$retainPinnedPositions(BlockPos centerPos, int maxEntries, CallbackInfo ci)
	{
		if(TriWasaConfigs.VERIFIER_POSITION_PICKER.getBooleanValue() == false
				|| this.triwasa$pinnedSelections.isEmpty())
		{
			return;
		}

		Iterator<Map.Entry<BlockMismatch, Set<BlockPos>>> iterator =
				this.triwasa$pinnedSelections.entrySet().iterator();

		while(iterator.hasNext())
		{
			Map.Entry<BlockMismatch, Set<BlockPos>> entry = iterator.next();
			BlockMismatch selectedMismatch = entry.getKey();
			entry.getValue().removeIf(pos -> {
				BlockMismatch current = this.blockMismatches.get(pos);
				return current == null || !current.equals(selectedMismatch);
			});

			if(entry.getValue().isEmpty())
			{
				iterator.remove();
			}
		}

		if(this.triwasa$pinnedSelections.isEmpty())
		{
			return;
		}

		Map<BlockPos, MismatchType> combinedPositions = new HashMap<>();
		for(MismatchRenderPos entry : this.mismatchPositionsForRender)
		{
			BlockMismatch current = this.blockMismatches.get(entry.pos());
			if(current == null || !this.triwasa$pinnedSelections.containsKey(current))
			{
				combinedPositions.put(entry.pos(), entry.type());
			}
		}

		for(Map.Entry<BlockMismatch, Set<BlockPos>> entry : this.triwasa$pinnedSelections.entrySet())
		{
			MismatchType type = entry.getKey().mismatchType();
			for(BlockPos pos : entry.getValue())
			{
				combinedPositions.put(pos, type);
			}
		}

		VerifierPositionOverlayFilter.retainPinnedPositions(
				this.mismatchPositionsForRender,
				this.mismatchBlockPositionsForRender,
				combinedPositions
		);
	}
}