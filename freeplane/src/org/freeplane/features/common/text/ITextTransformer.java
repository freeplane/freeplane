package org.freeplane.features.common.text;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;

public interface ITextTransformer extends Comparable<ITextTransformer> {
	String transformText(String nodeText, NodeModel nodeModel);

	EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl, KeyEvent firstEvent,
	                                boolean isNewNode, boolean editLong);

	/** used for determining the transformer sequence when more than one transformer is present.
	 * Transformers are sorted by priority numerically, that is the transformer with the least priority value
	 * comes first. */
	int getPriority();
}
