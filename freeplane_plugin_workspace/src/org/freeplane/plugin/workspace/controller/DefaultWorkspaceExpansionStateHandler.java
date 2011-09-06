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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.WorkspaceController;

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
	public void restoreExpansionState() {
		lock();
		IndexedTree index = WorkspaceController.getController().getIndexTree();
		DefaultTreeModel model = WorkspaceController.getController().getViewModel();
		JTree view = WorkspaceController.getController().getWorkspaceViewTree();
		Iterator<String> iterator = getSet().iterator(); 
		while(iterator.hasNext()) {
			String key = iterator.next();
			if(index.contains(key)) {			
				view.expandPath(new TreePath(model.getPathToRoot(index.get(key))));
			} else {
				iterator.remove();
			}
		}
		while(!collapseStack.isEmpty()) {
			String key = collapseStack.pop();
			if(index.contains(key)) {			
				view.collapsePath(new TreePath(model.getPathToRoot(index.get(key))));
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
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		if(node != null) {
			String key = (String) WorkspaceController.getController().getIndexTree().getKeyByUserObject(node.getUserObject());
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
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		if(node != null) {
			String key = (String) WorkspaceController.getController().getIndexTree().getKeyByUserObject(node.getUserObject());
			if(key != null && getSet().contains(key)) {
				removePathKey(key);
			}
		}
	}
}
