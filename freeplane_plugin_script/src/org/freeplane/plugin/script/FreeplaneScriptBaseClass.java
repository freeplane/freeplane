package org.freeplane.plugin.script;

import groovy.lang.Binding;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;

import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.script.proxy.Convertible;
import org.freeplane.plugin.script.proxy.Proxy.NodeRO;

/** All methods of this class are available as "global" methods in every script.
 * Only documented methods are meant to be used in scripts. */
public abstract class FreeplaneScriptBaseClass extends Script {
	private final Pattern nodeIdPattern = Pattern.compile("ID_\\d+");
	private final MetaClass nodeMetaClass;
	private Map<Object, Object> boundVariables;
	private NodeRO node;

	
    public FreeplaneScriptBaseClass() {
	    super();
	    nodeMetaClass = InvokerHelper.getMetaClass(NodeRO.class);
	    initBinding();
    }

    @SuppressWarnings("unchecked")
	public void initBinding() {
	    boundVariables = super.getBinding().getVariables();
	    // this is important: we need this reference no matter if "node" is overridden later by the user
	    node = (NodeRO) boundVariables.get("node");
    }

	@Override
    public void setBinding(Binding binding) {
	    super.setBinding(addStaticBindings(binding));
	    initBinding();
    }

	private Binding addStaticBindings(Binding binding) {
		binding.setProperty("logger", new LogUtils());
		binding.setProperty("ui", new UITools());
		binding.setProperty("htmlUtils", HtmlUtils.getInstance());
		binding.setProperty("textUtils", new TextUtils());
	    return binding;
    }

	/* <ul>
	 * <li> translate raw node ids to nodes.
	 * <li> "imports" node's methods into the script's namespace
	 * </ul>
	 */
	public Object getProperty(String property) {
		// shortcuts for the most usual cases
		if (property.equals("node")) {
			return node;
		}			
		if (property.equals("c")) {
			return boundVariables.get(property);
		}			
		if (nodeIdPattern.matcher(property).matches()) {
			return N(property);
		}
		else {
			final Object boundValue = boundVariables.get(property);
			if (boundValue != null) {
				return boundValue;
			}
			else {
				try {
					return nodeMetaClass.getProperty(node, property);
				}
				catch (MissingMethodException e) {
					return super.getProperty(property);
				}
			}
		}
	}

	/*
	 * extends super class version by node instance methods.
	 */
    public Object invokeMethod(String methodName, Object args) {
        try {
            return super.invokeMethod(methodName, args);
        }
        catch (MissingMethodException mme) {
            try {
                return nodeMetaClass.invokeMethod(node, methodName, args);
            }
            catch (MissingMethodException e) {
            	throw e;
            }
        }
    }

	/** Shortcut for node.map.node(id) - necessary for ids to other maps. */
	public NodeRO N(String id) {
		final NodeRO node = (NodeRO) getBinding().getVariable("node");
		return node.getMap().node(id);
	}

	/** Shortcut for node.map.node(id).text. */
	public String T(String id) {
		final NodeRO n = N(id);
		return n == null ? null : n.getText();
	}

	/** Shortcut for node.map.node(id).value. */
	public Object V(String id) {
		final NodeRO n = N(id);
		try {
	        return n == null ? null : n.getValue();
        }
        catch (ExecuteScriptException e) {
        	return null;
        }
	}

	/** returns valueIfNull if value is null and value otherwise. */
	public Object ifNull(Object value, Object valueIfNull) {
		return value == null ? valueIfNull : value;
	}

	/** rounds a number to integral type. */
	public Long round(final Double d) {
		if (d == null)
			return null;
		return Math.round(d);
	}
	
	/** round to the given number of decimal places: <code>round(0.1234, 2) -> 0.12</code> */
	public Double round(final Double d, final int precision) {
		if (d == null)
			return d;
		double factor = 1;
		for (int i = 0; i < precision; i++) {
			factor *= 10.;
		}
		return Math.round(d * factor) / factor;
	}

	/** formats according to the internal standard, that is the conversion will be reversible
	 * for types that are handled special by the scripting api namely Dates and Numbers.
	 * @see Convertible#toString(Object) */
	public String toString(final Object o) {
		return Convertible.toString(o);
	}

//	/** Shortcut for new {@link org.freeplane.plugin.script.proxy.Convertible}. */
//	public Convertible convertible(String string) {
//		return new Convertible(FormulaUtils.eval string, node.get);
//	}
}
