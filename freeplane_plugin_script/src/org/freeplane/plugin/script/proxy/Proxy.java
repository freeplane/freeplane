package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	interface Attributes {
		String get(String key);

		boolean remove(String key); // returns true on success

		// possibly add higher level functions allowing to deal with non-unique
		// keys
		// simplistic interface for unique keys
		void set(String key, String value);
	}

	// /////////

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
		// selects branchRoot and all children
		void centerOnNode(Node center);

		Node getSelected();

		// returns the Node of that selected node which is closest to the root
		List<Node> getSelecteds();

		// returns List<Node> of Node objects
		List<Node> getSortedSelection();

		void select(Node toSelect);

		// toSelect is a List<Node> of Node objects
		void selectBranch(Node branchRoot);

		void selectMultipleNodes(java.util.List<Node> toSelect);
		
		void deactivateUndo();
	}

	interface Edge {
		Color getColor();

		// can be "Parent","Thin","1","2","4","8"
		EdgeStyle getType();

		int getWidth();

		void setColor(Color color);

		void setType(EdgeStyle type);

		void setWidth(int width);
	}

	interface ExternalObject {
		String getURI(); // empty string means that there's no external object

		float getZoom();

		void setURI(String uri); // setting empty String uri means remove
									// external object (as for Links);

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

		// deletes first occurence of icon with name iconID, returns true if
		// success (icon existed);
		java.util.List<String> getIcons();

		// returns List<Node> of Strings (corresponding to iconID above);
		// iconID is one of "Idea","Question","Important", etc.
		boolean removeIcon(String iconID);
	}

	interface Link {
		String get();

		boolean set(String target); // empty String means remove link (as in
									// user interface);
	}

	interface Node {
		Connector addConnectorTo(Node target);

		// adds a new Connector object to List<Node> connectors and returns
		// reference for optional further editing (style);; also enlists the
		// Connector on the target Node object
		Connector addConnectorTo(String targetNodeID);

		// inserts *new* node as child, takes care of all construction work and
		// internal stuff
		Node createChild();

		Node createChild(int position);

		void delete();

		Attributes getAttributes();

		int getChildPosition(Node childNode);

		List<Node> getChildren();

		Collection<Connector> getConnectorsIn();

		// List<Node> of Connector objects
		// methods
		Collection<Connector> getConnectorsOut();

		ExternalObject getExternalObject();

		Icons getIcons();

		Link getLink();

		String getNodeID();

		int getNodeLevel(boolean countHidden);

		String getNoteText();

		Node getParentNode();

		String getPlainTextContent();

		Node getRootNode();

		NodeStyle getStyle();

		String getText();

		boolean isDescendantOf(Node p);

		boolean isFolded();

		// same as above, inserts as last child
		boolean isLeaf();

		boolean isLeft();

		boolean isRoot();

		boolean isVisible();

		// removes connector from List<Node> connectors; does the corresponding
		// on the target Node object referenced by connectorToBeRemoved
		void moveTo(Node parentNode);

		void moveTo(Node parentNode, int position);

		// as above, using String nodeID instead of Node object to establish the
		// connector
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
