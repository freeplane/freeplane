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
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.jdesktop.swingworker.SwingWorker;

public class MindmapUpdateController {
	private final ArrayList<AMindmapUpdater> updaters = new ArrayList<AMindmapUpdater>();
	
	public void addMindmapUpdater(AMindmapUpdater updater) {
		this.updaters.add(updater);
	}

	public List<AMindmapUpdater> getMindmapUpdaters() {
		return this.updaters;
	}

	public boolean updateAllMindmapsInWorkspace() {
		List<URI> uris = WorkspaceUtils.getModel().getAllNodesFiltered(".mm");
		for (URI uri : uris) {
			System.out.println("uri: "+uri);
		}
		return updateMindmaps(uris);
	}

	public boolean updateRegisteredMindmapsInWorkspace() {
		List<URI> uris = DocearController.getController().getLibrary().getMindmaps();
		return updateMindmaps(uris);
	}

	public boolean updateOpenMindmaps() {
		List<URI> maps = getAllOpenMapUris();

		return updateMindmaps(maps);
	}

	private List<URI> getAllOpenMapUris() {
		List<URI> maps = new ArrayList<URI>();
		for (MapModel map : getAllOpenMaps()) {
			maps.add(map.getFile().toURI());
		}
		return maps;
	}

	private List<MapModel> getAllOpenMaps() {
		List<MapModel> maps = new ArrayList<MapModel>();
		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (String name : openMaps.keySet()) {
			maps.add(openMaps.get(name));
		}
		return maps;
	}

	public boolean updateCurrentMindmap() {
		return updateCurrentMindmap(false);
	}

	public boolean updateCurrentMindmap(boolean closeWhenDone) {
		List<URI> maps = new ArrayList<URI>();
		maps.add(Controller.getCurrentController().getMap().getFile().toURI());
		Controller.getCurrentController().getMap().setSaved(false);

		return updateMindmaps(maps, closeWhenDone);
	}

	public boolean updateMindmapsInList(List<MapModel> maps) {
		List<URI> uris = new ArrayList<URI>();

		for (MapModel map : maps) {
			uris.add(map.getFile().toURI());
		}

		return updateMindmaps(uris);

	}

	public boolean updateMindmaps(List<URI> uris) {
		return updateMindmaps(uris, false);
	}
	
	public boolean updateMindmaps(List<URI> uris, boolean closeWhenDone) {
		SwingWorker<Void, Void> thread = getUpdateThread(uris, closeWhenDone);

		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText(TextUtils.getText("updating_mindmaps_headline"));
		workerDialog.setSubHeadlineText(TextUtils.getText("updating_mindmaps_subheadline"));
		workerDialog.showDialog(thread);
		workerDialog = null;

		return !thread.isCancelled();
	}

	public SwingWorker<Void, Void> getUpdateThread(final List<URI> uris) {
		return getUpdateThread(uris, false);
	}

	public SwingWorker<Void, Void> getUpdateThread(final List<URI> uris, final boolean closeWhenDone) {

		return new SwingWorker<Void, Void>() {
			private int totalCount;
			private int count = 0;
			private boolean mapHasChanged = false;
			
			private final long start = System.currentTimeMillis();

			@Override
			protected Void doInBackground() throws Exception {
				try {
					if (uris == null || uris.size() == 0) {
						return null;
					}
					NodeView.setModifyModelWithoutRepaint(true);
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("computing_node_count"));
					totalCount = uris.size();
					if (canceled())
						return null;
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					fireProgressUpdate(100 * count / totalCount);
	
					for (AMindmapUpdater updater : getMindmapUpdaters()) {
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, updater.getTitle());
						if (canceled())
							return null;
						for (URI uri : uris) {
							fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null,
									TextUtils.getText("updating_references_for_map") + uri.getPath());
							mapHasChanged = false;
							MapModel map = getMapModel(uri);
							fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_against_p1")
									+ getMapTitle(map) + TextUtils.getText("updating_against_p2"));
							this.mapHasChanged = updater.updateMindmap(map);
							fireProgressUpdate(100 * count / totalCount);
							if (this.mapHasChanged) {
								if (!isMapOpen(uri)) {
									saveMap(map);
									MapChangeEvent event = new MapChangeEvent(this, UrlManager.MAP_URL, map.getURL(), null);
									Controller.getCurrentModeController().getMapController().fireMapChanged(event);
									map.destroy();
								}
								else {
									map.setSaved(false);
									map.setReadOnly(false);
								}
							}
	
							count++;
	
						}
					}
					
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_references_mapviews"));
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("updating_references_mapviews"));
				}				
				catch (Exception e) {
					LogUtils.warn(e);
				}
				return null;
			}

			private boolean isMapOpen(URI uri) {
				URL url;
				try {
					url = WorkspaceUtils.resolveURI(uri).toURL();
					String mapExtensionKey = Controller.getCurrentController().getMapViewManager()
							.checkIfFileIsAlreadyOpened(url);

					if (mapExtensionKey == null) {
						return false;
					}
					else {
						return true;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			private String getMapTitle(MapModel map) {
				String mapTitle = "";
				if (map.getFile() != null) {
					mapTitle = map.getFile().getName();
				}
				else {
					mapTitle = map.getTitle();
				}
				return mapTitle;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void done() {
				NodeView.setModifyModelWithoutRepaint(false);
				for (MapView view : (List<MapView>) Controller.getCurrentController().getViewController().getMapViewManager()
						.getMapViewVector()) {
					boolean opened = false;
					URI mapUri = view.getModel().getFile().toURI();
					for (URI uri : uris) {
						if (uri.equals(mapUri)) {
							opened = true;
						}
					}
					if (opened) {						
						NodeView nodeView = view.getNodeView(view.getModel().getRootNode());
						nodeView.updateAll();
					}
				}

				if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("update_canceled"));
				}
				else {
					this.firePropertyChange(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("update_complete"));
				}
				
				if (closeWhenDone) {
					try {
						this.firePropertyChange(SwingWorkerDialog.CLOSE, null, null);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					long time = System.currentTimeMillis() - this.start;
					this.firePropertyChange(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("execution_time")+" "+time+" ms");
				}

			}

			private boolean canceled() throws InterruptedException {
				Thread.sleep(1L);
				return (this.isCancelled() || Thread.currentThread().isInterrupted());
			}

			private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue)
					throws InterruptedException, InvocationTargetException {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						firePropertyChange(propertyName, oldValue, newValue);
					}
				});
			}

			private void fireProgressUpdate(final int progress) throws InterruptedException, InvocationTargetException {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setProgress(progress);
					}
				});
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
				}
				;

				if (mapExtensionKey != null) {
					map = Controller.getCurrentController().getViewController().getMapViewManager().getMaps()
							.get(mapExtensionKey);
					if (map != null) {
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

			private void saveMap(MapModel map) throws InterruptedException, InvocationTargetException {
				if (!this.mapHasChanged) {
					return;
				}
				fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("update_references_save_map")
						+ map.getURL().getPath());

				System.out.println("saving map: " + map.getURL());
				map.setSaved(false);
				((MFileManager) UrlManager.getController()).save(map, false);
			}
		};
	}
}
