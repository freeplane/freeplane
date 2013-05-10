package org.freeplane.plugin.workspace.model.project;

import java.io.File;
import java.net.URI;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.UniqueIDCreator;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class DefaultWorkspaceProjectCreator implements IWorkspaceProjectCreater {

	public AWorkspaceProject newProject(final String projectID, final URI projectHome) {
		
		return new AWorkspaceProject() {	
			private String id = projectID;
			private URI home = projectHome;
			
			@Override
			public String getProjectID() {
				if(this.id == null) {
					this.id = UniqueIDCreator.getCreator().uniqueID();
				}
				return this.id;
			}
			
			@Override
			public URI getProjectHome() {
				return this.home;
			}
			
			@Override
			public String getProjectName() {
				if(getModel().getRoot() == null) {
					return this.getProjectID();
				}
				return getModel().getRoot().getName().trim();
			}

			@Override
			public URI getProjectDataPath() {
				return URIUtils.createURI(getProjectHome().toString()+"/_data/"+getProjectID());
			}

			public URI getRelativeURI(URI uri) {
				//WORKSPACE - todo: check new implementation 
				/* windows paths on different drives are getting a relative version as well 
				 * -> modify method that builds the relative paths  
				 *  
				 */
				try {
					URI relativeUri = LinkController.getController().createRelativeURI(new File(getProjectHome()), new File(uri), LinkController.LINK_RELATIVE_TO_MINDMAP);
					if(Compat.isWindowsOS() && relativeUri.getRawPath().contains(":")) {
						return uri;
					}
					else {
						return new URI(WorkspaceController.PROJECT_RESOURCE_URL_PROTOCOL + "://"+ getProjectID() +"/"+relativeUri.getRawPath());
					}
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
				return null;
			}

			@Override
			public ProjectVersion getVersion() {
				String version = null;
				if(getModel().getRoot() != null) {
					version = ((ProjectRootNode)getModel().getRoot()).getVersion();
				}
				
				if(version == null) {
					version = "freeplane 1.0";
				}
				
				return new ProjectVersion(version);
			}
		};
	}

}