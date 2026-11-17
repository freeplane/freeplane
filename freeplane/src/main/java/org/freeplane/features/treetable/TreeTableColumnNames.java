package org.freeplane.features.treetable;

public class TreeTableColumnNames {

	public static String unusedName(final String prefix, final TreeTableColumns columns) {
		return unusedName(prefix, "", columns);
	}

	public static String unusedName(final String prefix, final String parentKey, final TreeTableColumns columns) {
		for (int index = columns.size() + 1;; index++) {
			final String name = prefix + " " + index;
			if (!columns.containsAttributeKey(TreeTableHeader.pathKey(parentKey, name)))
				return name;
		}
	}

	private TreeTableColumnNames() {
	}
}
