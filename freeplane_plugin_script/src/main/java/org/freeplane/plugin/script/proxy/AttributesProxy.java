/**
 *
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.view.swing.map.NodeView;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

class AttributesProxy extends AbstractProxy<NodeModel> implements Proxy.Attributes {
	AttributesProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

    @Override
	public boolean containsKey(String name) {
        final int index = findFirst(name);
        return (index != -1);
    }

	@Override
	@Deprecated
	public Object get(final String name) {
		return getFirst(name);
	}

	@Override
	public Object getFirst(final String name) {
		final int index = findFirst(name);
		if (index == -1) {
			return null;
		}
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		final Attribute attribute = attributeTableModel.getAttribute(index);
		return getTransformedAttributeValue(attribute);
	}

	@Override
	public List<Object> getAll(final String name) {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<Object> result = new ArrayList<Object>();
		for (final Attribute attribute : attributeTableModel.getAttributes()) {
			if (attribute.getName().equals(name)) {
				result.add(getTransformedAttributeValue(attribute));
			}
		}
		return result;
	}

	@Override
	public List<String> getNames() {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<String> result = new ArrayList<String>(attributeTableModel.getRowCount());
		for (final Attribute a : attributeTableModel.getAttributes()) {
			result.add(a.getName());
		}
		return result;
	}

	@Override
	@Deprecated
	public List<String> getAttributeNames() {
		return getNames();
	}

	@Override
	public List<? extends Convertible> getValues() {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return Collections.emptyList();
		}
		final ArrayList<Convertible> result = new ArrayList<Convertible>(attributeTableModel.getRowCount());
		for (final Attribute a : attributeTableModel.getAttributes()) {
			result.add(ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), getTransformedAttributeValue(a)));
		}
		return result;
	}

	private Object getTransformedAttributeValue(final Attribute a) {
		final Object value = a.getValue();
		final Object content = TextController.getController().getTransformedObjectNoFormattingNoThrow(value, getDelegate(), null);
		return content;
	}

	@Override
	public Map<String, Object> getMap() {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return Collections.emptyMap();
		}
		final LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(attributeTableModel.getRowCount());
		for (final Attribute a : attributeTableModel.getAttributes()) {
			result.put(a.getName(), getTransformedAttributeValue(a));
		}
		return result;
    }

	@Override
	public List<? extends Convertible> findValues(Closure<Boolean> closure) {
		try {
			final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
			if (attributeTableModel == null) {
				return Collections.emptyList();
			}
			final ArrayList<Convertible> result = new ArrayList<Convertible>(
			    attributeTableModel.getRowCount());
			for (final Attribute a : attributeTableModel.getAttributes()) {
				final Object transformedAttributeValue = getTransformedAttributeValue(a);
				final Object bool = closure.call(new Object[] { a.getName(), transformedAttributeValue });
				if (result == null) {
					throw new RuntimeException("findValues(): closure returned null instead of boolean/Boolean");
				}
				if ((Boolean) bool)
					result.add(ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), transformedAttributeValue));
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

	@Override
	public Object get(final int index) {
		return getAndCheckNodeAttributeTableModelForIndex(index, "get:").getValue(index);
	}

    private NodeAttributeTableModel getAndCheckNodeAttributeTableModelForIndex(final int index, String errorPrefix) {
        final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			throw new IndexOutOfBoundsException(errorPrefix + index);
		}
        return attributeTableModel;
    }

    @Override
	public String getKey(int index) {
		return getAndCheckNodeAttributeTableModelForIndex(index, "getKey:").getAttribute(index).getName();
    }

	@Override
	public void set(final int index, final Object value) {
		final NodeAttributeTableModel attributeTableModel = getAndCheckNodeAttributeTableModelForIndex(index, "set1:");
        String oldPattern = getOldValueFormatPattern(attributeTableModel, index);
		getAttributeController().performSetValueAt(attributeTableModel, ProxyUtils.transformObject(value, oldPattern), index, 1);
	}

	@Override
	public void set(final int index, final String name, final Object value) {
        final NodeAttributeTableModel attributeTableModel = getAndCheckNodeAttributeTableModelForIndex(index, "set2:");
        String oldPattern = getOldValueFormatPattern(attributeTableModel, index);
		getAttributeController().setAttribute(getDelegate(), index, new Attribute(name, ProxyUtils.transformObject(value, oldPattern)));
	}

	@Override
	public void set(final int index, final String name, final Object value, String pattern) {
		getAttributeController().setAttribute(getDelegate(), index, new Attribute(name, ProxyUtils.transformObject(value, pattern)));
	}

	@Override
	public void setFormat(final int index, String pattern) {
		final NodeAttributeTableModel attributeTableModel = getAndCheckNodeAttributeTableModelForIndex(index, "set1:");
		final Object value = attributeTableModel.getAttribute(index).getValue();
		getAttributeController().performSetValueAt(attributeTableModel, ProxyUtils.transformObject(value, pattern), index, 1);
	}

	@Override
	public int findFirst(final String name) {
		final List<String> attributeNames = getAttributeNames();
		for (int i = 0; i < attributeNames.size(); i++) {
			if (attributeNames.get(i).equals(name)) {
				return i;
			}
        }
		return -1;
	}

	@Override
	@Deprecated
	public int findAttribute(final String name) {
		return findFirst(name);
	}

	@Override
	@Deprecated
	public boolean remove(final String name) {
		final int index = findFirst(name);
		if (index == -1) {
			return false;
		}
		getAttributeController().removeAttribute(getDelegate(), index);
		return true;
	}

	@Override
	public boolean removeAll(final String name) {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return false;
		}
		final ArrayList<Integer> toRemove = new ArrayList<Integer>();
		final Vector<Attribute> attributes = attributeTableModel.getAttributes();
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

	@Override
	public void remove(final int index) {
        getAndCheckNodeAttributeTableModelForIndex(index, "remove:");
		getAttributeController().removeAttribute(getDelegate(), index);
	}

	@Override
	public void clear() {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		final int size = attributeTableModel.getRowCount();
		for (int i = size - 1; i >= 0; i--) {
			getAttributeController().removeAttribute(getDelegate(), i);
		}
	}

	@Override
	public void set(final String name, final Object value) {
		final int index = findFirst(name);
		if (index == -1) {
            final Attribute attribute = new Attribute(name, ProxyUtils.transformObject(value, null));
            getAttributeController().addAttribute(getDelegate(), attribute);
		}
		else {
		    final String oldPattern = getOldValueFormatPattern(getNodeAttributeTableModel(), index);
			final Attribute attribute = new Attribute(name, ProxyUtils.transformObject(value, oldPattern));
            getAttributeController().setAttribute(getDelegate(), index, attribute);
		}
	}

    private String getOldValueFormatPattern(NodeAttributeTableModel attributeTableModel, int index) {
        final Object value = attributeTableModel.getAttribute(index).getValue();
        return (value instanceof IFormattedObject) ? ((IFormattedObject) value).getPattern() : null;
    }

    @Override
	public void add(final String name, final Object value) {
		final Attribute attribute = new Attribute(name, ProxyUtils.transformObject(value, null));
		getAttributeController().addAttribute(getDelegate(), attribute);
	}

    @Override
	public void add(final String name, final Object value, String pattern) {
		final Attribute attribute = new Attribute(name, ProxyUtils.transformObject(value, pattern));
		getAttributeController().addAttribute(getDelegate(), attribute);
	}

	@Override
	public int size() {
		final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
		if (attributeTableModel == null) {
			return 0;
		}
		return attributeTableModel.getRowCount();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	private MAttributeController getAttributeController() {
		return (MAttributeController) AttributeController.getController();
	}

	private NodeAttributeTableModel getNodeAttributeTableModel() {
		return NodeAttributeTableModel.getModel(getDelegate());
	}

    /** make <code>if (node.attributes) println "has attributes"</code> work. */
    public boolean asBoolean() {
        return !isEmpty();
    }

	@Override
	public void optimizeWidths() {
		for (INodeView view : getDelegate().getViewers()) {
			if (view instanceof NodeView) {
				// getAttributeView() will check for null for itself:
				((NodeView) view).getAttributeView().setOptimalColumnWidths();
				return;
			}
		}
	}

    @Override
	@SuppressWarnings("unchecked")
    public Iterator<Map.Entry<String, Object>> iterator() {
        final NodeAttributeTableModel attributeTableModel = getNodeAttributeTableModel();
        if (attributeTableModel == null) {
            return  (Iterator<Map.Entry<String, Object>>) (Object) Collections.emptyMap().entrySet().iterator();
        }
        return new Iterator<Map.Entry<String, Object>>() {
            final private Iterator<Attribute> iterator = attributeTableModel.getAttributes().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Map.Entry<String, Object> next() {
                final Attribute attribute = iterator.next();
                return new Map.Entry<String, Object>() {

                    @Override
                    public String getKey() {
                        return attribute.getName();
                    }

                    @Override
                    public Object getValue() {
                        return getTransformedAttributeValue(attribute);
                    }

                    @Override
                    public Object setValue(Object value) {
                        final Object oldValue = getValue();
                        attribute.setValue(value);
                        return oldValue;
                    }
                    ;
                };
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
}
