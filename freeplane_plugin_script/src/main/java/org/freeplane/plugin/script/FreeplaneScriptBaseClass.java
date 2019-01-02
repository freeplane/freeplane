package org.freeplane.plugin.script;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.api.ControllerRO;
import org.freeplane.api.NodeRO;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.proxy.AbstractProxy;
import org.freeplane.plugin.script.proxy.Convertible;

import groovy.lang.Closure;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

/** All methods of this class are available as "global" methods in every script.
 * Only documented methods are meant to be used in scripts.
 * <p>The following global objects are provided as shortcuts by the binding of this class:
 * <ul>
 * <li><b>ui:</b> see {@link UITools}</li>
 * <li><b>logger:</b> see {@link LogUtils}</li>
 * <li><b>htmlUtils:</b> see {@link HtmlUtils}</li>
 * <li><b>textUtils:</b> see {@link TextUtils}</li>
 * <li><b>menuUtils:</b> see {@link MenuUtils}</li>
 * <li><b>config:</b> see {@link ConfigProperties}</li>
 * </ul>
 * The following classes may also be useful in scripting:
 * <ul>
 * <li>{@link FreeplaneVersion}</li>
 * </ul>
 */
public abstract class FreeplaneScriptBaseClass extends Script {
	/**
	 * Accessor for Freeplane's configuration: In scripts available
	 * as "global variable" <code>config</code>.
	 */
	public static class ConfigProperties {
		public boolean getBooleanProperty(final String name) {
			return ResourceController.getResourceController().getBooleanProperty(name);
		}

		public double getDoubleProperty(final String name, final double defaultValue) {
			return ResourceController.getResourceController().getDoubleProperty(name, defaultValue);
		}

		public int getIntProperty(final String name) {
			return ResourceController.getResourceController().getIntProperty(name);
		}

		public int getIntProperty(final String name, final int defaultValue) {
			return ResourceController.getResourceController().getIntProperty(name, defaultValue);
		}

		public long getLongProperty(final String name, final int defaultValue) {
			return ResourceController.getResourceController().getLongProperty(name, defaultValue);
		}

		public String getProperty(final String name) {
			return ResourceController.getResourceController().getProperty(name);
		}

		public String getProperty(final String name, final String defaultValue) {
			return ResourceController.getResourceController().getProperty(name, defaultValue);
		}

		public Properties getProperties() {
			return ResourceController.getResourceController().getProperties();
		}

		/** support config['key'] from Groovy. */
		public String getAt(final String name) {
            return getProperty(name);
		}

		public ResourceBundle getResources() {
		    return ResourceController.getResourceController().getResources();
		}

		public String getFreeplaneUserDirectory() {
			return ResourceController.getResourceController().getFreeplaneUserDirectory();
		}
	}

	private final Pattern nodeIdPattern = Pattern.compile("ID_\\d+");
	private final MetaClass nodeMetaClass;
	private Object script;
	private Map<Object, Object> boundVariables;
	private NodeRO node;
	private ControllerRO controller;


    public FreeplaneScriptBaseClass() {
	    super();
	    nodeMetaClass = InvokerHelper.getMetaClass(NodeRO.class);
	    DefaultGroovyMethods.mixin(Number.class, NodeArithmeticsCategory.class);
    }

	void updateBoundVariables() {
	    boundVariables = getBinding().getVariables();
	    final Object boundScript = boundVariables.remove("script");
	    if(boundScript != null)
	    	script = boundScript;
	    // this is important: we need this reference no matter if "node" is overridden later by the user
	    node = (NodeRO) boundVariables.get("node");
	    controller = (ControllerRO) boundVariables.get("c");
    }

    /* <ul>
	 * <li> translate raw node ids to nodes.
	 * <li> "imports" node's methods into the script's namespace
	 * </ul>
	 */
	@Override
	public Object getProperty(String property) {
		// shortcuts for the most usual cases
		if (property.equals("node")) {
			return node;
		}
		if (property.equals("c")) {
			return controller;
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
				catch (MissingPropertyException e) {
					return super.getProperty(property);
				}
			}
		}
	}

	/*
	 * extends super class version by node instance methods.
	 */
    @Override
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

    /** round to the given number of decimal places: <code>round(0.1234, 2) &rarr; 0.12</code> */
    public Double round(final Double d, final int precision) {
            if (d == null)
                    return d;
            double factor = 1;
            for (int i = 0; i < precision; i++) {
                    factor *= 10.;
            }
            return Math.round(d * factor) / factor;
    }

    /** parses text to the proper data type, if possible, setting format to the standard. Parsing is configured via
     * config file scanner.xml
     * <pre>
     * assert parse('2012-11-30') instanceof Date
     * assert parse('1.22') instanceof Number
     * // if parsing fails the original string is returned
     * assert parse('2012XX11-30') == '2012XX11-30'
     *
     * def d = parse('2012-10-30')
     * c.statusInfo = "${d} is ${new Date() - d} days ago"
     * </pre> */
    public Object parse(final String text) {
        return ScannerController.getController().parse(text);
    }

    /** uses formatString to return a FormattedObject.
     * <p><em>Note:</em> If you want to format the node core better use the format node attribute instead:
     * <pre>
     * node.object = new Date()
     * node.format = 'dd/MM/yy'
     * </pre>
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public Object format(final Object object, final String formatString) {
        return FormatController.format(object, formatString);
    }

    /** Applies default date-time format for dates or default number format for numbers. All other objects are left unchanged.
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public Object format(final Object object) {
        return FormatController.formatUsingDefault(object);
    }

    /** Applies default date format (instead of standard date-time) format on the given date.
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public Object formatDate(final Date date) {
        final String format = FormatController.getController().getDefaultDateFormat().toPattern();
        return FormatController.format(date, format);
    }

    /** formats according to the internal standard, that is the conversion will be reversible
     * for types that are handled special by the scripting api namely Dates and Numbers.
     * @see Convertible#toString(Object) */
    public String toString(final Object o) {
        return Convertible.toString(o);
    }

    /** opens a {@link URI} */
    public void loadUri(final URI uri) {
    	final NodeModel delegate = ((AbstractProxy<NodeModel>)node).getDelegate();
        LinkController.getController().loadURI(delegate, uri);
    }

    /**
     * Executes given closure.
     *
     * If there are any cyclic dependencies formulas are skipped and no warnings or exceptions are thrown.
     */
    public <T> T ignoreCycles(final Closure<T> closure) {
    	return FormulaUtils.ignoreCycles(closure::call);
    }

	@Override
	public String toString() {
		return "Script [" + script + "]";
	}



//	/** Shortcut for new {@link org.freeplane.api.Convertible}. */
//	public Convertible convertible(String string) {
//		return new Convertible(FormulaUtils.eval string, node.get);
//	}
}
