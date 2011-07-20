package org.freeplane.plugin.workspace.view;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class NodeSelectionListener implements TreeSelectionListener {
	
	public void valueChanged(TreeSelectionEvent event) {
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)(event.getPath().getLastPathComponent());
	    if(hasInterface(node.getUserObject())) {
        	((TreeSelectionListener)node.getUserObject()).valueChanged(event);
        }
	}
	
	private boolean hasInterface(Object obj) {
    	Class<?>[] interfaces = obj.getClass().getInterfaces();
    	for(Class<?> interf : interfaces) {
    		if(TreeSelectionListener.class.getName().equals(interf.getName())) {
    			return true;
    		}
    	}
    	return false;
    }
}
