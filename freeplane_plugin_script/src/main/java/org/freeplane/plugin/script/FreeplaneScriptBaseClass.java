package org.freeplane.plugin.script;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.api.ControllerRO;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.NodeRO;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.TimePeriodUnits;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.proxy.AbstractProxy;
import org.freeplane.plugin.script.proxy.Convertible;
import org.freeplane.plugin.script.proxy.ProxyFactory;

import groovy.lang.Binding;
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
	    private final ResourceController resourceController = ResourceController.getResourceController();

        public boolean getBooleanProperty(String key) {
            return resourceController.getBooleanProperty(key);
        }

        public boolean getBooleanProperty(String key, boolean defaultValue) {
            return resourceController.getBooleanProperty(key, defaultValue);
        }

        public boolean getProperty(String key, boolean defaultValue) {
            return getBooleanProperty(key, defaultValue);
        }

        public <T extends Enum<T>> T getEnumProperty(String propertyName, Enum<T> defaultValue) {
            return resourceController.getEnumProperty(propertyName, defaultValue);
        }

        public <T extends Enum<T>> T getProperty(String propertyName, Enum<T> defaultValue) {
            return getEnumProperty(propertyName, defaultValue);
        }

        public double getDoubleProperty(String key) {
            return resourceController.getDoubleProperty(key);
        }

        public double getDoubleProperty(String key, double defaultValue) {
            return resourceController.getDoubleProperty(key, defaultValue);
        }

        public double getProperty(String key, double defaultValue) {
            return getDoubleProperty(key, defaultValue);
        }

        public int getIntProperty(String key) {
            return resourceController.getIntProperty(key);
        }

        public int getIntProperty(String key, int defaultValue) {
            return resourceController.getIntProperty(key, defaultValue);
        }

        public int getProperty(String key, int defaultValue) {
            return getIntProperty(key, defaultValue);
        }

        public long getLongProperty(String key, long defaultValue) {
            return resourceController.getLongProperty(key, defaultValue);
        }

        public long getProperty(String key, long defaultValue) {
            return getLongProperty(key, defaultValue);
        }

        public int getLengthProperty(String name) {
            return resourceController.getLengthProperty(name);
        }

        public Quantity<LengthUnit> getLengthQuantityProperty(String name) {
            return resourceController.getLengthQuantityProperty(name);
        }

        public int getTimeProperty(String name) {
            return resourceController.getTimeProperty(name);
        }

        public Quantity<TimePeriodUnits> getTimeQuantityProperty(String name) {
            return resourceController.getTimeQuantityProperty(name);
        }

        public Color getColorProperty(String name) {
            return resourceController.getColorProperty(name);
        }

        public String getProperty(String key) {
            return resourceController.getProperty(key);
        }

        public String getProperty(String key, String value) {
            return resourceController.getProperty(key, value);
        }

        public String getDefaultProperty(String key) {
            return resourceController.getDefaultProperty(key);
        }

        public Collection<IFreeplanePropertyListener> getPropertyChangeListeners() {
            return resourceController.getPropertyChangeListeners();
        }

        public URL getResource(String resourcePath) {
            return resourceController.getResource(resourcePath);
        }

        public InputStream getResourceStream(String resFileName) throws IOException {
            return resourceController.getResourceStream(resFileName);
        }

        public String getResourceBaseDir() {
            return resourceController.getResourceBaseDir();
        }

        public String getInstallationBaseDir() {
            return resourceController.getInstallationBaseDir();
        }

        public String getLanguageCode() {
            return resourceController.getLanguageCode();
        }

        public String getDefaultLanguageCode() {
            return resourceController.getDefaultLanguageCode();
        }

        public void setDefaultProperty(String key, String value) {
            resourceController.setDefaultProperty(key, value);
        }

        public void setProperty(String property, boolean value) {
            resourceController.setProperty(property, value);
        }

        public void setProperty(String name, int value) {
            resourceController.setProperty(name, value);
        }

        public void setProperty(String name, long value) {
            resourceController.setProperty(name, value);
        }

        public void setProperty(String name, double value) {
            resourceController.setProperty(name, value);
        }

        public void setProperty(String property, String value) {
            resourceController.setProperty(property, value);
        }

        public Icon getIcon(String iconKey) {
            return resourceController.getIcon(iconKey);
        }

        public URL getIconResource(String resourcePath) {
            return resourceController.getIconResource(resourcePath);
        }

        public Icon getImageIcon(String iconKey) {
            return resourceController.getImageIcon(iconKey);
        }

        public Locale getSystemLocale() {
            return resourceController.getSystemLocale();
        }

        public String[] getArrayProperty(String key, String separator) {
            return resourceController.getArrayProperty(key, separator);
        }

        public Properties getProperties() {
			return resourceController.getProperties();
		}

		/** support config['key'] from Groovy. */
        public String getAt(final String name) {
            return getProperty(name);
        }

        public void setAt(final String name, final String value) {
            setProperty(name, value);
        }

		public ResourceBundle getResources() {
		    return resourceController.getResources();
		}

		public String getFreeplaneUserDirectory() {
			return resourceController.getFreeplaneUserDirectory();
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
    }

    public FreeplaneScriptBaseClass(Binding binding) {
        this();
        setBinding(binding);
    }

    @Override
    public void setBinding(Binding binding) {
        super.setBinding(binding);
        if(binding.hasVariable("node") && binding.hasVariable("c")) {
            Object nodeProxy = binding.getVariable("node");
            Object controllerProxy = binding.getVariable("c");
            boundVariables = binding.getVariables();
            if (nodeProxy instanceof NodeRO || nodeProxy == null) {
                node = (NodeRO) nodeProxy;
            }
            if (controllerProxy instanceof ControllerRO || controllerProxy == null) {
                controller = (ControllerRO) controllerProxy;
            }
        }
    }

    void setScript(Object script) {
		this.script = script;
	}

	FreeplaneScriptBaseClass withBinding(final NodeModel node, ScriptContext scriptContext) {
		try {
        	FreeplaneScriptBaseClass instance = boundVariables != null ? getClass().newInstance() : this;
        	instance.script = script;
            ControllerRO controllerProxy = ProxyFactory.createController(scriptContext);
            NodeRO nodeProxy = ProxyFactory.createNode(node, scriptContext);
        	Binding binding = createBinding(nodeProxy, controllerProxy);
        	instance.setBinding(binding);
        	return instance;
        }
        catch (InstantiationException | IllegalAccessException e) {
        	throw new RuntimeException(e);
        }
	}

    protected Binding createBinding(NodeRO nodeProxy, ControllerRO controllerProxy) {
        Binding binding = new Binding(new LinkedHashMap(getBinding().getVariables()));
		binding.setVariable("c", controllerProxy);
		binding.setVariable("node", nodeProxy);
        return binding;
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
			else if(node != null){
				try {
					return nodeMetaClass.getProperty(node, property);
				}
				catch (MissingPropertyException e) {/**/}
			}
			return super.getProperty(property);
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
		return node.getMindMap().node(id);
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
        LinkController.getController().loadURI(delegate, new Hyperlink(uri));
    }

    /** opens a link */
    public void loadUri(final String link) {
    	try {
			final NodeModel delegate = ((AbstractProxy<NodeModel>)node).getDelegate();
			LinkController.getController().loadURI(delegate, LinkController.createHyperlink(link));
		} catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
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
