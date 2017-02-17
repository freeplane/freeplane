package org.freeplane.plugin.openmaps.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.openmaps.OpenMapsNodeHook;

/**
 * @author Blair Archibald
 */
public class ViewOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsViewLocation";
	
	final OpenMapsNodeHook nodeHookReference;
	
	public ViewOpenMapsAction(OpenMapsNodeHook nodeHook) {
		super(actionIdentifier);
		nodeHookReference = nodeHook;
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == null || !(event.getSource() instanceof NodeModel))
			nodeHookReference.viewCurrentlySelectedLocation(null); // use currently selected node!
		else
			nodeHookReference.viewCurrentlySelectedLocation((NodeModel)(event.getSource())); // use node that was clicked!
	}
}
