package org.freeplane.view.swing.map.attribute;

import org.freeplane.features.common.attribute.IAttributeTableModel;

public interface IAttributeTableModelTransformer extends IAttributeTableModel{
	Object transformValueAt(int row, int col) throws Exception;
}
