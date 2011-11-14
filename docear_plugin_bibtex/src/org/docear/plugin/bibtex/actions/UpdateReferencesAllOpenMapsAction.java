package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docear.plugin.pdfutilities.ui.SwingWorkerDialog;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.jdesktop.swingworker.SwingWorker;

public class UpdateReferencesAllOpenMapsAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesAllOpenMapsAction(String key) {
		super(key);		
	}

	
	public void actionPerformed(ActionEvent e) {
		List<MapModel> maps = new ArrayList<MapModel>();
		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (String name : openMaps.keySet()) {
			maps.add(openMaps.get(name));			
		}
		
		SwingWorker<Void, Void> thread = UpdateReferencesCurrentMapAction.getReferenceUpdateThread(maps);		
		
		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText("Reference Update");
		workerDialog.setSubHeadlineText("Updating References in progress....");
		workerDialog.showDialog(thread);
		workerDialog = null;
	}

}
