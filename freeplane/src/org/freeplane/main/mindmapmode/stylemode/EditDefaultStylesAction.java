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
public class EditDefaultStylesAction extends AFreeplaneAction {
	public EditDefaultStylesAction() {
		super("EditDefaultStylesAction");
	}
	
	private Controller controller = null;

	private void init() {
		this.controller = Controller.getCurrentController();
		if (dialog != null) {
			Controller.setCurrentController ((Controller) dialog.getRootPane().getClientProperty(Controller.class));
			return;
		}
		dialog = new JDialog(Controller.getCurrentController().getViewController().getJFrame());
		dialog.setSize(800, 300);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Controller controller = SModeControllerFactory.getInstance().createController(dialog);
		modeController = (SModeController) controller.getModeController();
		final ViewController viewController = controller.getViewController();
		viewController.init(controller);
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(final ComponentEvent e) {
				final IMapViewManager mapViewManager = modeController.getController().getMapViewManager();
				final MapModel map = mapViewManager.getModel();
				final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
				mapViewManager.close(true);
				Controller.setCurrentController(EditDefaultStylesAction.this.controller);
				super.componentHidden(e);
				switch (modeController.getStatus()) {
					case JOptionPane.OK_OPTION:
						if (undoHandler.canUndo()) {
							((MFileManager) MFileManager.getController()).save(map);
						}
				}
			}
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JDialog dialog;
	private SModeController modeController;

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
			modeController.getMapController().newMap(styles.toURL());
		}
		catch (final Exception e1) {
			e1.printStackTrace();
			return;
		}
		dialog.setLocationRelativeTo(Controller.getCurrentController().getViewController().getJFrame());
		dialog.setVisible(true);
	}
}
