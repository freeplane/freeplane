package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.SaveAll;

public class UpdateReferencesCurrentMapAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesCurrentMapAction(String key) {
		super(key);	
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
//		ReferenceUpdater referenceUpdater = new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps"));
//		ArrayList<MapModel> maps = new ArrayList<MapModel>();
//		maps.add(Controller.getCurrentController().getMap());
//		referenceUpdater.run(maps);
		
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		mindmapUpdateController.updateCurrentMindmap();
	}
	
	

	
}
