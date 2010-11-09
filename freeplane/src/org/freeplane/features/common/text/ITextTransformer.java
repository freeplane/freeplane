package org.freeplane.features.common.text;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;


public interface ITextTransformer {
	String transformText(String nodeText, NodeModel nodeModel);

	EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl, KeyEvent firstEvent, boolean isNewNode, boolean editLong);
}
