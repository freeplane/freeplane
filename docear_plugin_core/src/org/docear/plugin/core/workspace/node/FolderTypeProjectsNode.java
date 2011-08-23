/**
 * author: Marcel Genzmehr
 * 23.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.net.URI;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

/**
 * 
 */
public class FolderTypeProjectsNode extends FolderNode implements IWorkspaceNodeEventListener {
	
	private URI pathURI = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderTypeProjectsNode(String type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setPathURI(URI uri) {
		this.pathURI = uri;
	}
	
	@ExportAsAttribute("path")
	public URI getPathURI() {
		return this.pathURI;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getController().getPopups()
					.showPopup(VirtualFolderNode.POPUP_KEY, component, event.getX(), event.getY());

		}
	}
}
