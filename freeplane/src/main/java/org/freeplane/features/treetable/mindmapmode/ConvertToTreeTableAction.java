package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableModel;

@EnabledAction(checkOnNodeChange = true)
public class ConvertToTreeTableAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public ConvertToTreeTableAction() {
		super("ConvertToTreeTableAction");
	}

	@Override
	protected void setEnabled() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		super.setEnabled(selected != null && !TreeTableModel.isTreeTableRoot(selected));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected == null || TreeTableModel.isTreeTableRoot(selected))
			return;
		TreeTableAppearance.ensureRowStyleExistsIn(selected.getMap());
		Controller.getCurrentModeController().execute(new ToggleTreeTableActor(selected), selected.getMap());
	}
}
