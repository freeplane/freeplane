package org.freeplane.features.common.text;

import org.freeplane.features.common.map.NodeModel;

public interface ITextTransformer extends Comparable<ITextTransformer> {
	String transformText(String nodeText, NodeModel nodeModel);

	/** used for determining the transformer sequence when more than one transformer is present.
	 * Transformers are sorted by priority numerically, that is the transformer with the least priority value
	 * comes first. */
	int getPriority();
}
