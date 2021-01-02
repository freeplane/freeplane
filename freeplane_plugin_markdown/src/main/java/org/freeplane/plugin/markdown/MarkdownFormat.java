package org.freeplane.plugin.markdown;

import org.freeplane.features.format.IdentityPatternFormat;
import org.freeplane.features.map.NodeModel;

public class MarkdownFormat extends IdentityPatternFormat {

	static final String MARKDOWN_FORMAT = "markdownPatternFormat";

	MarkdownFormat() {
		super(MARKDOWN_FORMAT);
	}

	@Override
    public boolean canFormat(Class<?> cls){
    	return NodeModel.class.isAssignableFrom(cls);
    }
}
