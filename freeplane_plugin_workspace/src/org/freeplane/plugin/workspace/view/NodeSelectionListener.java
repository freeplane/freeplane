package org.freeplane.plugin.workspace.view;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class NodeSelectionListener implements TreeSelectionListener {
	
	public void valueChanged(TreeSelectionEvent event) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)(event.getPath().getLastPathComponent());
	    System.out.println("NodeSelectionChange: "+event);
	}
}
