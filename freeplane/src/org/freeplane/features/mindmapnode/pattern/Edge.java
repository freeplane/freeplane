/**
 * 
 */
package org.freeplane.features.mindmapnode.pattern;

// TODO rladstaetter 28.02.2009 make attributes final
class Edge {
	private String color;
	private String style;
	private String width;

	public Edge() {
	}

	public Edge(final String color, final String style, final String width) {
		super();
		this.setColor(color);
		this.setStyle(style);
		this.setWidth(width);
	}

	public String getColor() {
		return color;
	}

	public String getStyle() {
		return style;
	}

	public String getWidth() {
		return width;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public void setStyle(final String style) {
		this.style = style;
	}

	public void setWidth(final String width) {
		this.width = width;
	}
}
