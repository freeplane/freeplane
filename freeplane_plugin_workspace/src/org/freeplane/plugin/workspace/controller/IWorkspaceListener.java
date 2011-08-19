/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.freeplane.plugin.workspace.controller;


/**
 * 
 */
public interface IWorkspaceListener {
	public void workspaceChanged(WorkspaceEvent event);
	public void workspaceInitialize(WorkspaceEvent event);
	public void workspaceFinalize(WorkspaceEvent event);

}
