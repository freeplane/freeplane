package org.freeplane.features.common.clipboard;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;

public class CopyIDAction extends AFreeplaneAction {
	public CopyIDAction(final Controller controller) {
		super("CopyIDAction", controller);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		final List<NodeModel> selectedNodes = controller.getSelection().getSelection();
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
		final ClipboardController clipboardController = (ClipboardController) getModeController().getExtension(
		    ClipboardController.class);
		clipboardController.setClipboardContents(new StringSelection(idString));
		controller.getViewController().out(idString);
	}
}
