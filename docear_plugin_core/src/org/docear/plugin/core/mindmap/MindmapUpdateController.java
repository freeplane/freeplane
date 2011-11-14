package org.docear.plugin.core.mindmap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docear.plugin.core.DocearController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MindmapUpdateController {
	
	private final ArrayList<AMindmapUpdater> updaters = new ArrayList<AMindmapUpdater>();
	
	private void updateMindmaps(List<MapModel> maps) {
		for (MapModel map : maps) {
			for (AMindmapUpdater updater : this.updaters) {
				updateNodes(map.getRootNode(), updater);
			}
		}		
	}
	
	private void updateNodes(NodeModel parent, AMindmapUpdater mindmapupdater) {
		mindmapupdater.updateNode(parent);
		
		for(NodeModel child : parent.getChildren()) {
			updateNodes(child, mindmapupdater);
		}
	}
	
	public void addMindmapUpdater(AMindmapUpdater updater) {
		this.updaters.add(updater);
	}
	
	public void updateAllMindmapsInWorkspace() {
		//TODO: getAllMindmaps from Workspace
	}
	
	public void updateRegisteredMindmapsInWorkspace() {
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
		
		updateMindmaps(maps);
	}
	
	public void updateOpenMindmaps() {
		List<MapModel> maps = new ArrayList<MapModel>();
		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (String name : openMaps.keySet()) {
			maps.add(openMaps.get(name));			
		}
	}
	
	public void updateCurrentMindmap() {
		List<MapModel> maps = new ArrayList<MapModel>();
		maps.add(Controller.getCurrentController().getMap());
		
		updateMindmaps(maps);
	}
	
}
