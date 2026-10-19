package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.mindmapmode.clipboard.MMapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableNodes;

@EnabledAction(checkOnNodeChange = true)
public class PasteAsTreeTableAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public PasteAsTreeTableAction() {
		super("PasteAsTreeTableAction");
	}

	@Override
	protected void setEnabled() {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		super.setEnabled(TreeTableNodes.canConvertToTable(selected));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		if (selected != null)
			((MMapClipboardController) MapClipboardController.getController()).pasteAsTreeTable(selected);
	}
}
