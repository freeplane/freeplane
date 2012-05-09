package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;

public class ShowJabrefPreferencesAction extends AFreeplaneAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShowJabrefPreferencesAction(String key) {
		super(key);	
	}
	
	

	public void actionPerformed(ActionEvent e) {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame().preferences();
	}
	
	public void afterMapChange(final Object newMap) {
	}
	
}
