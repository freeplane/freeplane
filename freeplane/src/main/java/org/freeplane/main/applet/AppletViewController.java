/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.main.applet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapViewScrollPane;

/**
 * @author Dimitry Polivaev
 */
class AppletViewController extends FrameController implements IMapViewChangeListener {
	final private FreeplaneApplet applet;
	private JComponent mComponentInSplitPane;
	private JComponent mapContentBox;
	private JScrollPane scrollPane;
	public AppletViewController( final FreeplaneApplet applet, Controller controller,
	                            final IMapViewManager mapViewController) {
		super(controller, mapViewController, "");
		this.applet = applet;
		mapViewController.addMapViewChangeListener(this);
	}


	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return (FreeplaneMenuBar) applet.getJMenuBar();
	}

	public FreeplaneVersion getFreeplaneVersion() {
		return FreeplaneVersion.getVersion();
	}


	@Override
	public void init(Controller controller) {
		mapContentBox = new JPanel(new BorderLayout());
		scrollPane = new MapViewScrollPane();
		mapContentBox.add(scrollPane, BorderLayout.CENTER);
		applet.getContentPane().add(mapContentBox, BorderLayout.CENTER);
		super.init(controller);
		SwingUtilities.updateComponentTreeUI(applet);
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
					};
				});
			}
			catch (final InterruptedException e) {
				LogUtils.severe(e);
			}
			catch (final InvocationTargetException e) {
				LogUtils.severe(e);
			}
		}
		getController().selectMode(BModeController.MODENAME);
		String initialMapName = ResourceController.getResourceController().getProperty("browsemode_initial_map");
		if (initialMapName != null && initialMapName.startsWith(".")) {
			String locationUrl = applet.getParameter("location_href");
			try {
				final URI codebase = locationUrl != null ?  new URI(locationUrl):applet.getCodeBase().toURI();
				URI uri = codebase.resolve(new URI(null, null, initialMapName, null));
				URL documentBase = new URL(uri.getScheme(), uri.getHost(),  uri.getPort(), uri.getPath());
				initialMapName = documentBase.toString();
			}
			catch (final Exception e) {
				UITools.errorMessage(TextUtils.format("map_load_error", initialMapName));
				System.err.println(e);
				return;
			}
			/* end: new handling for relative urls. fc, 29.10.2003. */
		}
		if (initialMapName != "") {
			try {
				final URL mapUrl = new URL(initialMapName);
				getController().getModeController().getMapController().openMap(mapUrl);
			}
			catch (final Exception e) {
				LogUtils.severe(e);
			}
		}
	}

	@Override
	public void insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		if (mComponentInSplitPane == pMindMapComponent) {
			return;
		}
		removeSplitPane();
		mComponentInSplitPane = pMindMapComponent;
		mapContentBox.add(pMindMapComponent, BorderLayout.SOUTH);
		mapContentBox.revalidate();
	}

	@Override
	public boolean isApplet() {
		return true;
	}

	@Override
	public void openDocument(final URI location) {
		try {
			final String scheme = location.getScheme();
			final String host = location.getHost();
			final String path = location.isOpaque() ? location.getSchemeSpecificPart() : location.getPath();
			final int port = location.getPort();
			final String query = location.getQuery();
			final String fragment = location.getFragment();
			final StringBuilder file = new StringBuilder(path);
			if(query != null){
				file.append('?');
				file.append(query);
			}
			if(fragment != null){
				file.append('#');
				file.append(fragment);
			}
			final URL url = new URL(scheme, host, port, file.toString());
			openDocument(url);
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openDocument(final URL doc) {
		applet.showDocument(doc);
	}

	@Override
	public void removeSplitPane() {
		if (mComponentInSplitPane != null) {
			mapContentBox.remove(mComponentInSplitPane);
			mapContentBox.revalidate();
			mComponentInSplitPane = null;
		}
	}

	@Override
	protected void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
		applet.setJMenuBar(menuBar);
	}

	@Override
	public void setTitle(final String frameTitle) {
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		applet.setWaitingCursor(waiting);
	}

	public void start() {
		try {
			final IMapSelection selection = getController().getSelection();
			if (selection != null) {
				selection.selectRoot();
			}
			else {
				System.err.println("View is null.");
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

	public void afterViewChange(Component oldView, Component newView) {
		if(scrollPane != null)
			scrollPane.setViewportView(newView);
    }

	public void afterViewClose(Component oldView) {
    }

	public void afterViewCreated(Component mapView) {
    }

	public void beforeViewChange(Component oldView, Component newView) {
    }

	public void previousMapView() {
		throw new RuntimeException("Method not implemented");
	}

	public void nextMapView() {
		throw new RuntimeException("Method not implemented");
	}


	@Override
	public Component getCurrentRootComponent() {
		return applet;
	}


	@Override
	public Component getMenuComponent() {
		return applet;
	}
}
