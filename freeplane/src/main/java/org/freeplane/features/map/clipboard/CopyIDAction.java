package org.freeplane.features.map.clipboard;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Collection;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class CopyIDAction extends AFreeplaneAction {
	public CopyIDAction() {
		super("CopyIDAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final Collection<NodeModel> selectedNodes = controller.getSelection().getSelection();
		StringBuilder sb = null;
		for (final NodeModel node : selectedNodes) {
			if (sb == null) {
				sb = new StringBuilder();
			}
			else {
				sb.append(", ");
			}
			sb.append(node.createID());
		}
		final String idString = sb.toString();
		final MapClipboardController clipboardController = (MapClipboardController) Controller.getCurrentModeController().getExtension(
		    MapClipboardController.class);
		clipboardController.setClipboardContents(new StringSelection(idString));
		controller.getViewController().out(idString);
	}
}
