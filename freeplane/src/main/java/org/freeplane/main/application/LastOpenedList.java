/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.application;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class manages a list of the maps that were opened last. It aims to
 * provide persistence for the last recent maps and the last selected nodes (in separate properties).
 * Maps should be shown in the format:"mode\:key",ie."Mindmap\:/home/joerg/freeplane.mm"
 */

public class LastOpenedList implements IMapViewChangeListener, IMapChangeListener {
    static class RecentFile {
        public RecentFile(String restorable, String mapName) {
            this.restorable = restorable;
            this.mapName = mapName;
        }
        public RecentFile(String restorable) {
            this(restorable, null);
        }
        String restorable;
        /** map.toString(), not-null only if opened. */
        String mapName;
        /** persisted, but not necessary not-null. */
        String lastVisitedNodeId;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((restorable == null) ? 0 : restorable.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RecentFile other = (RecentFile) obj;
            if (restorable == null) {
                if (other.restorable != null)
                    return false;
            }
            else if (!restorable.equals(other.restorable))
                return false;
            return true;
        }
		@Override
        public String toString() {
	        return "RecentFileL(" + restorable + "@" + lastVisitedNodeId + ")";
        }
    }

	private static final String LAST_OPENED_LIST_LENGTH = "last_opened_list_length";
	private static final String LAST_OPENED = "lastOpened_1.0.20";
	private static final String LAST_LOCATIONS = "lastLocations";
	private static boolean PORTABLE_APP = System.getProperty("portableapp", "false").equals("true");
	private static String USER_DRIVE = System.getProperty("user.home", "").substring(0, 2);

	final private List<RecentFile> lastOpenedList = new LinkedList<RecentFile>();
	private RecentFile mapSelectedOnStart;

	LastOpenedList() {
		restore();
		
	}

	public void registerMenuContributor(final ModeController modeController) {
		modeController.addUiBuilder(Phase.ACTIONS, "lastOpenedMaps", new EntryVisitor() {

			@Override
			public void visit(Entry target) {
				updateMenus(modeController, target);
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		}, new ChildActionEntryRemover(modeController));

	}

	public void afterViewChange(final Component oldView, final Component newView) {
		if (newView == null) {
			updateMenus();
			return;
		}
		final MapModel map = getMapModel(newView);
		final String restoreString = getRestoreable(map);
		updateList(map, restoreString);
    }

	public void afterViewClose(final Component oldView) {
		updateLastVisitedNodeId(oldView);
	}

	private boolean selectLastVisitedNode(RecentFile recentFile) {
		if (recentFile != null && recentFile.lastVisitedNodeId != null) {
			final MapModel map = Controller.getCurrentController().getMap();
			final NodeModel node = map.getNodeForID(recentFile.lastVisitedNodeId);
			if (node != null && node.hasVisibleContent()) {
				IMapSelection selection = Controller.getCurrentController().getSelection();
				// don't override node selection done by UriManager.loadURI()
				if (selection.isSelected(map.getRootNode()))
					selection.selectAsTheOnlyOneSelected(node);
				return true;
			}
		}
		return false;
	}

	private boolean saveLastPositionInMapEnabled() {
	    return ResourceController.getResourceController().getBooleanProperty("save_last_position_in_map");
    }

	public void afterViewCreated(final Component mapView) {
		final MapModel map = getMapModel(mapView);
		final RecentFile recentFile = findRecentFileByMapModel(map);
		// the next line will only succeed if the map is already opened 
		if (saveLastPositionInMapEnabled() && ! selectLastVisitedNode(recentFile)) {
			ensureSelectLastVisitedNodeOnOpen(map, recentFile);
		}
	}

	private void ensureSelectLastVisitedNodeOnOpen(final MapModel map, final RecentFile recentFile) {
	    final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (recentFile != null && recentFile.lastVisitedNodeId != null) {
			mapController.addNodeSelectionListener(new INodeSelectionListener() {
				public void onSelect(NodeModel node) {
					if (node.getMap() == map) {
						// only once
						mapController.removeNodeSelectionListener(this);
						final NodeModel toSelect = map.getNodeForID(recentFile.lastVisitedNodeId);
						// don't restore an old position if a new one is selected
						if (toSelect != null && node.isRoot())
							Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(toSelect);
					}
				}

				public void onDeselect(NodeModel node) {
				}
			});
		}
    }

	private MapModel getMapModel(final Component mapView) {
	    final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		return mapViewManager.getModel(mapView);
    }

	public void beforeViewChange(final Component oldView, final Component newView) {
	}

	private int getMaxMenuEntries() {
		return ResourceController.getResourceController().getIntProperty(LAST_OPENED_LIST_LENGTH, 25);
	}

	private String getRestorable(final File file) {
		if (file == null //
				|| !AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
					@Override
					public Boolean run() {
						return file.exists();
					}
		})) {
			return null;
		}
		final String absolutePath = file.getAbsolutePath();
		if (!PORTABLE_APP || !USER_DRIVE.endsWith(":")) {
			return "MindMap:" + absolutePath;
		}
		final String diskName = absolutePath.substring(0, 2);
		if (!diskName.equals(USER_DRIVE)) {
			return "MindMap:" + absolutePath;
		}
		return "MindMap::" + absolutePath.substring(2);
	}

	public String getRestoreable( final MapModel map) {
		if (map == null) {
			return null;
		}
		//ignore documentation maps loaded using documentation actions
		if(map.containsExtension(DocuMapAttribute.class))
			return null;
		final ModeController modeController = Controller.getCurrentModeController();
		if (modeController == null || !modeController.getModeName().equals(MModeController.MODENAME)) {
			return null;
		}
		final File file = map.getFile();
		return getRestorable(file);
	}

	public void mapChanged(final MapChangeEvent event) {
		if (!event.getProperty().equals(UrlManager.MAP_URL)) {
			return;
		}
		final URL after = (URL) event.getNewValue();
		if (after != null) {
			final String fileAfter = after.getFile();
			if (fileAfter != null) {
				final String restorable = getRestorable(new File(fileAfter));
				updateList(event.getMap(), restorable);
			}
		}
	}

	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
	}

	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	}

    public void open(final RecentFile recentFile) throws FileNotFoundException, MalformedURLException, IOException,
            URISyntaxException, XMLException {
        if (recentFile == null)
            return;
        final StringTokenizer tokens = new StringTokenizer(recentFile.restorable, ":");
        if (!tokens.hasMoreTokens())
            return;
        final boolean changedToMapView = tryToChangeToMapView(recentFile);
        if (changedToMapView)
            return;
        final String mode = tokens.nextToken();
        Controller.getCurrentController().selectMode(mode);
        File file = createFileFromRestorable(tokens);
		if(!changedToMapView)
            Controller.getCurrentModeController().getMapController().newMap(Compat.fileToUrl(file));
        else {
            final MapModel map = Controller.getCurrentController().getMap();
            Controller.getCurrentModeController().getMapController().newMapView(map);
        }
    }

	public File createFileFromRestorable(StringTokenizer tokens) {
		String fileName = tokens.nextToken(";").substring(1);
		if (PORTABLE_APP && fileName.startsWith(":") && USER_DRIVE.endsWith(":")) {
			fileName = USER_DRIVE + fileName.substring(1);
		}
		File file = new File(fileName);
		return file;
	}

	void openLastMapOnStart() {
		if (mapSelectedOnStart != null) {
			if(!tryToChangeToMapView(mapSelectedOnStart))
				safeOpen(mapSelectedOnStart);
		}
	}

	private void restore() {
        final List<String> lastOpened = getListPropertyNotNull(LAST_OPENED);
        final List<String> lastLocation = getListPropertyNotNull(LAST_LOCATIONS);
        for (int i = 0; i < lastOpened.size(); i++) {
            final RecentFile recentFile = new RecentFile(lastOpened.get(i));
            if (lastLocation.size() == lastOpened.size())
                recentFile.lastVisitedNodeId = lastLocation.get(i);
            lastOpenedList.add(recentFile);
        }
        if (!lastOpenedList.isEmpty()) {
            mapSelectedOnStart = lastOpenedList.get(0);
        }
    }

    private List<String> getListPropertyNotNull(String key) {
        final String lastOpened = ResourceController.getResourceController().getProperty(key, "");
        return ConfigurationUtils.decodeListValue(lastOpened, true);
    }

	public void safeOpen(final RecentFile recentFile) {
		try {
			open(recentFile);
		}
		catch (final Exception ex) {
			LogUtils.warn(ex);
			final String message = TextUtils.format("remove_file_from_list_on_error", recentFile.restorable);
			UITools.showFrame();
			final Component frame = UITools.getMenuComponent();
			final int remove = JOptionPane.showConfirmDialog(frame, message, "Freeplane", JOptionPane.YES_NO_OPTION);
			if (remove == JOptionPane.YES_OPTION) {
				lastOpenedList.remove(recentFile);
				updateMenus();
			}
		}
	}

	public void saveProperties() {
	    updateLastVisitedNodeIds();
	    ResourceController.getResourceController().setProperty(LAST_OPENED,
		    ConfigurationUtils.encodeListValue(getRestoreables(), true));
	    ResourceController.getResourceController().setProperty(LAST_LOCATIONS,
	        ConfigurationUtils.encodeListValue(getLastVisitedNodeIds(), true));
	}

	private void updateLastVisitedNodeIds() {
		final List<? extends Component> mapViews = Controller.getCurrentController().getMapViewManager()
		    .getMapViewVector();
		for (Component component : mapViews) {
			updateLastVisitedNodeId(component);
		}
	}

	private void updateLastVisitedNodeId(final Component mapView) {
		if (!(mapView instanceof MapView))
			return;
		final NodeView selected = ((MapView) mapView).getSelected();
		final RecentFile recentFile = findRecentFileByMapModel(getMapModel(mapView));
		if (selected != null && recentFile != null) {
			NodeModel selectedNode = selected.getModel();
			// if a map has never been visited restoration of the selection has not yet taken place
			if (!selectedNode.isRoot())
				recentFile.lastVisitedNodeId = selectedNode.getID();
		}
	}

	private RecentFile findRecentFileByRestorable(String restorable) {
		for (RecentFile recentFile : lastOpenedList) {
			if (recentFile.restorable.equals(restorable))
				return recentFile;
		}
		return null;
	}

	private RecentFile findRecentFileByMapModel(final MapModel map) {
		return findRecentFileByRestorable(getRestoreable(map));
	}

    private List<String> getRestoreables() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
            result.add(recentFile.restorable);
        }
		return result;
	}

	private List<String> getLastVisitedNodeIds() {
	    ArrayList<String> result = new ArrayList<String>(lastOpenedList.size());
	    for (RecentFile recentFile : lastOpenedList) {
	        result.add(recentFile.lastVisitedNodeId);
	    }
	    return result;
	}

    private boolean tryToChangeToMapView(final RecentFile mapSelectedOnStart) {
		return Controller.getCurrentController().getMapViewManager().tryToChangeToMapView(mapSelectedOnStart.mapName);
	}

	private void updateList(final MapModel map, final String restoreString) {
		//ignore documentation maps loaded using documentation actions
		if(map.containsExtension(DocuMapAttribute.class))
			return;
		if (restoreString != null) {
			RecentFile recentFile = findRecentFileByRestorable(restoreString);
			if (recentFile != null) {
				lastOpenedList.remove(recentFile);
				recentFile.mapName = map.getTitle();
				lastOpenedList.add(0, recentFile);
			}
			else {
				lastOpenedList.add(0, new RecentFile(restoreString, map.getTitle()));
			}
		}
		updateMenus();
	}

	private void updateMenus() {
		final IUserInputListenerFactory userInputListenerFactory = Controller.getCurrentModeController()
		    .getUserInputListenerFactory();
		userInputListenerFactory.rebuildMenus("lastOpenedMaps");
	}

	private void updateMenus(ModeController modeController, Entry target) {
		List<AFreeplaneAction> openMapActions = createOpenLastMapActionList();
		for (AFreeplaneAction openMapAction : openMapActions) {
			modeController.addActionIfNotAlreadySet(openMapAction);
			new EntryAccessor().addChildAction(target, openMapAction);
		}
	}

	public List<AFreeplaneAction> createOpenLastMapActionList() {
		Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
	    int i = 0;
	    int maxEntries = getMaxMenuEntries();
	    List<AFreeplaneAction> openMapActions = new ArrayList<AFreeplaneAction>(maxEntries);
	    for (final RecentFile recentFile : lastOpenedList) {
	    	if (i == 0
	    	        && (!modeController.getModeName().equals(MModeController.MODENAME) || controller.getMap() == null || controller
	    	            .getMap().getURL() == null)) {
	    		i++;
	    		maxEntries++;
	    	}
	    	if (i == maxEntries) {
	    		break;
	    	}

			final AFreeplaneAction openMapAction = new OpenLastOpenedAction(i++, this, recentFile);
			createOpenMapItemName(openMapAction, recentFile.restorable);
	    	openMapActions.add(openMapAction);
	    }
	    return openMapActions;
    }

	private void createOpenMapItemName(AFreeplaneAction openMapAction, final String restorable) {
		final int separatorIndex = restorable.indexOf(':');
		if(separatorIndex == -1)
			openMapAction.putValue(Action.NAME, restorable);

		String key = restorable.substring(0, separatorIndex);
		String fileName = restorable.substring(separatorIndex);
		String keyName = TextUtils.getText("open_as" + key, key);
		openMapAction.putValue(Action.SHORT_DESCRIPTION, keyName);
		openMapAction.putValue(Action.DEFAULT, fileName);
		if(fileName.startsWith("::"))
			fileName = fileName.substring(2);
		else
			fileName = fileName.substring(1);

		openMapAction.putValue(Action.NAME, keyName + " " + fileName);

    }

	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}
}
