package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

@EnabledAction(checkOnPopup = true)
public class NodeCutAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;

	public NodeCutAction() {
		super("workspace.action.node.cut");
	}
		
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node.isSystem()) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
		//FIXME: this function is not available yet
		setEnabled(false);
	}
	
	public void actionPerformed(final ActionEvent e) {
		//TODO: IMPLEMENTATION
    }


}
