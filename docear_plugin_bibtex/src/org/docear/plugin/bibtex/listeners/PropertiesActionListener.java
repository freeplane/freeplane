package org.docear.plugin.bibtex.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.docear.plugin.bibtex.actions.ShowJabrefPreferencesAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class PropertiesActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
	if ("show_jabref_preferences".equals(e.getActionCommand())) {
	    ShowJabrefPreferencesAction action = new ShowJabrefPreferencesAction("show_jabref_preferences");
	    action.actionPerformed(null);
	}
	
    }

}
