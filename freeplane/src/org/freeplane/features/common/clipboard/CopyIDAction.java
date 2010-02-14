package org.freeplane.features.common.clipboard;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;

public class CopyIDAction extends AFreeplaneAction {

	public CopyIDAction(Controller controller) {
		super("CopyIDAction", controller);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		Controller controller = getController();
		List<NodeModel> selectedNodes = controller.getSelection().getSelection();
		StringBuilder sb = null;
		for(NodeModel node:selectedNodes){
			if(sb == null){
				sb = new StringBuilder();
			}
			else{
				sb.append(", ");
			}
			sb.append(node.createID());
		}
		String idString = sb.toString();
		final ClipboardController clipboardController = (ClipboardController) getModeController()
		.getExtension(ClipboardController.class);
		clipboardController.setClipboardContents(new StringSelection(idString));
		controller.getViewController().out(idString);
	}

}
