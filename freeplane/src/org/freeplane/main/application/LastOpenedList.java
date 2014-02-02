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
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * This class manages a list of the maps that were opened last. It aims to
 * provide persistence for the last recent maps. Maps should be shown in the
 * format:"mode\:key",ie."Mindmap\:/home/joerg/freeplane.mm"
 */
class LastOpenedList implements IMapViewChangeListener, IMapChangeListener {
	private static final String MENU_CATEGORY = "main_menu_most_recent_files";
	private static final String LAST_OPENED_LIST_LENGTH = "last_opened_list_length";
	private static final String LAST_OPENED = "lastOpened_1.0.20";
	// // 	private final Controller controller;
	private static boolean PORTABLE_APP = System.getProperty("portableapp", "false").equals("true");
	private static String USER_DRIVE = System.getProperty("user.home", "").substring(0, 2);
	/**
	 * Contains Restore strings.
	 */
	final private List<String> lastOpenedList = new LinkedList<String>();
	/**
	 * Contains Restore string => map name (map.toString()).
	 */
	final private Map<String, String> mRestorableToMapName = new HashMap<String, String>();
	private String mapSelectedOnStart;

	LastOpenedList() {
//		this.controller = controller;
		restoreList(LAST_OPENED);
	}

	public void afterViewChange(final Component oldView, final Component newView) {
		if (newView == null) {
			updateMenus();
			return;
		}
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final MapModel map = mapViewManager.getModel(newView);
		final String restoreString = getRestoreable(map);
		updateList(map, restoreString);
	}

	public void afterViewClose(final Component oldView) {
	}

	public void afterViewCreated(final Component mapView) {
	}

	public void beforeViewChange(final Component oldView, final Component newView) {
	}

	private int getMaxMenuEntries() {
		return ResourceController.getResourceController().getIntProperty(LAST_OPENED_LIST_LENGTH, 25);
	}

	private String getRestorable(final File file) {
		if (file == null) {
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
		if (!modeController.getModeName().equals(MModeController.MODENAME)) {
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

	public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
	}

	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
	}

	public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                        final NodeModel child, final int newIndex) {
	}

	public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
	}

	public void open(final String restoreable) throws FileNotFoundException, MalformedURLException,
	        IOException, URISyntaxException, XMLException {
		if (restoreable == null)
			return;
		final StringTokenizer tokens = new StringTokenizer(restoreable, ":");
		if (!tokens.hasMoreTokens())
			return;
		final boolean changedToMapView;
			changedToMapView = tryToChangeToMapView(restoreable);
		if (changedToMapView)
			return;
		final String mode = tokens.nextToken();
		Controller.getCurrentController().selectMode(mode);
		String fileName = tokens.nextToken(";").substring(1);
		if (PORTABLE_APP && fileName.startsWith(":") && USER_DRIVE.endsWith(":")) {
			fileName = USER_DRIVE + fileName.substring(1);
		}
		if(!changedToMapView)
			Controller.getCurrentModeController().getMapController().newMap(Compat.fileToUrl(new File(fileName)));
		else{
			final MapModel map = Controller.getCurrentController().getMap();
			Controller.getCurrentModeController().getMapController().newMapView(map);
		}
	}

	void openLastMapOnStart() {
		if (mapSelectedOnStart != null) {
			if(!tryToChangeToMapView(mapSelectedOnStart))
				safeOpen(mapSelectedOnStart);
		}
	}

	private void remove(final String restoreable) {
		lastOpenedList.remove(restoreable);
		updateMenus();
	}

	private void restoreList(final String key) {
		final String restored = ResourceController.getResourceController().getProperty(key, null);
		if (restored != null && !restored.equals("")) {
			lastOpenedList.addAll(ConfigurationUtils.decodeListValue(restored, true));
			if (!lastOpenedList.isEmpty()) {
				mapSelectedOnStart = lastOpenedList.get(0);
			}
		}
	}

	void safeOpen(final List<String> maps) {
		for (final String restoreable : maps) {
			safeOpen(restoreable);
		}
	}

	public void safeOpen(final String restoreable) {
		try {
			open(restoreable);
		}
		catch (final Exception ex) {
			LogUtils.warn(ex);
			final String message = TextUtils.format("remove_file_from_list_on_error", restoreable);
			UITools.showFrame();
			final Frame frame = UITools.getFrame();
			final int remove = JOptionPane.showConfirmDialog(frame, message, "Freeplane", JOptionPane.YES_NO_OPTION);
			if (remove == JOptionPane.YES_OPTION) {
				remove(restoreable);
			}
		}
	}

	public void saveProperties() {
		ResourceController.getResourceController().setProperty(LAST_OPENED,
		    ConfigurationUtils.encodeListValue(lastOpenedList, true));
	}

	private boolean tryToChangeToMapView(final String restoreable) {
		return Controller.getCurrentController().getMapViewManager().tryToChangeToMapView(mRestorableToMapName.get(restoreable));
	}

	private void updateList(final MapModel map, final String restoreString) {
		//ignore documentation maps loaded using documentation actions
		if(map.containsExtension(DocuMapAttribute.class))
			return;
		if (restoreString != null) {
			if (lastOpenedList.contains(restoreString)) {
				lastOpenedList.remove(restoreString);
			}
			lastOpenedList.add(0, restoreString);
			mRestorableToMapName.put(restoreString, map.getTitle());
		}
		updateMenus();
	}

	private void updateMenus() {
		Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.removeChildElements(MENU_CATEGORY);
		int i = 0;
		int maxEntries = getMaxMenuEntries();
		for (final String key : lastOpenedList) {
			if (i == 0
			        && (!modeController.getModeName().equals(MModeController.MODENAME) || controller.getMap() == null || controller
			            .getMap().getURL() == null)) {
				i++;
				maxEntries++;
			}
			if (i == maxEntries) {
				break;
			}
			final AFreeplaneAction lastOpenedActionListener = new OpenLastOpenedAction(i++, this);
			final IFreeplaneAction decoratedAction = menuBuilder.decorateAction(lastOpenedActionListener);
			final JMenuItem item = new JFreeplaneMenuItem(decoratedAction);
			item.setActionCommand(key);
			String text = createOpenMapItemName(key);
			item.setText(text);
			item.setMnemonic(0);
			menuBuilder.addMenuItem(MENU_CATEGORY, item, MENU_CATEGORY + '/' + lastOpenedActionListener.getKey(),
			    UIBuilder.AS_CHILD);
		}
	}

	private String createOpenMapItemName(final String restorable) {
		final int separatorIndex = restorable.indexOf(':');
		if(separatorIndex == -1)
			return restorable;
		String key = restorable.substring(0, separatorIndex);
		String fileName = restorable.substring(separatorIndex);
		if(fileName.startsWith("::"))
			return TextUtils.getText("open_as" + key, key) + " " + fileName.substring(2);
		else
			return TextUtils.getText("open_as" + key, key) + " " + fileName.substring(1);
    }

	public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
	                           final NodeModel child, final int newIndex) {
	}
}
