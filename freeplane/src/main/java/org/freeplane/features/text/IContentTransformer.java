package org.freeplane.features.text;

import org.freeplane.features.map.NodeModel;

public interface IContentTransformer extends Comparable<IContentTransformer> {
    enum Mode { VIEW, TEXT };

	Object transformContent(NodeModel node, Object nodeProperty, Object content, TextController textController, Mode mode) throws TransformationException;
	boolean isFormula(Object content);

	/** used for determining the transformer sequence when more than one transformer is present.
	 * Transformers are sorted by priority numerically, that is the transformer with the least priority value
	 * comes first. */
	int getPriority();
	
	boolean markTransformation();
}
