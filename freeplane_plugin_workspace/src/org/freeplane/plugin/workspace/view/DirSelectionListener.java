package org.freeplane.plugin.workspace.view;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class DirSelectionListener implements TreeSelectionListener {
	
	public void valueChanged(TreeSelectionEvent event) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)(event.getPath().getLastPathComponent());
	    FileNode fnode = extractFileNode(node);
	}
	
	private FileNode extractFileNode(DefaultMutableTreeNode node) {
        if (node == null)
            return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData)
            obj = ((IconData)obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode)obj;
        else
            return null;
    }
}
