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

	public boolean containsAttributeKey(final String attributeKey) {
		for (final TreeTableColumn column : columns) {
			if (column.getAttributeKey().equals(attributeKey))
				return true;
		}
		return false;
	}

	public boolean acceptsRenaming(final String oldAttributeKey, final String newAttributeKey) {
		return containsAttributeKey(oldAttributeKey) && newAttributeKey != null
			&& !newAttributeKey.trim().isEmpty() && !containsAttributeKey(newAttributeKey);
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
