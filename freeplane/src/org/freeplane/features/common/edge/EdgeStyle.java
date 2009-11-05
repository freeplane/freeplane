package org.freeplane.features.common.edge;

import org.freeplane.core.util.LogTool;

public enum EdgeStyle{

	EDGESTYLE_BEZIER ( "bezier", 1f),
	EDGESTYLE_HIDDEN ( "hide_edge", 1f),
	EDGESTYLE_HORIZONTAL ( "horizontal", 1f),
	EDGESTYLE_LINEAR ( "linear", 1f),
	EDGESTYLE_SHARP_BEZIER ( "sharp_bezier", 0.25f),
	EDGESTYLE_SHARP_LINEAR ( "sharp_linear", 0f);
	
	static final int MAX_NODE_LINE_WIDTH = 4;
	
	private String name;
	private float nodeLineWeight;

	private EdgeStyle (String name, float nodeLineWeight){
		this.name = name;
		this.nodeLineWeight = nodeLineWeight;
	}

	public String toString() {
		return name;
	}
	
	public int getNodeLineWidth(int width){
		final int nlWidth = (int) (nodeLineWeight * width);
		if (nlWidth == 0) return 1 ;
		if (nlWidth > MAX_NODE_LINE_WIDTH) return MAX_NODE_LINE_WIDTH;
		return nlWidth;
	}
	
	static public EdgeStyle getStyle(String name){
		if(name == null){
			return null;
		}
		for(EdgeStyle style: EdgeStyle.class.getEnumConstants()){
			if(style.name.equals(name)){
				return style;
			}
		}
		LogTool.warn("unknown edge style name " + name);
		return null;
	}
	
	static public String toString(EdgeStyle style){
		return style == null ? null : style.toString();
	}
}
