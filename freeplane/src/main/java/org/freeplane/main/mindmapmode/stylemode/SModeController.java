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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 18.09.2009
 */
public class SModeController extends MModeController {

	@SuppressWarnings("serial")
    public SModeController(final Controller controller) {
		super(controller);
		final Window dialog = ((DialogController) controller.getViewController()).getDialog();
		dialog.addComponentListener(new ComponentAdapter() {
			public void componentShown(final ComponentEvent e) {
				status = JOptionPane.DEFAULT_OPTION;
			}
		});
		final String key = "styledialog";
		AFreeplaneAction okAction = new AFreeplaneAction(key + ".ok") {
			public void actionPerformed(final ActionEvent e) {
				status = JOptionPane.OK_OPTION;
				closeDialog();
			}
		};
		controller.addAction(okAction);
		AFreeplaneAction cancelAction = new AFreeplaneAction(key + ".cancel") {
			public void actionPerformed(final ActionEvent e) {
				status = JOptionPane.CANCEL_OPTION;
				closeDialog();
			}
		};
		controller.addAction(cancelAction);
		AFreeplaneAction tryToCloseAction = new AFreeplaneAction("QuitAction") {
			public void actionPerformed(final ActionEvent e) {
				tryToCloseDialog();
			}
		};
		controller.addAction(tryToCloseAction);
	}

	private int status = JOptionPane.DEFAULT_OPTION;

	public int getStatus() {
		return status;
	}
	
	static public final String MODENAME = "StyleMap";
	
	@Override
	public String getModeName() {
		return SModeController.MODENAME;
	}


	protected void closeDialog() {
		final Window dialog = ((DialogController) getController().getViewController()).getDialog();
		dialog.setVisible(false);
	}

	public void setStatus(int status) {
	   this.status = status;
    }
	
	void tryToCloseDialog() {
	    final IMapViewManager mapViewManager = getController().getMapViewManager();
	    final MapModel map = mapViewManager.getModel();
	    final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
	    final Window dialog = ((DialogController) getController().getViewController()).getDialog();
	    if (! undoHandler.canUndo()){
	    	dialog.setVisible(false);
	    	return;
	    }
	    final String text = TextUtils.getText("save_unsaved_styles");
	    final String title = TextUtils.getText("SaveAction.text");
	    final int returnVal = JOptionPane.showOptionDialog(
	    	dialog, text, title,
	    	JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
	    if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
	    	return;
	    }
	    setStatus(returnVal == JOptionPane.YES_OPTION ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION);
	    dialog.setVisible(false);
    }

	@Override
    public void startup() {
    }

	public boolean canEdit(NodeModel model) {
		return model.getNodeLevel() >= 2;
	}

	public boolean shouldCenterCompactMaps() {
		return true;
	}
}
