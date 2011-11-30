package org.docear.plugin.core.mindmap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.jdesktop.swingworker.SwingWorker;

public class MindmapUpdateController {	
	private final ArrayList<AMindmapUpdater> updaters = new ArrayList<AMindmapUpdater>();
	
	public void addMindmapUpdater(AMindmapUpdater updater) {
		this.updaters.add(updater);
	}
	
	public List<AMindmapUpdater> getMindmapUpdaters() {
		return this.updaters;
	}
	
	public void updateAllMindmapsInWorkspace() {	
		List<URI> uris = WorkspaceUtils.getModel().getAllNodesFiltered(".mm");
		updateMindmaps(uris);
	}
	
	public void updateRegisteredMindmapsInWorkspace() {
		List<URI> uris = DocearController.getController().getLibrary().getMindmaps();		
		updateMindmaps(uris);
	}
	
	public void updateOpenMindmaps() {
		List<MapModel> maps = new ArrayList<MapModel>();
		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (String name : openMaps.keySet()) {
			maps.add(openMaps.get(name));			
		}
		
		updateMindmaps(maps);
	}
	
	public void updateCurrentMindmap() {
		List<MapModel> maps = new ArrayList<MapModel>();
		maps.add(Controller.getCurrentController().getMap());
		
		updateMindmaps(maps);
	}
	
	private void updateMindmaps(List<?> maps) {		
		SwingWorker<Void, Void> thread = getUpdateThread(maps);		
		
		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText(TextUtils.getText("updating_mindmaps_headline"));
		workerDialog.setSubHeadlineText(TextUtils.getText("updating_mindmaps_subheadline"));
		workerDialog.showDialog(thread);
		workerDialog = null;
	}
	
	public SwingWorker<Void, Void> getUpdateThread(final List<?> maps){
		
		return new SwingWorker<Void, Void>(){			
			private int totalCount;
			private int count = 0;
			
			private boolean isUri = false;
			private boolean mapHasChanged = false;

			@Override
			protected Void doInBackground() throws Exception {
				
				if (maps == null || maps.size() == 0) {
					return null;
				}
				
				isUri = maps.get(0) instanceof URI;
				
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("computing_node_count"));
				if (isUri) {
					totalCount = maps.size();
				}
				else {
					computeTotalNodeCount(maps);
				}
				if(canceled()) return null;				
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireProgressUpdate(100 * count / totalCount);
				
				for (AMindmapUpdater updater : getMindmapUpdaters()) {
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, updater.getTitle());
					if(canceled()) return null;
					MapModel map = null;
					for(Object o : maps) {
						mapHasChanged = false;
						if (isUri) {							
							map = getMapModel((URI) o);							
						}
						else {
							map = (MapModel) o;
						}
						fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_against_p1")+map.getTitle()+TextUtils.getText("updating_against_p2"));
						updateNodes(map.getRootNode(), updater);
						if (isUri) {							
							saveMap(map);
							map.destroy();
							count++;
							fireProgressUpdate(100 * count / totalCount);
						}
					}			
				}
				return null;
			}
			
			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("update_canceled"));
				}
				else {
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("update_complete"));
				}
				
			}
			
			private void updateNodes(NodeModel parent, AMindmapUpdater mindmapupdater) throws InterruptedException, InvocationTargetException {	
				if (parent == null) {
					return;
				}
				if (mindmapupdater.updateNode(parent)) {
					this.mapHasChanged = true;
				}
				
				for(NodeModel child : parent.getChildren()) {
					updateNodes(child, mindmapupdater);
					if(canceled()) return;		
					
					if (!isUri) {
						count++;
						fireProgressUpdate(100 * count / totalCount);
					}

				}
			}

			
			private boolean canceled() throws InterruptedException{
				Thread.sleep(1L);
				return (this.isCancelled() || Thread.currentThread().isInterrupted());
			}
			
			private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue) throws InterruptedException, InvocationTargetException{				
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	firePropertyChange(propertyName, oldValue, newValue);										
				            }
				        }
				   );	
			}
			
			private void fireProgressUpdate(final int progress) throws InterruptedException, InvocationTargetException{
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	setProgress(progress);						
				            }
				        }
				   );	
			}
			
			@SuppressWarnings("unchecked")
			private void computeTotalNodeCount(List<?> maps) {
				for(MapModel map : (List<MapModel>) maps){
					computeTotalNodeCount(map.getRootNode());
				}					
			}

			private void computeTotalNodeCount(NodeModel node) {
				if(node.isRoot()){
					this.totalCount++;
				}
				this.totalCount += node.getChildCount();
				for(NodeModel child : node.getChildren()){
					computeTotalNodeCount(child);
				}					
			}
			
			private MapModel getMapModel(URI uri) {
				MapModel map = null;
				
				URL url;
				String mapExtensionKey;
				try {
					url = WorkspaceUtils.resolveURI(uri).toURL();
					mapExtensionKey = Controller.getCurrentController().getMapViewManager().checkIfFileIsAlreadyOpened(url);					
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				};
											
				if (mapExtensionKey != null) {
					map = Controller.getCurrentController().getViewController().getMapViewManager().getMaps()
							.get(mapExtensionKey);
					if (map!=null) {
						return map;
					}
				}
				
				map = new MMapModel(null);
				AttributeRegistry.createRegistry(map);
				try {
					File f = WorkspaceUtils.resolveURI(uri);
					if (f.exists()) {
						UrlManager.getController().load(url, map);
					}
				}
				catch (Exception e) {			
					e.printStackTrace();
				}
				
				return map;
			
			}
			
			private void saveMap(MapModel map) {
				if (!this.mapHasChanged) {					
					return;
				}
				System.out.println("saving map ");
				map.setSaved(false);
				((MFileManager) UrlManager.getController()).save(map, false);				
			}
		};
	}
	
}
