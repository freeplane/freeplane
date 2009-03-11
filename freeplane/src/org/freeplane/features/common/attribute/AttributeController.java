/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.common.attribute;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.IMapLifeCycleListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev 22.11.2008
 */
public class AttributeController implements IExtension {
	public static AttributeController getController(final ModeController modeController) {
		return (AttributeController) modeController.getExtension(AttributeController.class);
	}

	public static void install(final ModeController modeController, final AttributeController attributeController) {
		modeController.addExtension(AttributeController.class, attributeController);
	}

	final private ModeController modeController;

	public AttributeController(final ModeController modeController) {
		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final MapReader mapReader = mapController.getMapReader();
		final AttributeBuilder attributeBuilder = new AttributeBuilder(this, mapReader);
		attributeBuilder.registerBy(readManager, writeManager);
		modeController.getMapController().addMapLifeCycleListener(new IMapLifeCycleListener() {
			public void onCreate(final MapModel map) {
				AttributeRegistry.createRegistry(getModeController(), map);
			}

			public void onRemove(final MapModel map) {
			}
		});
	}

	public NodeAttributeTableModel createAttributeTableModel(final NodeModel node) {
		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node
		    .getExtension(NodeAttributeTableModel.class);
		if (attributeModel != null) {
			return attributeModel;
		}
		attributeModel = new NodeAttributeTableModel(node);
		node.addExtension(attributeModel);
		if (node.areViewsEmpty()) {
			return attributeModel;
		}
		getModeController().getMapController().nodeRefresh(node);
		return attributeModel;
	}

	public ModeController getModeController() {
		return modeController;
	}

	public void performInsertRow(final NodeAttributeTableModel model, final int row, final String name,
	                             final String value) {
		throw new UnsupportedOperationException();
	}

	public void performRegistryAttribute(final String name) {
		throw new UnsupportedOperationException();
	}

	public void performRegistryAttributeValue(final String name, final String value) {
		throw new UnsupportedOperationException();
	}

	public void performRegistrySubtreeAttributes(final NodeModel model) {
		throw new UnsupportedOperationException();
	}

	public void performRemoveAttribute(final String name) {
		throw new UnsupportedOperationException();
	}

	public void performRemoveAttributeValue(final String name, final String value) {
		throw new UnsupportedOperationException();
	}

	public Attribute performRemoveRow(final NodeAttributeTableModel model, final int row) {
		throw new UnsupportedOperationException();
	}

	public void performReplaceAtributeName(final String oldName, final String newName) {
		throw new UnsupportedOperationException();
	}

	public void performReplaceAttributeValue(final String name, final String oldValue, final String newValue) {
		throw new UnsupportedOperationException();
	}

	public void performSetColumnWidth(final NodeAttributeTableModel model, final int col, final int width) {
		throw new UnsupportedOperationException();
	}

	public void performSetFontSize(final AttributeRegistry registry, final int size) {
		throw new UnsupportedOperationException();
	}

	public void performSetRestriction(final int row, final boolean restricted) {
		throw new UnsupportedOperationException();
	}

	public void performSetValueAt(final NodeAttributeTableModel model, final Object o, final int row, final int col) {
		throw new UnsupportedOperationException();
	}

	public void performSetVisibility(final int index, final boolean isVisible) {
		throw new UnsupportedOperationException();
	}
}
