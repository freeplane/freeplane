package org.freeplane.features.mindmapmode.text;

import java.awt.event.KeyEvent;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;

public interface IEditBaseCreator{
	public static enum EditedComponent{TEXT, DETAIL, NOTE}

	public EditNodeBase createEditor(final NodeModel nodeModel, final EditedComponent editedComponent,
	                                 final EditNodeBase.IEditControl editControl, String text, final KeyEvent firstEvent,
	                                 final boolean  editLong);
}
