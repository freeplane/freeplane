package org.freeplane.plugin.workspace.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class IconCellRenderer extends DefaultTreeCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IconCellRenderer() {
	    setLeafIcon(null);
	    setOpenIcon(null);
	    setClosedIcon(null);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	    // Invoke default implementation
	    Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	    Object obj = node.getUserObject();
	    if(obj != null) {
		    setText(obj.toString());
		    if (obj instanceof Boolean)
		        setText("Retrieving data...");
	    } 
	    return result;
	}
}



