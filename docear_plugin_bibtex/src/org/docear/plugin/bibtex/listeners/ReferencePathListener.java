package org.docear.plugin.bibtex.listeners;

import java.io.File;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.CoreConfiguration;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class ReferencePathListener implements ChangeListener {

	public void stateChanged(ChangeEvent e) {
		File file = WorkspaceUtils.resolveURI(CoreConfiguration.referencePathObserver.getUri());
		if (file != null && file.exists()) {
			ReferencesController.getController().getJabrefWrapper().replaceDatabase(file, true);
		}
	}

}
