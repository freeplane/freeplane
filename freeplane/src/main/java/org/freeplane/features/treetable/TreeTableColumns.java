package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeTableColumns {
	private static final TreeTableColumns EMPTY = new TreeTableColumns(Collections.<TreeTableColumn> emptyList());

	private final List<TreeTableColumn> columns;

	public static TreeTableColumns empty() {
		return EMPTY;
	}

	public static TreeTableColumns of(final TreeTableColumn... columns) {
		return new TreeTableColumns(Arrays.asList(columns));
	}

	public static TreeTableColumns of(final List<TreeTableColumn> columns) {
		return new TreeTableColumns(columns);
	}

	private TreeTableColumns(final List<TreeTableColumn> columns) {
		rejectDuplicateAttributeKeys(columns);
		this.columns = Collections.unmodifiableList(new ArrayList<TreeTableColumn>(columns));
	}

	private static void rejectDuplicateAttributeKeys(final List<TreeTableColumn> columns) {
		final Set<String> knownKeys = new HashSet<String>();
		for (final TreeTableColumn column : columns) {
			if (!knownKeys.add(column.getAttributeKey()))
				throw new IllegalArgumentException("duplicate tree table column key " + column.getAttributeKey());
		}
	}

	public int size() {
		return columns.size();
	}

	public boolean isEmpty() {
		return columns.isEmpty();
	}

	public TreeTableColumn get(final int index) {
		return columns.get(index);
	}

	public List<TreeTableColumn> asList() {
		return columns;
	}

	public int indexOfAttributeKey(final String attributeKey) {
		for (int index = 0; index < columns.size(); index++) {
			if (columns.get(index).getAttributeKey().equals(attributeKey))
				return index;
		}
		return -1;
	}

	public boolean containsAttributeKey(final String attributeKey) {
		return indexOfAttributeKey(attributeKey) >= 0;
	}

	public TreeTableColumns insertedAt(final int index, final TreeTableColumn column) {
		if (index < 0 || index > columns.size())
			throw new IndexOutOfBoundsException("can not insert tree table column at " + index);
		final List<TreeTableColumn> changed = new ArrayList<TreeTableColumn>(columns);
		changed.add(index, column);
		return new TreeTableColumns(changed);
	}

	public TreeTableColumns insertedLeftOf(final int index, final TreeTableColumn column) {
		return insertedAt(index, column);
	}

	public TreeTableColumns insertedRightOf(final int index, final TreeTableColumn column) {
		return insertedAt(index + 1, column);
	}

	public TreeTableColumns appended(final TreeTableColumn column) {
		return insertedAt(columns.size(), column);
	}

	public TreeTableColumns removedAt(final int index) {
		final List<TreeTableColumn> changed = new ArrayList<TreeTableColumn>(columns);
		changed.remove(index);
		return new TreeTableColumns(changed);
	}

	public TreeTableColumns replacedAt(final int index, final TreeTableColumn column) {
		final List<TreeTableColumn> changed = new ArrayList<TreeTableColumn>(columns);
		changed.set(index, column);
		return new TreeTableColumns(changed);
	}

	public boolean acceptsRenaming(final String oldAttributeKey, final String newAttributeKey) {
		return containsAttributeKey(oldAttributeKey) && newAttributeKey != null
			&& !newAttributeKey.trim().isEmpty() && !containsAttributeKey(newAttributeKey);
	}

	public TreeTableColumns renamed(final String oldAttributeKey, final String newAttributeKey) {
		final int index = indexOfAttributeKey(oldAttributeKey);
		if (index < 0)
			throw new IllegalArgumentException("unknown tree table column key " + oldAttributeKey);
		return replacedAt(index, columns.get(index).withAttributeKey(newAttributeKey));
	}

	public TreeTableColumns movedFromTo(final int fromIndex, final int toIndex) {
		if (toIndex < 0 || toIndex >= columns.size())
			throw new IndexOutOfBoundsException("can not move tree table column to " + toIndex);
		final List<TreeTableColumn> changed = new ArrayList<TreeTableColumn>(columns);
		changed.add(toIndex, changed.remove(fromIndex));
		return new TreeTableColumns(changed);
	}

	@Override
	public int hashCode() {
		return columns.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TreeTableColumns))
			return false;
		return columns.equals(((TreeTableColumns) obj).columns);
	}

	@Override
	public String toString() {
		return "TreeTableColumns" + columns;
	}
}
