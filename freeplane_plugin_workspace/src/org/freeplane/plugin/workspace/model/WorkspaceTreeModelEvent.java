package org.freeplane.plugin.workspace.model;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

public class WorkspaceTreeModelEvent extends TreeModelEvent {
	
	private static final long serialVersionUID = 1L;
	
	private WorkspaceTreeModelEventType type;
	private Object from;
	private Object to;

	public WorkspaceTreeModelEvent(Object source, TreePath path, WorkspaceTreeModelEventType type, Object from, Object to) {
		super(source, path);
		this.type = type;
		this.from = from;
		this.to = to;
	}

	public Object getFrom() {
		return from;
	}

	public Object getTo() {
		return to;
	}
	
	public WorkspaceTreeModelEventType getType() {
		return type;
	}	

	public enum WorkspaceTreeModelEventType{
		rename,
		move,
		delete
	}
	
}


