package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableModel;

public class ToggleTreeTableAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public ToggleTreeTableAction() {
		super("ToggleTreeTableAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected == null)
			return;
		if (!TreeTableModel.isTreeTableRoot(selected))
			TreeTableAppearance.ensureRowStyleExistsIn(selected.getMap());
		Controller.getCurrentModeController().execute(new ToggleTreeTableActor(selected), selected.getMap());
	}
}
