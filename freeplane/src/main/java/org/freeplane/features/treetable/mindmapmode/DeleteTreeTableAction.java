package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableNodes;

@EnabledAction(checkOnNodeChange = true)
public class DeleteTreeTableAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public DeleteTreeTableAction() {
		super("DeleteTreeTableAction");
	}

	@Override
	protected void setEnabled() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		super.setEnabled(selected != null && TreeTableNodes.enclosingTreeTableRoot(selected) != null);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected == null)
			return;
		final NodeModel treeTableRoot = TreeTableNodes.enclosingTreeTableRoot(selected);
		if (treeTableRoot != null)
			TreeTableEditing.confirmAndDeleteTable(treeTableRoot);
	}
}
