/**
 * 
 */
package org.freeplane.features.mindmapnode.pattern;

// TODO rladstaetter 28.02.2009 make attributes final
class Edge {
	private String style;
	private String width;
	private String color;
	public String getColor() {
    	return color;
    }
	public String getStyle() {
    	return style;
    }
	public String getWidth() {
    	return width;
    }
	public Edge(String color, String style, String width) {
        super();
        this.setColor(color);
        this.setStyle(style);
        this.setWidth(width);
    }
	public Edge() {
    }
	public void setWidth(String width) {
	    this.width = width;
    }
	public void setColor(String color) {
	    this.color = color;
    }
	public void setStyle(String style) {
	    this.style = style;
    }
}