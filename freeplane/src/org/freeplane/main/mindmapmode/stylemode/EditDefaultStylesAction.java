/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.FileUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.mindmapmode.file.MFileManager;

/**
 * @author Dimitry Polivaev
 * 13.09.2009
 */
public class EditDefaultStylesAction extends  AEditStylesAction  {
	public EditDefaultStylesAction() {
		super("EditDefaultStylesAction");
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		init();
		try {
			final ResourceController resourceController = ResourceController.getResourceController();
			final File freeplaneUserDirectory = new File(resourceController.getFreeplaneUserDirectory());
			final File styles = new File(freeplaneUserDirectory, "default.stylemm");
			if (!styles.exists()) {
				FileUtils
				    .copyFromURL(resourceController.getResource("/styles/default.stylemm"), freeplaneUserDirectory);
			}
			getModeController().getMapController().newMap(styles.toURL(), false);
		}
		catch (final Exception e1) {
			e1.printStackTrace();
			return;
		}
		dialog.setLocationRelativeTo(Controller.getCurrentController().getViewController().getJFrame());
		dialog.setVisible(true);
	}

	@Override
    void commit(MapModel map) {
		((MFileManager) MFileManager.getController()).save(map);
    }

	@Override
    void rollback() {
    }
}
