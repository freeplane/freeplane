package org.freeplane.features.text.mindmapmode;


import java.util.function.Supplier;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.freeplane.features.map.NodeModel;


public interface IEditBaseCreator{
	public EditNodeBase createEditor(final NodeModel node, Object nodeProperty,
	                                 Object content, final EditNodeBase.IEditControl editControl, final boolean  editLong);
	public JEditorPane createTextEditorPane(Supplier<JScrollPane> scrollPaneSupplier, final NodeModel node, Object nodeProperty,
            Object content);
}
