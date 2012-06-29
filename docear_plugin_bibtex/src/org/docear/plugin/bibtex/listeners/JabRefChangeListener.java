package org.docear.plugin.bibtex.listeners;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import net.sf.jabref.DatabaseChangeEvent;
import net.sf.jabref.DatabaseChangeListener;
import net.sf.jabref.export.DocearReferenceUpdateController;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public class JabRefChangeListener implements DatabaseChangeListener, PropertyChangeListener {

	private final Component focusTarget;
	private Component previousFocus = null;
	private boolean memorize = true;

	public JabRefChangeListener() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
		focusTarget = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBar();
	}

	public synchronized void databaseChanged(DatabaseChangeEvent e) {
		if (DocearReferenceUpdateController.isLocked() || e.getType() == DatabaseChangeEvent.REMOVED_ENTRY) {
			return;
		}
		
		if (e.getType() == DatabaseChangeEvent.ADDED_ENTRY) {
			ReferencesController.getController().setInAdd(Controller.getCurrentController().getMap());
			ReferencesController.getController().setAddedEntry(e.getEntry());
			return;
		}
		
		if (e.getEntry() == null || e.getEntry().getCiteKey() == null) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if (DocearReferenceUpdateController.isLocked()) {
						return;
					}
					DocearReferenceUpdateController.lock();

					memorize = false;
					focusTarget.requestFocus();
					
					MapModel currentMap = Controller.getCurrentController().getMap();
					if (currentMap == null) {
						return;
					}

					MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
					mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
					mindmapUpdateController.updateCurrentMindmap(true);
				}
				finally {
					DocearReferenceUpdateController.unlock();
					if (previousFocus != null) {
						previousFocus.requestFocus();
						memorize = true;
					}
				}
			}

		});

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("permanentFocusOwner".equals(evt.getPropertyName()) && evt.getNewValue() != null) {
			if (memorize || (!memorize && !focusTarget.equals(evt.getNewValue()) && !focusTarget.equals(evt.getOldValue()))) {
				previousFocus = (Component) evt.getNewValue();
			}
		}
	}

}
