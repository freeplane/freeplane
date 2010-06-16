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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.browsemode.BModeController;

/**
 * @author Dimitry Polivaev
 */
class AppletViewController extends ViewController {
	final private JApplet applet;
	private JComponent mComponentInSplitPane;
	private JPanel southPanel;

	public AppletViewController(final Controller controller, final JApplet applet,
	                            final IMapViewManager mapViewController) {
		super(controller, mapViewController);
		this.applet = applet;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.views.ViewController#getContentPane()
	 */
	@Override
	public Container getContentPane() {
		return applet.getContentPane();
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return (FreeplaneMenuBar) applet.getJMenuBar();
	}

	public FreeplaneVersion getFreeplaneVersion() {
		return FreeplaneVersion.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getJFrame()
	 */
	@Override
	public JFrame getJFrame() {
		throw new IllegalArgumentException("The applet has no frames");
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getSouthPanel()
	 */
	public JPanel getSouthPanel() {
		return southPanel;
	}

	@Override
	public void init() {
		getContentPane().add(getScrollPane(), BorderLayout.CENTER);
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(getStatusBar(), BorderLayout.SOUTH);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		super.init();
		SwingUtilities.updateComponentTreeUI(applet);
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
					};
				});
			}
			catch (final InterruptedException e) {
				LogTool.severe(e);
			}
			catch (final InvocationTargetException e) {
				LogTool.severe(e);
			}
		}
		getController().selectMode(BModeController.MODENAME);
		String initialMapName = ResourceController.getResourceController().getProperty("browsemode_initial_map");
		if (initialMapName != null && initialMapName.startsWith(".")) {
			/* new handling for relative urls. fc, 29.10.2003. */
			try {
				final URL documentBaseUrl = new URL(applet.getDocumentBase(), initialMapName);
				initialMapName = documentBaseUrl.toString();
			}
			catch (final java.net.MalformedURLException e) {
				UITools.errorMessage(ResourceBundles.getText("url_load_error") + " " + initialMapName);
				System.err.println(e);
				return;
			}
			/* end: new handling for relative urls. fc, 29.10.2003. */
		}
		if (initialMapName != "") {
			try {
				final URL mapUrl = new URL(initialMapName);
				getController().getModeController().getMapController().newMap(mapUrl);
			}
			catch (final Exception e) {
				LogTool.severe(e);
			}
		}
	}

	@Override
	public JSplitPane insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		if (mComponentInSplitPane == pMindMapComponent) {
			return null;
		}
		removeSplitPane();
		mComponentInSplitPane = pMindMapComponent;
		southPanel.add(pMindMapComponent, BorderLayout.CENTER);
		southPanel.revalidate();
		return null;
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
			final String path = location.getPath();
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
		applet.getAppletContext().showDocument(doc, "_blank");
	}

	@Override
	public void removeSplitPane() {
		if (mComponentInSplitPane != null) {
			southPanel.remove(mComponentInSplitPane);
			southPanel.revalidate();
			mComponentInSplitPane = null;
		}
	}

	@Override
	protected void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
		applet.setJMenuBar(menuBar);
	}

	@Override
	public void setTitle(final String title) {
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		if (waiting) {
			applet.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			applet.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			applet.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			applet.getRootPane().getGlassPane().setVisible(false);
		}
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
			LogTool.severe(e);
		}
	}
}
