package org.freeplane.plugin.workspace.model.project;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.WorkspaceModelEventType;

public abstract class AWorkspaceProject {	
	
	private Map<Class<? extends IWorkspaceProjectExtension>, IWorkspaceProjectExtension> extensions = new LinkedHashMap<Class<? extends IWorkspaceProjectExtension>, IWorkspaceProjectExtension>();
	
	private static IWorkspaceProjectCreater creator = null;

	private ProjectModel model;
	
	public abstract ProjectVersion getVersion();
	
	public abstract URI getProjectHome();
	
	public abstract String getProjectID();
	
	public abstract URI getProjectDataPath();
	
	public abstract URI getRelativeURI(URI uri);
	
	public abstract String getProjectName();
	
	
	public ProjectModel getModel() {
		if(this.model == null) {
			this.model = new ProjectModel(this);
			this.model.addProjectModelListener(new DefaultModelChangeListener());
		}
		return this.model;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IWorkspaceProjectExtension> T getExtensions(Class<T> key) {
		synchronized (extensions) {
			return (T) extensions.get(key);
		}
	}

	public IWorkspaceProjectExtension addExtension(IWorkspaceProjectExtension extension) {
		synchronized (extensions) {
			return addExtension(extension.getClass(), extension);
		}
	}
	
	public IWorkspaceProjectExtension addExtension(Class<? extends IWorkspaceProjectExtension> key, IWorkspaceProjectExtension extension) {
		if(extension == null) {
			return null;
		}
		if(key == null) {
			key = extension.getClass();
		}
		synchronized (extensions) {
			return this.extensions.put(key, extension);
		}
	}

	public IWorkspaceProjectExtension removeExtension(Class<? extends IWorkspaceProjectExtension> key) {
		if(key == null) {
			return null;
		}
		synchronized (extensions) {
			return this.extensions.remove(key);
		}
	}	
		
	public static void setCurrentProjectCreator(IWorkspaceProjectCreater pCreator) {
		creator = pCreator;
	}
	
	public static AWorkspaceProject create(String projectID, URI projectHome) {
		if(projectHome == null) {
			throw new IllegalArgumentException("projectHome(URI)");
		}
		
		if(creator == null) {
			creator =  new DefaultWorkspaceProjectCreator();
		}
		
		return creator.newProject(projectID, projectHome);
	}
	
	public String toString() {
		return getModel().getRoot().getName() +"[id="+getProjectID()+";home="+getProjectHome()+"]";
	}

	private final class DefaultModelChangeListener implements IProjectModelListener {
		
		public void treeStructureChanged(WorkspaceModelEvent event) {
		}

		public void treeNodesRemoved(WorkspaceModelEvent event) {
		}

		public void treeNodesInserted(WorkspaceModelEvent event) {
		}

		public void treeNodesChanged(WorkspaceModelEvent event) {
			if(event.getType() == WorkspaceModelEventType.RENAMED && getModel().getRoot().equals(event.getTreePath().getLastPathComponent())) {
				//WORKSPACE - info: if the project folder should have the same name as the project
//				File file = URIUtils.getAbsoluteFile(getProjectHome());
//				File targetFile = new File(file.getParentFile(), getModel().getRoot().getName());
//				if(file.exists()) {
//					file.renameTo(targetFile);
//				}
				//setProjectHome(targetFile.toURI());
				
			}
		}
	}
}
