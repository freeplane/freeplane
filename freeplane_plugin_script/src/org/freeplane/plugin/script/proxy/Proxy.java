package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.awt.Color;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	/** Attributes are name - value pairs assigned to a node. A node may have multiple attributes
	 * with the same name. */
	interface Attributes {
		/** returns the <em>first</em> value of an attribute with the given name or null otherwise. */
		String get(final String name);

		/** returns all values for the attribute name. */
		List<String> getAll(final String name);

		/** returns all attribute names in the proper sequence.
		 * <pre>
		 *   // rename attribute
		 *   int i = 0;
		 *   for (String name : attributes.getAttributeNames()) {
		 *       if (name.equals("xy"))
		 *           attributes.set(i, "xyz", attributes.get(i));
		 *       ++i;
		 *   }
		 * </pre> */
		public List<String> getAttributeNames();

		/** returns the attribute value at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>.*/
		String get(final int index);

		/** sets the value of the attribute at an index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String value);

		/** sets name and value of the attribute at the given index. This method will not create new attributes.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void set(final int index, final String name, final String value);

		/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
		         * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
		         * must be used. */
		public int findAttribute(final String name);

		/** removes <em>all</em> attributes with this name.
		 * @returns true on removal of an existing attribute and false otherwise. */
		boolean remove(final String name);

		/** removes the attribute at the given index.
		 * @throws IndexOutOfBoundsException if index is out of range <tt>(index
		 *         &lt; 0 || index &gt;= size())</tt>. */
		void remove(final int index);

		/** adds an attribute if there is no attribute with the given name or changes
		 * the value <em>of the first</em> attribute with the given name. */
		void set(final String name, final String value);

		/** adds an attribute no matter if an attribute with the given name already exists. */
		void add(final String name, final String value);

		/** the number of attributes. It is <code>size() == getAttributeNames().size()</code>. */
		int size();
	}

	interface Connector {
		Color getColor();

		ArrowType getEndArrow();

		String getMiddleLabel();

		Node getSource();

		String getSourceLabel();

		ArrowType getStartArrow();

		Node getTarget();

		String getTargetLabel();

		void setColor(Color color);

		void setEndArrow(ArrowType arrowType);

		void setMiddleLabel(String label);

		void setSimulatesEdge(boolean simulatesEdge);

		void setSourceLabel(String label);

		void setStartArrow(ArrowType arrowType);

		void setTargetLabel(String label);

		boolean simulatesEdge();
	}

	interface Controller {
		void centerOnNode(Node center);

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

		void select(Node toSelect);

		/** selects branchRoot and all children */
		void selectBranch(Node branchRoot);

		/** toSelect is a List<Node> of Node objects */
		void selectMultipleNodes(List<Node> toSelect);

		/** reset undo / redo lists and deactivate Undo for current script */
		void deactivateUndo();

		/** The main info for the status line, null to remove*/
		public void setStatusInfo(String info);

		/** Info for status line, null to remove*/
		public void setStatusInfo(String key, String info);

		/** Info for status line, null to remove*/
		public void setStatusInfo(String key, Icon icon);

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @see Node.find(ICondition) for searches on subtrees */
		public List<Node> find(ICondition condition);

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
		public List<Node> find(Closure closure);
	}

	interface Edge {
		Color getColor();

		EdgeStyle getType();

		int getWidth();

		void setColor(Color color);

		void setType(EdgeStyle type);

		/** can be -1 for default, 0 for thin, >0 */
		void setWidth(int width);
	}

	interface ExternalObject {
		/** empty string means that there's no external object */
		String getURI();

		float getZoom();

		/** setting empty String uri means remove external object (as for Links); */
		void setURI(String uri);

		void setZoom(float zoom);
	}

	interface Font {
		String getName();

		int getSize();

		boolean isBold();

		boolean isBoldSet();

		boolean isItalic();

		boolean isItalicSet();

		boolean isNameSet();

		boolean isSizeSet();

		void resetBold();

		void resetItalic();

		void resetName();

		void resetSize();

		void setBold(boolean bold);

		void setItalic(boolean italic);

		void setName(String name);

		void setSize(int size);
	}

	interface Icons {
		void addIcon(String name);

		/** returns List<Node> of Strings (corresponding to iconID above);
		 * iconID is one of "Idea","Question","Important", etc. */
		List<String> getIcons();

		/** deletes first occurence of icon with name iconID, returns true if
		 * success (icon existed); */
		boolean removeIcon(String iconID);
	}

	interface Link {
		String get();

		/** target is a URI.
		 * An empty String will remove the link.
		 * To get a local link (i.e. to another node) target should be: "#" + nodeID */
		boolean set(String target);
	}

	interface Map {
		Node getRootNode();

		/** returns the node if the map contains it or null otherwise. */
		Node node(String id);
	}

	interface Node {
		Connector addConnectorTo(Node target);

		/** adds a new Connector object to List<Node> connectors and returns
		 * reference for optional further editing (style); also enlists the
		 * Connector on the target Node object. */
		Connector addConnectorTo(String targetNodeID);

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff inserts as last child. */
		Node createChild();

		/** inserts *new* node as child, takes care of all construction work and
		 * internal stuff */
		Node createChild(int position);

		void delete();

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

		String getNodeID();

		/** if countHidden is false then only nodes that are matched by the
		 * current filter are counted. */
		int getNodeLevel(boolean countHidden);

		String getNoteText();

		Node getParentNode();

		/** use this method to remove all tags from an HTML node. */
		String getPlainTextContent();

		NodeStyle getStyle();

		String getText();

		boolean isDescendantOf(Node p);

		boolean isFolded();

		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		boolean isVisible();

		/** removes connector from List<Node> connectors; does the corresponding
		 * on the target Node object referenced by connectorToBeRemoved */
		void moveTo(Node parentNode);

		void moveTo(Node parentNode, int position);

		/** as above, using String nodeID instead of Node object to establish the connector*/
		void removeConnector(Connector connectorToBeRemoved);

		void setFolded(boolean folded);

		void setNoteText(String text);

		void setText(String text);

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true. */
		List<Node> find(ICondition condition);

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true. See {@link Controller#find(Closure)} for details. */
		List<Node> find(Closure closure);

		Date getLastModifiedAt();

		void setLastModifiedAt(Date date);

		Date getCreatedAt();

		void setCreatedAt(Date date);
	}

	interface NodeStyle {
		void applyPattern(String patternName);

		Color getBackgroundColor();

		Edge getEdge();

		Font getFont();

		Color getNodeTextColor();

		void setBackgroundColor(Color color);

		void setNodeTextColor(Color color);
	}
}
