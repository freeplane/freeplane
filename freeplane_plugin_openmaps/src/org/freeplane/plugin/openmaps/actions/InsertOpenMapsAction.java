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
	
	final OpenMapsNodeHook nodeHookReference;
	
	public InsertOpenMapsAction(OpenMapsNodeHook nodeHook) {
		super(actionIdentifier);
		nodeHookReference = nodeHook;
	}

	public void actionPerformed(ActionEvent event) {
		nodeHookReference.chooseLocation();
	}
}
