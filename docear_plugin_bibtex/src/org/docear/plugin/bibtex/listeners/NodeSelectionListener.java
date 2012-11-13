package org.docear.plugin.bibtex.listeners;


import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class NodeSelectionListener implements INodeSelectionListener {

	
	public void init() {
		Controller.getCurrentModeController().getMapController().addNodeSelectionListener(this);		
	}
	
	
	public void onDeselect(NodeModel node) {		
	}


	public void onSelect(NodeModel node) {
		try {
			NodeAttributeTableModel attributes = AttributeController.getController(MModeController.getMModeController()).createAttributeTableModel(Controller.getCurrentModeController().getMapController().getSelectedNode());
			attributes.addTableModelListener(ReferencesController.getController().getAttributeListener());
			//DOCEAR - only enabled when no entry is selected 
			if(ReferencesController.getController().getJabrefWrapper().getBasePanel().getSelectedEntries().length < 1) {
				if(attributes.getAttributeKeyList().contains(TextUtils.getText("bibtex_key"))) {
					String bibKey = (String)attributes.getAttribute(attributes.getAttributePosition(TextUtils.getText("bibtex_key"))).getValue();
					JabRefCommons.showInReferenceManager(bibKey);
				}			
			}
		}
		catch (NullPointerException e) {
			// in case the basePanel is not existent (anymore)
		}
	}

}
