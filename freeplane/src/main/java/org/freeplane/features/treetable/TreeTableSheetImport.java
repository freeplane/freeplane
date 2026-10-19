package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeTableSheetImport {
	public static class Item {
		private final String text;
		private final Map<String, String> values = new LinkedHashMap<String, String>();
		private final List<Item> children = new ArrayList<Item>();

		private Item(final String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public Map<String, String> getValues() {
			return Collections.unmodifiableMap(values);
		}

		public List<Item> getChildren() {
			return Collections.unmodifiableList(children);
		}

		@Override
		public String toString() {
			return text + (values.isEmpty() ? "" : values.toString()) + (children.isEmpty() ? "" : children.toString());
		}
	}

	private final TreeTableSheet sheet;
	private final String unnamedColumnPrefix;
	private final int treeColumnCount;
	private final int headerRowCount;
	private final List<Item> columns = new ArrayList<Item>();
	private final List<String> columnKeys = new ArrayList<String>();
	private final Set<String> usedColumnKeys = new HashSet<String>();
	private final List<Item> rows = new ArrayList<Item>();

	public static TreeTableSheetImport of(final TreeTableSheet sheet, final String unnamedColumnPrefix) {
		return sheet.getRowCount() == 0 ? null : new TreeTableSheetImport(sheet, unnamedColumnPrefix);
	}

	private TreeTableSheetImport(final TreeTableSheet sheet, final String unnamedColumnPrefix) {
		this.sheet = sheet;
		this.unnamedColumnPrefix = unnamedColumnPrefix;
		treeColumnCount = treeColumnCount();
		headerRowCount = headerRowCount();
		columns.addAll(headerItems(0, treeColumnCount, sheet.getColumnCount()));
		readRows();
	}

	public String getRootText() {
		return sheet.get(0, 0).trim();
	}

	public List<Item> getColumns() {
		return Collections.unmodifiableList(columns);
	}

	public List<Item> getRows() {
		return Collections.unmodifiableList(rows);
	}

	private int treeColumnCount() {
		for (int column = 1; column < sheet.getColumnCount(); column++) {
			if (!sheet.isEmpty(0, column))
				return column;
		}
		return Math.max(1, sheet.getColumnCount());
	}

	private int headerRowCount() {
		int row = 1;
		while (row < sheet.getRowCount() && isEmptyInTreeColumns(row) && !isEmptyInValueColumns(row))
			row++;
		return row;
	}

	private boolean isEmptyInTreeColumns(final int row) {
		return firstFilledColumn(row, 0, treeColumnCount) < 0;
	}

	private boolean isEmptyInValueColumns(final int row) {
		return firstFilledColumn(row, treeColumnCount, sheet.getColumnCount()) < 0;
	}

	private int firstFilledColumn(final int row, final int fromColumn, final int toColumn) {
		for (int column = fromColumn; column < toColumn; column++) {
			if (!sheet.isEmpty(row, column))
				return column;
		}
		return -1;
	}

	private List<Item> headerItems(final int row, final int fromColumn, final int toColumn) {
		final List<Item> items = new ArrayList<Item>();
		int start = fromColumn;
		while (start < toColumn) {
			final int end = row == headerRowCount - 1 ? start + 1 : nextFilledColumn(row, start + 1, toColumn);
			addHeaderItems(items, row, start, end);
			start = end;
		}
		return items;
	}

	private int nextFilledColumn(final int row, final int fromColumn, final int toColumn) {
		final int filledColumn = firstFilledColumn(row, fromColumn, toColumn);
		return filledColumn < 0 ? toColumn : filledColumn;
	}

	private void addHeaderItems(final List<Item> items, final int row, final int start, final int end) {
		final String text = sheet.get(row, start).trim();
		if (row == headerRowCount - 1 || isEmptyBelow(row, start, end)) {
			items.add(columnItem(text));
			for (int column = start + 1; column < end; column++)
				items.add(columnItem(""));
		}
		else if (text.isEmpty())
			items.addAll(headerItems(row + 1, start, end));
		else {
			final Item group = new Item(text);
			group.children.addAll(headerItems(row + 1, start, end));
			items.add(group);
		}
	}

	private boolean isEmptyBelow(final int row, final int start, final int end) {
		for (int below = row + 1; below < headerRowCount; below++) {
			if (firstFilledColumn(below, start, end) >= 0)
				return false;
		}
		return true;
	}

	private Item columnItem(final String text) {
		final String key = uniqueColumnKey(text);
		usedColumnKeys.add(key);
		columnKeys.add(key);
		return new Item(key);
	}

	private String uniqueColumnKey(final String text) {
		final String base = text.isEmpty() ? unnamedColumnPrefix : text;
		if (!text.isEmpty() && !usedColumnKeys.contains(text))
			return text;
		for (int index = text.isEmpty() ? 1 : 2;; index++) {
			final String key = base + " " + index;
			if (!usedColumnKeys.contains(key) && !isColumnTitle(key))
				return key;
		}
	}

	private boolean isColumnTitle(final String key) {
		for (int column = treeColumnCount; column < sheet.getColumnCount(); column++) {
			if (sheet.get(headerRowCount - 1, column).trim().equals(key))
				return true;
		}
		return false;
	}

	private void readRows() {
		final List<Item> parents = new ArrayList<Item>();
		for (int row = headerRowCount; row < sheet.getRowCount(); row++) {
			if (isEmptyInTreeColumns(row) && isEmptyInValueColumns(row))
				continue;
			final int filledColumn = firstFilledColumn(row, 0, treeColumnCount);
			final int depth = Math.min(Math.max(filledColumn, 0), parents.size());
			final Item item = new Item(filledColumn < 0 ? "" : sheet.get(row, filledColumn).trim());
			for (int column = 0; column < columnKeys.size(); column++) {
				final int sheetColumn = treeColumnCount + column;
				if (!sheet.isEmpty(row, sheetColumn))
					item.values.put(columnKeys.get(column), sheet.get(row, sheetColumn).trim());
			}
			while (parents.size() > depth)
				parents.remove(parents.size() - 1);
			(depth == 0 ? rows : parents.get(depth - 1).children).add(item);
			parents.add(item);
		}
	}
}
