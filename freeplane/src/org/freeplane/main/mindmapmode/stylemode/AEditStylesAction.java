package org.freeplane.main.mindmapmode.stylemode;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

abstract class AEditStylesAction extends AFreeplaneAction {
	private Controller controller = null;
	private SModeController modeController;
	abstract void commit();
	abstract void rollback();

	public AEditStylesAction(String key) {
		super(key);
	}

	SModeController getModeController() {
	    return modeController;
    }
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	protected JDialog dialog;

	protected void init() {
    	this.controller = Controller.getCurrentController();
    	if (dialog != null) {
    		Controller.setCurrentController ((Controller) dialog.getRootPane().getClientProperty(Controller.class));
    		return;
    	}
    	dialog = new JDialog(Controller.getCurrentController().getViewController().getJFrame());
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
    	Controller controller = SModeControllerFactory.getInstance().createController(dialog);
    	modeController = (SModeController) controller.getModeController();
    	final ViewController viewController = controller.getViewController();
    	viewController.init(controller);
    	dialog.addComponentListener(new ComponentAdapter() {
    		@Override
    		public void componentHidden(final ComponentEvent e) {
    	    	final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(getKey() + ".dialog");
    	    	windowConfigurationStorage.storeDialogPositions(dialog);
    			final IMapViewManager mapViewManager = modeController.getController().getMapViewManager();
    			final MapModel map = mapViewManager.getModel();
    			final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
    			mapViewManager.close(true);
    			Controller.setCurrentController(AEditStylesAction.this.controller);
    			super.componentHidden(e);
    			switch (modeController.getStatus()) {
    				case JOptionPane.OK_OPTION:
    					if (undoHandler.canUndo()) {
    						commit();
    						break;
    					}
    				case JOptionPane.CANCEL_OPTION:
    					rollback();
    			}
    		}

    	});
    }
}
