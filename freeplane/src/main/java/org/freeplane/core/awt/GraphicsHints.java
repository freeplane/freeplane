package org.freeplane.core.awt;

import java.awt.RenderingHints;

public class GraphicsHints  extends RenderingHints.Key{

    public static final GraphicsHints CACHE_ICONS = new GraphicsHints(0);
    public static final GraphicsHints FORCE_TEXT_TO_SHAPE = new GraphicsHints(1);

	private GraphicsHints(int privateValue) {
		super(privateValue);
	}

	@Override
	public boolean isCompatibleValue(Object val) {
		return val == Boolean.TRUE || val == Boolean.FALSE || val == null;
	}
}

