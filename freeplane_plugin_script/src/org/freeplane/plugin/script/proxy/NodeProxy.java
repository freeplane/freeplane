/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.map.MMapController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.plugin.script.proxy.Proxy.Connector;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeProxy extends AbstractProxy<NodeModel> implements Node {
	private Tracer tracer = new Tracer();

	public NodeProxy(final NodeModel node) {
		super(node);
	}

	// Node: R/W
	public Proxy.Connector addConnectorTo(final Proxy.Node target) {
		return addConnectorTo(target.getId());
	}

	// Node: R/W
	public Proxy.Connector addConnectorTo(final String targetNodeID) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		final ConnectorModel connectorModel = linkController.addConnector(getDelegate(), targetNodeID);
		return new ConnectorProxy(connectorModel);
	}

	// Node: R/W
	public Proxy.Node createChild() {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate());
		return new NodeProxy(newNodeModel);
	}

	// Node: R/W
	public Proxy.Node createChild(final int position) {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate(), position);
		return new NodeProxy(newNodeModel);
	}

	// Node: R/W
	public void delete() {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.deleteNode(getDelegate());
	}

	// NodeRO: R
	public Proxy.Attributes getAttributes() {
		return new AttributesProxy(getDelegate());
	}

	// NodeRO: R
	public int getChildPosition(final Proxy.Node childNode) {
		// no need to trace this since it's already logged
		final NodeModel childNodeModel = ((NodeProxy) childNode).getDelegate();
		return getDelegate().getChildPosition(childNodeModel);
	}

	// NodeRO: R
	public List<Proxy.Node> getChildren() {
		tracer.accessedChildrenOf(getDelegate());
		return new ArrayList<Proxy.Node>(new AbstractList<Proxy.Node>() {
			@Override
			public Proxy.Node get(final int index) {
				final NodeModel child = (NodeModel) getDelegate().getChildAt(index);
				return new NodeProxy(child);
			}

			@Override
			public int size() {
				return getDelegate().getChildCount();
			}
		});
	}

	// NodeRO: R
	public Collection<Connector> getConnectorsIn() {
		return new ConnectorInListProxy(getDelegate());
	}

	// NodeRO: R
	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(getDelegate());
	}

	// NodeRO: R
	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy(getDelegate());
	}

	// NodeRO: R
	public Proxy.Icons getIcons() {
		return new IconsProxy(getDelegate());
	}

	// NodeRO: R
	public Proxy.Link getLink() {
		return new LinkProxy(getDelegate());
	}

	// NodeRO: R
	public String getId() {
		return getDelegate().createID();
	}
	
	// NodeRO: R
	public String getNodeID() {
		return getId();
	}

	// NodeRO: R
	public int getNodeLevel(final boolean countHidden) {
		return getDelegate().getNodeLevel(countHidden);
	}

	// NodeRO: R
	public String getPlainNoteText() {
		final String noteText = NoteModel.getNoteText(getDelegate());
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText);
	}

	// NodeRO: R
	public String getNoteText() {
		return NoteModel.getNoteText(getDelegate());
	}

	// NodeRO: R
	public Proxy.Node getParent() {
		tracer.accessedParent(getDelegate());
		final NodeModel parentNode = getDelegate().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode) : null;
	}
	
	// NodeRO: R
	public Proxy.Node getParentNode() {
		return getParent();
	}

	// NodeRO: R
	public String getPlainText() {
		return TextController.getController().getPlainTextContent(getDelegate());
	}

	// NodeRO: R
	@Deprecated
	// use getPlainText() instead
	public String getPlainTextContent() {
		return TextController.getController().getPlainTextContent(getDelegate());
	}

	// NodeRO: R
	public Proxy.NodeStyle getStyle() {
		return new NodeStyleProxy(getDelegate());
	}

	// NodeRO: R
	public String getText() {
		return getDelegate().getText();
	}

	// NodeRO: R
	public Convertible getTo() {
	    final NodeModel nodeModel = getDelegate();
		return new ConvertibleNodeText(nodeModel);
    }
	
	// NodeRO: R
	public Object getValue() {
		final NodeModel nodeModel = getDelegate();
		return FormulaUtils.evalNodeText(nodeModel);
	}

	// NodeRO: R
	public boolean isDescendantOf(final Proxy.Node otherNode) {
		// no need to trace this since it's already logged
		final NodeModel otherNodeModel = ((NodeProxy) otherNode).getDelegate();
		NodeModel node = this.getDelegate();
		do {
			if (node.equals(otherNodeModel)) {
				return true;
			}
			node = node.getParentNode();
		} while (node != null);
		return false;
	}

	// NodeRO: R
	public boolean isFolded() {
		return getDelegate().isFolded();
	}

	// NodeRO: R
	public boolean isLeaf() {
		return getDelegate().isLeaf();
	}

	// NodeRO: R
	public boolean isLeft() {
		return getDelegate().isLeft();
	}

	// NodeRO: R
	public boolean isRoot() {
		return getDelegate().isRoot();
	}

	// NodeRO: R
	public boolean isVisible() {
		return getDelegate().isVisible();
	}

	// Node: R/W
	public void moveTo(final Proxy.Node parentNode) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.moveNodeAsChild(getDelegate(), parentNodeProxy.getDelegate(), getDelegate().isLeft(), false);
	}

	// Node: R/W
	public void moveTo(final Proxy.Node parentNode, final int position) {
		final NodeProxy parentNodeProxy = (NodeProxy) parentNode;
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.moveNode(getDelegate(), parentNodeProxy.getDelegate(), position, getDelegate().isLeft(), false);
	}

	// Node: R/W
	public void removeConnector(final Proxy.Connector connectorToBeRemoved) {
		final ConnectorProxy connectorProxy = (ConnectorProxy) connectorToBeRemoved;
		final ConnectorModel link = connectorProxy.getConnector();
		final MLinkController linkController = (MLinkController) LinkController.getController();
		linkController.removeArrowLink(link);
	}

	// Node: R/W
	public void setFolded(final boolean folded) {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.setFolded(getDelegate(), folded);
	}

	// Node: R/W
	public void setPlainNoteText(String text) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), (text == null ? null : HtmlUtils.plainToHTML(text)));
	}

	// Node: R/W
	public void setNoteText(final String text) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), text);
	}

	// Node: R/W
	public void setText(final String text) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeText(getDelegate(), text);
	}

	// NodeRO: R
	public Proxy.Map getMap() {
		final MapModel map = getDelegate().getMap();
		return map != null ? new MapProxy(map) : null;
	}

	// NodeRO: R
	public List<Node> find(final ICondition condition) {
		return ProxyUtils.find(condition, getDelegate());
	}

	// NodeRO: R
	public List<Node> find(final Closure closure) {
		return ProxyUtils.find(closure, getDelegate());
	}

	// NodeRO: R
	public Date getLastModifiedAt() {
		return getDelegate().getHistoryInformation().getLastModifiedAt();
	}

	// Node: R/W
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

	// NodeRO: R
	public Date getCreatedAt() {
		return getDelegate().getHistoryInformation().getCreatedAt();
	}

	// Node: R/W
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
