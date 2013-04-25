package org.freeplane.plugin.workspace.model;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceModelEvent extends TreeModelEvent {

	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject project; 
	private final WorkspaceModelEventType type;
	private final Object oldValue;
	private final Object newValue;
	
	public WorkspaceModelEvent(AWorkspaceProject project, Object source, Object[] path, int[] childIndices, Object[] children) {
		super(source, path, childIndices, children);
		this.project = project;
		this.type = WorkspaceModelEventType.DEFAULT;
		this.oldValue = null;
		this.newValue = null;		
	}

	public WorkspaceModelEvent(AWorkspaceProject project, Object source, TreePath path, int[] childIndices, Object[] children) {
		super(source, path, childIndices, children);
		this.project = project;
		this.type = WorkspaceModelEventType.DEFAULT;
		this.oldValue = null;
		this.newValue = null;
	}

	public WorkspaceModelEvent(AWorkspaceProject project, Object source, Object[] path) {
		this(project, source, new TreePath(path));
	}

	public WorkspaceModelEvent(AWorkspaceProject project, Object source, TreePath path) {
		this(project, source, path, WorkspaceModelEventType.DEFAULT, null, null);
	}

	public WorkspaceModelEvent(AWorkspaceProject project, Object source, TreePath path, WorkspaceModelEventType type, Object oldValue, Object newValue) {
		super(source, path);
		this.project = project;
		this.type = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public AWorkspaceProject getProject() {
		return this.project;
	}
	
	public WorkspaceModelEventType getType() {
		return type;
	}	

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	public enum WorkspaceModelEventType {
		DEFAULT,
		RENAMED,
		MOVED,
		DELETED
	}

}
