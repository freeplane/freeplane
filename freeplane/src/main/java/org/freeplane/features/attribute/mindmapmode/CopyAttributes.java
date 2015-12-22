/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.attribute.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

@EnabledAction(checkOnNodeChange=true)
class CopyAttributes extends AFreeplaneAction {
	private static Object[] attributes = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Object[] getAttributes() {
		return attributes;
	}

	public CopyAttributes() {
		super("CopyAttributes");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		copyAttributes(node);
	}

	/**
	 */
	private void copyAttributes(final NodeModel node) {
		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
		if(model == null){
			attributes = null;
			return;
		}
		final int attributeTableLength = model.getAttributeTableLength();
		attributes = new Object[attributeTableLength * 2];
		for(int i = 0; i < attributeTableLength; i++){
			final Attribute attribute = model.getAttribute(i);
			attributes[2 * i] = attribute.getName();
			attributes[2 * i+1] = attribute.getValue();
		}
	}
	@Override
    public void setEnabled() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if(node != null){
			final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
			setEnabled(model != null && model.getAttributeTableLength() > 0);
		}
		else
			setEnabled(false);
    }
}

@EnabledAction(checkOnNodeChange = true)
class PasteAttributes extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PasteAttributes() {
		super("PasteAttributes");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		pasteAttributes(node);
	}

	/**
	 */
	private void pasteAttributes(final NodeModel node) {
		Object[] attributes = CopyAttributes.getAttributes();
		if (attributes == null) {
			JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getCurrentRootComponent(), TextUtils
			    .getText("no_copy_attributes_before_paste_attributes"), "" /*=Title*/, JOptionPane.ERROR_MESSAGE);
			return;
		}
		final MAttributeController controller = MAttributeController.getController();
		for(int i = 0; i < attributes.length;){
			final String name = attributes[i++].toString();
			final Object value = attributes[i++];
			controller.addAttribute(node, new Attribute(name, value));
		}
	}

	@Override
    public void setEnabled() {
		setEnabled(CopyAttributes.getAttributes() != null);
    }
}

@EnabledAction(checkOnNodeChange = true)
class AddStyleAttributes extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddStyleAttributes() {
		super("AddStyleAttributes");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		pasteAttributes(node);
	}

	/**
	 */
	private void pasteAttributes(final NodeModel node) {
		final NodeModel model = getAttributes(node);
		if(model == null){
			return;
		}
		final MAttributeController controller = MAttributeController.getController();
		controller.copyAttributesToNode(model, node);
	}

	private NodeModel getAttributes(final NodeModel node) {
		final IStyle style = LogicalStyleController.getController().getFirstStyle(node);
		final MapStyleModel extension = MapStyleModel.getExtension(node.getMap());
		final NodeModel styleNode = extension.getStyleNode(style);
		return styleNode;
    }
	
	@Override
    public void setEnabled() {
		for (final NodeModel selected : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
			if(getAttributes(selected) != null){
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
    }

}
