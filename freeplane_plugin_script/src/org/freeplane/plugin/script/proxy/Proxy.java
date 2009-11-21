package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.link.ArrowType;

public interface Proxy {
	interface Controller{
		Selection getSelection();
		View getView();
	}
 
    interface Selection {
        Node getSelected();
            //returns the Node of that selected node which is closest to the root
        List<Node> getSelecteds();
            //returns List<Node> of Node objects
        List<Node> getSelectedsByDepth();
        void select(Node toSelect);
        void selectMultipleNodes(java.util.List<Node> toSelect);
            //toSelect is a List<Node> of Node objects
        void selectBranch(Node branchRoot);
            //selects branchRoot and all children
    }
 
    interface View {
        void centerOnNode(Node center);
            //corresponds to menu item View/Center selected node
        void zoomOnNodes(java.util.List<Node> zoomOn);
            //zoomOn is a List<Node> of Node objects
            //centers on and adjusts zoom to have all selected nodes in the viewport 
    }
 
   ///////////
 
        interface Node {
            String getText();
            String getNoteText();
            void setNoteText(String text);
            void setText(String text);
            String getPlainTextContent();
            Attributes getAttributes();
            Link getLink();
            Icons getIcons();
            ExternalObject getExternalObject();
            NodeStyle getStyle();
            List<Node> getChildren();
            Node getParentNode();
            Node getRootNode();
            String getNodeID();
                //List<Node> of Connector objects
            //methods
            Collection<Connector> getConnectorsOut();
            Collection<Connector> getConnectorsIn();
            Connector addConnectorTo(Node target);
                //adds a new Connector object  to List<Node> connectors and returns reference for optional further editing (style);; also enlists the Connector on the target Node object
            Connector addConnectorTo(String targetNodeID);
                //as above, using String nodeID instead of Node object to establish the connector
            void removeConnector(Connector connectorToBeRemoved);
                //removes connector from List<Node> connectors; does the corresponding on the target Node object referenced by connectorToBeRemoved
            void moveTo(Node parentNode);
            void moveTo(Node parentNode, int position);
            void delete();
            int getChildPosition(Node childNode);
            Node createChild(int position);
                // inserts *new* node as child, takes care of all construction work and internal stuff
            Node createChild();
                // same as above, inserts as last child
            boolean isLeaf();
            boolean isDescendantOf(Node p);
            int getNodeLevel(boolean countHidden);
            boolean isRoot();
            boolean isFolded();
            boolean isLeft();
            void setFolded(boolean folded);
        }
 
 
            interface Attributes {
                //simplistic  interface for unique keys
                void set(String key, String value);
                String get(String key);
                boolean remove(String key); // returns true on success
                // possibly add higher level functions allowing to deal with non-unique keys
            }
 
            interface Link {
                void set(Node targetNode);
                boolean set(String target); // empty String means remove link (as in user interface);
                boolean remove(); // same as set("");
                String getTargetID();
                Node getTargetNode();
            }
 
        interface Connector {
            Node getTarget();
            Node getSource();
            String getSourceLabel();
            void setSourceLabel(String label);
            String getMiddleLabel();
            void setMiddleLabel(String label);
            String getTargetLabel();
            void setTargetLabel(String label);
            boolean simulatesEdge();
            void setSimulatesEdge(boolean  simulatesEdge);
            Color getColor();
            void  setColor(Color color);
            ArrowType getStartArrow();
            void setStartArrow(ArrowType arrowType);
            ArrowType getEndArrow();
            void setEndArrow(ArrowType arrowType);
        }
 
            interface Icons {
                void addIcon(String name);
                    // iconID is one of  "Idea","Question","Important", etc.
                boolean removeIcon(String iconID);
                    // deletes first occurence of icon with name iconID, returns true if success (icon existed);
                java.util.List<Icons> getIcons();
                    //returns List<Node> of Strings (corresponding to iconID above);
            }
 
            interface ExternalObject  {
                String getURI();         // empty string means that there's no external object 
                void setURI(String uri); // setting empty String uri means remove external object (as for Links);
                float getSize();
                void setSize(float size);
            }
 
            interface NodeStyle {
                Color getBackgroundColor();
                void  setBackgroundColor(Color color);
                Color getNodeTextColor();
                void setNodeTextColor(Color color);
                Font getFont();
                Edge getEdge();
                boolean isVisible();
                void applyPattern(String patternName);
            }
 
            interface Font {
                boolean isUnderlined();
                void setUnderlined(boolean underlined);
                void resetUnderlined();
                boolean isItalic();
                void setItalic(boolean italic);
                void resetItalic();
                boolean isBold();
                void setBold(boolean bold);
                void resetBold();
                int getSize();
                void setSize(int size);
                void resetSize();
            }
 
            interface Edge {
                Color getColor();
                void setColor(Color color);
                int getWidth();
                void setWidth(int width);
                    // can be "Parent","Thin","1","2","4","8"
                EdgeStyle getType();
                void setType(EdgeStyle type);
            }
}
