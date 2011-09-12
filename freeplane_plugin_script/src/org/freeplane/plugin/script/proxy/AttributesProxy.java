/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

class AttributesProxy extends AbstractProxy<NodeModel> implements Proxy.Attributes {
	AttributesProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	@Deprecated
	public Object get(final String name) {
		return getFirst(name);
	}

	public Object getFirst(final String name) {
		final int index = findAttribute(name);
		if (index == -1) {
			return null;
		}
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		return nodeAttributeTableModel.getAttribute(index).getValue();
	}

	public List<Object> getAll(final String name) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<Object> result = new ArrayList<Object>();
		for (final Attribute attribute : nodeAttributeTableModel.getAttributes()) {
			if (attribute.getName().equals(name)) {
				result.add(attribute.getValue());
			}
		}
		return result;
	}

	public List<String> getNames() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<String> result = new ArrayList<String>(nodeAttributeTableModel.getRowCount());
		for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
			result.add(a.getName());
		}
		return result;
	}

	@Deprecated
	public List<String> getAttributeNames() {
		return getNames();
	}
	
	public List<? extends Convertible> getValues() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<Convertible> result = new ArrayList<Convertible>(nodeAttributeTableModel.getRowCount());
		for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
			result.add(ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), a.getValue()));
		}
		return result;
	}

	public Map<String, Object> getMap() {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			return Collections.emptyMap();
		}
		final LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(nodeAttributeTableModel.getRowCount());
		for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
			result.put(a.getName(), a.getValue());
		}
		return result;
    }

	public List<? extends Convertible> findValues(Closure<Boolean> closure) {
		try {
			final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
			if (nodeAttributeTableModel == null) {
				return Collections.emptyList();
			}
			final ArrayList<Convertible> result = new ArrayList<Convertible>(
			    nodeAttributeTableModel.getRowCount());
			for (final Attribute a : nodeAttributeTableModel.getAttributes()) {
				final Object bool = closure.call(new Object[] { a.getName(), a.getValue() });
				if (result == null) {
					throw new RuntimeException("findValues(): closure returned null instead of boolean/Boolean");
				}
				if ((Boolean) bool)
					result.add(ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), a.getValue()));
			}
			return result;
		}
		catch (final MissingMethodException e) {
			throw new RuntimeException("findValues(): closure needs to accept two args and must return boolean/Boolean"
			        + " e.g. findValues{k,v -> k != 'TOTAL'}", e);
		}
		catch (final ClassCastException e) {
			throw new RuntimeException("findValues(): closure returned " + e.getMessage()
			        + " instead of boolean/Boolean");
		}
	}

	public Object get(final int index) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("get:" + index);
		}
		return nodeAttributeTableModel.getValue(index);
	}

    public String getKey(int index) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("getKey:" + index);
		}
		return nodeAttributeTableModel.getAttribute(index).getName();
    }

	public void set(final int index, final Object value) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("set1:" + index);
		}
		getAttributeController().performSetValueAt(nodeAttributeTableModel, value, index, 1);
	}

	public void set(final int index, final String name, final Object value) {
		final NodeAttributeTableModel nodeAttributeTableModel = getNodeAttributeTableModel();
		if (nodeAttributeTableModel == null) {
			throw new IndexOutOfBoundsException("set2:" + index);
		}
		getAttributeController().setAttribute(getDelegate(), index, new Attribute(name, value));
	}

	public int findFirst(final String name) {
		final List<String> attributeNames = getAttributeNames();
		for (int i = 0; i < attributeNames.size(); i++) {
			if (attributeNames.get(i).equals(name)) {
				return i;
			}
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

	public void set(final String name, final Object value) {
		final int index = findFirst(name);
		final Attribute attribute = new Attribute(name, value);
		if (index == -1) {
			getAttributeController().addAttribute(getDelegate(), attribute);
		}
		else {
			getAttributeController().setAttribute(getDelegate(), index, attribute);
		}
	}

	public void add(final String name, final Object value) {
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
