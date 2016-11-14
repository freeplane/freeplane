package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.styles.IStyle;
import org.freeplane.plugin.script.ExecuteScriptException;

import groovy.lang.Closure;

/**
 * This interface alone defines the api for accessing the internal state of the Freeplane. All read-write methods
 * and properties (with rare, documented exceptions in {@link Controller} and {@link Map}) support undo and
 * rollback on exceptions.
 * <p>
 * Every Proxy subinterface comes in two variants:
 * <ul>
 * <li>A read-only interface, like {@link NodeRO}. This collects only the methods that don't change the
 *     underlying object (in case of <code>NodeRO</code> this would be <code>NodeModel</code>.
 * <li>A read-write interface, like {@link Node}. This inherits from the respective read-only interface all its
 *     methods and properties and adds write access to the underlying object.
 * </ul>
 * The main point of this distinction are formulas: <em>Only the methods defined in the read-only interfaces are
 * supported in Formulas!</em>. Changing values in a Formula are against the Formula concept and lead to corruption
 * of the caching mechanism for Formulas.
 */
public interface Proxy {
	/** Node's attribute table: <code>node.attributes</code> - read-only.
	 * <p>
	 * Attributes are name - value pairs assigned to a node. A node may have multiple attributes
	 * with the same name. 
	 */
	interface AttributesRO {
		/** alias for {@link #getFirst(String)}.
		 * @deprecated before 1.1 - use {@link #get(int)}, {@link #getFirst(String)} or {@link #getAll(String)} instead. */
		@Deprecated
		Object get(final String name);

		/** returns the <em>first</em> value of an attribute with the given name or null otherwise.
		 * @since 1.2 */
		Object getFirst(final String name);

		/** returns true if there is any attribute with key <a>name</a>.
		 * @since 1.4 */
		boolean containsKey(final String name);
		
		/** returns all values for the attribute name. */
		List<Object> getAll(final String name);

		/** returns all attribute names in the proper sequence. The number of names returned
		 * is equal to the number of attributes.
		 * <pre>
		 *   // rename attribute
		 *   int i = 0;
		 *   for (String name : attributes.getNames()) {
		 *       if (name.equals("xy"))
		 *           attributes.set(i, "xyz", attributes.get(i));
		 *       ++i;
		 *   }
		 * </pre> */
		List<String> getNames();

		/** @deprecated since 1.2 use #getNames() instead. */
		List<String> getAttributeNames();

		/** returns all values as a list of {@link Convertible}.
		 * @since 1.2 */
		List<? extends Convertible> getValues();

		/** returns all attributes as a map. Note that this will erase duplicate keys.
		 * <code>node.attributes = otherNode.attributes.map</code>
		 * @since 1.2 */
		java.util.Map<String, Object> getMap();

		/** returns the attribute value at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
		Object get(final int index);
		
		/** returns the attribute key at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
		String getKey(final int index);

		/** @deprecated since 1.2 - use {@link #findFirst(String)} instead. */
		int findAttribute(final String name);

		/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
		 * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
		 * must be used.
		 * @since 1.2*/
		int findFirst(final String name);

		/** returns the values of all attributes for which the closure returns true. The fact that the values are
		 * returned as a list of {@link Convertible} enables conversion. The following formula sums all attributes
		 * whose names are not equal to 'TOTAL':
		 * <pre>{@code
		 *  = attributes.findValues{key, val -> key != 'TOTAL'}.sum(0){it.num0}
		 * }</pre>
		 * @param closure A closure that accepts two arguments (String key, Object value) and returns boolean/Boolean. 
		 * @since 1.2 */
		List<? extends Convertible> findValues(Closure<Boolean> closure);

		/** the number of attributes. It is <code>size() == getAttributeNames().size()</code>. */
		int size();
		
		/** returns <code>getAttributeNames().isEmpty()</code>.
         * @since 1.2 */
		boolean isEmpty();
	}

	/** Node's attribute table: <code>node.attributes</code> - read-write.
	 * <p>
	 * <b>Notes on attribute setters:</b><ul>
	 * <li> All setter methods try to convert strings to dates, numbers or URIs.
	 * <li> All setter methods apply a default formatting (for display) of the value for dates and numbers.
	 * <li> Attributes don't have style properties so the value objects must know about the right formatting for
	 *      themselves.
	 * <li> To enforce a certain formatting use format(): <pre>node['creationDate'] = format(new Date(), 'MM/yyyy')</pre>
     * </ul>
     * <p>
     * <b>Examples:</b>
     * <pre>
     *   // == text
     *   node["attribute name"] = "a value"
     *   assert node["attribute name"].text == "a value"
     *   assert node.attributes.getFirst("attribute name") == "a value" // the same
     *
     *   // == numbers and others
     *   // converts numbers and other stuff with toString()
     *   node["a number"] = 1.2
     *   assert node["a number"].text == "1.2"
     *   assert node["a number"].num == 1.2d
     *
     *     *   // == dates
     *   def date = new Date()
     *   node["a date"] = date
     *   assert node["a date"].object.getClass().simpleName == "FormattedDate"
     *   assert node["a date"].date == format(date)
     *
     *   // == enforce formats on attribute values
     *   node["another date"] = format(date, 'yyyy|MM|dd')
     *   assert node["another date"].date == format(date, 'yyyy|MM|dd')
     *
     *   // change the date while keeping the silly format
     *   def index = node.attributes.findAttribute("another date")
     *   node.attributes.set(index, new Date(0L))
     *
     *   // == URIs
     *   def uri = new URI("http://www.freeplane.org")
     *   node["uri"] = uri
     *   assert node["uri"].object.getClass().simpleName == "URI"
     *   assert node["uri"].object == uri
     *
     *   // == remove an attribute
     *   node["removed attribute"] = "to be removed"
     *   assert node["removed attribute"] == "to be removed"
     *   node["removed attribute"] = null
     *   assert node.attributes.findFirst("removed attribute") == -1
     * </pre>
	 */
	interface Attributes extends AttributesRO {
		/** sets the value of the attribute at an index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
		void set(final int index, final Object value);

		/** sets name and value of the attribute at the given index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
		void set(final int index, final String name, final Object value);

		/** removes the <em>first</em> attribute with this name.
		 * @return true on removal of an existing attribute and false otherwise.
		 * @deprecated before 1.1 - use {@link #remove(int)} or {@link #removeAll(String)} instead. */
		@Deprecated
		boolean remove(final String name);

		/** removes <em>all</em> attributes with this name.
		 * @return true on removal of an existing attribute and false otherwise. */
		boolean removeAll(final String name);

		/** removes the attribute at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
		void remove(final int index);

		/** adds an attribute if there is no attribute with the given name or changes
		 * the value <em>of the first</em> attribute with the given name. */
		void set(final String name, final Object value);

		/** adds an attribute no matter if an attribute with the given name already exists. */
		void add(final String name, final Object value);

		/** removes all attributes.
		 * @since 1.2 */
		void clear();

		/** allows application of Groovy collection methods like each(), collect(), ...
		 * <pre>
		 *   def keyList = node.attributes.collect { it.key }
         *   def values = node.attributes.collect { it.value }
         *   node.attributes.each {
         *       if (it.key =~ /.*day/)
         *           it.value += ' days'
         *   }
		 * </pre>
		 * @since 1.3.2 */
		Iterator<java.util.Map.Entry<String, Object>> iterator();

		/** optimize widths of attribute view columns according to contents. 
		 * @since 1.4 */
		void optimizeWidths();
	}

    /** Here are four ways to enable a cloud on the current node and switch it off again:
     * <pre>
     *   node.cloud.enabled = true
     *   node.cloud.enabled = false 
     *   
     *   node.cloud.shape = 'ROUND_RECT' // either 'ARC', 'STAR', 'RECT' or 'ROUND_RECT'
     *   node.cloud.shape = null
     *   
     *   node.cloud.color = java.awt.Color.YELLOW
     *   node.cloud.color = null
     *   
     *   node.cloud.colorCode = '#00FF66'
     *   node.cloud.color = null
     * </pre>
     * @since 1.3 */
    interface Cloud {
        /**  @since 1.3 */
        boolean getEnabled();
        /**  @since 1.3 */
        void setEnabled(boolean enable);

        /** @return either null (if cloud is not enabled), "ARC", "STAR", "RECT" or "ROUND_RECT".
         *  @since 1.3 */
        String getShape();
        /** @param shape use "ARC", "STAR", "RECT" or "ROUND_RECT". null removes the cloud
         *  @since 1.3 */
        void setShape(String shape);

        /** @return either null (if cloud is not enabled) or the current cloud color.
         * @since 1.3 */
        Color getColor();
        /** @since 1.3 */
        void setColor(Color color);

        /** @return either null (if cloud is not enabled) or a HTML color spec.
         *  @since 1.3 */
        String getColorCode();
        /** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
         *  @since 1.3 */
        void setColorCode(String rgbString);
    }
    
    /** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
	 * - read-only. */
	interface ConnectorRO {
        /** returns one of LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE.
         *  @since 1.3 */
	    String getShape();

	    Color getColor();

		String getColorCode();

        /**  @since 1.2 */
		boolean hasEndArrow();

		/**@deprecated since 1.2 - use {@link #hasEndArrow()} instead */
		ArrowType getEndArrow();

		String getMiddleLabel();

		/** The node without the arrow. On connectors with arrows at both ends one of the ends. */
		Node getSource();

		String getSourceLabel();

        /** @since 1.2 */
		boolean hasStartArrow();
		
		/** @deprecated since 1.2 - use {@link #hasStartArrow()} instead */
		ArrowType getStartArrow();
		
		/** The node with the arrow. On connectors with arrows at both ends one of the ends. */
		Node getTarget();

		String getTargetLabel();

		boolean simulatesEdge();

		/** returns a Point.
		 * @since 1.3.3 */
		List<Integer> getStartInclination();
		
		/** returns a Point.
		 * @since 1.3.3 */
		List<Integer> getEndInclination();
	}

	/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
	 * - read-write. */
	interface Connector extends ConnectorRO {
        /** @param shape one of LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE.
         *  @since 1.3 */
        void setShape(String shape);

        void setColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setColorCode(String rgbString);

		/** @since 1.2 */
		void setEndArrow(boolean showArrow);

        /** @deprecated since 1.2 - use {@link #setEndArrow(boolean)} instead */
		void setEndArrow(ArrowType arrowType);

		void setMiddleLabel(String label);

		void setSimulatesEdge(boolean simulatesEdge);

		void setSourceLabel(String label);

        /** @since 1.2 */
        void setStartArrow(boolean showArrow);

        /** @deprecated since 1.2 - use {@link #setStartArrow(boolean)} instead */
		void setStartArrow(ArrowType arrowType);

		void setTargetLabel(String label);

        /** startPoint, endPoint: list of two integers representing a Point.
         * @since 1.3.3 */
        void setInclination(final List<Integer> startPoint, final List<Integer> endPoint);
	}

	/** Access to global state: in scripts, this is available as global variable <code>c</code> - read-only. */
	interface ControllerRO {
		/** if multiple nodes are selected returns one (arbitrarily chosen)
		 * selected node or the selected node for a single node selection. */
		Node getSelected();

		/** A read-only list of selected nodes. That is you cannot select a node by adding it to the returned list. */
		List<Node> getSelecteds();

		/** returns {@code List<Node>} sorted by the node's vertical position.
		 *
		 * @param differentSubtrees if true
		 *   children/grandchildren/grandgrandchildren/... nodes of selected
		 *   parent nodes are excluded from the result. */
		List<Node> getSortedSelection(boolean differentSubtrees);

		/**
		 * returns Freeplane version.
		 * Use it like this:
		 * <pre>{@code
		 *   import org.freeplane.core.util.FreeplaneVersion
		 *   import org.freeplane.core.ui.components.UITools
		 * 
		 *   def required = FreeplaneVersion.getVersion("1.1.2");
		 *   if (c.freeplaneVersion < required)
		 *       UITools.errorMessage("Freeplane version " + c.freeplaneVersion
		 *           + " not supported - update to at least " + required);
		 * }</pre>
		 */
		FreeplaneVersion getFreeplaneVersion();

		/** returns the directory where user settings, logfiles, templates etc. are stored.
		 * @since 1.2 */
		File getUserDirectory();

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/**
		 * Starting from the root node, recursively searches for nodes (in breadth-first sequence) for which
		 * <code>closure.call(node)</code> returns true.
		 * <p>
		 * A find method that uses a Groovy closure ("block") for simple custom searches. As this closure
		 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
		 * evaluate every node property, like attributes, icons, node text or notes.
		 * <p>
		 * Examples:
		 * <pre>
		 *    def nodesWithNotes = c.find{ it.noteText != null }
		 *    
		 *    def matchingNodes = c.find{ it.text.matches(".*\\d.*") }
		 *    def texts = matchingNodes.collect{ it.text }
		 *    print "node texts containing numbers:\n " + texts.join("\n ")
		 * </pre>
		 * See {@link Node#find(Closure)} for searches on subtrees.
		 * @param closure a Groovy closure that returns a boolean value. The closure will receive
		 *        a NodeModel as an argument which can be tested for a match.
		 * @return all nodes for which <code>closure.call(NodeModel)</code> returns true.
		 */
		List<Node> find(Closure<Boolean> closure);

		/**
		 * Returns all nodes of the map in breadth-first order, that is, for the following map,
		 * <pre>
		 *  1
		 *    1.1
		 *      1.1.1
		 *      1.1.2
		 *    1.2
		 *  2
		 * </pre>
		 * [1, 1.1, 1.1.1, 1.1.2, 1.2, 2] is returned.
		 * See {@link Node#find(Closure)} for searches on subtrees.
		 * @see #findAllDepthFirst()
		 * @since 1.2 */
		List<Node> findAll();

		/**
		 * Returns all nodes of the map in depth-first order, that is, for the following map,
		 * <pre>
		 *  1
		 *    1.1
		 *      1.1.1
		 *      1.1.2
		 *    1.2
		 *  2
		 * </pre>
		 * [1.1.1, 1.1.2, 1.1, 1.2, 1, 2] is returned.
		 * See {@link Node#findAllDepthFirst()} for subtrees.
		 * @since 1.2 */
		List<Node> findAllDepthFirst();

		/** returns the current zoom factor. A value of 1 means 100%.
		 * @since 1.2 */
		float getZoom();
		
		/** returns false if the system 'nonInteractive' is set. This can be used in actions to not open dialogs etc.
		 * @since 1.2 */
		boolean isInteractive();

		/** returns a list of export type descriptions that can be used to specify a specific export type
		 * in {@link #export(Map, File, String, boolean)}. These descriptions are internationalized.
		 * @since 1.3.5 */
		List<String> getExportTypeDescriptions();

        /** exports map to destination file, example:
         * <pre>
         *   println c.exportTypeDescriptions.join('\n')
         *   boolean overwriteExistingFile = true
         *   c.export(node.map, new File('/tmp/t.png'), 'Portable Network Graphic (PNG) (.png)', overwriteExistingFile)
         * </pre>
         * @param exportTypeDescription Use {@link #getExportTypeDescriptions()} to look up available exportTypes.
         *   Note that the file format does not suffice to specify a specific export since there may be more than
         *   one, as for HTML.
         * @since 1.3.5 */
        void export(Map map, File destinationFile, String exportTypeDescription, boolean overwriteExisting);
	}

	/** Access to global state: in scripts, this is available as global variable <code>c</code> - read-write. */
	interface Controller extends ControllerRO {
		void centerOnNode(Node center);

		/** Starts editing node, normally in the inline editor. Does not block until edit has finished.
		 * @since 1.2.2 */
		void edit(Node node);

		/** opens the appropriate popup text editor. Does not block until edit has finished.
		 * @since 1.2.2 */
		void editInPopup(Node node);
		
		void select(Node toSelect);
		
		/** selects multiple Nodes.
		 * @since 1.4 */
		void select(Collection<Node> toSelect);

		/** selects branchRoot and all children */
		void selectBranch(Node branchRoot);

		/** same as {@link #select(Collection)} */
		void selectMultipleNodes(Collection<Node> toSelect);

		/** reset undo / redo lists and deactivate Undo for current script */
		void deactivateUndo();

		/** invokes undo once - for testing purposes mainly.
		 * @since 1.2 */
		void undo();

		/** invokes redo once - for testing purposes mainly.
		 * @since 1.2 */
		void redo();

		/** The main info for the status line with key="standard", use null to remove. Removes icon if there is one. */
		void setStatusInfo(String info);

		/** Info for status line, null to remove. Removes icon if there is one.
		 * @see #setStatusInfo(String, String, String) */
		void setStatusInfo(String infoPanelKey, String info);

		/** Info for status line - text and icon - null stands for "remove" (text or icon)
		 * @param infoPanelKey "standard" is the left most standard info panel. If a panel with
		 *        this name doesn't exist it will be created.
		 * @param info Info text
		 * @param iconKey key as those that are used for nodes (see {@link Icons#addIcon(String)}).
		 * <pre>
		 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
		 *   c.setStatusInfo("standard", "hi there!", "button_ok");
		 * </pre>
		 * @see FreeplaneIconUtils
		 * @since 1.2 */
		void setStatusInfo(String infoPanelKey, String info, String iconKey);

		/** @deprecated since 1.2 - use {@link #setStatusInfo(String, String, String)} */
		void setStatusInfo(String infoPanelKey, Icon icon);

		/** opens a new map with a default name in the foreground.
		 * @since 1.2 */
		Map newMap();

		/** opens a new map for url in the foreground if it isn't opened already.
		 * @since 1.2 */
		Map newMap(URL url);
		
		/** opens a new map based on given template.
		 * @since 1.5 */
		public Map newMapFromTemplate(File templateFile);

		/** a value of 1 means 100%.
		 * @since 1.2 */
		void setZoom(final float ratio);

		/** a list of all opened maps.
		 * @since 1.5 */
		List<Map> getOpenMaps();
	}

	/** Edge to parent node: <code>node.style.edge</code> - read-only. */
	interface EdgeRO {
		Color getColor();

		String getColorCode();

		EdgeStyle getType();

		int getWidth();
	}

	/** Edge to parent node: <code>node.style.edge</code> - read-write. */
	interface Edge extends EdgeRO {
		void setColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setColorCode(String rgbString);

		void setType(EdgeStyle type);

		/** can be -1 for default, 0 for thin, &gt;0 */
		void setWidth(int width);
	}

	/** External object: <code>node.externalObject</code> - read-only. */
	interface ExternalObjectRO {
		/** returns the object's uri if set or null otherwise.
		 * @since 1.2 */
		String getUri();

		/** returns the current zoom level as ratio, i.e. 1.0 is returned for 100%.
		 * If there is no external object 1.0 is returned. */
		float getZoom();
		
		/** @deprecated since 1.2 - use {@link #getUri()} instead. */
		String getURI();
	}

	/** External object: <code>node.externalObject</code> - read-write. */
	interface ExternalObject extends ExternalObjectRO {
        /** setting null uri means remove external object.
         * Starting with Freeplane 1.2.23 there is an additional setUri(Object) method that also accepts File,
         * URI and URL arguments.
         * @since 1.2 */
		void setUri(String target);
		
		/** setting null uri means remove external object. */
		void setFile(File target);
		
		/** set to 1.0 to set it to 100%. If the node has no object assigned this method does nothing. */
		void setZoom(float zoom);
		
		/** @deprecated since 1.2 - use {@link #setUri(String)} instead. */
		void setURI(String uri);
	}

	/** Node's font: <code>node.style.font</code> - read-only. */
	interface FontRO {
		String getName();

		int getSize();

		boolean isBold();

		boolean isBoldSet();

		boolean isItalic();

		boolean isItalicSet();

		boolean isNameSet();

		boolean isSizeSet();
	}

	/** Node's font: <code>node.style.font</code> - read-write. */
	interface Font extends FontRO {
		void resetBold();

		void resetItalic();

		void resetName();

		void resetSize();

		void setBold(boolean bold);

		void setItalic(boolean italic);

		void setName(String name);

		void setSize(int size);
	}

	/** Node's icons: <code>node.icons</code> - read-only. */
	interface IconsRO {
		/** returns the name of the icon at the given index (starting at 0) or null if {@code index >= size}.
		 * Use it like this: <pre>
		 *   def secondIconName = node.icons[1]
		 * </pre>
		 * @since 1.2 */
		String getAt(int index);

		/** returns the name of the first icon if the node has an icon assigned or null otherwise. Equivalent: <code>node.icons[0]</code>.
		 * @since 1.2 */
		String getFirst();

		/** returns true if the node has an icon of this name.
		 * @since 1.2 */
		boolean contains(String name);

		/** returns the number of icons the node has.
		 * @since 1.2 */
		int size();

		/** returns a read-only list of the names of the icons the node has. Think twice before you use this method
		 * since it leads to ugly code, e.g. use <code>node.icons.first</code> or <code>node.icons[0]</code> instead of
		 * <code>node.icons.icons[0]</code>. Perhaps you could also use iteration over icons, see. */
		List<String> getIcons();
		
		/** returns a list of the urls of the icons the node has. */
		List<URL> getUrls();

        /** allows application of Groovy collection methods like each(), collect(), ...
         * <pre>
         *   def freeIcons = node.icons.findAll { it.startsWith('free') }
         * </pre>
         * @since 1.3.2 */
        Iterator<String> iterator();
	}

	/** Node's icons: <code>node.icons</code> - read-write. */
	interface Icons extends IconsRO {
		/**
		 * adds an icon to a node if an icon for the given key can be found. The same icon can be added multiple
		 * times.
		 * <pre>
		 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
		 *   node.icons.addIcon("button_ok")
		 * </pre>
		 * @see FreeplaneIconUtils */
		void add(String name);

		/** @since 1.4 */
		void addAll(Collection<String> names);

		/** @since 1.4 */
		void addAll(IconsRO icons);

		/** @deprecated since 1.2 - use {@link #add(String)} instead. */
		void addIcon(String name);

		/** deletes the icon at the given index, returns true if success (icon existed). */
		boolean remove(int index);
		
		/** deletes first occurence of icon with the given name, returns true if success (icon existed). */
		boolean remove(String name);

		/** @deprecated since 1.2 - use {@link #remove(String)} instead. */
		boolean removeIcon(String name);
		
		/** removes all icons.
		 * @since 1.2 */
		void clear();
	}

	/** Node's link: <code>node.link</code> - read-only.
	 * <p>
	 * None of the getters will throw an exception, even if you call, e.g. getNode() on a File link.
	 * Instead they will return null. To check the link type evaluate getUri().getScheme() or the result
	 * of the special getters.*/
	interface LinkRO {
		/** returns the link text, a stringified URI, if a link is defined and null otherwise.
		 * @since 1.2 */
		String getText();

		/** returns the link as URI if defined and null otherwise. Won't throw an exception.
		 * @since 1.2 */
		URI getUri();

		/** returns the link as File if defined and if the link target is a valid File URI and null otherwise.
		 * @see File#File(URI)
		 * @since 1.2 */
		File getFile();

		/** returns the link as Node if defined and if the link target is a valid local link to a node and null otherwise.
		 * @since 1.2 */
		Node getNode();

		/** @deprecated since 1.2 - use {@link #getText()} instead. */
		String get();
	}

	/** Node's link: <code>node.link</code> - read-write.
	 * To set links use the attributes of the {@link Link} and {@link LinkRO} object:
	 * <pre>
	 * // a normal href 
	 * node.link.text = 'http://www.google.com'
	 * // create a node to the parent node
	 * node.link.node = node.parent
	 * // if you have a URI object
	 * node.link.uri = new URI('http://www.google.com')
	 * // file
	 * node.link.file = map.file
	 * </pre>
	 */
	interface Link extends LinkRO {
		/** target is a stringified URI. Removes any link if uri is null.
		 * To get a local link (i.e. to another node) target should be: "#" + nodeId or better use setNode(Node).
		 * @throws IllegalArgumentException if target is not convertible into a {@link URI}.
		 * @since 1.2 */
		void setText(String target);

		/** sets target to uri. Removes any link if uri is null.
		 * @since 1.2 */
		void setUri(URI uri);

		/** sets target to file. Removes any link if file is null.
		 * @since 1.2 */
		void setFile(File file);

		/** target is a node of the same map. Shortcut for setTarget("#" + node.nodeId)
		 * Removes any link if node is null.
		 * @throws IllegalArgumentException if node belongs to another map.
		 * @since 1.2 */
		void setNode(Node node);

		/** @deprecated since 1.2 - use {@link #setText(String)} instead.
		 * @return true if target could be converted to an URI and false otherwise. */
		boolean set(String target);

		/** removes the link. Same as <code>node.link.text = null</code>.
		 * @return <tt>true</tt> if there was a link to remove.
		 * @since 1.4 */
		boolean remove();
	}

	/** The map a node belongs to: <code>node.map</code> - read-only. */
	interface MapRO {
		/** @since 1.2 */
		Node getRoot();

		/** @deprecated since 1.2 - use {@link #getRoot()} instead. */
		Node getRootNode();

		/** get node by id.
		 * @return the node if the map contains it or null otherwise. */
		Node node(String id);

		/** returns the filenname of the map as a java.io.File object if available or null otherwise. */
		File getFile();

		/** returns the title of the MapView.
		 * @since 1.2 */
		String getName();

		/** @since 1.2 */
		boolean isSaved();

        /** @since 1.2 */
        Color getBackgroundColor();

        /** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
         *  @since 1.2 */
        String getBackgroundColorCode();
	}

	/** The map a node belongs to: <code>node.map</code> - read-write. */
	interface Map extends MapRO {
		/**
		 * closes a map. Note that there is <em>no undo</em> for this method!
		 * @param force close map even if there are unsaved changes.
		 * @param allowInteraction {@code if (allowInteraction && ! force)} a saveAs dialog will be opened if there are
		 *        unsaved changes.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map contains changes and parameter force is false.
		 * @since 1.2
		 */
		boolean close(boolean force, boolean allowInteraction);

		/**
		 * saves the map to disk. Note that there is <em>no undo</em> for this method.
		 * @param allowInteraction if a saveAs dialog should be opened if the map has no assigned URL so far.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
		 * @since 1.2
		 */
		boolean save(boolean allowInteraction);

		/** @since 1.2 */
		void setSaved(boolean isSaved);

		/** Sets the map (frame/tab) title. Note that there is <em>no undo</em> for this method!
		 * @since 1.2 */
		void setName(String title);

        /** @since 1.2 */
        void setBackgroundColor(Color color);

        /** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
         *  @since 1.2 */
        void setBackgroundColorCode(String rgbString);
		
		/** install a Groovy closure as the current filter in this map. If <code>closure</code> is null then filtering will
		 * be disabled. The filter state of a node can be checked by {@link Node#isVisible()}. <br>
		 * To undo filtering use <em>Tools &rarr; Undo</em>. After execution of the following you have to use it seven times to
		 * return to the initial filter state.
		 * <pre>
		 * // show only matching nodes
		 * node.map.filter{ it.text.contains("todo") }
		 * // equivalent:
		 * node.map.filter = { it.text.contains("todo") }
		 * 
		 * // show ancestors of matching nodes
		 * node.map.filter(true, false){ it.text.contains("todo") }
		 * // equivalent:
		 * node.map.setFilter(true, false, { it.text.contains("todo") })
		 * 
		 * // show descendants of matching nodes
		 * node.map.filter(false, true){ it.text.contains("todo") }
		 * // equivalent:
		 * node.map.setFilter(false, true, { it.text.contains("todo") })
		 * 
		 * // remove filter
		 * node.map.filter = null
		 * </pre>
		 * @since 1.2 */
		public void filter(final Closure<Boolean> closure);

		/** alias for {@link #filter(Closure)}. Enables assignment to the <code>filter</code> property.
		 * @since 1.2 */
		public void setFilter(final Closure<Boolean> closure);
		
		/** With {@link #filter(Closure)} neither ancestors not descendants of the visible nodes are shown. Use this
		 * method to control these options.
		 * @see #filter(Closure)
		 * @since 1.2 */
		public void filter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure);

		/** alias for {@link #setFilter(boolean, boolean, Closure)}
		 * @see #filter(Closure)
		 * @since 1.2 */
		public void setFilter(final boolean showAncestors, final boolean showDescendants, final Closure<Boolean> closure);

		/** reinstalls the previously undone filter if there is any.
		 * Note: undo/redo for filters is separate to the undo/redo for other map state.
		*  @since 1.2 */
		public void redoFilter();

		/** removes the current filter and reinstalls the previous filter if there is any.
		 * Note: undo/redo for filters is separate to the undo/redo for other map state.
		 *  @since 1.2 */
		public void undoFilter();

		/** returns an accessor to the map specific storage. The value is never null
		 *  @since 1.3.6 */
		public Proxy.Properties getStorage();
	}

	/** The currently selected node: <code>node</code> - read-only. */
	interface NodeRO {
		Attributes getAttributes();

		/** allows to access attribute values like array elements. Note that the returned type is a
		 * {@link Convertible}, not a String. Nevertheless it behaves like a String in almost all respects,
		 * that is, in Groovy scripts it understands all String methods like lenght(), matches() etc.
		 * <pre>
		 *   // standard way
		 *   node.attributes.set("attribute name", "12")
		 *   // implicitely use getAt()
		 *   def val = node["attribute name"]
		 *   // use all conversions that Convertible provides (num, date, string, ...)
		 *   assert val.num == new Long(12)
		 *   // or use it just like a string
		 *   assert val.startsWith("1")
		 *   // check for availability of an attribute this way:
		 *   if (node["unknown attribute"])
		 *      // surprise: the node has an attribute with key "unknown attribute"
		 * </pre>
		 * @throws ExecuteScriptException 
		 * @since 1.2
		 */
		Convertible getAt(String attributeName);

		/** a reference to an accessor object for cloud properties of this node. This property is never null.
		 * @since 1.2
		 */
		Cloud getCloud();

        /** returns the index (0..) of this node in the (by Y coordinate sorted)
		 * list of this node's children. Returns -1 if childNode is not a child
		 * of this node. */
		int getChildPosition(Node childNode);

		/** returns the children of this node ordered by Y coordinate. */
		List<Node> getChildren();

		Collection<Connector> getConnectorsIn();

		Collection<Connector> getConnectorsOut();

		/** returns the raw HTML text of the details if there is any or null otherwise.
		 * @since 1.2 */
		String getDetailsText();

		/** returns the text of the details as a Convertible like {@link #getNote()} for notes:
		 * <ul>
		 * <li>node.details.to.plain plain text of the node, possibly after formula evaluation
		 * <li>node.details.plain the same.
		 * <li>node.details.string the same.
		 * <li>node.details.html the raw html text. No formula evaluation is applied.
		 * <li>node.details.text the same.
		 * </ul>
		 * @since 1.2 */
		Convertible getDetails();

		/** returns true if node details are hidden.
		 * @since 1.2 */
		boolean getHideDetails();

		ExternalObject getExternalObject();

		/** a reference to an accessor object for icons of this node. This property is never null. */
		Icons getIcons();

		/** a reference to an accessor object for link properties of this node. This property is never null. */
		Link getLink();

		/** use it to create and inspect {@link Reminder}s. This property is never null. */
		Reminder getReminder();

		/** the map this node belongs to. */
		Map getMap();

		/** @deprecated since 1.2 - use Node.getId() instead. */
		String getNodeID();

		/** @since 1.2 */
		String getId();

		/** if countHidden is false then only nodes that are matched by the
		 * current filter are counted. */
		int getNodeLevel(boolean countHidden);

		/** returns the text of the details as a Convertible. Convertibles behave like Strings in most respects.
		 * Additionally String methods are overridden to handle Convertible arguments as if the argument were the
		 * result of Convertible.getText().
         * <ul>
         * <li>node.note.to.plain plain text of the node, possibly after formula evaluation
         * <li>node.note.plain the same.
         * <li>node.note.text the same.
         * <li>node.note.html the raw html text. No formula evaluation is applied.
         * <li>node.note.string the same.
         * </ul>
		 * @return Convertible getString(), getText() and toString() will return plain text instead of the HTML.
		 *         Use {@link #getNoteText()} to get the HTML text.
		 * @throws ExecuteScriptException 
		 * @since 1.2
		 */
		Convertible getNote();

		/** Returns the HTML text of the node. (Notes always contain HTML text.) 
		 * @throws ExecuteScriptException */
		String getNoteText();

		/** @since 1.2 */
		Node getParent();

		/** @deprecated since 1.2 - use {@link #getParent()} instead. */
		Node getParentNode();

        /** a list of all nodes starting from this node upto (and including) the root node.
         * <pre>
         *   def path = pathToRoot.collect{ it.plainText }.join('.')
         * </pre>
         * @since 1.3.3 */
        List<Node> getPathToRoot();

        /** returns the next node with respect to this node in depth-first order.
         * Returns null if this node is the only one in the map. */
        Node getNext();

        /** returns the previous node with respect to this node in depth-first order.
         * Returns null if this node is the only one in the map. */
        Node getPrevious();

		/** The style attributes of a node can either be changed by assigning a named style like this:
		 * <pre>node.style.name = 'style.ok'</pre>
		 * or by changing attributes for this node individually like this:
		 * <pre>node.style.textColorCode = '#FF0000'</pre>
		 * Conditional styles of a node can only be investigated by {@link Node#hasStyle(String)}. Here a script that
		 * creates an index of all nodes having the style 'todo':
		 * <pre>
		 * def todos = node.map.root.createChild('To Do')
		 * c.find{ it.hasStyle('todo') }.each {
		 *     def child = todos.createChild(it.text)
		 *     child.link.node = it
		 * }
		 * </pre> */
		NodeStyle getStyle();

		/** returns true if the node has the style of this name - either manually set or as a conditional style or it is
		 * "default" which all nodes have. The following statement will always be true:
		 * @since 1.2 */
		boolean hasStyle(String styleName);

		/** Raw text of this node which might be plain or HTML text.
		 * Possible transformations (formula evaluation, formatting, ...) are not applied.
		 * <p>
		 * See
		 * <ul>
		 * <li> {@link #getPlainText()} for plain text or use {@link HtmlUtils#htmlToPlain(String)}.
		 * <li> {@link #getHtmlText()} for HTML text or use {@link HtmlUtils#plainToHTML(String)}.
		 * <li> {@link #getTransformedText()} or {@link #getValue()} for text after formula evaluation.
		 * <li> {@link #getObject()} for possible typed content.
		 * <li> {@link #getTo()} for text/object conversions.
		 * </ul>
		 * @since 1.2 */
		String getText();

		/** Plain text after removal of possible HTML markup.
		 * Possible transformations (formula evaluation, formatting, ...) are not applied.
		 * @since 1.2 */
		String getPlainText();

		/** Plain text after removal of possible HTML markup. Formulas are not evaluated.
		 * @deprecated since 1.2 - use getPlainText() or getTo().getPlain() instead. */
		String getPlainTextContent();

		/** Plain text after removal of possible HTML markup.
		 * Possible transformations (formula evaluation, formatting, ...) are not applied.
		 * @since 1.2 */
		String getHtmlText();

		/** Plain or HTML text of this node after possible transformation (formula evaluation, formatting, ...).
		 * @since 1.2 */
		String getTransformedText();

		/** Plain or HTML text of this node after possible transformation (formula evaluation, formatting, ...)
		 * and after text shortening.
		 * See {@link #isMinimized()} for node shortening.
		 * @since 1.2 */
		String getDisplayedText();
		
		/** Plain text of this node after possible transformation and forced text shortening.
		 * @since 1.2 */
		String getShortText();

		/** The object that's displayed as the node text - normally the raw text of this node (then this method is
		 * equivalent to {@link #getText()}).
		 * But in case of typed content (for numbers, dates and calendars) {@link #getObject()} returns
		 * a proper {@link IFormattedObject}. Use {@link #getPlainText()} to remove HTML.
		 * See {@link Node#setObject(Object)} for details.
		 * @since 1.2 */
		Object getObject();

		/** returns the format string of the formatter if available and null otherwise.
		 * @since 1.2 */
		String getFormat();

		/**
		 * returns an object that performs conversions (method name is choosen to give descriptive code):
		 * <dl>
		 * <dt>node.to.num <dd>Long or Double, see {@link Convertible#getDate()}.
		 * <dt>node.to.date <dd>Date, see {@link Convertible#getDate()}.
		 * <dt>node.to.string <dd>Text, see {@link Convertible#getString()}.
		 * <dt>node.to.text <dd>an alias for getString(), see {@link Convertible#getText()}.
		 * <dt>node.to.object <dd>returns what fits best, see {@link Convertible#getObject()}.
		 * </dl>
		 * @return ConvertibleObject
		 * @throws ExecuteScriptException on formula evaluation errors
		 * @since 1.2
		 */
		Convertible getTo();

		/** an alias for {@link #getTo()}.
		 * @throws ExecuteScriptException on formula evaluation errors
		 * @since 1.2 */
		Convertible getValue();

		/** Returns a <a href="http://www.freesoft.org/CIE/RFC/1521/7.htm">BASE64</a> encoded node text
		 * (see {@link Node#setBinary(byte[])}) as a binary object. Errors are signaled by a null return value.
		 * Whitespace characters are ignored.<br>
		 * Note that this method is not able to catch all encoding errors!
		 * @since 1.2 */
		byte[] getBinary();

		/** returns true if p is a parent, or grandparent, ... of this node, or if it <em>is equal</em>
		 * to this node; returns false otherwise. */
		boolean isDescendantOf(Node p);

		/** if this node is folded. Note that the folding state only concerns the visibility of the <em>child nodes</em>. */
		boolean isFolded();

		/** returns true if this node is freely positionable.
		 * @since 1.2 */
		public boolean isFree();

		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		/** if this node is visible or not (due to filtering). Node folding is not considered.
		 * See {@link #isFolded()} for folding state. */
		boolean isVisible();
		
		/** if this node's text is shortened for display. */
		boolean isMinimized();

		/** The count of node sharing their content with this node. Use {@code if (node.countNodesSharingContent() > 0)}
		 * to check if a node has any clones.
		 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
		 * @return 0 if this node is standalone or the number of other nodes sharing content otherwise. 
		 * @see #getNodesSharingContent()
		 * @see Proxy.Node#appendAsCloneWithSubtree(Proxy.NodeRO)
		 * @see Proxy.Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
		 * @since 1.5 */
		int getCountNodesSharingContent();

		/** The count of nodes sharing their content and subtree with this node.
		 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
		 * @return 0 if this node has no other nodes it is sharing its content and subtree with or its count otherwise. 
		 * @see #getNodesSharingContentAndSubtree()
		 * @see Proxy.Node#appendAsCloneWithSubtree(Proxy.NodeRO)
		 * @see Proxy.Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
		 * @since 1.5 */
		int getCountNodesSharingContentAndSubtree();
		
		/** The count of nodes sharing their content with this node.
		 * <br><em>Note:</em> {@link #getCountNodesSharingContent()} &ge; {@link #getCountNodesSharingContentAndSubtree()}.
		 * @return 0 if this node is standalone or the number of other nodes sharing content otherwise. 
		 * @see #getCountNodesSharingContent()
		 * @see Proxy.Node#appendAsCloneWithSubtree(Proxy.NodeRO)
		 * @see Proxy.Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
		 * @since 1.5 */
		List<Node> getNodesSharingContent();
		
		/** The nodes sharing their content and subtree with this node.
		 * @return 0 if this node has no other nodes it is sharing its content and subtree with or its count otherwise. 
		 * @see #getCountNodesSharingContentAndSubtree()
		 * @see Proxy.Node#appendAsCloneWithSubtree(Proxy.NodeRO)
		 * @see Proxy.Node#appendAsCloneWithoutSubtree(Proxy.NodeRO)
		 * @since 1.5 */
		List<Node> getNodesSharingContentAndSubtree();

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true. See {@link Controller#find(Closure)} for details. */
		List<Node> find(Closure<Boolean> closure);

		/** Returns all nodes of the branch that starts with this node in breadth-first order.
		 * See {@link Controller#findAll()} for map-global searches.
		 * @since 1.2 */
		List<Node> findAll();
		
		/** Returns all nodes of the branch that starts with this node in depth-first order.
		 * See {@link Controller#findAllDepthFirst()} for map-global searches.
		 * @since 1.2 */
		List<Node> findAllDepthFirst();

		Date getLastModifiedAt();

		Date getCreatedAt();

        /**@since 1.3.7 */
		int getHorizontalShift();

        /**@since 1.3.7 */
		int getVerticalShift();

        /**@since 1.3.7 */
		int getMinimalDistanceBetweenChildren();

	}

	/** The currently selected node: <code>node</code> - read-write. */
	interface Node extends NodeRO {
		/** adds a new Connector to the given target node and returns the new
		 * connector for optional further editing (style); also enlists the
		 * Connector on the target Node object. */
		Connector addConnectorTo(Node target);

		/** as above, using String targetNodeId instead of Node object to establish the connector. */
		Connector addConnectorTo(String targetNodeId);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff inserts as last child. */
		Node createChild();

		/** like {@link #createChild()} but sets the node text to the given text.
		 * <pre>
		 * // instead of 
		 * def child = node.createChild(); child.setObject(value);
		 * // use
		 * def child = node.createChild(value);
		 * </pre>
		 * @since 1.2 */
		Node createChild(Object value);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff */
		Node createChild(int position);

		/** inserts a copy of node as a new child.
		 * @since 1.2 */
		Node appendChild(NodeRO node);

		/** inserts a copy of the branch starting with node as a new child branch.
		 * @since 1.2 */
		Node appendBranch(NodeRO node);
		
		/** inserts the node as a clone of toBeCloned <em>including</em> its current and/or future
		 * subtree. That is all changes of descendent nodes of toBeCloned are reflected in the subtree
		 * of the new node <em>and vice versa</em>.
		 * <br><em>Note:</em> Cloning works symmetrically so we could better speak of two
		 * shared nodes instead of clone and cloned since none of both is privileged.
		 * @return the new child node
		 * @throws IllegalArgumentException if
		 *     a) this node (the to-be-parent) is contained in the subtree of toBeCloned,  
		 *     b) toBeCloned is the root node,
		 *     c) toBeCloned comes from a different map.
		 * @since 1.5 */
		Node appendAsCloneWithSubtree(NodeRO toBeCloned);
		
		/** inserts the node as a clone of toBeCloned <em>without</em> its current and/or future
		 * subtree. That is toBeCloned and the new node have children of their own. 
		 * <br><em>Note:</em> Cloning works symmetrically so we could better speak of two
		 * shared nodes instead of clone and cloned since none of both is privileged.
		 * @return the new child node
		 * @throws IllegalArgumentException if
		 *     a) this node (the to-be-parent) is contained in the subtree of toBeCloned,  
		 *     b) toBeCloned is the root node,
		 *     c) toBeCloned comes from a different map.
		 * @since 1.5 */
		Node appendAsCloneWithoutSubtree(NodeRO toBeCloned);

		/** inserts the node(s) copied from clipboard as clone(s). Errors like
		 * if the clipboard doesn't contain proper content will only be reported to the log.
		 * You should prefer {@link #appendAsCloneWithSubtree(Proxy.NodeRO)} or {@link #appendAsCloneWithoutSubtree(Proxy.NodeRO)}
		 * instead if possible - they give you more control.
		 * @since 1.5 */
		void pasteAsClone();

		void delete();

		void moveTo(Node parentNode);

		void moveTo(Node parentNode, int position);

		/** removes the given connector on both sides. */
		void removeConnector(Connector connectorToBeRemoved);

		/**
		 * A node's text is String valued. This methods provides automatic conversion to String in the same way as
		 * for {@link #setText(Object)}, that is special conversion is provided for dates and calendars, other
		 * types are converted via value.toString().
		 * 
		 * If the conversion result is not valid HTML it will be automatically converted to HTML.
		 * 
		 * @param details An object for conversion to String. Use null to unset the details. Works well for all types
		 *        that {@link Convertible} handles, particularly {@link Convertible}s itself.
		 * @since 1.2
		 */
		void setDetails(Object details);

        /** Sets the raw (HTML) note text. */
        void setDetailsText(String html);

		/** use node.hideDetails = true/false to control visibility of details.
		 * @since 1.2 */
		void setHideDetails(boolean hide);

		void setFolded(boolean folded);

		/** set to true if this node should be freely positionable:
		 * <pre>
		 *   node.free = true
		 *   node.style.floating = true
		 * </pre>
         * @since 1.2 */
        void setFree(boolean free);

        void setMinimized(boolean shortened);

		/**
		 * Set the note text:
		 * <ul>
		 * <li>This methods provides automatic conversion to String in a way that node.getNote().getXyz()
		 *     methods will be able to convert the string properly to the wanted type.
		 * <li>Special conversion is provided for dates and calendars: They will be converted in a way that
		 *     node.note.date and node.note.calendar will work. All other types are converted via value.toString().
		 * <li>If the conversion result is not valid HTML it will be automatically converted to HTML.
		 * </ul>
		 * <p>
		 * <pre>{@code
		 *   // converts numbers and other stuff with toString()
		 *   node.note = 1.2
		 *   assert node.note.text == "<html><body><p>1.2"
		 *   assert node.note.plain == "1.2"
		 *   assert node.note.num == 1.2d
		 *   // == dates
		 *   // a date in some non-UTC time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   // - the missing end tags don't matter for rendering
		 *   node.note = date
		 *   assert node.note == "<html><body><p>1970-01-01T02:00:00.000+0000"
		 *   assert node.note.plain == "1970-01-01T02:00:00.000+0000"
		 *   assert node.note.date == date
		 *   // == remove note
		 *   node.note = null
		 *   assert node.note.text == null
		 * }</pre>
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself.
		 * @since 1.2 (note that the old setNoteText() did not support non-String arguments.
		 */
		void setNote(Object value);

		/** Sets the raw (HTML) note text. */
		void setNoteText(String html);

		/** If <code>value</code> is a String the node object is set to it verbatim. For all other argument types it's
		 * an alias for {@link #setObject(Object)}.
		 * <pre>
		 * node.text = '006'
		 * assert node.object.class.simpleName == "String"
		 * node.object = '006'
		 * assert node.text == '6'
		 * assert node.object.class.simpleName == "Long"
		 * </pre>
		 * @see #setObject(Object)
		 * @since 1.2, semantics changed for Strings with 1.2.17 */
		void setText(Object value);
		
		/**
		 * A node's text object is normally String valued but it can be of any type since every Object can be converted
		 * to String for display. This methods provides automatic conversion to String in a way that node.to.getXyz()
		 * methods will be able to convert the string properly to the wanted type.
		 * <p>
		 * Special support is provided for numbers, dates and calendars that are stored unconverted. For display of
		 * them a standard formatter is used (use #setFormat() to change it). You may also pass {@link IFormattedObject}
		 * instances ({@link FormattedDate}, {@link FormattedNumber} or {@link FormattedObject}) directly to determine
		 * the format in one pass.
		 * <p>
		 * All other types are converted via value.toString().
		 * <p><b>Numbers</b>
		 * <pre>
		 * double number = 1.2222222d
		 * node.object = number
		 * // to enable math with node.object its type is not FormattedNumber
		 * assert node.object.class.simpleName == "Double"
		 * assert node.to.object.class.simpleName == "Double"
		 * // use globally bound TextUtils object
		 * def defaultNumberFormat = textUtils.defaultNumberFormat
		 * assert node.format != null
		 * // e.g. "1.22"
		 * assert node.text == defaultNumberFormat.format(number)
		 * assert node.to.num == number
		 * assert node.to.num + 1.0 == number + 1.0
		 * assert node.object + 1.0 == number + 1.0
		 * </pre>
		 * <p><b>Dates</b>
		 * <pre>
		 * def date = new Date(0) // when Unix time began
		 * node.object = date
		 * assert node.object.class.simpleName == "FormattedDate"
		 * assert node.to.object.class.simpleName == "FormattedDate"
		 * // use globally bound TextUtils object
		 * def defaultDateFormat = textUtils.defaultDateFormat
		 * assert node.object.toString() == defaultDateFormat.format(date)
		 * assert node.format == defaultDateFormat.pattern
		 * // e.g. "01/01/1970"
		 * assert node.text == defaultDateFormat.format(date)
		 * assert node.to.date == date
		 * </pre>
		 * <p><b>Date/Time</b>
		 * <pre>
		 * def date = new Date(0) // when Unix time began
		 * // the default format for dates does not contain a time component. Use node.dateTime to override it.
		 * node.dateTime = date
		 * assert node.object.class.simpleName == "FormattedDate"
		 * assert node.to.object.class.simpleName == "FormattedDate"
		 * // use globally bound TextUtils object
		 * def defaultDateFormat = textUtils.defaultDateTimeFormat
		 * assert node.object.toString() == defaultDateFormat.format(date)
		 * assert node.format == defaultDateFormat.pattern
		 * // e.g. "01/01/1970 01:00"
		 * assert node.text == defaultDateFormat.format(date)
		 * assert node.to.date == date
		 * </pre>
		 * @param value A not-null object.
		 * @since 1.2 */
		void setObject(Object value);

		/** sets the node text to a default formatted datetime object. (After setObject(Date) no time component is
		 * displayed so use this method if you want the time to be displayed.)
		 * @see #setObject(Object)
		 * @since 1.2 */
		void setDateTime(Date date);

		/** Converts data to a <a href="http://www.freesoft.org/CIE/RFC/1521/7.htm">BASE64</a> encoded string and
		 * sets it as this node's text. Long lines are folded to a length a bit less than 80.
		 * @since 1.2 */
		void setBinary(byte[] data);

		/** sets the format string of the formatter. It has to be appropriate for the data type of the contained object,
		 * otherwise the format is simply ignored. For instance use "dd.MM.yyyy" for dates but not for numbers:
		 * <pre>
		 * node.object = new Date()
		 * node.format = "dd.MMM.yyyy"  // ok: "13.07.2011"
		 * node.format = "#.00"  // still "13.07.2011". See log: "cannot format 13.07.2011 with #.00: multiple points"
		 * </pre>
		 * Numbers:
		 * <pre>
		 * node.object = 1.122
		 * node.format = "#.##"   // ok: "1.12" (US, GB, ...) or "1,12" (Germany, ...)
		 * node.format = "#.0000" // ok: "1.1220" (US, GB, ...) or "1,1220" (Germany, ...)
		 * </pre>
		 * @see #setObject(Object)
		 * @since 1.2 */
		void setFormat(String format);

		void setLastModifiedAt(Date date);

		void setCreatedAt(Date date);

		// Attributes
		/**
		 * Allows to set and to change attribute like array (or map) elements.
		 * See description of {@link Attributes} for details.
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
		 * @return the new value
		 */
		Object putAt(String attributeName, Object value);

		/** allows to set all attributes at once:
		 * <pre>
		 *   node.attributes = [:] // clear the attributes
		 *   assert node.attributes.size() == 0
		 *   node.attributes = ["1st" : "a value", "2nd" : "another value"] // create 2 attributes 
		 *   assert node.attributes.size() == 2
		 *   node.attributes = ["one attrib" : new Double(1.22)] // replace all attributes
		 *   assert node.attributes.size() == 1
		 *   assert node.attributes.getFirst("one attrib") == "1.22" // note the type conversion
		 *   assert node["one attrib"] == "1.22" // here we compare Convertible with String
		 * </pre>
		 */
		void setAttributes(java.util.Map<String, Object> attributes);

		void setLeft(boolean isLeft);

        /** Returns true if the node is password protected, no matter if currently accessible (password entered) or not.
         * @since 1.3.6 */
		boolean hasEncryption();
        
        /** decrypts a node and remove the password protection.
         * @since 1.3.6 */
        void removeEncryption(String password);
		
		/** Returns true if the node has password protection and is currently unaccessible (password has to be entered).
		 * @since 1.3.6 */
		boolean isEncrypted();
		
		/** encrypts a node. If the node has child nodes the branch is folded.
		 * @since 1.3.6 */
		void encrypt(String password);

		/** decrypts a node without removing the encryption.
         * @since 1.3.6 */
		void decrypt(String password);

        /**@since 1.3.7 */
    	void setHorizontalShift(final int horizontalShift);

        /**@since 1.5.6 */
    	void setHorizontalShift(Quantity<LengthUnits> verticalShift);

        /** use length units like "1 cm" or "6 pt"
         * @since 1.5.6 */
    	void setHorizontalShift(String verticalShift);

    	/**@since 1.3.7 */
    	void setVerticalShift(final int verticalShift);

        /**@since 1.5.6 */
    	void setVerticalShift(Quantity<LengthUnits> verticalShift);

        /** use length units like "1 cm" or "6 pt"
         * @since 1.5.6 */
    	void setVerticalShift(String verticalShift);

        /**@since 1.3.7 */
    	void setMinimalDistanceBetweenChildren(final int minimalDistanceBetweenChildren);

        /**@since 1.5.6 */
    	void setMinimalDistanceBetweenChildren(Quantity<LengthUnits> verticalShift);

        /** use length units like "1 cm" or "6 pt"
         * @since 1.5.6 */
    	void setMinimalDistanceBetweenChildren(String verticalShift);
    	
    	/**
    	 * A sort method that uses the result of the Groovy closure ("block") for comparison. As this closure
    	 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
    	 * evaluate every node property, like attributes, icons, node text or notes.
    	 * <p>
    	 * Examples:
    	 * <pre>
    	 *    // sort by details text
    	 *    node.sortChildrenBy{ it.details.to.plain }
    	 *    // sort numerically
    	 *    node.sortChildrenBy{ it.to.num0 }
    	 * </pre>
    	 * @param closure a Groovy closure that returns a Comparable value like a String. The closure will receive
    	 *        a NodeModel as an argument.
    	 * @since 1.4.1
    	 */
		void sortChildrenBy(Closure<Comparable<Object>> closure);
	}

	/** Node's style: <code>node.style</code> - read-only. */
	interface NodeStyleRO {
		IStyle getStyle();

		/** Returns the name of the node's style if set or null otherwise. For styles with translated names the
		 * translation key is returned to make the process robust against language setting changes.
		 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
		 * @since 1.2.2 */
		String getName();
		
		Node getStyleNode();

		Color getBackgroundColor();

		/** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		String getBackgroundColorCode();

		Edge getEdge();

		Font getFont();

		/** @deprecated since 1.2 - use {@link #getTextColor()} instead. */
		Color getNodeTextColor();

		/** @since 1.2 */
		Color getTextColor();

		String getTextColorCode();

        /** @since 1.2 true if the floating style is set for the node (aka "free node"). */
        boolean isFloating();

        /** @since 1.2.20 */
        int getMinNodeWidth();
        
        /** @since 1.2.20 */
        int getMaxNodeWidth();

        /** @since 1.3.8 */
        boolean isNumberingEnabled();
	}

	/** Node's style: <code>node.style</code> - read-write. */
	interface NodeStyle extends NodeStyleRO {
		void setStyle(IStyle style);

		/** Selects a style by name, see menu Styles &rarr; Pre/Userdefined styles for valid style names or use
		 * {@link #getName()} to display the name of a node's style.
		 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
		 * @param styleName can be the name visible in the style menu or its translation key as returned by
		 *        {@link #getName()}. (Names of predefined styles are subject to translation.)
		 *        Only translation keys will continue to work if the language setting is changed.
		 * @throws IllegalArgumentException if the style does not exist.
		 * @since 1.2.2 */
		void setName(String styleName);

		void setBackgroundColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setBackgroundColorCode(String rgbString);

		/** @deprecated since 1.2 - use {@link #setTextColor(Color)} instead. */
		void setNodeTextColor(Color color);

		/** @since 1.2 */
		void setTextColor(Color color);

		/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
		 *  @since 1.2 */
		void setTextColorCode(String rgbString);

        /** sets the floating style for the node (aka "free node"). Should normally only be applied to direct
         *  children of the root node.
         *  @since 1.2 */
        void setFloating(boolean floating);

        /** minNodeWidth in px - set to -1 to restore default.
         * @since 1.2.20 */
        void setMinNodeWidth(int width);
        
        /** Set to null to restore default 
         * @since 1.5.6 */
        void setMinNodeWidth(Quantity<LengthUnits> width);

        /** use length units like "1 cm" or "6 pt"
         * @since 1.5.6 */
        void setMinNodeWidth(String width);
        
        /** minNodeWidth in px - set to -1 to restore default.
         * @since 1.2.20 */
        void setMaxNodeWidth(int width);
        
        /** Set to null to restore default 
         * @since 1.5.6 */
        void setMaxNodeWidth(Quantity<LengthUnits> width);

        /** use length units like "1 cm" or "6 pt"
         * @since 1.5.6 */
        void setMaxNodeWidth(String width);
        
        /** @since 1.3.8 */
        void setNumberingEnabled(boolean enabled);
	}

    public interface Properties {
        /** Provides map-like access to properties. Note that the returned type is a
         * {@link Convertible}, not a String as in the basic storage. Nevertheless it behaves like a String in almost
         * all respects, that is, in Groovy scripts it understands all String methods like lenght(), matches() etc.
         * <br>
         * Note that unlike Attributes.getAt() this method will return <em>null</em> if the property is not set!
         * @since 1.3.6 */
        Convertible getAt(String key);

        /**
         * Allows to set and to change properties.
         * @param value An object for conversion to String. Works well for all types that {@link Convertible}
         *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
         * @return the new value
         * @since 1.3.6 */
        Convertible putAt(String key, Object value);

        /** returns the names of all attributes.
         * @since 1.3.6 */
        java.util.Set<String> keySet();
    }

	/** Reminder: <code>node.reminder</code> - read-only.
	 * <pre>
     *  def rem = node.reminder
     *  if (!rem.remindAt)
     *      c.statusInfo = "this node has no reminder"
     *  else
     *      c.statusInfo = "reminder fires at ${rem.remindAt} and then every ${rem.period} ${rem.periodUnit}"
	 * </pre> */
    interface ReminderRO {
        /** The timestamp when the reminder fires first. */
        Date getRemindAt();
        /** One of ["MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"]. */
        String getPeriodUnit();
        /** Count in units of "PeriodUnit". (period=2, periodUnit="WEEK") reminds every two weeks. */
        Integer getPeriod();
        /** optional: a Groovy script to execute when the reminder fires. */
        String getScript();
    }

    /** Reminder: <code>node.reminder</code> - read-write. For creating and modifying reminders:
     * <pre>
     *  def reminder = node.reminder
     *  if (!reminder)
     *      c.statusInfo = "node has no reminder"
     *  else
     *      c.statusInfo = "node has a reminder: $reminder"
     *  
     *  def inAMinute = new Date(System.currentTimeMillis() + 60*1000)
     *  node.reminder.createOrReplace(inAMinute, "WEEK", 2)
     *  if (node.map.file) {
     *      node.reminder.setScript("loadUri(new URI('${node.map.file.toURI()}#${node.id}'))")
     *  }
     *  // a click on the node opens time management dialog
     *  node.link.text = 'menuitem:_$TimeListAction$0'
     * </pre> */
    interface Reminder extends ReminderRO {
//        /** Creates a new reminder. Removes existing reminders for the same node if they exist.
//         * @param remindAt The timestamp when the reminder should fire. */
//        void createOrReplace(Date remindAt);
        /** Creates a periodic reminder. To make the reminder fire every second week:
         * <pre>
         *   node.reminder.createOrReplace(new Date() + 1, "WEEK", 2)
         * </pre>
         * @param remindAt The timestamp when the reminder fires first.
         * @param periodUnit one of ["MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"].
         * @param period counts the periodUnits. */
        void createOrReplace(Date remindAt, String periodUnit, Integer period);

        /** optional: a Groovy script to execute when the reminder fires.
         * @param scriptSource the script itself, not a path to a file.
         * @throws NullPointerException if there is no reminder yet. */
        void setScript(String scriptSource);

        /** removes a reminder from a node. It's not an error if there is no reminder to remove. */
        void remove();
    }
}
