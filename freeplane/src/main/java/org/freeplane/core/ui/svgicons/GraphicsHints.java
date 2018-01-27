package org.freeplane.core.ui.svgicons;

import java.awt.RenderingHints;

public class GraphicsHints  extends RenderingHints.Key{
	
	final public static GraphicsHints CACHE_ICONS = new GraphicsHints(0); 
	
	private GraphicsHints(int privateValue) {
		super(privateValue);
	}

	@Override
	public boolean isCompatibleValue(Object val) {
		return val == Boolean.TRUE;
	}
}

