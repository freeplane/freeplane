package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.SplmmReferenceUpdater;
import org.docear.plugin.bibtex.dialogs.ExistingReferencesDialog;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

public class ConvertSplmmReferencesAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConvertSplmmReferencesAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent arg0) {
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new SplmmReferenceUpdater(TextUtils.getText("update_splmm_references_all_mindmaps")));
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		mindmapUpdateController.updateCurrentMindmap();
		
		
	}

}
