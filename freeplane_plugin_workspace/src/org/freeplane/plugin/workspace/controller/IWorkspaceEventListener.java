/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.freeplane.plugin.workspace.controller;


/**
 * 
 */
public interface IWorkspaceEventListener {
	/**
	 * @deprecated Please use one of the other methods
	 * @param event
	 */
//	public void processEvent(WorkspaceEvent event);
	
	
	/**
	 * this method is called if a workspace is about to be opened
	 * @param event
	 */
	public void openWorkspace(WorkspaceEvent event);
	
	/**
	 * this method is called if a workspace is about to be closed
	 * @param event
	 */
	public void closeWorkspace(WorkspaceEvent event);
	
	/**
	 * this method is called if a workspace is loaded and ready
	 * @param event
	 */
	public void workspaceReady(WorkspaceEvent event);
	
	/**
	 * this method is called if something in a workspace has changed (e.g. new nodes were added)
	 * @param event
	 */
	public void workspaceChanged(WorkspaceEvent event);
	
	/**
	 * this method is called if the tool bar for the workspace was changed
	 * @param event
	 */
	public void toolBarChanged(WorkspaceEvent event);


	/**
	 * this method is called if a configuration for the workspace was loaded successfully
	 * @param event
	 */
	public void configurationLoaded(WorkspaceEvent event);


	/**
	 * this method is called before a configuration is loaded
	 * @param event
	 */
	public void configurationBeforeLoading(WorkspaceEvent event);
}
