package org.freeplane.features.text.mindmapmode;


import javax.swing.JEditorPane;

import org.freeplane.features.map.NodeModel;


public interface IEditBaseCreator{
	public EditNodeBase createEditor(final NodeModel node, Object nodeProperty,
	                                 Object content, final EditNodeBase.IEditControl editControl, final boolean  editLong);
	public JEditorPane createTextEditorPane(final NodeModel node, Object nodeProperty,
            Object content);
}
