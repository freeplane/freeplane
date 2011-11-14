package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.SwingWorkerDialog;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.jdesktop.swingworker.SwingWorker;

public class UpdateReferencesInLibrary extends AFreeplaneAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesInLibrary(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {		
		List<MapModel> maps = new ArrayList<MapModel>();
		List<URI> uris = DocearController.getController().getLibrary().getMindmaps();
		
		for (URI uri : uris) {
			System.out.println("uri: "+uri);
			try {
				Controller.getCurrentModeController().getMapController().newMap(WorkspaceUtils.resolveURI(uri).toURI().toURL(), false);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			maps.add(Controller.getCurrentController().getMap());			
		}
		
		
		SwingWorker<Void, Void> thread = UpdateReferencesCurrentMapAction.getReferenceUpdateThread(maps);		
		
		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText("Reference Update");
		workerDialog.setSubHeadlineText("Updating References in progress....");
		workerDialog.showDialog(thread);
		workerDialog = null;			
		
	}

}
