package org.freeplane.plugin.workspace.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;

public class WorkspaceNodeRenderer extends DefaultTreeCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Icon defaultLeafIcon;
	private Icon defaultOpenIcon;
	private Icon defaultClosedIcon;

	public WorkspaceNodeRenderer() {
		defaultLeafIcon = this.getLeafIcon();
		defaultOpenIcon = this.getOpenIcon();
		defaultClosedIcon = this.getClosedIcon();
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	    // Invoke default implementation
	    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	    Object obj = node.getUserObject();
	    setNodeIcon(obj);
	    if(obj != null) {	    	
		    setText(obj.toString());
		    if (obj instanceof Boolean)
		        setText("Retrieving data...");
		    else if (obj instanceof AWorkspaceNode) {
		    	setText(((AWorkspaceNode)obj).getName());
		    }
	    }
	    
	    return this;
	}

	/**
	 * @param value
	 */
	private void setNodeIcon(Object userObject) {
		//System.out.println(userObject);
		if(userObject == null || userObject instanceof WorkspaceRoot) {
			setLeafIcon(defaultLeafIcon);
		    setOpenIcon(defaultOpenIcon);
		    setClosedIcon(defaultClosedIcon);
		} 
		else {
//			setLeafIcon(defaultLeafIcon);
//		    setOpenIcon(defaultOpenIcon);
//		    setClosedIcon(defaultClosedIcon);
		}
	}
}



