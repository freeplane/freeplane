package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;

@EnabledAction(checkOnNodeChange = true)
public class AddTreeTableColumnAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public AddTreeTableColumnAction() {
		super("AddTreeTableColumnAction");
	}

	@Override
	protected void setEnabled() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		super.setEnabled(selected != null && enclosingTreeTableRoot(selected) != null);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected == null)
			return;
		final NodeModel treeTableRoot = enclosingTreeTableRoot(selected);
		if (treeTableRoot == null)
			return;
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		final TreeTableColumns newColumns = columns.appended(new TreeTableColumn(unusedColumnName(columns)));
		Controller.getCurrentModeController().execute(
			new SetTreeTableColumnsActor(treeTableRoot, newColumns, "addTreeTableColumn"), treeTableRoot.getMap());
	}

	private static String unusedColumnName(final TreeTableColumns columns) {
		final String prefix = TextUtils.getText("treetable.new_column");
		for (int index = columns.size() + 1;; index++) {
			final String name = prefix + " " + index;
			if (!columns.containsAttributeKey(name))
				return name;
		}
	}

	private NodeModel enclosingTreeTableRoot(final NodeModel node) {
		if (TreeTableModel.isTreeTableRoot(node))
			return node;
		return TreeTableNodes.treeTableRootOf(node);
	}
}
