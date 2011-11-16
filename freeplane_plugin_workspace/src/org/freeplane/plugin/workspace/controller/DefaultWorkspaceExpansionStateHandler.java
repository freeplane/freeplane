/**
 * author: Marcel Genzmehr
 * 30.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.util.Iterator;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceIndexedTreeModel;

/**
 * 
 */
public class DefaultWorkspaceExpansionStateHandler extends AWorkspaceExpansionStateHandler implements TreeExpansionListener {

	private static boolean locked = false;
	private Stack<String> collapseStack;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DefaultWorkspaceExpansionStateHandler() {
		super();
		collapseStack = new Stack<String>();
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	/** 
	 * {@inheritDoc}
	 */
	public void restoreExpansionStates() {
		lock();		
		WorkspaceIndexedTreeModel model = WorkspaceController.getController().getWorkspaceModel();
		JTree view = WorkspaceController.getController().getWorkspaceViewTree();
		Iterator<String> iterator = getSet().iterator(); 
		while(iterator.hasNext()) {
			String key = iterator.next();
			if(model.containsNode(key)) {			
				view.expandPath(model.getNode(key).getTreePath());
			} else {
				iterator.remove();
			}
		}
		while(!collapseStack.isEmpty()) {
			String key = collapseStack.pop();
			if(model.containsNode(key)) {			
				view.collapsePath(model.getNode(key).getTreePath());
			}
		}
		unlock();
	}
	
	public void reset() {
		removeAll();
	}
	
	private boolean lock() {
		if(locked()) return false;
		locked = true;
		return true;
	}
	
	private void unlock() {
		locked = false;
	}
	
	private boolean locked() {
		return locked;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/** 
	 * {@inheritDoc}
	 */
	public void treeExpanded(TreeExpansionEvent event) {		
		AWorkspaceTreeNode node = (AWorkspaceTreeNode) event.getPath().getLastPathComponent();
		if(node != null) {
			String key = node.getKey();
			if(key != null) {
				if(locked() && !getSet().contains(key)){
					collapseStack.push(key);
				} else {
					addPathKey(key);
				}
			}
		}		
	}

	/** 
	 * {@inheritDoc}
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
		if(locked()) return;
		AWorkspaceTreeNode node = (AWorkspaceTreeNode) event.getPath().getLastPathComponent();
		if(node != null) {
			String key = node.getKey();
			if(key != null && getSet().contains(key)) {
				removePathKey(key);
			}
		}
	}
}
