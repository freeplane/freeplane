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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewChangeListener;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * This class manages a list of the maps that were opened last. It aims to
 * provide persistence for the last recent maps. Maps should be shown in the
 * format:"mode\:key",ie."Mindmap\:/home/joerg/freeplane.mm"
 */
class LastOpenedList implements IMapViewChangeListener {
	private static final String SEPARATOR = ";";
	private final Controller controller;
	/**
	 * Contains Restore strings.
	 */
	final private List<String> lastOpenedList = new LinkedList<String>();
	final private int maxEntries;
	/**
	 * Contains Restore string => map name (map.toString()).
	 */
	final private Map<String, String> mRestorableToMapName = new HashMap<String, String>();

	LastOpenedList(final Controller controller, final String restored, final int maxEntries) {
		this.controller = controller;
		this.maxEntries = maxEntries;
		if (restored != null) {
			lastOpenedList.addAll(Arrays.asList(restored.split(SEPARATOR)));
		}
	}

	public void afterViewChange(final Component oldView, final Component newView) {
		mapOpened(newView);
	}

	public void afterViewClose(final Component oldView) {
	}

	public void afterViewCreated(final Component mapView) {
	}

	public void beforeViewChange(final Component oldView, final Component newView) {
	}

	String getStringRep() {
		final StringBuilder strBldr = new StringBuilder();
		for (final String s : lastOpenedList) {
			strBldr.append(s + SEPARATOR);
		}
		return strBldr.toString();
	}

	ListIterator listIterator() {
		return lastOpenedList.listIterator();
	}

	void mapOpened(final Component mapView) {
		if (mapView == null) {
			return;
		}
		final IMapViewManager mapViewManager = controller.getMapViewManager();
		final ModeController modeController = mapViewManager.getModeController(mapView);
		final MapModel map = mapViewManager.getModel(mapView);
		final String restoreString = UrlManager.getController(modeController).getRestoreable(map);
		if (restoreString != null) {
			if (lastOpenedList.contains(restoreString)) {
				lastOpenedList.remove(restoreString);
			}
			lastOpenedList.add(0, restoreString);
			mRestorableToMapName.put(restoreString, map.getTitle());
			while (lastOpenedList.size() > maxEntries) {
				lastOpenedList.remove(lastOpenedList.size() - 1);
			}
		}
		updateMenus();
	}

	public void open(final Controller controller, final String restoreable) throws FileNotFoundException,
	        XMLParseException, MalformedURLException, IOException, URISyntaxException {
		final boolean changedToMapView = controller.getMapViewManager().tryToChangeToMapView(
		    mRestorableToMapName.get(restoreable));
		if ((restoreable != null) && !(changedToMapView)) {
			final StringTokenizer token = new StringTokenizer(restoreable, ":");
			if (token.hasMoreTokens()) {
				final String mode = token.nextToken();
				if (controller.selectMode(mode)) {
					final String fileName = token.nextToken("").substring(1);
					controller.getModeController().getMapController().newMap(Compat.fileToUrl(new File(fileName)));
				}
			}
		}
	}

	private void updateMenus() {
		final MenuBuilder menuBuilder = controller.getModeController().getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.removeChildElements(FreeplaneMenuBar.FILE_MENU + "/last");
		int i = -1;
		for (final ListIterator it = listIterator(); it.hasNext();) {
			final String key = (String) it.next();
			if (++i == 0) {
				continue;
			}
			final AFreeplaneAction lastOpenedActionListener = new OpenLastOpenedAction(i, key, controller, this);
			menuBuilder.addAction(FreeplaneMenuBar.FILE_MENU + "/last", lastOpenedActionListener, UIBuilder.AS_CHILD);
		}
	}

	public void remove(String restoreable) {
	    lastOpenedList.remove(restoreable);
	    updateMenus();
    }
}
