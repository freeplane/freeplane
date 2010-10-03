/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.freeplane.features.common.attribute.Attribute;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.plugin.script.ScriptContext;

class AttributesProxy extends AbstractProxy<NodeModel> implements Proxy.Attributes {
	AttributesProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	@Deprecated
	public String get(final String name) {
		return getFirst(name);
	}

	public String getFirst(final String name) {
		final int index = findAttribute(name);
		if (index == -1) {
			return null;
		}
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		return nodeAttributeTableModel.getAttribute(index).getValue();
	}

	public List<String> getAll(final String name) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<String> result = new ArrayList<String>();
		for (final Attribute attribute : nodeAttributeTableModel.getAttributes()) {
			if (attribute.getName().equals(name)) {
				result.add(attribute.getValue());
			}
		}
		return result;
	}

	public List<String> getAttributeNames() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<String> result = new ArrayList<String>();
		for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
			result.add(a.getName());
		}
		return result;
	}

	public String get(final int index) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("get:" + index);
		}
		final Object value = nodeAttributeTableModel.getValue(index);
		return value == null ? null : value.toString();
	}

	public void set(final int index, final String value) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("set1:" + index);
		}
		getAttributeController().performSetValueAt(nodeAttributeTableModel, value, index, 1);
	}

	public void set(final int index, final String name, final String value) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("set2:" + index);
		}
		getAttributeController().setAttribute(getDelegate(), index, new Attribute(name, value));
	}

	public int findFirst(final String name) {
		final List<String> attributeNames = getAttributeNames();
		int i = 0;
		for (final String a : attributeNames) {
			if (a.equals(name)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	@Deprecated
	public int findAttribute(final String name) {
		return findFirst(name);
	}

	@Deprecated
	public boolean remove(final String name) {
		final int index = findFirst(name);
		if (index == -1) {
			return false;
		}
		getAttributeController().removeAttribute(getDelegate(), index);
		return true;
	}

	public boolean removeAll(final String name) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return false;
		}
		final ArrayList<Integer> toRemove = new ArrayList<Integer>();
		final Vector<Attribute> attributes = nodeAttributeTableModel.getAttributes();
		for (int i = 0; i < attributes.size(); ++i) {
			if (attributes.get(i).getName().equals(name)) {
				toRemove.add(i);
			}
		}
		// do it backwards in order not to invalidate the first indexes
		for (int i = toRemove.size() - 1; i >= 0; --i) {
			getAttributeController().removeAttribute(getDelegate(), toRemove.get(i));
		}
		return !toRemove.isEmpty();
	}

	public void remove(final int index) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("remove:" + index);
		}
		getAttributeController().removeAttribute(getDelegate(), index);
	}

	public void clear() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		final int size = nodeAttributeTableModel.getRowCount();
		for (int i = size - 1; i >= 0; i--) {
			getAttributeController().removeAttribute(getDelegate(), i);
		}
	}

	public void set(final String name, final String value) {
		final int index = findFirst(name);
		final Attribute attribute = new Attribute(name, value);
		if (index == -1) {
			getAttributeController().addAttribute(getDelegate(), attribute);
		}
		else {
			getAttributeController().setAttribute(getDelegate(), index, attribute);
		}
	}

	public void add(final String name, final String value) {
		final Attribute attribute = new Attribute(name, value);
		getAttributeController().addAttribute(getDelegate(), attribute);
	}

	public int size() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return 0;
		}
		return nodeAttributeTableModel.getRowCount();
	}

	private MAttributeController getAttributeController() {
		return (MAttributeController) AttributeController.getController();
	}

	private NodeAttributeTableModel getNodeAttributeTableModel() {
		return NodeAttributeTableModel.getModel(getDelegate());
	}
}
