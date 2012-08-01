package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.mindmapmode.SaveAll;

public class UpdateReferencesCurrentMapAction extends AFreeplaneAction implements IDocearEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesCurrentMapAction(String key) {
		super(key);
		DocearController.getController().addDocearEventListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		perform(true);
	}
	
	public void perform(boolean showDialog) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(showDialog);
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		mindmapUpdateController.updateCurrentMindmap();
	}

	@Override
	public void handleEvent(DocearEvent event) {
		if (DocearEventType.UPDATE_MAP.equals(event.getType())) {
			ReferenceUpdater updater = new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps"));
			updater.updateMindmap((MapModel) event.getEventObject());
		}
	}
	
	

	
}
