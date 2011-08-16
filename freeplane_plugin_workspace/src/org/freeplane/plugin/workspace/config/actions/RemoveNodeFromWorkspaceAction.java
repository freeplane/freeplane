package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.view.WorkspacePopupMenu;

public class RemoveNodeFromWorkspaceAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveNodeFromWorkspaceAction() {
		super("RemoveNodeFromWorkspaceAction");
	}

	public void actionPerformed(final ActionEvent e) {
		String currentLocation = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation();
		String temp = currentLocation + File.separator + "workspace_temp.xml";
		String config = currentLocation + File.separator + "workspace.xml";
		
		DefaultMutableTreeNode node = this.getNodeFromActionEvent(e);
		
		WorkspacePopupMenu menu = (WorkspacePopupMenu) ((JFreeplaneMenuItem) e.getSource()).getParent();
		JTree tree = (JTree) menu.getInvoker();
		DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
		treeModel.removeNodeFromParent(node);
		
		try {
			WorkspaceController.getCurrentWorkspaceController().saveConfigurationAsXML(new FileWriter(temp));
			
			FileChannel from = new FileInputStream(temp).getChannel();
			FileChannel to = new FileOutputStream(config).getChannel();

			to.transferFrom(from, 0, from.size());
			to.close();
			from.close();
			
			File tempFile = new File(temp);
			tempFile.delete();
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
