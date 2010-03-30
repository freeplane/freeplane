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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.model.MapChangeEvent;
import org.freeplane.core.model.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.ResUtil;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 * 13.09.2009
 */
public class EditDefaultStylesAction extends AFreeplaneAction {
	public EditDefaultStylesAction(MModeController mainModeController) {
	    super("EditDefaultStylesAction", mainModeController.getController());
    }

	private void init() {
		if(dialog != null){
			return;
		}
	    dialog = new JDialog(getController().getViewController().getJFrame());
		dialog.setSize(800, 300);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		modeController = SModeControllerFactory.getInstance().createModeController(dialog);
		final ViewController viewController = modeController.getController().getViewController();
		viewController.init();
		dialog.addComponentListener(new ComponentAdapter() {

			@Override
            public void componentHidden(ComponentEvent e) {
	            final IMapViewManager mapViewManager = modeController.getController().getMapViewManager();
	            final MapModel map = mapViewManager.getModel();
	            final IUndoHandler undoHandler = (IUndoHandler)map.getExtension(IUndoHandler.class);
	            	switch(modeController.getStatus()){
	            	case JOptionPane.OK_OPTION:
	            		if(undoHandler.canUndo()){
	            			((MFileManager)MFileManager.getController(modeController)).save(map);
	            		}
	            	}
				mapViewManager.close(true);
	            super.componentHidden(e);
            }
			
		});
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private JDialog dialog;
	private SModeController modeController;

	public void actionPerformed(ActionEvent e) {
		init();
		try {
			
			ResourceController resourceController = ResourceController.getResourceController();
			File freeplaneUserDirectory = new File(resourceController.getFreeplaneUserDirectory());
			File styles = new File(freeplaneUserDirectory, "default.stylemm");
			if (! styles.exists()){
				ResUtil.copyFromURL(resourceController.getResource("/styles/default.stylemm"), freeplaneUserDirectory);
			}
			modeController.getMapController().newMap(styles.toURL());
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		dialog.setLocationRelativeTo(getController().getViewController().getJFrame());
		dialog.setVisible(true);
	}
}
