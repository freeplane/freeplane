package org.freeplane.features.treetable;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class TreeTableModel implements IExtension {
	private TreeTableColumns columns = TreeTableColumns.empty();

	public static TreeTableModel of(final NodeModel node) {
		return node.getExtension(TreeTableModel.class);
	}

	public static TreeTableModel createFor(final NodeModel node) {
		TreeTableModel model = node.getExtension(TreeTableModel.class);
		if (model == null) {
			model = new TreeTableModel();
			node.addExtension(model);
		}
		return model;
	}

	public static void removeFrom(final NodeModel node) {
		node.removeExtension(TreeTableModel.class);
	}

	public static boolean isTreeTableRoot(final NodeModel node) {
		return node.getExtension(TreeTableModel.class) != null;
	}

	public static TreeTableColumns columnsOf(final NodeModel node) {
		final TreeTableModel model = node.getExtension(TreeTableModel.class);
		return model != null ? model.getColumns() : TreeTableColumns.empty();
	}

	public TreeTableColumns getColumns() {
		return columns;
	}

	public void setColumns(final TreeTableColumns columns) {
		if (columns == null)
			throw new IllegalArgumentException("tree table columns must not be null");
		this.columns = columns;
	}
}
