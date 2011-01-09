package org.freeplane.features.mindmapmode.text;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;

public interface IEditBaseCreator{
	EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl, KeyEvent firstEvent, boolean isNewNode, boolean editLong);
}
