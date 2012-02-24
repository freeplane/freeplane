package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.features.AnnotationModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

@EnabledAction(checkOnPopup = true)
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
			
			((MapView) Controller.getCurrentController().getViewController().getMapView()).getNodeView(node).updateAll();
		}
		
	}
	
	public void setEnabled() {
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if (node == null) {
			setEnabled(false);
			return;
		}
		
		AnnotationModel annotation = (AnnotationModel) node.getExtension(AnnotationModel.class);
		if (annotation != null && annotation.getAnnotationID() != null) {
			setEnabled(false);
			return;
		}
		
		final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
		
		if (bibtexKey != null && bibtexKey.length()>0) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
		
	}

}
