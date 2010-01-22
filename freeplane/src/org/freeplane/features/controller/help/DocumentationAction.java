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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.browsemode.BModeController;

class DocumentationAction extends AFreeplaneAction {
	private static final String NAME = "documentation";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DocumentationAction(final Controller controller) {
		super("DocumentationAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final File baseDir = new File(resourceController.getResourceBaseDir()).getAbsoluteFile().getParentFile();
		final String defaultMap = resourceController.getProperty("browsemode_initial_map");
		final File file;
		if(defaultMap.endsWith(".mm")){
			final String languageCode = ((ResourceBundles) resourceController.getResources()).getLanguageCode();
			String map = defaultMap.substring(0, defaultMap.length() -3) + "_" + languageCode + ".mm";
			 File localFile = new File(baseDir, map);
			 if(localFile.canRead()){
				 file = localFile;
			 }
			 else{
				 file = new File(baseDir, defaultMap);
			 }
		}
		else{
			 file = new File(baseDir, defaultMap);
		}
		try {
	        final URL endUrl = file.toURL();
	        SwingUtilities.invokeLater(new Runnable() {
	        	public void run() {
	        		try {
	        			if (getController().selectMode(BModeController.MODENAME)) {
	        				getModeController().getMapController().newMap(endUrl);
	        			}
	        		}
	        		catch (final Exception e1) {
	        			LogTool.severe(e1);
	        		}
	        	}
	        });
        }
        catch (MalformedURLException e1) {
        	LogTool.warn(e1);
        }
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}
