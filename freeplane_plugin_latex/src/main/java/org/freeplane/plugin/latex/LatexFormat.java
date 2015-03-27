package org.freeplane.plugin.latex;

import org.freeplane.features.format.IdentityPatternFormat;
import org.freeplane.features.map.NodeModel;

public class LatexFormat extends IdentityPatternFormat {

	static final String LATEX_FORMAT = "latexPatternFormat";

	LatexFormat() {
		super(LATEX_FORMAT);
	}

	@Override
    public boolean canFormat(Class<?> cls){
    	return NodeModel.class.isAssignableFrom(cls);
    }
}
