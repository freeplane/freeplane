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
package org.freeplane.features.mindmapmode.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogTool;

/**
 * Reverts the map to the saved version. In Xml, the old map is stored as xml
 * and as an undo action, the new map is stored, too. Moreover, the filename of
 * the doAction is set to the appropriate map file's name. The undo action has
 * no file name associated. The action goes like this: close the actual map and
 * open the given Xml/File. If only a Xml string is given, a temporary file name
 * is created, the xml stored into and this map is opened instead of the actual.
 *
 * @author foltin
 */
class RevertAction extends AFreeplaneAction {
	private static class RevertActionInstance {
		final private Controller controller;
		private String filePrefix;
		private String localFileName;
		private String map;

		public RevertActionInstance(final Controller controller) {
			super();
			this.controller = controller;
		}

		public void act() {
			final MapController mapController = controller.getModeController().getMapController();
			try {
				controller.close(true);
				if (this.getLocalFileName() != null) {
					mapController.newMap(Compat.fileToUrl(new File(this.getLocalFileName())));
				}
				else {
					String filePrefix = ResourceBundles.getText("freeplane_reverted");
					if (this.getFilePrefix() != null) {
						filePrefix = this.getFilePrefix();
					}
					final File tempFile = File.createTempFile(filePrefix,
					    org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION, new File(ResourceController
					        .getResourceController().getFreeplaneUserDirectory()));
					final FileWriter fw = new FileWriter(tempFile);
					fw.write(this.getMap());
					fw.close();
					mapController.newMap(Compat.fileToUrl(tempFile));
				}
			}
			catch (final Exception e) {
				LogTool.severe(e);
			}
		}

		public String getFilePrefix() {
			return filePrefix;
		}

		public String getLocalFileName() {
			return localFileName;
		}

		public String getMap() {
			return map;
		}

		public void setFilePrefix(final String filePrefix) {
			this.filePrefix = filePrefix;
		}

		public void setLocalFileName(final String localFileName) {
			this.localFileName = localFileName;
		}

		public void setMap(final String map) {
			this.map = map;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public RevertAction(final Controller controller) {
		super("RevertAction", controller);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		try {
			final File file = getController().getMap().getFile();
			if (file == null) {
				UITools.errorMessage(ResourceBundles.getText("map_not_saved"));
				return;
			}
			final RevertActionInstance doAction = createRevertXmlAction(file);
			doAction.act();
		}
		catch (final IOException e) {
			LogTool.severe(e);
		}
	}

	public RevertActionInstance createRevertXmlAction(final File file) throws IOException {
		final String fileName = file.getAbsolutePath();
		final FileReader f = new FileReader(file);
		final StringBuilder buffer = new StringBuilder();
		for (int c; (c = f.read()) != -1;) {
			buffer.append((char) c);
		}
		f.close();
		return createRevertXmlAction(buffer.toString(), fileName, null);
	}

	public RevertActionInstance createRevertXmlAction(final MapModel map, final String fileName, final String filePrefix)
	        throws IOException {
		final StringWriter writer = new StringWriter();
		getModeController().getMapController().getMapWriter().writeMapAsXml(map, writer, Mode.FILE, true);
		return createRevertXmlAction(writer.getBuffer().toString(), fileName, filePrefix);
	}

	/**
	 * @param filePrefix
	 *            is used to generate the name of the reverted map in case that
	 *            fileName is null.
	 */
	public RevertActionInstance createRevertXmlAction(final String xmlPackedFile, final String fileName,
	                                                  final String filePrefix) {
		final RevertActionInstance revertXmlAction = new RevertActionInstance(getController());
		revertXmlAction.setLocalFileName(fileName);
		revertXmlAction.setMap(xmlPackedFile);
		revertXmlAction.setFilePrefix(filePrefix);
		return revertXmlAction;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.actions.ActorXml#getDoActionClass()
	 */
	public void openXmlInsteadOfMap(final String xmlFileContent) {
		final RevertActionInstance doAction = createRevertXmlAction(xmlFileContent, null, null);
		doAction.act();
	}
}
