package org.freeplane.features.treetable;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class TreeTableModel implements IExtension {
	private boolean textWrapped;

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

	public static boolean wrapsText(final NodeModel treeTableRoot) {
		final TreeTableModel model = of(treeTableRoot);
		return model != null && model.isTextWrapped();
	}

	public boolean isTextWrapped() {
		return textWrapped;
	}

	public void setTextWrapped(final boolean textWrapped) {
		this.textWrapped = textWrapped;
	}

	public static TreeTableColumns columnsOf(final NodeModel node) {
		return isTreeTableRoot(node) ? TreeTableHeader.columnsOf(node) : TreeTableColumns.empty();
	}
}
