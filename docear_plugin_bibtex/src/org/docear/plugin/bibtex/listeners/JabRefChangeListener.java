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
		memorize = false;
		//WorkspaceController.getController().getWorkspaceViewTree().requestFocus();
		focusTarget.requestFocus();
		
		if (DocearReferenceUpdateController.isLocked()) {
			return;
		}
		
		if (e.getEntry() == null || e.getEntry().getCiteKey() == null) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			

			public void run() {	
				DocearReferenceUpdateController.lock();
				MapModel currentMap = Controller.getCurrentController().getMap();
				if (currentMap == null) {
					return;
				}

				MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
				mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils
						.getText("update_references_open_mindmaps")));
				mindmapUpdateController.updateCurrentMindmap(true);

				DocearReferenceUpdateController.unlock();
				
				if(previousFocus != null) {
						previousFocus.requestFocus();
						memorize=true;
				}
				
			}
		});
			
	}

	
	public void propertyChange(PropertyChangeEvent evt) {
		if("permanentFocusOwner".equals(evt.getPropertyName()) && evt.getNewValue() != null) {
			if(memorize || (!memorize && !focusTarget.equals(evt.getNewValue()) && !focusTarget.equals(evt.getOldValue())) ) {
				previousFocus = (Component) evt.getNewValue();
			}
		}		
	}
	
}
