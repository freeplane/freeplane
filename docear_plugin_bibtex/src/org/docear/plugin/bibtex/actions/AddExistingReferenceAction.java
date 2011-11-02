package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import org.docear.plugin.bibtex.dialogs.ExistingReferencesDialog;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

public class AddExistingReferenceAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddExistingReferenceAction(String key) {
		super(key);
	}

	public void actionPerformed(ActionEvent arg0) {
		ExistingReferencesDialog dialog = new ExistingReferencesDialog(Controller.getCurrentController().getViewController().getFrame());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

	}

}
