package org.freeplane.plugin.workspace.io;

import java.io.IOException;

import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public interface IProjectSettingsIOHandler {
	public enum LOAD_RETURN_TYPE {
		/**
		 * a new project was created and loaded
		 */
		NEW_PROJECT, 
		/**
		 * an existing project was loaded
		 */
		EXISTING_PROJECT, 
		/**
		 * compatible mode return_type: if an old project version was converted into a newer version 
		 */
		CONVERTED_PROJECT
	}
	
	/**
	 * This method will load an existing project or try to create a new project with the given settings.
	 * 
	 * @param project container with settings necessary to load or create a project
	 * @return {@link LOAD_RETURN_TYPE} if the an existing project was loaded, or if a new project was created
	 * @throws IOException
	 */
	public LOAD_RETURN_TYPE loadProject(AWorkspaceProject project) throws IOException;
	
	/**
	 * This method will try to save the given project settings.
	 * 
	 * @param project
	 * @throws IOException
	 */
	public void storeProject(AWorkspaceProject project) throws IOException;
}
