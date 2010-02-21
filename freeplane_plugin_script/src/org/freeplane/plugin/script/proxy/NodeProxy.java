/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.undo.IActor;
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
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeProxy extends AbstractProxy<NodeModel> implements Node {

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
				getDelegate(), targetNodeID);
		return new ConnectorProxy(connectorModel, getModeController());
	}

	public Node createChild() {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate());
		return new NodeProxy(newNodeModel, getModeController());
	}

	public Node createChild(final int position) {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate(), position);
		return new NodeProxy(newNodeModel, getModeController());
	}

	public void delete() {
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.deleteNode(getDelegate());
	}

	public Proxy.Attributes getAttributes() {
		return new AttributesProxy(getDelegate(), getModeController());
	}

	public int getChildPosition(final Node childNode) {
		final NodeModel childNodeModel = ((NodeProxy) childNode).getDelegate();
		return getDelegate().getChildPosition(childNodeModel);
	}

	public List<Node> getChildren() {
		return new AbstractList<Node>() {

			@Override
			public Node get(final int index) {
				final NodeModel child = (NodeModel) getDelegate().getChildAt(index);
				return new NodeProxy(child, getModeController());
			}

			@Override
			public int size() {
				return getDelegate().getChildCount();
			}
		};
	}

	public Collection<Connector> getConnectorsIn() {
		return new ConnectorInListProxy(getDelegate(), getModeController());
	}

	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(getDelegate(), getModeController());
	}

	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy(getDelegate(), getModeController());
	}

	public Proxy.Icons getIcons() {
		return new IconsProxy(getDelegate(), getModeController());
	}

	public Proxy.Link getLink() {
		return new LinkProxy(getDelegate(), getModeController());
	}

	public String getNodeID() {
		return getDelegate().createID();
	}

	public int getNodeLevel(final boolean countHidden) {
		return getDelegate().getNodeLevel(countHidden);
	}

	public String getNoteText() {
		return NoteModel.getNoteText(getDelegate());
	}

	public Node getParentNode() {
		final NodeModel parentNode = getDelegate().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode,
				getModeController()) : null;
	}

	public String getPlainTextContent() {
		return getDelegate().getPlainTextContent();
	}

	public Proxy.NodeStyle getStyle() {
		return new NodeStyleProxy(getDelegate(), getModeController());
	}

	public String getText() {
		return getDelegate().getText();
	}

	public boolean isDescendantOf(final Node otherNode) {
		final NodeModel otherNodeModel = ((NodeProxy) otherNode).getDelegate();
		NodeModel node = this.getDelegate();
		do {
			if (node.equals(otherNodeModel)) {
				return true;
			}
			node = getDelegate().getParentNode();
		} while (node != null);
		return false;
	}

	public boolean isFolded() {
		return getDelegate().isFolded();
	}

	public boolean isLeaf() {
		return getDelegate().isLeaf();
	}

	public boolean isLeft() {
		return getDelegate().isLeft();
	}

	public boolean isRoot() {
		return getDelegate().isRoot();
	}

	public boolean isVisible() {
		return getDelegate().isVisible();
	}

	public void moveTo(final Node parentNode) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.moveNodeAsChild(getDelegate(), parentNodeProxy.getDelegate(),
				getDelegate().isLeft(), false);
	}

	public void moveTo(final Node parentNode, final int position) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController()
				.getMapController();
		mapController.moveNode(getDelegate(), parentNodeProxy.getDelegate(), position,
				getDelegate().isLeft(), false);
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
		mapController.setFolded(getDelegate(), folded);
	}

	public void setNoteText(final String text) {
		final MNoteController noteController = (MNoteController) NoteController
				.getController(getModeController());
		noteController.setNoteText(getDelegate(), text);
	}

	public void setText(final String text) {
		final MTextController textController = (MTextController) TextController
				.getController(getModeController());
		textController.setNodeText(getDelegate(), text);
	}

	public Map getMap() {
		final MapModel map = getDelegate().getMap();
		return map != null ? new MapProxy(map,
				getModeController()) : null;
    }

	public List<Node> find(ICondition condition) {
		return ProxyUtils.find(condition, getModeController(), getDelegate());
	}

	public List<Node> find(final Closure closure) {
		return ProxyUtils.find(closure, getModeController(), getDelegate());
	}
	
	public Date getLastModifiedAt() {
		return getDelegate().getHistoryInformation().getLastModifiedAt();
	}
	
	public void setLastModifiedAt(final Date date) {
		final Date oldDate = getDelegate().getHistoryInformation().getLastModifiedAt();
		final IActor actor = new IActor() {
			public void act() {
				getDelegate().getHistoryInformation().setLastModifiedAt(date);
			}

			public String getDescription() {
				return "setLastModifiedAt";
			}

			public void undo() {
				getDelegate().getHistoryInformation().setLastModifiedAt(oldDate);
			}
		};
		getModeController().execute(actor, getDelegate().getMap());
	}
	
	public Date getCreatedAt() {
		return getDelegate().getHistoryInformation().getCreatedAt();
	}
	
	public void setCreatedAt(final Date date) {
		final Date oldDate = getDelegate().getHistoryInformation().getCreatedAt();
		final IActor actor = new IActor() {
			public void act() {
				getDelegate().getHistoryInformation().setCreatedAt(date);
			}

			public String getDescription() {
				return "setCreatedAt";
			}

			public void undo() {
				getDelegate().getHistoryInformation().setCreatedAt(oldDate);
			}
		};
		getModeController().execute(actor, getDelegate().getMap());
	}
}