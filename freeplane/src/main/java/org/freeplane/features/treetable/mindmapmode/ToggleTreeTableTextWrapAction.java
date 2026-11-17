package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;

@EnabledAction(checkOnNodeChange = true)
@SelectableAction(checkOnNodeChange = true)
public class ToggleTreeTableTextWrapAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public ToggleTreeTableTextWrapAction() {
		super("ToggleTreeTableTextWrapAction");
	}

	@Override
	protected void setEnabled() {
		super.setEnabled(selectedTreeTableRoot() != null);
	}

	@Override
	public void setSelected() {
		final NodeModel treeTableRoot = selectedTreeTableRoot();
		setSelected(treeTableRoot != null && TreeTableModel.wrapsText(treeTableRoot));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel treeTableRoot = selectedTreeTableRoot();
		if (treeTableRoot != null)
			TreeTableEditing.toggleTextWrap(treeTableRoot);
	}

	private NodeModel selectedTreeTableRoot() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		return selected != null ? TreeTableNodes.enclosingTreeTableRoot(selected) : null;
	}
}
