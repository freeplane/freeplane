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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

/**
 * @author Dimitry Polivaev
 * 13.09.2009
 */
@SuppressWarnings("serial")
public class EditStylesAction extends AFreeplaneAction {
	private Controller mainController = null;
	private SModeController modeController;
	protected JDialog dialog;
	private Component currentMapView;

	public EditStylesAction() {
		super("EditStylesAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
		final Controller currentController = Controller.getCurrentController();
		final MapModel map = currentController.getMap();
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		final MapModel styleMap = mapStyleModel.getStyleMap();
		if(styleMap == null){
			UITools.errorMessage(TextUtils.getText("no_styles_found_in_map"));
			return;
		}
		
		final IMapViewManager mapViewManager = currentController.getMapViewManager();
		currentMapView = mapViewManager.getMapViewComponent();
		mapViewManager.changeToMapView((Component)null);
		
		final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
		undoHandler.startTransaction();
		init();
		SModeController modeController = getModeController();
		modeController.getMapController().newMapView(styleMap);
		Controller controller = modeController.getController();
		Component mapViewComponent = controller.getMapViewManager().getMapViewComponent();
		((DialogController) controller.getViewController()).setMapView(mapViewComponent);
		dialog.setLocationRelativeTo(currentController.getViewController().getCurrentRootComponent());
		dialog.setVisible(true);
	}

	void commit() {
	    final Controller currentController = Controller.getCurrentController();
		final MapModel currentMap = currentController.getMap();
	    LogicalStyleController.getController().refreshMap(currentMap);
	    final ModeController currentModeController = Controller.getCurrentModeController();
		currentModeController.commit();
    }

	void rollback() {
	    Controller.getCurrentModeController().rollback();
    }

	SModeController getModeController() {
	    return modeController;
	}

	private void init() {
		this.mainController = Controller.getCurrentController();
		if (dialog != null) {
			Controller.setCurrentController ((Controller) dialog.getRootPane().getClientProperty(Controller.class));
			return;
		}
		dialog = new JDialog(UITools.getCurrentFrame());
		final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(getKey() + ".dialog");
		windowConfigurationStorage.restoreDialogPositions(dialog);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				getModeController().tryToCloseDialog();
			}
		});
		Controller styleDialogController = SModeControllerFactory.getInstance().createController(dialog);
		modeController = (SModeController) styleDialogController.getModeController();
		final ViewController viewController = styleDialogController.getViewController();
		viewController.init(styleDialogController);
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(final ComponentEvent e) {
		    	final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(getKey() + ".dialog");
		    	windowConfigurationStorage.storeDialogPositions(dialog);
				final IMapViewManager mapViewManager = modeController.getController().getMapViewManager();
				final MapModel map = mapViewManager.getModel();
				final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
				modeController.getMapController().closeWithoutSaving(map);
				Controller.setCurrentController(mainController);
				super.componentHidden(e);
				mainController.getMapViewManager().changeToMapView(currentMapView);
				currentMapView = null;
				switch (modeController.getStatus()) {
					case JOptionPane.OK_OPTION:
						if (undoHandler.canUndo()) {
							commit();
							break;
						}
						//$FALL-THROUGH$
					case JOptionPane.CANCEL_OPTION:
						rollback();
				}
			}
	
		});
	}
}
