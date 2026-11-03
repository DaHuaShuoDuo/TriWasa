package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Local search, stable sorting, and row-based scrolling over the aggregate index.
 */
public final class BigEnderChestViewController
{
	private List<BigEnderChestMessages.EntryRecord> allEntries = List.of();
	private List<BigEnderChestMessages.EntryRecord> filteredEntries = List.of();
	private long indexedRevision = Long.MIN_VALUE;
	private Map<Long, BigEnderChestMessages.EntryRecord> indexedMap = Map.of();
	private String query = "";
	private SortField sortField = SortField.NAME;
	private SortDirection sortDirection = SortDirection.ASCENDING;
	private int firstRow;
	private int rows = 2;
	private long anchorSerial;

	public void refresh(
			Map<Long, BigEnderChestMessages.EntryRecord> entries,
			long revision
	)
	{
		if(revision == this.indexedRevision && entries == this.indexedMap)
		{
			return;
		}
		long previousSerial = visibleAnchorSerial();
		this.indexedRevision = revision;
		this.indexedMap = entries;
		this.allEntries = List.copyOf(entries.values());
		rebuildFiltered();
		restoreSerial(previousSerial);
	}

	public void setQuery(String query)
	{
		String normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
		if(normalized.equals(this.query)) return;
		long previousSerial = visibleAnchorSerial();
		this.query = normalized;
		rebuildFiltered();
		restoreSerial(previousSerial);
	}

	public String query()
	{
		return this.query;
	}

	public void setSortField(SortField sortField)
	{
		if(this.sortField == sortField) return;
		long previousSerial = visibleAnchorSerial();
		this.sortField = sortField;
		rebuildFiltered();
		restoreSerial(previousSerial);
	}

	public void setSortDirection(SortDirection sortDirection)
	{
		if(this.sortDirection == sortDirection) return;
		long previousSerial = visibleAnchorSerial();
		this.sortDirection = sortDirection;
		rebuildFiltered();
		restoreSerial(previousSerial);
	}

	public SortField sortField()
	{
		return this.sortField;
	}

	public SortDirection sortDirection()
	{
		return this.sortDirection;
	}

	public void setViewportRows(int rows)
	{
		if(rows == this.rows) return;
		long previousSerial = this.anchorSerial != 0
				? this.anchorSerial
				: visibleAnchorSerial();
		this.rows = Math.max(1, rows);
		restoreSerial(previousSerial);
	}

	public List<BigEnderChestMessages.EntryRecord> visibleEntries()
	{
		int capacity = capacity();
		int start = Math.min(this.firstRow * 9, maxFirstIndex(capacity));
		int end = Math.min(this.filteredEntries.size(), start + capacity);
		if(start >= end)
		{
			return List.of();
		}
		this.anchorSerial = this.filteredEntries.get(start).templateSerial();
		return this.filteredEntries.subList(start, end);
	}

	public BigEnderChestMessages.EntryRecord entryAtSlot(int slot)
	{
		int index = this.firstRow * 9 + slot;
		return index >= 0 && index < this.filteredEntries.size()
				? this.filteredEntries.get(index)
				: null;
	}

	public boolean scrollRows(int delta, int visibleSlots)
	{
		int previous = this.firstRow;
		setFirstRow(this.firstRow + delta);
		return previous != this.firstRow;
	}

	public void setFirstRow(int row)
	{
		int maxRow = maxFirstIndex(capacity()) / 9;
		int updated = Math.clamp(row, 0, maxRow);
		if(updated == this.firstRow)
		{
			return;
		}
		this.firstRow = updated;
	}

	public int firstRow()
	{
		return this.firstRow;
	}

	public int filteredCount()
	{
		return this.filteredEntries.size();
	}

	public int totalCount()
	{
		return this.allEntries.size();
	}

	private int capacity()
	{
		return Math.max(9, this.rows * 9);
	}

	private int maxFirstIndex(int capacity)
	{
		if(this.filteredEntries.isEmpty())
		{
			return 0;
		}
		int unaligned = Math.max(0, this.filteredEntries.size() - capacity);
		return Math.ceilDiv(unaligned, 9) * 9;
	}

	private void rebuildFiltered()
	{
		List<BigEnderChestMessages.EntryRecord> result = new ArrayList<>();
		for(BigEnderChestMessages.EntryRecord entry : this.allEntries)
		{
			if(this.query.isEmpty() || searchText(entry).contains(this.query))
			{
				result.add(entry);
			}
		}
		Comparator<BigEnderChestMessages.EntryRecord> primary =
				this.sortField == SortField.COUNT
						? Comparator.comparingLong(BigEnderChestMessages.EntryRecord::totalAmount)
						: Comparator.comparing(this::registryName);
		if(this.sortDirection == SortDirection.DESCENDING)
		{
			primary = primary.reversed();
		}
		Comparator<BigEnderChestMessages.EntryRecord> tieBreak =
				Comparator.comparing(this::registryName)
						.thenComparing(this::componentPatch)
						.thenComparingLong(BigEnderChestMessages.EntryRecord::templateSerial);
		result.sort(primary.thenComparing(tieBreak));
		this.filteredEntries = List.copyOf(result);
		this.firstRow = Math.min(
				this.firstRow,
				maxFirstIndex(capacity()) / 9
		);
	}

	private void restoreSerial(long serial)
	{
		if(serial == 0 || this.filteredEntries.isEmpty())
		{
			this.firstRow = 0;
			this.anchorSerial = this.filteredEntries.isEmpty()
					? 0
					: this.filteredEntries.getFirst().templateSerial();
			return;
		}
		int index = -1;
		for(int i = 0; i < this.filteredEntries.size(); i++)
		{
			if(this.filteredEntries.get(i).templateSerial() == serial)
			{
				index = i;
				break;
			}
		}
		if(index < 0)
		{
			this.firstRow = Math.min(
					this.firstRow,
					maxFirstIndex(capacity()) / 9
			);
		}
		else
		{
			this.firstRow = Math.min(
					index / 9,
					maxFirstIndex(capacity()) / 9
			);
		}
		this.anchorSerial = serial;
	}

	private long visibleAnchorSerial()
	{
		int index = this.firstRow * 9;
		return index >= 0 && index < this.filteredEntries.size()
				? this.filteredEntries.get(index).templateSerial()
				: this.anchorSerial;
	}

	private String searchText(BigEnderChestMessages.EntryRecord entry)
	{
		return (entry.item().getHoverName().getString()
				+ "\n" + registryName(entry)
				+ "\n" + componentPatch(entry))
				.toLowerCase(Locale.ROOT);
	}

	private String registryName(BigEnderChestMessages.EntryRecord entry)
	{
		Identifier id = BuiltInRegistries.ITEM.getKey(entry.item().getItem());
		return id == null ? entry.item().getItem().getDescriptionId() : id.toString();
	}

	private String componentPatch(BigEnderChestMessages.EntryRecord entry)
	{
		return entry.item().getComponentsPatch().toString();
	}

	public enum SortField
	{
		NAME,
		COUNT
	}

	public enum SortDirection
	{
		ASCENDING,
		DESCENDING
	}
}
