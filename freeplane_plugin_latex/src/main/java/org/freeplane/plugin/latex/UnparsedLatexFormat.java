package org.freeplane.plugin.latex;

import org.freeplane.features.format.IdentityPatternFormat;
import org.freeplane.features.map.NodeModel;

public class UnparsedLatexFormat extends IdentityPatternFormat {

	static final String UNPARSED_LATEX_FORMAT = "unparsedLatexPatternFormat";

	UnparsedLatexFormat() {
		super(UNPARSED_LATEX_FORMAT);
	}

	@Override
    public boolean canFormat(Class<?> cls){
    	return NodeModel.class.isAssignableFrom(cls);
    }
}
