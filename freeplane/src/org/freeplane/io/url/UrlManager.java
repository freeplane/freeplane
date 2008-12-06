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
package org.freeplane.io.url;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager {
	static private boolean actionsCreated = false;
	private static File lastCurrentDir = null;
	final private ModeController modeController;

	public UrlManager(final ModeController modeController) {
		super();
		this.modeController = modeController;
		createActions();
	}

	/**
	 *
	 */
	private void createActions() {
		if (!actionsCreated) {
			actionsCreated = true;
			Freeplane.getController().addAction("open",
			    new OpenAction(modeController));
			Freeplane.getController().addAction("save",
			    new SaveAction(modeController));
			Freeplane.getController().addAction("saveAs",
			    new SaveAsAction(modeController));
		}
	}

	/**
	 * Creates a file chooser with the last selected directory as default.
	 */
	public JFileChooser getFileChooser(final FileFilter filter) {
		final JFileChooser chooser = new JFileChooser();
		final File parentFile = getMapsParentFile();
		if (parentFile != null && getLastCurrentDir() == null) {
			setLastCurrentDir(parentFile);
		}
		if (getLastCurrentDir() != null) {
			chooser.setCurrentDirectory(getLastCurrentDir());
		}
		if (filter != null) {
			chooser.addChoosableFileFilter(filter);
		}
		return chooser;
	}

	public File getLastCurrentDir() {
		return lastCurrentDir;
	}

	protected File getMapsParentFile() {
		final MapModel map = Freeplane.getController().getMap();
		if ((map != null) && (map.getFile() != null)
		        && (map.getFile().getParentFile() != null)) {
			return map.getFile().getParentFile();
		}
		return null;
	}

	public ModeController getModeController() {
		return modeController;
	}

	public String getRestoreable(final MapModel map) {
		return null;
	}

	public void handleLoadingException(final Exception ex) {
		final String exceptionType = ex.getClass().getName();
		if (exceptionType.equals("freemind.main.XMLParseException")) {
			final int showDetail = JOptionPane.showConfirmDialog(modeController
			    .getMapView(), modeController.getText("map_corrupted"),
			    "FreeMind", JOptionPane.YES_NO_OPTION,
			    JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				Freeplane.getController().errorMessage(ex);
			}
		}
		else if (exceptionType.equals("java.io.FileNotFoundException")) {
			Freeplane.getController().errorMessage(ex.getMessage());
		}
		else {
			org.freeplane.main.Tools.logException(ex);
			Freeplane.getController().errorMessage(ex);
		}
	}

	public NodeModel load(final URL url, final MapModel map) {
		NodeModel root = null;
		InputStreamReader urlStreamReader = null;
		try {
			urlStreamReader = new InputStreamReader(url.openStream());
		}
		catch (final AccessControlException ex) {
			Freeplane.getController().errorMessage(
			    "Could not open URL " + url.toString() + ". Access Denied.");
			System.err.println(ex);
			return null;
		}
		catch (final Exception ex) {
			Freeplane.getController().errorMessage(
			    "Could not open URL " + url.toString() + ".");
			System.err.println(ex);
			return null;
		}
		try {
			root = modeController.getMapController().createNodeTreeFromXml(
			    (map), urlStreamReader);
			urlStreamReader.close();
			return root;
		}
		catch (final Exception ex) {
			System.err.println(ex);
			return null;
		}
	}

	public void setLastCurrentDir(final File lastCurrentDir) {
		UrlManager.lastCurrentDir = lastCurrentDir;
	}

	public void startup() {
	}
}
