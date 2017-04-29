package org.freeplane.features.text;

import javax.swing.Icon;

import org.freeplane.features.map.NodeModel;

public interface IContentTransformer extends Comparable<IContentTransformer> {

	Object transformContent(TextController textController, Object content, NodeModel node, Object transformedExtension) throws TransformationException;
	boolean isFormula(TextController textController, Object content, NodeModel node, Object transformedExtension);
	Icon getIcon(TextController textController, Object content, NodeModel node, Object transformedExtension);

	/** used for determining the transformer sequence when more than one transformer is present.
	 * Transformers are sorted by priority numerically, that is the transformer with the least priority value
	 * comes first. */
	int getPriority();
	
	boolean markTransformation();
}
