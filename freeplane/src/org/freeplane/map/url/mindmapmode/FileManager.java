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
package org.freeplane.map.url.mindmapmode;

import java.awt.dnd.DropTarget;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.url.UrlManager;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class FileManager extends UrlManager {
	private class MindMapFilter extends FileFilter {
		@Override
		public boolean accept(final File f) {
			if (f.isDirectory()) {
				return true;
			}
			final String extension = Tools.getExtension(f.getName());
			if (extension != null) {
				if (extension
				    .equals(org.freeplane.map.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
					return true;
				}
				else {
					return false;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return getMModeController().getText("mindmaps_desc");
		}
	}

	public static final String FREEMIND_FILE_EXTENSION = "."
	        + FileManager.FREEMIND_FILE_EXTENSION_WITHOUT_DOT;
	public static final String FREEMIND_FILE_EXTENSION_WITHOUT_DOT = "mm";
	FileFilter filefilter = new MindMapFilter();

	/**
	 * @param modeController
	 */
	public FileManager(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
	}

	/**
	 *
	 */
	private void createActions(final MModeController modeController) {
		modeController.addAction("exportBranch", new ExportBranchAction());
		modeController.addAction("importBranch", new ImportBranchAction());
		modeController.addAction("importLinkedBranch", new ImportLinkedBranchAction());
		modeController.addAction("importLinkedBranchWithoutRoot",
		    new ImportLinkedBranchWithoutRootAction());
		modeController.addAction("importExplorerFavorites", new ImportExplorerFavoritesAction());
		modeController.addAction("importFolderStructure", new ImportFolderStructureAction());
		modeController.addAction("revertAction", new RevertAction());
	}

	protected JFileChooser getFileChooser() {
		return getFileChooser(getFileFilter());
	}

	public FileFilter getFileFilter() {
		return filefilter;
	}

	/**
	 * Creates a proposal for a file name to save the map. Removes all illegal
	 * characters. Fixed: When creating file names based on the text of the root
	 * node, now all the extra unicode characters are replaced with _. This is
	 * not very good. For chinese content, you would only get a list of ______
	 * as a file name. Only characters special for building file paths shall be
	 * removed (rather than replaced with _), like : or /. The exact list of
	 * dangeous characters needs to be investigated. 0.8.0RC3. Keywords: suggest
	 * file name.
	 *
	 * @param map
	 */
	private String getFileNameProposal(final MapModel map) {
		String rootText = (map.getRootNode()).getPlainTextContent();
		rootText = rootText.replaceAll("[&:/\\\\\0%$#~\\?\\*]+", "");
		return rootText;
	}

	/**
	 * @return
	 */
	public String getLinkByFileChooser(final MapModel map) {
		return getLinkByFileChooser(map, getFileFilter());
	}

	public String getLinkByFileChooser(final MapModel map, final FileFilter fileFilter) {
		URL link;
		String relative = null;
		File input;
		JFileChooser chooser = null;
		if (map.getFile() == null) {
			JOptionPane.showMessageDialog(Controller.getController().getViewController()
			    .getContentPane(), getModeController().getText("not_saved_for_link_error"),
			    "FreeMind", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		if (getLastCurrentDir() != null) {
			chooser = new JFileChooser(getLastCurrentDir());
		}
		else {
			chooser = new JFileChooser();
		}
		if (fileFilter != null) {
			chooser.setFileFilter(fileFilter);
		}
		else {
			chooser.setFileFilter(chooser.getAcceptAllFileFilter());
		}
		final int returnVal = chooser.showOpenDialog(Controller.getController().getViewController()
		    .getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			input = chooser.getSelectedFile();
			setLastCurrentDir(input.getParentFile());
			try {
				link = Tools.fileToUrl(input);
				relative = link.toString();
			}
			catch (final MalformedURLException ex) {
				Controller.getController().errorMessage(getModeController().getText("url_error"));
				return null;
			}
			if (Controller.getResourceController().getProperty("links").equals("relative")) {
				try {
					relative = Tools.toRelativeURL(Tools.fileToUrl(map.getFile()), link);
				}
				catch (final MalformedURLException ex) {
					Controller.getController().errorMessage(
					    getModeController().getText("url_error"));
					return null;
				}
			}
		}
		return relative;
	}

	/**
	 * @return
	 */
	private MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	@Override
	public String getRestoreable(final MapModel map) {
		if (map == null) {
			return null;
		}
		final File file = map.getFile();
		if (file == null) {
			return null;
		}
		return "MindMap:" + file.getAbsolutePath();
	}

	public void open() {
		final JFileChooser chooser = getFileChooser();
		final int returnVal = chooser.showOpenDialog(getMModeController().getMapView());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles;
			if (chooser.isMultiSelectionEnabled()) {
				selectedFiles = chooser.getSelectedFiles();
			}
			else {
				selectedFiles = new File[] { chooser.getSelectedFile() };
			}
			for (int i = 0; i < selectedFiles.length; i++) {
				final File theFile = selectedFiles[i];
				try {
					setLastCurrentDir(theFile.getParentFile());
					getMModeController().getMapController().newMap(Tools.fileToUrl(theFile));
				}
				catch (final Exception ex) {
					handleLoadingException(ex);
					break;
				}
			}
		}
		Controller.getController().getViewController().setTitle();
	}

	public boolean save(final MapModel map) {
		if (map.isSaved()) {
			return true;
		}
		if (map.getURL() == null || map.isReadOnly()) {
			return saveAs(map);
		}
		else {
			return save(map, map.getFile());
		}
	}

	/**
	 * Return the success of saving
	 */
	public boolean save(final MapModel map, final File file) {
		try {
			final String lockingUser = ((MMapController) getModeController().getMapController())
			    .tryToLock(map, file);
			if (lockingUser != null) {
				Controller.getController().informationMessage(
				    Tools.expandPlaceholders(getMModeController().getText("map_locked_by_save_as"),
				        file.getName(), lockingUser));
				return false;
			}
		}
		catch (final Exception e) {
			Controller.getController().informationMessage(
			    Tools.expandPlaceholders(getMModeController().getText("locking_failed_by_save_as"),
			        file.getName()));
			return false;
		}
		return saveInternal((MindMapMapModel) map, file, false);
	}

	/**
	 * Save as; return false is the action was cancelled
	 */
	public boolean saveAs(final MapModel map) {
		final JFileChooser chooser = getFileChooser();
		if (getMapsParentFile() == null) {
			chooser.setSelectedFile(new File(getFileNameProposal(map)
			        + org.freeplane.map.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION));
		}
		chooser.setDialogTitle(getMModeController().getText("save_as"));
		final int returnVal = chooser.showSaveDialog(getMModeController().getMapView());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File f = chooser.getSelectedFile();
		setLastCurrentDir(f.getParentFile());
		final String ext = Tools.getExtension(f.getName());
		if (!ext
		    .equals(org.freeplane.map.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION_WITHOUT_DOT)) {
			f = new File(f.getParent(), f.getName()
			        + org.freeplane.map.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION);
		}
		if (f.exists()) {
			final int overwriteMap = JOptionPane.showConfirmDialog(getMModeController()
			    .getMapView(), getMModeController().getText("map_already_exists"), "FreeMind",
			    JOptionPane.YES_NO_OPTION);
			if (overwriteMap != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		save(map, f);
		Controller.getController().getMapViewManager().updateMapViewName();
		return true;
	}

	/**
	 * This method is intended to provide both normal save routines and saving
	 * of temporary (internal) files.
	 */
	boolean saveInternal(final MindMapMapModel map, final File file, final boolean isInternal) {
		if (!isInternal && map.isReadOnly()) {
			System.err.println("Attempt to save read-only map.");
			return false;
		}
		try {
			if (map.getTimerForAutomaticSaving() != null) {
				map.getTimerForAutomaticSaving().cancel();
			}
			final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(file)));
			getMModeController().getMapController().writeMapAsXml(map, fileout, true);
			if (!isInternal) {
				map.setFile(file);
				map.setSaved(true);
			}
			map.scheduleTimerForAutomaticSaving();
			return true;
		}
		catch (final FileNotFoundException e) {
			final String message = Tools.expandPlaceholders(getMModeController().getText(
			    "save_failed"), file.getName());
			if (!isInternal) {
				Controller.getController().errorMessage(message);
			}
			else {
				Controller.getController().getViewController().out(message);
			}
		}
		catch (final Exception e) {
			Logger.global.log(Level.SEVERE, "Error in MindMapMapModel.save(): ");
			Tools.logException(e);
		}
		map.scheduleTimerForAutomaticSaving();
		return false;
	}

	@Override
	public void startup() {
		final MModeController modeController = getMModeController();
		final MapView mapView = modeController.getMapView();
		if (mapView != null) {
			final FileOpener fileOpener = new FileOpener(modeController);
			new DropTarget(mapView, fileOpener);
		}
	}
}
