package org.docear.plugin.core.mindmap;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.actions.SaveAction;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
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
		//TODO: getAllMindmaps from Workspace
		List<URI> maps = null;
		
		if (maps!=null && maps.size()>0) {
			
		}

	}
	
	public void updateRegisteredMindmapsInWorkspace() {
		//List<MapModel> maps = new ArrayList<MapModel>();
		List<URI> uris = DocearController.getController().getLibrary().getMindmaps();
		
//		for (URI uri : uris) {
//			try {
//				Controller.getCurrentModeController().getMapController().newMap(WorkspaceUtils.resolveURI(uri).toURI().toURL(), false);
//			}
//			catch (Exception ex) {
//				ex.printStackTrace();
//			}
//			
//			maps.add(Controller.getCurrentController().getMap());			
//		}
		
		//updateMindmaps(maps);
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
						if (isUri) {
							Controller.getCurrentModeController().getMapController().newMap(WorkspaceUtils.resolveURI((URI) o).toURI().toURL(), false);
							map = Controller.getCurrentController().getMap();							
						}
						else {
							map = (MapModel) o;
						}
						System.out.println("debug working on "+map.getTitle());
						fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_against_p1")+map.getTitle()+TextUtils.getText("updating_against_p2"));
						updateNodes(map.getRootNode(), updater);
						if (isUri) {
							if (!map.isSaved()) {
								new SaveAction().actionPerformed(null);
							}
							Controller.getCurrentController().close(false);
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
				mindmapupdater.updateNode(parent);
				
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
		};
	}
	
}
