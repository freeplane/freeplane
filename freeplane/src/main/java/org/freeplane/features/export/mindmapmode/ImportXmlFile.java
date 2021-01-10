/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.export.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

/**
 * Applies an XSLT to the Document.xml file of MindManager(c) files.
 */
public class ImportXmlFile extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportXmlFile() {
		super("ImportXmlFile");
	}

	public void actionPerformed(final ActionEvent e) {
		final String type = "xml";
		final Component component = Controller.getCurrentController().getViewController().getCurrentRootComponent();
		final JFileChooser chooser = UITools.newFileChooser();
		final CaseSensitiveFileNameExtensionFilter filter = new CaseSensitiveFileNameExtensionFilter(type, null);
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(true);
		final File mmFile = Controller.getCurrentController().getMap().getFile();
		if (mmFile != null && mmFile.getParentFile() != null) {
			chooser.setSelectedFile(mmFile.getParentFile());
		}
		final int returnVal = chooser.showOpenDialog(component);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File chosenFile = chooser.getSelectedFile();
		importXmlFile(chosenFile);
	}

	private void importXmlFile(final File file) {
		final String xsltFileName = "/xslt/xml2mm.xsl";
		try{
			new XmlImporter(xsltFileName).importXml(file);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

}
