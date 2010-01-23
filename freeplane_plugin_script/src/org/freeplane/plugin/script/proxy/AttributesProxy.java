/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.attribute.Attribute;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;

class AttributesProxy extends AbstractProxy implements Proxy.Attributes {
	AttributesProxy(final NodeModel delegate,
			final MModeController modeController) {
		super(delegate, modeController);
	}

	public List<String> getAttributeNames() {
		final ArrayList<String> result = new ArrayList<String>();
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return result;
		}
		for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
			result.add(a.getName());
		}
		return result;
	}

	public int findAttribute(final String key) {
		List<String> attributeNames = getAttributeNames();
		int i = 0;
		for (final String a : attributeNames) {
			if (a.equals(key)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public String get(final String key) {
		final int attributeNumber = findAttribute(key);
		if (attributeNumber == -1) {
			return null;
		}
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		return nodeAttributeTableModel.getAttribute(attributeNumber).getValue();
	}

	MAttributeController getAttributeController() {
		return (MAttributeController) AttributeController
				.getController(getModeController());
	}

	NodeAttributeTableModel getNodeAttributeTableModel() {
		return NodeAttributeTableModel.getModel(getNode());
	}

	public boolean remove(final String key) {
		final int attributeNumber = findAttribute(key);
		if (attributeNumber == -1) {
			return false;
		}
		getAttributeController().removeAttribute(getNode(), attributeNumber);
		return true;
	}

	public void set(final String key, final String value) {
		final int attributeNumber = findAttribute(key);
		final Attribute attribute = new Attribute(key, value);
		if (attributeNumber == -1) {
			getAttributeController().addAttribute(getNode(), attribute);
			return;
		}
		getAttributeController().setAttribute(getNode(), attributeNumber,
				attribute);
	}

}