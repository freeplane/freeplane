package org.freeplane.plugin.workspace.event;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;


public class WorkspaceActionEvent {
	public static final int MOUSE_CLICK = 1;
	public static final int MOUSE_DBLCLICK = 256;
	public static final int MOUSE_LEFT = 2;
	public static final int MOUSE_RIGHT = 4;
	
	public static final int MOUSE_LEFT_CLICK = MOUSE_LEFT+MOUSE_CLICK;
	public static final int MOUSE_LEFT_DBLCLICK = MOUSE_LEFT+MOUSE_DBLCLICK;
	public static final int MOUSE_RIGHT_CLICK = MOUSE_RIGHT+MOUSE_CLICK;
	public static final int MOUSE_RIGHT_DBLCLICK = MOUSE_RIGHT+MOUSE_DBLCLICK;
	
	public static final int POPUP_TRIGGER = 64;
	public static final int WSNODE_CHANGED = 128;
	
	public static final int WSNODE_OPEN_DOCUMENT = MOUSE_LEFT_DBLCLICK;
		
	final private AWorkspaceTreeNode source;
	final private int eventType;
	final private int x;
	final private int y;
	final private Object baggage;
	
	private boolean consumed = false;
	
	public WorkspaceActionEvent(AWorkspaceTreeNode source, int eventType, int x, int y) {		
		this(source, eventType, x, y, null);
	}
	
	public WorkspaceActionEvent(AWorkspaceTreeNode source, int eventType, Object baggage) {		
		this(source, eventType, 0, 0, baggage);
	}
	
	public WorkspaceActionEvent(AWorkspaceTreeNode source, int eventType, int x, int y, Object baggage) {		
		this.x = x;
		this.y = y;
		this.source = source;
		this.eventType = eventType;
		this.baggage = baggage;
	}
	
	public AWorkspaceTreeNode getSource() {
		return source;
	}

	public int getType() {
		return eventType;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public Object getBaggage() {
		return baggage;
	}
	
	public void consume() {
		consumed = true;
	}
	
	public boolean isConsumed() {
		return consumed;
	}
	
	private String getTypeTranslated() {
		String type = "";
		if((this.getType()&MOUSE_LEFT) > 0)
			type += "MOUSE_LEFT";
		if((this.getType()&MOUSE_RIGHT) > 0)
			type += "MOUSE_RIGHT";
		if((this.getType()&MOUSE_CLICK) > 0)
			type += "_CLICK";
		if((this.getType()&MOUSE_DBLCLICK) > 0)
			type += "_DBLCLICK";
		if(this.getType() == WSNODE_CHANGED)
			type = "WSNODE_CHANGED";
		return type;
	}
	
	public String toString() {
		return "WorkspaceNodeEvent[type="+getTypeTranslated()+";x="+getX()+";y="+getY()+";source={"+getSource()+((getBaggage()!=null)?"};baggage={"+getBaggage():"")+"}]";
	}
	
	
}
