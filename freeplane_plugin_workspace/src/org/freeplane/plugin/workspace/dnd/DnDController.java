package org.freeplane.plugin.workspace.dnd;

import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


public class DnDController {

	private static AClipboardController systemCBCtrl = null;
	private static Set<Class<? extends AWorkspaceTreeNode>> dropExcludeNodeTypes = new LinkedHashSet<Class<? extends AWorkspaceTreeNode>>();

	public static AClipboardController getSystemClipboardController() {
		if(systemCBCtrl == null) {
			systemCBCtrl  = new AClipboardController() {
			};
		}
		return systemCBCtrl;
	}
	
	public static boolean isDropAllowed(AWorkspaceTreeNode node) {
		if(node == null) {
			return false;
		}
		synchronized (dropExcludeNodeTypes) {
			if(dropExcludeNodeTypes .contains(node.getClass())) {
				return false;
			}
			return true;
		}
	}
	
	public static void excludeFromDND(Class<? extends AWorkspaceTreeNode> clzz) {
		if(clzz == null) {
			return;
		}
		synchronized (dropExcludeNodeTypes) {
			dropExcludeNodeTypes.add(clzz);
		}
	}
	
	public static void resetExcludeDropSet() {
		synchronized (dropExcludeNodeTypes) {
			dropExcludeNodeTypes.clear();
		}
	}

}
