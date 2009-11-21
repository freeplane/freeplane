/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.plugin.script.proxy.Proxy.Connector;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeProxy extends AbstractProxy implements Node {

	public NodeProxy(NodeModel node, MModeController modeController) {
		super(node, modeController);
	}

	public void moveTo(Node parentNode) {
		final NodeProxy parentNodeProxy = (NodeProxy)parentNode;
		MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.moveNodeAsChild(getNode(), parentNodeProxy.getNode(), getNode().isLeft(), false);
	}

	public void moveTo(Node parentNode, int position) {
		final NodeProxy parentNodeProxy = (NodeProxy)parentNode;
		MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.moveNode(getNode(), parentNodeProxy.getNode(), position, getNode().isLeft(), false);
	}

	public void setFolded(boolean folded) {
		MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.setFolded(getNode(), folded);
	}

	public void delete() {
		MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.deleteNode(getNode());
	}

	public void removeConnector(Proxy.Connector connectorToBeRemoved) {
		ConnectorProxy connectorProxy = (ConnectorProxy) connectorToBeRemoved;
		ConnectorModel link = connectorProxy.getConnector();
		MLinkController linkController = (MLinkController) LinkController.getController(getModeController());
		linkController.removeArrowLink(link);
		
	}

	public boolean isRoot() {
		return getNode().isRoot();
	}

	public boolean isLeaf() {
		return getNode().isLeaf();
	}

	public boolean isLeft() {
		return getNode().isLeft();
	}

	public boolean isFolded() {
		return getNode().isFolded();
	}

	public boolean isDescendantOf(Node otherNode) {
		final NodeModel otherNodeModel = ((NodeProxy)otherNode).getNode();
		NodeModel node = this.getNode();
		do{
			if(node.equals(otherNodeModel)){
				return true;
			}
			node = getNode().getParentNode();
		}while (node != null);
		return false;
	}

	public Node createChild() {
		MMapController mapController = (MMapController) getModeController().getMapController();
		NodeModel newNodeModel = new NodeModel(getNode().getMap());
		mapController.insertNode(newNodeModel, getNode());
		return new NodeProxy(newNodeModel, getModeController());
	}

	public Node createChild(int position) {
		MMapController mapController = (MMapController) getModeController().getMapController();
		NodeModel newNodeModel = new NodeModel(getNode().getMap());
		mapController.insertNode(newNodeModel, getNode(), position);
		return new NodeProxy(newNodeModel, getModeController());
	}


	public String getText() {
		return getNode().getText();
	}

	public Proxy.NodeStyle getStyle() {
		return new NodeStyleProxy();
	}

	public Node getRootNode() {
		if(getNode().isRoot()){
			return this;
		}
		NodeModel rootNode = getNode().getMap().getRootNode();
		return new NodeProxy(rootNode, getModeController());
	}

	public Node getParentNode() {
		NodeModel parentNode = getNode().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode, getModeController()) : null;
	}

	public int getNodeLevel(boolean countHidden) {
		return getNode().getNodeLevel(countHidden);
	}

	public String getNodeID() {
		return getNode().getID();
	}

	public Proxy.Link getLink() {
		return new LinkProxy();
	}

	public Proxy.Icons getIcons() {
		return new IconsProxy();
	}

	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy();
	}

	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(getNode(), getModeController());
	}

	public Collection<Connector> getConnectorsIn() {
		return new ConnectorInListProxy(getNode(), getModeController());
	}
	public List<Node> getChildren() {
		return new AbstractList<Node>() {

			@Override
			public Node get(int index) {
				NodeModel child = (NodeModel) getNode().getChildAt(index);
				return new NodeProxy(child, getModeController());
			}

			@Override
			public int size() {
				return getNode().getChildCount();
			}
		};
	}

	public int getChildPosition(Node childNode) {
		NodeModel childNodeModel = ((NodeProxy)childNode).getNode();
		return getNode().getChildPosition(childNodeModel);
	}

	public Proxy.Attributes getAttributes() {
		return new AttributesProxy(getNode(), getModeController());
	}

	public Proxy.Connector addConnectorTo(String targetNodeID) {
		MLinkController linkController = (MLinkController) LinkController.getController(getModeController());
		ConnectorModel connectorModel = linkController.addConnector(getNode(), targetNodeID);
		return new ConnectorProxy(connectorModel, getModeController());
	}

	public Proxy.Connector addConnectorTo(Node target) {
		return addConnectorTo(target.getNodeID());
	}

	public String getNoteText() {
		return NoteModel.getNoteText(getNode());
	}

	public String getPlainTextContent() {
		return getNode().getPlainTextContent();
	}

	public void setNoteText(String text) {
		MNoteController noteController = (MNoteController) NoteController.getController(getModeController());
		noteController.setNoteText(getNode(), text);
	}

	public void setText(String text) {
		MTextController textController = (MTextController) TextController.getController(getModeController());
		textController.setNodeText(getNode(), text);
	}

}