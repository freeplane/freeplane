package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.SaveAll;

public class UpdateReferencesAllOpenMapsAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesAllOpenMapsAction(String key) {
		super(key);		
	}

	
	public void actionPerformed(ActionEvent e) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		mindmapUpdateController.updateOpenMindmaps();
	}

}
