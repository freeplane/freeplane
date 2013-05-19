package org.freeplane.plugin.workspace.components;

import java.awt.Component;

import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.handler.INodeTypeIconManager;
import org.freeplane.plugin.workspace.mindmapmode.InputController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;


public interface IWorkspaceView {

	public void expandPath(TreePath treePath);

	public void collapsePath(TreePath treePath);
	
	public void refreshView();
		
	public boolean containsComponent(Component comp);
		
	public WorkspaceTransferHandler getTransferHandler();	

	public TreePath getSelectionPath();

	public TreePath getPathForLocation(int x, int y);

	public INodeTypeIconManager getNodeTypeIconManager();

	public AWorkspaceTreeNode getNodeForLocation(int x, int y);
	
	public void addProjectSelectionListener(IProjectSelectionListener projectSelectionListener);
	
	public InputController getInputController();
}
