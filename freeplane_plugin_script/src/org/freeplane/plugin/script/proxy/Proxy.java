package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	interface AttributesRO {
		/** alias for {@link #getFirst(int)}.
		 * @deprecated before 1.1 - use {@link #get(int)}, {@link #getFirst(int)} or {@link #getAll(String)} instead. */
		@Deprecated
		String get(final String name);

		/** returns the <em>first</em> value of an attribute with the given name or null otherwise. @since 1.2 */
		String getFirst(final String name);

		/** returns all values for the attribute name. */
		List<String> getAll(final String name);

		/** returns all attribute names in the proper sequence. The number of names returned
		 * is equal to the number of attributes.
		 * <pre>
		 *   // rename attribute
		 *   int i = 0;
		 *   for (String name : attributes.getAttributeNames()) {
		 *       if (name.equals("xy"))
		 *           attributes.set(i, "xyz", attributes.get(i));
		 *       ++i;
		 *   }
		 * </pre> */
		List<String> getAttributeNames();

		/** returns the attribute value at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>.*/
		String get(final int index);

		/** @deprecated since 1.2 - use {@link #findFirst(String)} instead. */
		int findAttribute(final String name);

		/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
		 * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
		 * must be used. @since 1.2*/
		int findFirst(final String name);
		
		/** the number of attributes. It is <code>size() == getAttributeNames().size()</code>. */
		int size();
	}

	/** Attributes are name - value pairs assigned to a node. A node may have multiple attributes
	 * with the same name. */
	interface Attributes extends AttributesRO {
		/** sets the value of the attribute at an index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String value);

		/** sets name and value of the attribute at the given index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String name, final String value);

		/** removes the <em>first</em> attribute with this name.
		 * @returns true on removal of an existing attribute and false otherwise.
		 * @deprecated before 1.1 - use {@link #remove(int)} or {@link #removeAll(String)} instead. */
		@Deprecated
		boolean remove(final String name);

		/** removes <em>all</em> attributes with this name.
		 * @returns true on removal of an existing attribute and false otherwise. */
		boolean removeAll(final String name);

		/** removes the attribute at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void remove(final int index);

		/** adds an attribute if there is no attribute with the given name or changes
		 * the value <em>of the first</em> attribute with the given name. */
		void set(final String name, final String value);

		/** adds an attribute no matter if an attribute with the given name already exists. */
		void add(final String name, final String value);

		/** removes all attributes. @since 1.2 */
		void clear();
	}

	interface ConnectorRO {
		Color getColor();

		ArrowType getEndArrow();

		String getMiddleLabel();

		// FIXME: beware!
		Node getSource();

		String getSourceLabel();

		ArrowType getStartArrow();

		// FIXME: beware!
		Node getTarget();

		String getTargetLabel();

		boolean simulatesEdge();
	}

	interface Connector extends ConnectorRO {
		void setColor(Color color);

		void setEndArrow(ArrowType arrowType);

		void setMiddleLabel(String label);

		void setSimulatesEdge(boolean simulatesEdge);

		void setSourceLabel(String label);

		void setStartArrow(ArrowType arrowType);

		void setTargetLabel(String label);
	}

	interface ControllerRO {
		/** if multiple nodes are selected returns one (arbitrarily chosen)
		 * selected node or the selected node for a single node selection. */
		Node getSelected();

		List<Node> getSelecteds();

		/** returns List<Node> of Node objects sorted on Y
		 *
		 * @param differentSubtrees if true
		 *   children/grandchildren/grandgrandchildren/... nodes of selected
		 *   parent nodes are excluded from the result. */
		List<Node> getSortedSelection(boolean differentSubtrees);

		/**
		 * returns Freeplane version.
		 * Use it like this:
		 * <pre>
		 *   import org.freeplane.core.util.FreeplaneVersion
		 *   import org.freeplane.core.ui.components.UITools
		 * 
		 *   def required = FreeplaneVersion.getVersion("1.1.2");
		 *   if (c.freeplaneVersion < required)
		 *       UITools.errorMessage("Freeplane version " + c.freeplaneVersion
		 *           + " not supported - update to at least " + required);
		 * </pre>
		 */
		FreeplaneVersion getFreeplaneVersion();

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @see Node.find(ICondition) for searches on subtrees
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/**
		 * Starting from the root node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true.
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
		 * @param closure a Groovy closure that returns a boolean value. The closure will receive
		 *        a NodeModel as an argument which can be tested for a match.
		 * @return all nodes for which <code>closure.call(NodeModel)</code> returns true.
		 * @see Node.find(Closure) for searches on subtrees
		 */
		List<Node> find(Closure closure);
	}

	interface Controller extends ControllerRO {
		void centerOnNode(Node center);

		void select(Node toSelect);

		/** selects branchRoot and all children */
		void selectBranch(Node branchRoot);

		/** toSelect is a List<Node> of Node objects */
		void selectMultipleNodes(List<Node> toSelect);

		/** reset undo / redo lists and deactivate Undo for current script */
		void deactivateUndo();

		/** invokes undo once - for testing purposes mainly. @since 1.2 */
		void undo();

		/** invokes redo once - for testing purposes mainly. @since 1.2 */
		void redo();

		/** The main info for the status line with key="standard", use null to remove. Removes icon if there is one. */
		void setStatusInfo(String info);

		/** Info for status line, null to remove. Removes icon if there is one.
		 * @see {@link #setStatusInfo(String, String, String)} */
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

		/** opens a new map with a default name in the foreground. @since 1.2 */
		Map newMap();

		/** opens a new map for url in the foreground if it isn't opened already. @since 1.2 */
		Map newMap(URL url);
	}

	interface EdgeRO {
		Color getColor();

		EdgeStyle getType();

		int getWidth();
	}

	interface Edge extends EdgeRO {
		void setColor(Color color);

		void setType(EdgeStyle type);

		/** can be -1 for default, 0 for thin, >0 */
		void setWidth(int width);
	}

	interface ExternalObjectRO {
		/** empty string means that there's no external object */
		String getURI();

		float getZoom();
	}

	interface ExternalObject extends ExternalObjectRO {
		/** setting empty String uri means remove external object (as for Links); */
		void setURI(String uri);

		void setZoom(float zoom);
	}

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

	interface IconsRO {
		/** returns List<Node> of Strings (corresponding to iconID above);
		 * iconID is one of "Idea","Question","Important", etc. */
		List<String> getIcons();
	}

	interface Icons extends IconsRO {
		/**
		 * adds an icon to a node if an icon for the given key can be found. The same icon can be added multiple
		 * times.
		 * <pre>
		 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
		 *   node.icons.addIcon("button_ok")
		 * </pre>
		 * @see FreeplaneIconUtils */
		void addIcon(String name);

		/** deletes first occurence of icon with the given name, returns true if
		 * success (icon existed); */
		boolean removeIcon(String name);
	}

	interface LinkRO {
		String get();
	}

	interface Link extends LinkRO {
		/** target is a URI.
		 * An empty String will remove the link.
		 * To get a local link (i.e. to another node) target should be: "#" + nodeId */
		boolean set(String target);
	}

	interface MapRO {
		/** @since 1.2 */
		Node getRoot();

		/** @deprecated since 1.2 - use {@link #getRoot()} instead. */
		Node getRootNode();

		/** returns the node if the map contains it or null otherwise. */
		Node node(String id);

		/** returns the physical location of the map if available or null otherwise. */
		File getFile();

		/** returns the title of the MapView. @since 1.2 */
		String getName();
	}

	interface Map extends MapRO {
		/**
		 * closes a map. Note that there is no undo for this method.
		 * @param close map even if there are unsaved changes.
		 * @param allowInteraction if (allowInteraction && ! force) a saveAs dialog will be opened if there are
		 *        unsaved changes.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map contains changes and parameter force is false.
		 * @since 1.2
		 */
		boolean close(boolean force, boolean allowInteraction);

		/**
		 * saves the map to disk. Note that there is no undo for this method.
		 * @param allowInteraction if a saveAs dialog should be opened if the map has no assigned URL so far.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
		 * @since 1.2
		 */
		boolean save(boolean allowInteraction);
	}

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
		 * </pre>
		 * @since 1.2
		 */
		Convertible getAt(String attributeName);

		/** returns the index (0..) of this node in the (by Y coordinate sorted)
		 * list of this node's children. Returns -1 if childNode is not a child
		 * of this node. */
		int getChildPosition(Node childNode);

		/** returns the children of this node ordered by Y coordinate. */
		List<Node> getChildren();

		Collection<Connector> getConnectorsIn();

		Collection<Connector> getConnectorsOut();

		ExternalObject getExternalObject();

		Icons getIcons();

		Link getLink();

		/** the map this node belongs to. */
		Map getMap();

		/** @deprecated since 1.2 - use Node.getId() instead. */
		String getNodeID();

		/** @since 1.2 */
		String getId();

		/** if countHidden is false then only nodes that are matched by the
		 * current filter are counted. */
		int getNodeLevel(boolean countHidden);

		/**
		 * Returns a Convertible object for the plain not text. Convertibles behave like Strings in most respects.
		 * Additionally String methods are overridden to handle Convertible arguments as if the argument were the
		 * result of Convertible.getText().
		 * @return Convertible getString(), getText() and toString() will return plain text instead of the HTML.
		 *         Use {@link #getNoteText()} to get the HTML text.
		 * @since 1.2
		 */
		Convertible getNote();
		
		/** Returns the HTML text of the node. (Notes always contain HTML text.) */
		String getNoteText();

		/** @since 1.2 */
		Node getParent();

		/** @deprecated since 1.2 - use {@link #getParent()} instead. */
		Node getParentNode();

		NodeStyle getStyle();

		/** use this method to remove all tags from an HTML node. @since 1.2 */
		String getPlainText();

		/** use this method to remove all tags from an HTML node.
		 * @deprecated since 1.2 - use getPlainText() or getTo().getPlain() instead. */
		String getPlainTextContent();

		String getText();

		/**
		 * returns an object that performs conversions (method name is choosen to give descriptive code):
		 * <dl>
		 * <dt>node.to.num <dd>Long or Double, see {@link Convertible#getDate()}.
		 * <dt>node.to.date <dd>Date, see {@link Convertible#getDate()}.
		 * <dt>node.to.string <dd>Text, see {@link Convertible#getString()}.
		 * <dt>node.to.text <dd>an alias for getString(), see {@link Convertible#getText()}.
		 * <dt>node.to.object <dd>returns what fits best, see {@link Convertible#getObject()}.
		 * </dl>
		 * Note that parse errors result in {@link ConversionException}s.
		 * @return ConvertibleObject
		 * @since 1.2
		 */
		Convertible getTo();

		/** an alias for {@link #getTo()}. @since 1.2 */
		Convertible getValue();

		/** returns true if p is a parent, or grandparent, ... of this node, or if it <em>is equal<em>
		 * to this node; returns false otherwise. */
		boolean isDescendantOf(Node p);

		boolean isFolded();

		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		boolean isVisible();

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(Closure)} instead. */
		List<Node> find(ICondition condition);

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true. See {@link Controller#find(Closure)} for details. */
		List<Node> find(Closure closure);

		Date getLastModifiedAt();

		Date getCreatedAt();
	}

	interface Node extends NodeRO {
		Connector addConnectorTo(Node target);

		/** adds a new Connector object to List<Node> connectors and returns
		 * reference for optional further editing (style); also enlists the
		 * Connector on the target Node object. */
		Connector addConnectorTo(String targetNodeId);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff inserts as last child. */
		Node createChild();

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff */
		Node createChild(int position);

		void delete();

		/** removes connector from List<Node> connectors; does the corresponding
		 * on the target Node object referenced by connectorToBeRemoved */
		void moveTo(Node parentNode);

		void moveTo(Node parentNode, int position);

		/** as above, using String nodeId instead of Node object to establish the connector*/
		void removeConnector(Connector connectorToBeRemoved);

		void setFolded(boolean folded);

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
		 * <pre>
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
		 * </pre>
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself.
		 * @since 1.2 (note that the old setNoteText() did not support non-String arguments.
		 */
		void setNote(Object value);

		/** @deprecated since 1.2 - use {@link #setNote()} instead. */
		void setNoteText(String text);

		/**
		 * A node's text is String valued. This methods provides automatic conversion to String in a way that
		 * node.to.getXyz() methods will be able to convert the string properly to the wanted type.
		 * Special conversion is provided for dates and calendars: They will be converted in a way that
		 * node.to.date and node.to.calendar will work. All other types are converted via value.toString():
		 * <pre>
		 *   // converts non-Dates with toString()
		 *   node.text = 1.2
		 *   assert node.to.text == "1.2"
		 *   assert node.to.num == 1.2d
		 *   // == dates
		 *   // a date in some non-GMT time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   node.text = date
		 *   assert node.to.text == "1970-01-01T02:00:00.000+0000"
		 *   assert node.to.date == date
		 * </pre>
		 * @param value A not-null object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself.
		 */
		void setText(Object value);

		void setLastModifiedAt(Date date);

		void setCreatedAt(Date date);

		// Attributes
		/**
		 * Allows to set and to change attribute like array elements.
		 * <p>
		 * Note that attributes are String valued. This methods provides automatic conversion to String in a way that
		 * node["a name"].getXyz() methods will be able to convert the string properly to the wanted type.
		 * Special conversion is provided for dates and calendars: They will be converted in a way that
		 * node["a name"].date and node["a name"].calendar will work. All other types are converted via
		 * value.toString():
		 * <pre>
		 *   // == text
		 *   node["attribute name"] = "a value"
		 *   assert node["attribute name"] == "a value"
		 *   assert node.attributes.getFirst("attribute name") == "a value" // the same
		 *   // == numbers and others
		 *   // converts numbers and other stuff with toString()
		 *   node["a number"] = 1.2
		 *   assert node["a number"].text == "1.2"
		 *   assert node["a number"].num == 1.2d
		 *   // == dates
		 *   // a date in some non-GMT time zone
		 *   def date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").
		 *       parse("1970-01-01 00:00:00.000-0200")
		 *   // converts to "1970-01-01T02:00:00.000+0000" (GMT)
		 *   // - note the shift due to the different time zone
		 *   node["a date"] = date
		 *   assert node["a date"].text == "1970-01-01T02:00:00.000+0000"
		 *   assert node["a date"].date == date
		 *   // == remove an attribute
		 *   node["removed attribute"] = "to be removed"
		 *   assert node["removed attribute"] == "to be removed"
		 *   node["removed attribute"] = null
		 *   assert node.attributes.find("removed attribute") == -1
		 * </pre>
		 * @param value An object for conversion to String. Works well for all types that {@link Convertible}
		 *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
		 * @return the new value
		 */
		String putAt(String attributeName, Object value);

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
	}

	interface NodeStyleRO {
		Object getStyle();

		Node getStyleNode();

		Color getBackgroundColor();

		Edge getEdge();

		Font getFont();

		Color getNodeTextColor();
	}

	interface NodeStyle extends NodeStyleRO {
		void setStyle(Object key);

		void setBackgroundColor(Color color);

		void setNodeTextColor(Color color);
	}
}
