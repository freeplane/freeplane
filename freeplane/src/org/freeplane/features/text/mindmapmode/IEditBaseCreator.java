package org.freeplane.features.text.mindmapmode;

import java.awt.event.InputEvent;

import org.freeplane.features.map.NodeModel;


public interface IEditBaseCreator{
	public static enum EditedComponent{TEXT, DETAIL, NOTE}

	public EditNodeBase createEditor(final NodeModel nodeModel, final EditedComponent editedComponent,
	                                 final EditNodeBase.IEditControl editControl, String text, final InputEvent firstEvent,
	                                 final boolean  editLong);
}
