/**
 * author: Marcel Genzmehr
 * 14.12.2011
 */
package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
/**
 * FIX for issue that a new mindmap is always set to <code>saved</code> by default. 
 * This Action is used to set the new mindmap to <code>unsaved</code> right after its creation. 
 */
public class DocearNewMapAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;


	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * 
	 */
	public DocearNewMapAction() {
		super("NewMapAction");
	}
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final MapModel map = Controller.getCurrentController().getMap();
		if(map != null) {
			mapController.setSaved(map, false);
		}
	}
}
