package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AddNewGroupAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddNewGroupAction() {
		super("AddNewGroupAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("AddNewGroupAction: " + e.getActionCommand() + " : " + e.getID());
		
		String groupName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
				TextUtils.getText("enter_group_name"));
		
		WorkspaceUtils.createGroupNode(groupName, this.getNodeFromActionEvent(e));
		
		WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
	}

}
