package top.frostor.triwasa.easyplace.registry;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EasyPlaceCodecRegistry
{
	private final List<Entry> registrations = new ArrayList<>();
	private final Set<String> ids = new HashSet<>();
	private final Set<Integer> priorities = new HashSet<>();
	private List<Entry> frozenEntries = List.of();
	private boolean frozen;

	public synchronized void register(
			String id,
			int priority,
			EasyPlaceMatcher matcher,
			EasyPlaceStateEncoder encoder,
			EasyPlaceStateDecoder decoder
	)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(matcher, "matcher");
		Objects.requireNonNull(encoder, "encoder");
		Objects.requireNonNull(decoder, "decoder");
		if(frozen)
		{
			throw new IllegalStateException("Easy Place codec registry is frozen");
		}
		if(!ids.add(id))
		{
			throw new IllegalArgumentException("Duplicate adapter id: " + id);
		}
		if(!priorities.add(priority))
		{
			throw new IllegalArgumentException("Duplicate adapter priority: " + priority);
		}
		registrations.add(new Entry(id, priority, matcher, encoder, decoder));
	}

	public synchronized void freeze()
	{
		if(frozen)
		{
			return;
		}
		if(registrations.isEmpty())
		{
			throw new IllegalStateException("Registry must not be empty");
		}
		registrations.sort(Comparator.comparingInt(Entry::priority).reversed());
		frozenEntries = List.copyOf(registrations);
		frozen = true;
	}

	public EasyPlaceEncodedState encode(EasyPlaceCodecContext context)
	{
		Entry entry = resolve(context);
		return entry == null
				? new EasyPlaceEncodedState(-1, 0)
				: new EasyPlaceEncodedState(
						entry.encoder().encode(context),
						entry.encoder().encodeAuxiliary(context)
				);
	}

	public @Nullable BlockState decode(EasyPlaceCodecContext context)
	{
		Entry entry = resolve(context);
		return entry == null ? null : entry.decoder().decode(context);
	}

	public synchronized boolean isFrozen()
	{
		return frozen;
	}

	private @Nullable Entry resolve(EasyPlaceCodecContext context)
	{
		Objects.requireNonNull(context, "context");
		if(!frozen)
		{
			throw new IllegalStateException("Easy Place codec registry is not frozen");
		}
		for(Entry entry : frozenEntries)
		{
			if(entry.matcher().matches(context))
			{
				return entry;
			}
		}
		return null;
	}

	private record Entry(
			String id,
			int priority,
			EasyPlaceMatcher matcher,
			EasyPlaceStateEncoder encoder,
			EasyPlaceStateDecoder decoder
	)
	{
	}
}
