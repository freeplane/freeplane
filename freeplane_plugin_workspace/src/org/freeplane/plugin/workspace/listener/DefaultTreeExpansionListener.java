package org.freeplane.plugin.workspace.listener;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


//Make sure expansion is threaded and updating the tree model
//only occurs within the event dispatching thread.
public class DefaultTreeExpansionListener implements TreeExpansionListener {
		
    public void treeExpanded(TreeExpansionEvent event) {    	
        final AWorkspaceTreeNode node = (AWorkspaceTreeNode)event.getPath().getLastPathComponent();
        if(node instanceof TreeExpansionListener) {
        	((TreeExpansionListener)node).treeExpanded(event);
        }
    }
    
    public void treeCollapsed(TreeExpansionEvent event) {
    	final AWorkspaceTreeNode node = (AWorkspaceTreeNode)event.getPath().getLastPathComponent();
        if(node instanceof TreeExpansionListener) {
        	((TreeExpansionListener)node).treeCollapsed(event);
        }
    }   
}