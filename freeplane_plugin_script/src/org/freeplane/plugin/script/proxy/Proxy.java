package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	interface AttributesRO {
		/** returns the <em>first</em> value of an attribute with the given name or null otherwise.
		 * @deprecated use {@link #get(int)} or {@link #getAll(String)} instead. */
		@Deprecated
		String get(final String name);

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

		/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
		         * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
		         * must be used. */
		int findAttribute(final String name);

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
		 * @deprecated use {@link #remove(int)} or {@link #removeAll(String)} instead. */
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
		 * @see Node.find(ICondition) for searches on subtrees */
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

		/** invokes undo once - for testing purposes mainly. */
		void undo();
		
		/** invokes redo once - for testing purposes mainly. */
		void redo();

		/** The main info for the status line, null to remove*/
		void setStatusInfo(String info);

		/** Info for status line, null to remove*/
		void setStatusInfo(String key, String info);

		/** Info for status line, null to remove*/
		void setStatusInfo(String key, Icon icon);

		/** opens a new map with a default name in the foreground. */
		Map newMap();
		
		/** opens a new map for url in the foreground if it isn't opened already. */
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
		void addIcon(String name);

		/** deletes first occurence of icon with name iconID, returns true if
		 * success (icon existed); */
		boolean removeIcon(String iconID);
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
		Node getRoot();

		/** @deprecated use {@link #getRoot()} instead. */
		Node getRootNode();
		
		/** returns the node if the map contains it or null otherwise. */
		Node node(String id);

		/** returns the physical location of the map if available or null otherwise. */
		File getFile();

		/** returns the title of the MapView. */
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
		 */
		boolean close(boolean force, boolean allowInteraction);

		/**
		 * saves the map to disk. Note that there is no undo for this method.
		 * @param allowInteraction if a saveAs dialog should be opened if the map has no assigned URL so far.
		 * @return false if the saveAs was cancelled by the user and true otherwise.
		 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
		 */
		boolean save(boolean allowInteraction);
	}

	interface NodeRO {
		Attributes getAttributes();

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

		/** @deprecated use Node.getId() instead. */
		String getNodeID();
		
		String getId();

		/** if countHidden is false then only nodes that are matched by the
		 * current filter are counted. */
		int getNodeLevel(boolean countHidden);

		/** get the note text with all HTML tags removed. */
		String getPlainNoteText();

		String getNoteText();

		Node getParent();
		
		/** @deprecated use {@link #getParent()}. */
		Node getParentNode();

		NodeStyle getStyle();

		/** use this method to remove all tags from an HTML node. */
		String getPlainText();

		/** use this method to remove all tags from an HTML node.
		 * @deprecated use getPlainText() instead. */
		String getPlainTextContent();

		String getText();

		/** returns an object that performs conversions (name is choosen to give descriptive code):
		 * <dl>
		 * <dt>node.to.num <dd>Long or Double, see {@link Convertible#getDate()}.
		 * <dt>node.to.date <dd>Date, see {@link Convertible#getDate()}.
		 * <dt>node.to.value <dd>Text or, in case of a formula the evaluation result,
		 *     see {@link Convertible#getValue()}.
		 * <dt>node.to.string <dd>Text, see {@link Convertible#getString()}.
		 * <dt>node.to.text <dd>an alias for getString(), see {@link Convertible#getText()}.
		 * <dt>node.to.object <dd>returns what fits best, see {@link Convertible#getObject()}.
		 * </dl>
		 * Note that parse errors result in {@link ConversionException}s.
		 * @return ConvertibleObject
		 */
		Convertible getTo();

		/** returns node.text or, in case of a formula the evaluation result. */
		Object getValue();
		
		/** returns true if p is a parent, or grandparent, ... of this node, or if it <em>is equal<em>
		 * to this node; returns false otherwise. */
		boolean isDescendantOf(Node p);

		boolean isFolded();

		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		boolean isVisible();

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true. */
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

		/** set the note text. Required HTML tags will be automatically added. */
		void setPlainNoteText(String text);

		void setNoteText(String text);

		void setText(String text);

		void setLastModifiedAt(Date date);

		void setCreatedAt(Date date);
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
