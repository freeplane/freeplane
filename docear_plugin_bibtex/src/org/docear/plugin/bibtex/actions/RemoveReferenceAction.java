package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class RemoveReferenceAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent e) {
		for (NodeModel node : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
			ReferencesController.getController().getJabRefAttributes().removeReferenceFromNode(node);
		}
		
	}

}
