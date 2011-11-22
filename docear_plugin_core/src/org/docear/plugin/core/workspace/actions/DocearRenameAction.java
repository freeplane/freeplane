/**
 * author: Marcel Genzmehr
 * 22.11.2011
 */
package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.core.workspace.IDocearMindmap;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

@EnabledAction(checkOnPopup=true)
public class DocearRenameAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearRenameAction() {
		super("workspace.action.docear.node.rename");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node instanceof IDocearMindmap) {
			setEnabled();	
		} 
		else if(node.isSystem()) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
	
		if(targetNode instanceof IDocearMindmap) {
			String oldName = targetNode.getName();		
			String newName = JOptionPane.showInputDialog(UITools.getFrame(),
					TextUtils.getText("docear.confirm.action.rename.mindmap.text"), oldName);

			if (newName != null) {
				if(((IDocearMindmap) targetNode).changeNameTo(newName)) {
					WorkspaceUtils.saveCurrentConfiguration();
				}
				else {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("docear.error.rename.mindmap.text"), TextUtils.getText("docear.error.rename.mindmap.title"), 
							JOptionPane.ERROR_MESSAGE);
				}
				
			}
		}
		
	}
}
