package org.freeplane.plugin.workspace.controller;


public class WorkspaceNodeEvent {
	public static int MOUSE_CLICK = 1;
	public static int MOUSE_DBLCLICK = 256;
	public static int MOUSE_LEFT = 2;
	public static int MOUSE_RIGHT = 4;
	
	public static int MOUSE_LEFT_CLICK = MOUSE_LEFT+MOUSE_CLICK;
	public static int MOUSE_LEFT_DBLCLICK = MOUSE_LEFT+MOUSE_DBLCLICK;
	public static int MOUSE_RIGHT_CLICK = MOUSE_RIGHT+MOUSE_CLICK;
	public static int MOUSE_RIGHT_DBLCLICK = MOUSE_RIGHT+MOUSE_DBLCLICK;
		
	final Object source;
	final int eventType;
	final int x;
	final int y;
	
	public WorkspaceNodeEvent(Object source, int eventType, int x, int y) {
		this.x = x;
		this.y = y;
		this.source = source;
		this.eventType = eventType;
	}
	
	public Object getSource() {
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
		return type;
	}
	
	public String toString() {
		return "WorkspaceNodeEvent[type="+getTypeTranslated()+";x="+getX()+";y="+getY()+";source="+getSource()+"]";
	}
}
