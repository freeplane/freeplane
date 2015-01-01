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
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.FrameController;
import org.freeplane.view.swing.map.MapViewScrollPane;

class DialogController extends FrameController {
	final private JDialog dialog;

	public JDialog getDialog() {
		return dialog;
	}

	private JScrollPane mapViewScrollPane = null;

	/** Contains the value where the Note Window should be displayed (right, left, top, bottom) */
	/** Contains the Note Window Component 
	 * @param controller */
	public DialogController(Controller controller, final IMapViewManager mapViewController, final JDialog dialog) {
		super(controller, mapViewController, "dialog_");
		this.dialog = dialog;
		getContentPane().setLayout(new BorderLayout());
		mapViewScrollPane = new MapViewScrollPane();
		getContentPane().add(mapViewScrollPane, BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getContentPane()
	 */
	@Override
	public RootPaneContainer getRootPaneContainer() {
		return dialog;
	}

	@Override
	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return (FreeplaneMenuBar) dialog.getJMenuBar();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getJFrame()
	 */
	@Override
	public JFrame getJFrame() {
		return (JFrame) JOptionPane.getFrameForComponent(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#getLayeredPane()
	 */
	public JLayeredPane getLayeredPane() {
		return dialog.getLayeredPane();
	}

	@Override
	public void insertComponentIntoSplitPane(final JComponent pMindMapComponent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	@Override
	public void openDocument(final URI uri) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Open url in WWW browser. This method hides some differences between
	 * operating systems.
	 */
	@Override
	public void openDocument(final URL url) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setFreeplaneMenuBar(final FreeplaneMenuBar menuBar) {
		dialog.setJMenuBar(menuBar);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.FreeplaneMain#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(final String frameTitle) {
		dialog.setTitle(frameTitle);
	}

	@Override
	public void setWaitingCursor(final boolean waiting) {
		if (waiting) {
			dialog.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			dialog.getRootPane().getGlassPane().setVisible(true);
		}
		else {
			dialog.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			dialog.getRootPane().getGlassPane().setVisible(false);
		}
	}

	@Override
	public void removeSplitPane() {
		throw new UnsupportedOperationException();
	}

	public void setMapView(Component mapViewComponent) {
	    mapViewScrollPane.getViewport().setView(mapViewComponent);
    }

	public void previousMapView() {
		throw new RuntimeException("Method not implemented");
	}

	public void nextMapView() {
		throw new RuntimeException("Method not implemented");
	}
}
