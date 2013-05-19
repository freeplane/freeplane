/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


/**
 * 
 */
public abstract class AActionNode extends AWorkspaceTreeNode implements IWorkspaceNodeActionListener, IWorkspaceTransferableCreator {
	
	private static final long serialVersionUID = 1L;
	public static final String NODE_TYPE = "action";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param type
	 */
	public AActionNode(String type) {
		super(type);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public final WorkspaceTransferable getTransferable() {
		return null;
	}
	
	@ExportAsAttribute(name="system")
	public final boolean isSystem() {		
		return true;
	}
	
	public final void setSystem(boolean system) {
		super.setSystem(true);
	}
	
	public final void setTransferable(boolean enabled) {
		super.setTransferable(false);
	}
	
	@ExportAsAttribute(name="transferable",defaultBool=true)
	public final boolean isTransferable() {
		return false;
	}
	
	public void handleAction(WorkspaceActionEvent event) {		
		if(event.getType() == WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT) {
			AFreeplaneAction action = Controller.getCurrentController().getAction(getType());
			if(action == null) {
				LogUtils.severe("No action '"+getType()+"' found");
				return;
			}
			action.actionPerformed(new ActionEvent(this, 0, "execute"));
		}
		else if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}		
	}
	
	public boolean getAllowsChildren() {
		return false;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public String getTagName() {
		return "action";
	}

	@Override
	public void initializePopup() {		
	}
}
