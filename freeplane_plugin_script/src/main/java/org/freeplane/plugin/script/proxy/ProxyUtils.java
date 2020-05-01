package org.freeplane.plugin.script.proxy;

import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.api.Node;
import org.freeplane.api.NodeCondition;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeStream;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.script.ScriptContext;

import groovy.lang.Closure;

public class ProxyUtils {
	static List<? extends Node> createNodeList(final List<NodeModel> list, final ScriptContext scriptContext) {
		return new AbstractList<Node>() {
			final private List<NodeModel> nodeModels = list;

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = nodeModels.get(index);
				return new NodeProxy(nodeModel, scriptContext);
			}

			@Override
			public int size() {
				return nodeModels.size();
			}
		};
	}

	static List<? extends Node> find(final ICondition condition, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.createNodeList(ProxyUtils.findImpl(condition, node, false), scriptContext);
	}

	static List<? extends Node> findAll(final NodeModel node, final ScriptContext scriptContext, boolean depthFirst) {
		return ProxyUtils.createNodeList(ProxyUtils.findImpl(null, node, depthFirst), scriptContext);
	}

	static List<? extends Node> find(final Closure<Boolean> closure, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.find(createCondition(closure, scriptContext), node, scriptContext);
	}

	static List<? extends Node> find(boolean withAncestors, boolean withDescendants, final Closure<Boolean> closure, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.find(withAncestors, withDescendants, createCondition(closure, scriptContext), node, scriptContext);
	}

	private static List<? extends Node> find(boolean withAncestors, boolean withDescendants, ICondition createCondition,
											 NodeModel node, ScriptContext scriptContext) {
		final Filter filter = Filter.createFilter(createCondition, withAncestors, withDescendants, false);
		filter.calculateFilterResults(node);
		final List<NodeModel> allNodes = ProxyUtils.findImpl(null, node, false);
		return ProxyUtils.createNodeList(allNodes.stream().filter(filter::isVisible)
			.collect(Collectors.toList()), scriptContext);
	}

	static ICondition createCondition(final Closure<Boolean> closure, final ScriptContext scriptContext) {
	    final ICondition condition = closure == null ? null : new ASelectableCondition() {
			@Override
			public boolean checkNode(final NodeModel node) {
				try {
					final Boolean result = closure
					    .call(new Object[] { new NodeProxy(node, scriptContext) });
					if (result == null) {
						throw new RuntimeException("find(): closure returned null instead of boolean/Boolean");
					}
					return result;
				}
				catch (final ClassCastException e) {
					throw new RuntimeException("find(): closure returned " + e.getMessage()
					        + " instead of boolean/Boolean");
				}
			}

			@Override
			protected String createDescription() {
				return "<Closure>";
			}

			@Override
			protected String getName() {
				return  "Closure";
			}
		};
	    return condition;
    }

	static List<? extends Node> find(final NodeCondition condition, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.find(createCondition(condition, scriptContext), node, scriptContext);
	}

	static List<? extends Node> find(boolean withAncestors, boolean withDescendants, final NodeCondition condition, final NodeModel node, final ScriptContext scriptContext) {
		return ProxyUtils.find(withAncestors, withDescendants, createCondition(condition, scriptContext), node, scriptContext);
	}

	static ICondition createCondition(final NodeCondition condition, final ScriptContext scriptContext) {
		final ICondition filterCondition = condition == null ? null : new ASelectableCondition() {
			@Override
			public boolean checkNode(final NodeModel node) {
				return condition.check(new NodeProxy(node, scriptContext));
			}

			@Override
			protected String createDescription() {
				return "<Code>";
			}

			@Override
			protected String getName() {
				return  "Code";
			}
		};
	    return filterCondition;
    }
	/** finds from any node downwards.
	 * @param condition if null every node will match. */
	@SuppressWarnings("unchecked")
	private static List<NodeModel> findImpl(final ICondition condition, final NodeModel node, boolean depthFirst) {
		Stream<NodeModel> nodes = depthFirst ? NodeStream.bottomUpOf(node) : NodeStream.of(node);
		if(condition != null)
		    nodes = nodes.filter(condition::checkNode);
		return nodes.collect(Collectors.toList());
	}

	public static List<Proxy.Node> createListOfChildren(final NodeModel nodeModel, final ScriptContext scriptContext) {
        return new ArrayList<Proxy.Node>(new AbstractList<Proxy.Node>() {
    		@Override
    		public Proxy.Node get(final int index) {
    			final NodeModel child = nodeModel.getChildAt(index);
    			return new NodeProxy(child, scriptContext);
    		}

    		@Override
    		public int size() {
    			return nodeModel.getChildCount();
    		}
    	});
    }

	/** this method is null-safe, i.e. value may be null and the result is not null. */
	public static Convertible attributeValueToConvertible(final NodeModel nodeModel, final ScriptContext scriptContext,
	                                             Object value) {
		if (value instanceof IFormattedObject)
			value = ((IFormattedObject) value).getObject();
		if (value instanceof Number)
			return new ConvertibleNumber((Number) value);
		else if (value instanceof Date)
			return new ConvertibleDate((Date) value);
		return new ConvertibleText(nodeModel, scriptContext, value == null ? null : value.toString());
	}

	public static Convertible nodeModelToConvertible(final NodeModel nodeModel, final ScriptContext scriptContext) {
        Object value = nodeModel.getUserObject();
    	if (value instanceof IFormattedObject)
    		value = ((IFormattedObject) value).getObject();
    	if (value instanceof Number)
    		return new ConvertibleNumber((Number) value);
    	else if (value instanceof Date)
    		return new ConvertibleDate((Date) value);
    	return new ConvertibleNodeText(nodeModel, scriptContext);
    }

	public static <T>  List<T> createList(final Collection<T> collection) {
		return new AbstractList<T>() {
			private int lastIndex;
			private Iterator<T> iterator;
			@Override
            public T get(int index) {
				if(index >= size())
					throw new NoSuchElementException();
				if(index == 0)
					return collection.iterator().next();
				if(iterator == null || index <= lastIndex){
					lastIndex = -1;
					iterator = collection.iterator();
				}
				try{
					T object;
					for(object = null; lastIndex < index; lastIndex++)
						object = iterator.next();
					return object;
				}
				catch (ConcurrentModificationException e) {
					iterator = null;
					return get(index);
				}
            }

			@Override
            public int indexOf(Object o) {
				final Iterator<T> it = iterator();
				int i = -1;
				while(it.hasNext()){
					i++;
					final T next = it.next();
					if(o ==next || o != null && o.equals(next))
						return i;
				}
				return -1;
			}

			@Override
			public int lastIndexOf(Object o) {
				final Iterator<T> it = iterator();
				int i = -1;
				int result = -1;
				while(it.hasNext()){
					i++;
					final T next = it.next();
					if(o ==next || o != null && o.equals(next))
						result = i;
				}
				return result;
			}

			@Override
            public Iterator<T> iterator() {
	            return collection.iterator();
            }


			@Override
            public int size() {
	            return collection.size();
            }
		};
    }

	/** used for node core texts and for attribute values. Note that it would lead to an error on reopening of a map
	 * if we would allow to assign GStrings here. So all unknown stuff is cast to String. */
    static Object transformObject(Object objectToTransform, String pattern) {
        final Object object = createFormattedObjectIfPossible(objectToTransform, pattern);
        if (object instanceof IFormattedObject)
    		return object;
    	else if (object instanceof Number)
            return new FormattedNumber((Number) object);
    	else if (object instanceof Date)
            return createDefaultFormattedDate((Date) object);
    	else if (object instanceof Calendar)
            return createDefaultFormattedDate(((Calendar) object).getTime());
    	else if (object instanceof URI)
    	    return object;
        else
            return Convertible.toString(object);
    }

    private static Object createFormattedObjectIfPossible(Object object, String pattern) {
        if (object instanceof String) {
			final Object object1 = object;
			final String oldFormat = pattern;
			object = ((MTextController) TextController.getController()).guessObject(object1, oldFormat);
		}
		else if (pattern != null)
            object = FormatController.format(object, pattern);
        return object;
    }

    static FormattedDate createDefaultFormattedDate(final Date date) {
        return FormattedDate.createDefaultFormattedDate(date.getTime(), IFormattedObject.TYPE_DATE);
    }

    static FormattedDate createDefaultFormattedDateTime(final Date date) {
        return FormattedDate.createDefaultFormattedDate(date.getTime(), IFormattedObject.TYPE_DATETIME);
    }
}
