package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	/** simplistic interface for unique keys */
	interface Attributes {
		String get(String key);

		public List<String> getAttributeNames();

		/** returns the index of an attribute if it exists or -1 otherwise */
		public int findAttribute(final String key);

		/** returns true on removal of an existing attribute and false otherwise. */
		boolean remove(String key);

		void set(String key, String value);
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

		/** empty String means remove link (as in user interface). */
		boolean set(String target);
	}
	
	interface Map {
		Node getRootNode();
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

		int getChildPosition(Node childNode);

		List<Node> getChildren();

		Collection<Connector> getConnectorsIn();

		Collection<Connector> getConnectorsOut();

		ExternalObject getExternalObject();

		Icons getIcons();

		Link getLink();
		
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
