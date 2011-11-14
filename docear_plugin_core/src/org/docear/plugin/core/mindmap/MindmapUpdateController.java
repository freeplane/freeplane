package org.docear.plugin.core.mindmap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.jdesktop.swingworker.SwingWorker;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

public class MindmapUpdateController {	
	private final ArrayList<AMindmapUpdater> updaters = new ArrayList<AMindmapUpdater>();
	
	public void addMindmapUpdater(AMindmapUpdater updater) {
		this.updaters.add(updater);
	}
	
	public List<AMindmapUpdater> getMindmapUpdaters() {
		return this.updaters;
	}
	
	public void updateAllMindmapsInWorkspace() {
		//TODO: getAllMindmaps from Workspace
	}
	
	public void updateRegisteredMindmapsInWorkspace() {
		List<MapModel> maps = new ArrayList<MapModel>();
		List<URI> uris = DocearController.getController().getLibrary().getMindmaps();
		
		for (URI uri : uris) {
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
		
		updateMindmaps(maps);
	}
	
	public void updateCurrentMindmap() {
		List<MapModel> maps = new ArrayList<MapModel>();
		maps.add(Controller.getCurrentController().getMap());
		
		updateMindmaps(maps);
	}
	
	private void updateMindmaps(List<MapModel> maps) {
		SwingWorker<Void, Void> thread = getUpdateThread(maps);		
		
		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText(TextUtils.getText("updating_mindmaps_headline"));
		workerDialog.setSubHeadlineText(TextUtils.getText("updating_mindmaps_subheadline"));
		workerDialog.showDialog(thread);
		workerDialog = null;			
				
	}
	
	public SwingWorker<Void, Void> getUpdateThread(final List<? extends MapModel> maps){
		
		return new SwingWorker<Void, Void>(){			
			private int totalNodeCount;
			private int count = 0;

			@Override
			protected Void doInBackground() throws Exception {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("computing_node_count"));
				computeTotalNodeCount(maps);				
				if(canceled()) return null;				
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireProgressUpdate(100 * count / totalNodeCount);
				
				for (AMindmapUpdater updater : getMindmapUpdaters()) {
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, updater.getTitle());
					if(canceled()) return null;
					for(MapModel map : maps) {
						fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_against_p1")+map.getTitle()+TextUtils.getText("updating_against_p2"));
						
						updateNodes(map.getRootNode(), updater);
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
				mindmapupdater.updateNode(parent);
				
				for(NodeModel child : parent.getChildren()) {
					updateNodes(child, mindmapupdater);
					if(canceled()) return;						
					count++;
					fireProgressUpdate(100 * count / totalNodeCount);

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
			
			private void computeTotalNodeCount(List<? extends MapModel> maps) {
				for(MapModel map : maps){
					computeTotalNodeCount(map.getRootNode());
				}					
			}

			private void computeTotalNodeCount(NodeModel node) {
				if(node.isRoot()){
					this.totalNodeCount++;
				}
				this.totalNodeCount += node.getChildCount();
				for(NodeModel child : node.getChildren()){
					computeTotalNodeCount(child);
				}					
			}
		};
	}
	
}
