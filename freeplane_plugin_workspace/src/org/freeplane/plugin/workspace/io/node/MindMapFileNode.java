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
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class MindMapFileNode extends DefaultFileNode {
	
	private static final long serialVersionUID = 1L;
	
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

	public AWorkspaceTreeNode clone() {
		MindMapFileNode node = new MindMapFileNode(getName(), getFile());
		return clone(node);
	}
	
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
