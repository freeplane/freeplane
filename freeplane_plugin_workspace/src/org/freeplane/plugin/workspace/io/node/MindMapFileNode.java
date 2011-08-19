/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;
import java.net.URL;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

/**
 * 
 */
public class MindMapFileNode extends DefaultFileNode {
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param name
	 * @param file
	 */
	public MindMapFileNode(String name, File file) {
		super(name, file);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void handleEvent(WorkspaceNodeEvent event) {
		if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			try {
				final URL mapUrl = Compat.fileToUrl(getFile());
				Controller.getCurrentModeController().getMapController().newMap(mapUrl, false);
			}
			catch (final Exception e) {
				LogUtils.severe(e);
			}
		} 
		else 
			super.handleEvent(event);
	}
}
