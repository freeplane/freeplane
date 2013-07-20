package org.freeplane.plugin.workspace.features;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.event.AWorkspaceEvent;
import org.freeplane.plugin.workspace.event.IWorkspaceListener;
import org.freeplane.plugin.workspace.handler.IOController;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;

public abstract class AWorkspaceModeExtension implements IExtension {
	private final IOController workspaceIOController = new IOController();
	private final Set<IWorkspaceListener> listeners = new LinkedHashSet<IWorkspaceListener>();
	private ProjectLoader projectLoader; 
	
	public AWorkspaceModeExtension(ModeController modeController) {
	}
	public abstract void start(ModeController modeController);
	
	public abstract WorkspaceModel getModel();
	public abstract void setModel(WorkspaceModel model);
	public abstract IWorkspaceView getView();
	public abstract FileReadManager getFileTypeManager();
	public abstract URI getDefaultProjectHome();
	public abstract AWorkspaceProject getCurrentProject();
	public abstract void save();
	
	public abstract void shutdown();
	
	public IOController getIOController() {
		return workspaceIOController;
	}

	public final void addWorkspaceListener(IWorkspaceListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public final void removeWorkspaceListener(IWorkspaceListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public final void dispatchWorkspaceEvent(AWorkspaceEvent event) {
		synchronized (listeners) {
			for (IWorkspaceListener listener : listeners) {
				listener.handleWorkspaceEvent(event);
			}
		}
	}

	public ProjectLoader getProjectLoader() {
		if(this.projectLoader == null) {
			this.projectLoader = new ProjectLoader(); 
		}
		return this.projectLoader;
	}
	
	public void setProjectLoader(ProjectLoader loader) {
		this.projectLoader = loader;
	}
}
