package org.freeplane.plugin.workspace.model.project;

import java.util.EventObject;

public class ProjectSelectionEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject selectProject;
	private final AWorkspaceProject oldProject;

	public ProjectSelectionEvent(Object source, AWorkspaceProject selected, AWorkspaceProject old) {
		super(source);
		this.selectProject = selected;
		this.oldProject = old;
	}

	public AWorkspaceProject getSelectedProject() {
		return selectProject;
	}

	public AWorkspaceProject getPreviousProject() {
		return oldProject;
	}
	
	public String toString() {
        return getClass().getName() + "[source=" + source + ";selected="+ getSelectedProject()  +";old="+getPreviousProject()+"]";
    }

}
