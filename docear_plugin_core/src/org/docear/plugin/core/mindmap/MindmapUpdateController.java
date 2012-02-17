package org.docear.plugin.core.mindmap;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.MapItem;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
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
		List<MapItem> maps = new ArrayList<MapItem>();
		for (URI uri : WorkspaceUtils.getModel().getAllNodesFiltered(".mm")) {
			maps.add(new MapItem(uri));
		}
		
		return updateMindmaps(maps);
	}
	
	public boolean updateRegisteredMindmapsInWorkspace() {
		return updateRegisteredMindmapsInWorkspace(false);
	}

	public boolean updateRegisteredMindmapsInWorkspace(boolean openMindmapsToo) {
		List<MapItem> maps = new ArrayList<MapItem>(); 
		
		for (URI uri : DocearController.getController().getLibrary().getMindmaps()) {
			maps.add(new MapItem(uri));
		}
		
		if (openMindmapsToo) {
			for (MapItem item : getAllOpenMaps()) {
				maps.add(item);
			}
		}
		
		return updateMindmaps(maps);
	}

	public boolean updateOpenMindmaps() {
		List<MapItem> maps = getAllOpenMaps();

		return updateMindmaps(maps);
	}

	private List<MapItem> getAllOpenMaps() {
		List<MapItem> maps = new ArrayList<MapItem>();
		Map<String, MapModel> openMaps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (String name : openMaps.keySet()) {
			maps.add(new MapItem(openMaps.get(name)));
		}
		return maps;
	}

	public boolean updateCurrentMindmap() {
		return updateCurrentMindmap(false);
	}

	public boolean updateCurrentMindmap(boolean closeWhenDone) {
		List<MapItem> maps = new ArrayList<MapItem>();
		
		try {
			maps.add(new MapItem(Controller.getCurrentController().getMap()));
		}
		catch (NullPointerException e) {			
		}

		Controller.getCurrentController().getMap().setSaved(false);

		return updateMindmaps(maps, closeWhenDone);
	}

	public boolean updateMindmapsInList(List<MapModel> maps) {
		List<MapItem> mapItems = new ArrayList<MapItem>();

		for (MapModel map : maps) {
			try {
				mapItems.add(new MapItem(map));
			}
			catch (NullPointerException e) {				
			}
		}

		return updateMindmaps(mapItems);

	}

	public boolean updateMindmaps(List<MapItem> uris) {
		return updateMindmaps(uris, false);
	}

	public boolean updateMindmaps(List<MapItem> maps, boolean closeWhenDone) {
		SwingWorker<Void, Void> thread = getUpdateThread(maps, closeWhenDone);

		SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
		workerDialog.setHeadlineText(TextUtils.getText("updating_mindmaps_headline"));
		workerDialog.setSubHeadlineText(TextUtils.getText("updating_mindmaps_subheadline"));
		workerDialog.showDialog(thread);
		workerDialog = null;

		return !thread.isCancelled();
	}

	public SwingWorker<Void, Void> getUpdateThread(final List<MapItem> maps) {
		return getUpdateThread(maps, false);
	}

	public SwingWorker<Void, Void> getUpdateThread(final List<MapItem> maps, final boolean closeWhenDone) {

		return new SwingWorker<Void, Void>() {
			private int totalCount;
			private boolean mapHasChanged = false;

			private final long start = System.currentTimeMillis();

			@Override
			protected Void doInBackground() throws Exception {
				try {
					if (maps == null || maps.size() == 0) {
						return null;
					}
					NodeView.setModifyModelWithoutRepaint(true);
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("computing_node_count"));
					totalCount = maps.size()*getMindmapUpdaters().size();
					if (canceled())
						return null;
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					int count = 0;
					fireProgressUpdate(100 * count / totalCount);

					for (AMindmapUpdater updater : getMindmapUpdaters()) {
						count++;
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, updater.getTitle());
						if (canceled())
							return null;
						for (MapItem mapItem : maps) {
							mapHasChanged = false;
							MapModel map = mapItem.getModel();
							if (map==null) {								
								continue;
							}
							fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null,
									updater.getTitle()+": " + mapItem.getIdentifierForDialog());
							fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_against_p1")
									+ getMapTitle(map) + TextUtils.getText("updating_against_p2"));							
							this.mapHasChanged = updater.updateMindmap(map);
							fireProgressUpdate(100 * count / totalCount);
							if (this.mapHasChanged) {
								if (!mapItem.isMapOpen()) {
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

						}
					}

					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("updating_mapviews"));
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("updating_mapviews"));
				}
				catch (InterruptedException e) {
					LogUtils.info("MindmapUpdateController aborted.");
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
				return null;
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

			protected void done() {
				NodeView.setModifyModelWithoutRepaint(false);
				for (MapItem item : maps) {
					if (item.isMapOpen()) {
						LogUtils.info("updating view for map: " + item.getIdentifierForDialog());						
						setAttributeViewType(item.getModel(), AttributeTableLayoutModel.HIDE_ALL);
						setAttributeViewType(item.getModel(), AttributeTableLayoutModel.SHOW_ALL);
						/*NodeView nodeView = view.getNodeView(view.getModel().getRootNode());
						nodeView.updateAll();*/
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
					this.firePropertyChange(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("execution_time") + " "
							+ time + " ms");
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

			

			private void saveMap(MapModel map) throws InterruptedException, InvocationTargetException {
				if (!this.mapHasChanged) {
					return;
				}
				fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("update_references_save_map")
						+ map.getURL().getPath());

				map.setSaved(false);
				((MFileManager) UrlManager.getController()).save(map, false);
			}
			
			protected void setAttributeViewType(final MapModel map, final String type) {
				final String attributeViewType = getAttributeViewType(map);
				if (attributeViewType != null && attributeViewType != type) {
					final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
					attributes.setAttributeViewType(type);
					final MapChangeEvent mapChangeEvent = new MapChangeEvent(this, map, ModelessAttributeController.ATTRIBUTE_VIEW_TYPE, attributeViewType, type);
					Controller.getCurrentModeController().getMapController().fireMapChanged(mapChangeEvent);
				}
			}
			
			protected String getAttributeViewType(final MapModel map) {
				if (map == null) {
					return null;
				}
				final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
				if (attributes == null) {
					return null;
				}
				final String attributeViewType = attributes.getAttributeViewType();
				return attributeViewType;
			}
		};
	}
}
