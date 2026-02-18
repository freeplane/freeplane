/*
 * Created on 30 Jan 2024 as GroovyStaticImports
 *
 * author dimitry
 */
package org.freeplane.plugin.script.classpath;

import java.util.Date;
// Enable or remove when ignoreCycles method is enabled or removed
//import java.util.function.Supplier;

import org.freeplane.api.NodeRO;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.ScannerController;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FreeplaneScriptBaseClass;
import org.freeplane.plugin.script.proxy.Convertible;
import org.freeplane.plugin.script.proxy.Proxy;
import org.freeplane.plugin.script.proxy.ScriptUtils;

/**
 * Optionally provides access to Freeplane scripting conventions, like {@code node} and {@code ui}, in {@code
 * .groovy} files that are stored in the script classpath directories. They are automatically compiled when Freeplane
 * starts and the resulting classes and their methods are available to scripts, and their methods are available as
 * functions in formula.
 * 
 * <p>This class needs to be explicitly imported into a script classpath {@code .groovy} file. It then provides all
 * global objects, variables and methods that are automatically available to regular Freeplane scripts.</p>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * import static org.freeplane.plugin.script.classpath.ScriptGlobalsImport.*
 * 
 * // Now you can use:
 * String nodeID = node.id
 * String nodeText = T(nodID)
 * ui.informationMessage("This is a message")
 * </pre>
 *
 * <p>If you only need a few of the global objects, you can make specific imports, for example:</p>
 *
 * <pre>
 * import static org.freeplane.plugin.script.classpath.ScriptGlobalsImport.node
 * import static org.freeplane.plugin.script.classpath.ScriptGlobalsImport.ui
 * </pre>
 *
 * <p>As an alternative to static import of {@code ScriptGlobalsImport}, you can also import a script utility class
 * directly.</p>
 *
 * <p>Since it is a static import, you can use the methods without prefix, just like in a Freeplane script. In addition
 * </p>
 *
 * <p>The following utilities are made available through static imports:</p>
 * <ul>
 * <li>{@link #logger} - for logging messages (see {@link LogUtils})</li>
 * <li>{@link #ui} - for GUI operations and dialogs (see {@link UITools})</li>
 * <li>{@link #htmlUtils} - for HTML/XML processing (see {@link HtmlUtils})</li>
 * <li>{@link #textUtils} - for text processing and translations (see {@link TextUtils})</li>
 * <li>{@link #menuUtils} - for menu operations (see {@link MenuUtils})</li>
 * <li>{@link #config} - for accessing Freeplane configuration (see {@link FreeplaneScriptBaseClass.ConfigProperties})</li>
 * </ul>
 * 
 * <p>Additionally, this class provides utility methods for common operations like null checking,
 * number rounding, text parsing, and formatting. You can also use the well-known global variables {@link #c} and
 * {@link #node}.</p>
 * 
 * @see org.freeplane.plugin.script.FreeplaneScriptBaseClass
 * @since 1.12.x (created on 30 Jan 2024)
 */
public class ScriptGlobalsImport {
    /** 
     * Utilities for logging messages to Freeplane's log file. Use for debugging and error reporting.
     * @see LogUtils
     */
    public final static LogUtils logger = new LogUtils();
    
    /** 
     * Utilities for GUI operations including dialogs, message boxes, and UI components access.
     * @see UITools
     */
    public final static UITools ui = new UITools();
    
    /** 
     * Utilities for HTML/XML processing including conversion between HTML and plain text.
     * @see HtmlUtils
     */
    public final static HtmlUtils htmlUtils = HtmlUtils.getInstance();
    
    /** 
     * Utilities for text processing, translations, formatting, and string operations.
     * @see TextUtils
     */
    public final static TextUtils textUtils = new TextUtils();
    
    /** 
     * Utilities for menu operations and menu item execution.
     * @see MenuUtils
     */
    public final static MenuUtils menuUtils = new MenuUtils();
    
    /**
     * Accessor for Freeplane's configuration properties. Provides access to all configuration settings.
     * <p>Note: In utility scripts and add-on classes, this static instance is the recommended way to access
     * configuration, as the global {@code config} variable is not available when compiling outside Freeplane.</p>
     */
    public final static FreeplaneScriptBaseClass.ConfigProperties config = new FreeplaneScriptBaseClass.ConfigProperties();

    /**
     * Makes {@code ScriptUtils.c()} available  as {@code c}
     * @see ScriptUtils
     */
    public final static Proxy.Controller c = ScriptUtils.c();

    /**
     * Makes {@code ScriptUtils.node()} available as {@code node}
     * @see ScriptUtils
     */
    public final static Proxy.Node node = ScriptUtils.node();

//    /**
//     * Executes the given closure while ignoring any cyclic dependencies in formulas.
//     * If there are cyclic dependencies, formulas are skipped without warnings or exceptions.
//     *
//     * @param <T> the return type of the closure
//     * @param closure the operation to execute
//     * @return the result of the closure execution
//     */
//    public static <T> T ignoreCycles(final Supplier<T> closure) {
//        return ScriptUtils.ignoreCycles(closure);
//    }

    /** Shortcut for node.map.node(id). */
    public NodeRO N(String id) {
        final NodeRO node = ScriptUtils.node();
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
	public static Object ifNull(Object value, Object valueIfNull) {
		return value == null ? valueIfNull : value;
	}

	/** rounds a number to integral type. */
    public static Long round(final Double d) {
            if (d == null)
                    return null;
            return Math.round(d);
    }

    /** round to the given number of decimal places: <code>round(0.1234, 2) &rarr; 0.12</code> */
    public static Double round(final Double d, final int precision) {
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
    public static Object parse(final String text) {
        return ScannerController.getController().parse(text);
    }

    /** uses formatString to return a FormattedObject.
     * <p><em>Note:</em> If you want to format the node core better use the format node attribute instead:
     * <pre>
     * node.object = new Date()
     * node.format = 'dd/MM/yy'
     * </pre>
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public static Object format(final Object object, final String formatString) {
        return FormatController.format(object, formatString);
    }

    /** Applies default date-time format for dates or default number format for numbers. All other objects are left unchanged.
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public static Object format(final Object object) {
        return FormatController.formatUsingDefault(object);
    }

    /** Applies default date format (instead of standard date-time) format on the given date.
     * @return {@link IFormattedObject} if object is formattable and the unchanged object otherwise. */
    public static Object formatDate(final Date date) {
        final String format = FormatController.getController().getDefaultDateFormat().toPattern();
        return FormatController.format(date, format);
    }

    /** formats according to the internal standard, that is the conversion will be reversible
     * for types that are handled special by the scripting api namely Dates and Numbers.
     * @see Convertible#toString(Object) */
    public static String toString(final Object o) {
        return Convertible.toString(o);
    }
}
