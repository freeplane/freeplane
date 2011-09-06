/**
 * author: Marcel Genzmehr
 * 30.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public abstract class AWorkspaceExpansionStateHandler {
	
	private Set<String> expandedSet;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public AWorkspaceExpansionStateHandler() {
		getSet();
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	protected Set<String> getSet() {
		if(expandedSet == null) {
			expandedSet = Collections.synchronizedSet(new HashSet<String>());
		}
		return expandedSet;
	}
	
	protected void addPathKey(String pathKey) {
		getSet().add(pathKey);
	}
	
	protected void removePathKey(String pathKey) {
		getSet().remove(pathKey);
	}
	
	protected void removeAll() {
		this.expandedSet.removeAll(this.expandedSet);
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/**
	 * try to expand all tree nodes that were previously expanded
	 */
	public abstract void restoreExpansionState();
	public abstract void reset();

}
