package org.freeplane.plugin.workspace.controller;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;


//Make sure expansion is threaded and updating the tree model
//only occurs within the event dispatching thread.
public class NodeExpansionListener implements TreeExpansionListener {
		
    public void treeExpanded(TreeExpansionEvent event) {    	
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
        if(hasInterface(node.getUserObject())) {
        	((TreeExpansionListener)node.getUserObject()).treeExpanded(event);
        }
    }
    
    public void treeCollapsed(TreeExpansionEvent event) {
    	final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
        if(TreeExpansionListener.class.isInstance(node.getUserObject())) {
        	((TreeExpansionListener)node.getUserObject()).treeCollapsed(event);
        }
    }
    
    private boolean hasInterface(Object obj) {
    	Class<?>[] interfaces = obj.getClass().getInterfaces();
    	for(Class<?> interf : interfaces) {
    		if(TreeExpansionListener.class.getName().equals(interf.getName())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    
}