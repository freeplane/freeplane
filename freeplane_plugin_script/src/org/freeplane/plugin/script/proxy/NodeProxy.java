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

	public NodeProxy(final NodeModel node, final MModeController modeController) {
		super(node, modeController);
	}

	public Proxy.Connector addConnectorTo(final Node target) {
		return addConnectorTo(target.getNodeID());
	}

	public Proxy.Connector addConnectorTo(final String targetNodeID) {
		final MLinkController linkController = (MLinkController) LinkController
				.getController(getModeController());
		final ConnectorModel connectorModel = linkController.addConnector(
				getNode(), targetNodeID);
		return new ConnectorProxy(connectorModel, getModeController());
	}

	public Node createChild() {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		final NodeModel newNodeModel = new NodeModel(getNode().getMap());
		mapController.insertNode(newNodeModel, getNode());
		return new NodeProxy(newNodeModel, getModeController());
	}

	public Node createChild(final int position) {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		final NodeModel newNodeModel = new NodeModel(getNode().getMap());
		mapController.insertNode(newNodeModel, getNode(), position);
		return new NodeProxy(newNodeModel, getModeController());
	}

	public void delete() {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.deleteNode(getNode());
	}

	public Proxy.Attributes getAttributes() {
		return new AttributesProxy(getNode(), getModeController());
	}

	public int getChildPosition(final Node childNode) {
		final NodeModel childNodeModel = ((NodeProxy) childNode).getNode();
		return getNode().getChildPosition(childNodeModel);
	}

	public List<Node> getChildren() {
		return new AbstractList<Node>() {

			@Override
			public Node get(final int index) {
				final NodeModel child = (NodeModel) getNode().getChildAt(index);
				return new NodeProxy(child, getModeController());
			}

			@Override
			public int size() {
				return getNode().getChildCount();
			}
		};
	}

	public Collection<Connector> getConnectorsIn() {
		return new ConnectorInListProxy(getNode(), getModeController());
	}

	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(getNode(), getModeController());
	}

	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy(getNode(), getModeController());
	}

	public Proxy.Icons getIcons() {
		return new IconsProxy(getNode(), getModeController());
	}

	public Proxy.Link getLink() {
		return new LinkProxy(getNode(), getModeController());
	}

	public String getNodeID() {
		return getNode().getID();
	}

	public int getNodeLevel(final boolean countHidden) {
		return getNode().getNodeLevel(countHidden);
	}

	public String getNoteText() {
		return NoteModel.getNoteText(getNode());
	}

	public Node getParentNode() {
		final NodeModel parentNode = getNode().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode,
				getModeController()) : null;
	}

	public String getPlainTextContent() {
		return getNode().getPlainTextContent();
	}

	public Node getRootNode() {
		if (getNode().isRoot()) {
			return this;
		}
		final NodeModel rootNode = getNode().getMap().getRootNode();
		return new NodeProxy(rootNode, getModeController());
	}

	public Proxy.NodeStyle getStyle() {
		return new NodeStyleProxy(getNode(), getModeController());
	}

	public String getText() {
		return getNode().getText();
	}

	public boolean isDescendantOf(final Node otherNode) {
		final NodeModel otherNodeModel = ((NodeProxy) otherNode).getNode();
		NodeModel node = this.getNode();
		do {
			if (node.equals(otherNodeModel)) {
				return true;
			}
			node = getNode().getParentNode();
		} while (node != null);
		return false;
	}

	public boolean isFolded() {
		return getNode().isFolded();
	}

	public boolean isLeaf() {
		return getNode().isLeaf();
	}

	public boolean isLeft() {
		return getNode().isLeft();
	}

	public boolean isRoot() {
		return getNode().isRoot();
	}

	public boolean isVisible() {
		return getNode().isVisible();
	}

	public void moveTo(final Node parentNode) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.moveNodeAsChild(getNode(), parentNodeProxy.getNode(),
				getNode().isLeft(), false);
	}

	public void moveTo(final Node parentNode, final int position) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.moveNode(getNode(), parentNodeProxy.getNode(), position,
				getNode().isLeft(), false);
	}

	public void removeConnector(final Proxy.Connector connectorToBeRemoved) {
		final ConnectorProxy connectorProxy = (ConnectorProxy) connectorToBeRemoved;
		final ConnectorModel link = connectorProxy.getConnector();
		final MLinkController linkController = (MLinkController) LinkController
				.getController(getModeController());
		linkController.removeArrowLink(link);

	}

	public void setFolded(final boolean folded) {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.setFolded(getNode(), folded);
	}

	public void setNoteText(final String text) {
		final MNoteController noteController = (MNoteController) NoteController
				.getController(getModeController());
		noteController.setNoteText(getNode(), text);
	}

	public void setText(final String text) {
		final MTextController textController = (MTextController) TextController
				.getController(getModeController());
		textController.setNodeText(getNode(), text);
	}

}