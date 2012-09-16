package org.freeplane.plugin.openmaps.actions;

import java.awt.event.ActionEvent;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.openmaps.OpenMapsNodeHook;

/**
 * @author Blair Archibald
 */
public class InsertOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsAddLocation";
	
	public InsertOpenMapsAction() {
		super(actionIdentifier);
	}

	public void actionPerformed(ActionEvent event) {
		final OpenMapsNodeHook nodeHook = new OpenMapsNodeHook();
		nodeHook.chooseLocation();
	}
}
