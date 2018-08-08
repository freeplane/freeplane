package org.freeplane.features.edge;

import org.freeplane.core.util.LogUtils;

public enum EdgeStyle implements org.freeplane.api.EdgeStyle{
    EDGESTYLE_BEZIER("bezier", 1f), EDGESTYLE_HIDDEN("hide_edge", 1f), EDGESTYLE_HORIZONTAL("horizontal", 1f), 
    EDGESTYLE_LINEAR("linear", 1f), EDGESTYLE_SHARP_BEZIER("sharp_bezier", 0.25f), EDGESTYLE_SHARP_LINEAR("sharp_linear", 0f),
	EDGESTYLE_SUMMARY("summary", 1f);
	private String name;
	private float nodeLineWeight;

	private EdgeStyle(final String name, final float nodeLineWeight) {
		this.name = name;
		this.nodeLineWeight = nodeLineWeight;
	}

	@Override
	public String toString() {
		return name;
	}

	public float getNodeLineWidth(final int width) {
		final float nlWidth = nodeLineWeight * width;
		return nlWidth;
	}

	static public EdgeStyle getStyle(final String name) {
		if (name == null) {
			return null;
		}
		for (final EdgeStyle style : EdgeStyle.class.getEnumConstants()) {
			if (style.name.equals(name)) {
				return style;
			}
		}
		LogUtils.warn("unknown edge style name " + name);
		return null;
	}

	static public String toString(final EdgeStyle style) {
		return style == null ? null : style.toString();
	}
}
