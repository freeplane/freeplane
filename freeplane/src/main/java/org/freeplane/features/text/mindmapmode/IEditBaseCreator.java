package org.freeplane.features.text.mindmapmode;


import org.freeplane.features.map.NodeModel;


public interface IEditBaseCreator{
	public EditNodeBase createEditor(final NodeModel nodeModel, final EditNodeBase.IEditControl editControl,
	                                 Object content, final boolean  editLong);
}
