/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.features.common.text.DetailTextModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.map.MMapController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Attributes;
import org.freeplane.plugin.script.proxy.Proxy.Connector;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeProxy extends AbstractProxy<NodeModel> implements Node {
	public NodeProxy(final NodeModel node, final ScriptContext scriptContext) {
		super(node, scriptContext);
		if (scriptContext != null)
			scriptContext.accessNode(node);
	}

	// Node: R/W
	public Proxy.Connector addConnectorTo(final Proxy.Node target) {
		return addConnectorTo(target.getId());
	}

	// Node: R/W
	public Proxy.Connector addConnectorTo(final String targetNodeID) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		final ConnectorModel connectorModel = linkController.addConnector(getDelegate(), targetNodeID);
		return new ConnectorProxy(connectorModel, getScriptContext());
	}

	// Node: R/W
	public Proxy.Node createChild() {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate());
		return new NodeProxy(newNodeModel, getScriptContext());
	}

	// Node: R/W
	public Proxy.Node createChild(final int position) {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate(), position);
		return new NodeProxy(newNodeModel, getScriptContext());
	}

	// Node: R/W
	public void delete() {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.deleteNode(getDelegate());
	}

	// NodeRO: R
	public Proxy.Attributes getAttributes() {
		return new AttributesProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public ConvertibleText getAt(String attributeName) {
		return new ConvertibleText(getDelegate(), getScriptContext(), getAttributes().getFirst(attributeName));
	}

	// Node: R/W
	public String putAt(String attributeName, Object value) {
		final String stringValue = Convertible.toString(value);
		final Attributes attributes = getAttributes();
		if (stringValue == null) {
			final int index = attributes.findFirst(attributeName);
			if (index != -1)
				attributes.remove(index);
			// else: ignore request
		}
		else {
			attributes.set(attributeName, stringValue);
		}
		return stringValue;
	}

	// Node: R/W
	public void setAttributes(Map<String, Object> attributeMap) {
		final Attributes attributes = getAttributes();
		attributes.clear();
		for (Entry<String, Object> entry : attributeMap.entrySet()) {
			attributes.set(entry.getKey(), Convertible.toString(entry.getValue()));
		}
	}

	// Node: R/W
	public void setDetails(Object details) {
		final MTextController textController = (MTextController) TextController.getController();
		if (details == null) {
			textController.setDetailsHidden(getDelegate(), false);
			textController.setDetails(getDelegate(), null);
		}
		else{
			textController.setDetails(getDelegate(), convertConvertibleToHtml(details));
		}
	}

	// Node: R/W
	public void setHideDetails(boolean hide) {
		MTextController controller = (MTextController) MTextController.getController();
		controller.setDetailsHidden(getDelegate(), hide);
    }

	// NodeRO: R
	public int getChildPosition(final Proxy.Node childNode) {
		final NodeModel childNodeModel = ((NodeProxy) childNode).getDelegate();
		return getDelegate().getChildPosition(childNodeModel);
	}

	// NodeRO: R
	public List<Proxy.Node> getChildren() {
		return ProxyUtils.createListOfChildren(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public Collection<Connector> getConnectorsIn() {
		return new ConnectorInListProxy(this);
	}

	// NodeRO: R
	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(this);
	}

	// NodeRO: R
	public Convertible getDetails() {
		return new ConvertibleText(getDelegate(), getScriptContext(),
		    DetailTextModel.getDetailTextText(getDelegate()));
	}
	
	// NodeRO: R
	public String getDetailsText() {
		return DetailTextModel.getDetailTextText(getDelegate());
	}

	// NodeRO: R
	public boolean getHideDetails() {
		final DetailTextModel detailText = DetailTextModel.getDetailText(getDelegate());
		return detailText != null && detailText.isHidden();
    }

	// NodeRO: R
	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public Proxy.Icons getIcons() {
		return new IconsProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public Proxy.Link getLink() {
		return new LinkProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Deprecated
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
	public String getPlainNote() {
		final String noteText = NoteModel.getNoteText(getDelegate());
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText);
	}

	// NodeRO: R
	@Deprecated
	public String getNoteText() {
		return getNote().getString();
	}

	// NodeRO: R
	public Convertible getNote() {
		return new ConvertibleNoteText(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public Proxy.Node getParent() {
		final NodeModel parentNode = getDelegate().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode, getScriptContext()) : null;
	}

	// NodeRO: R
	@Deprecated
	public Proxy.Node getParentNode() {
		return getParent();
	}

	// NodeRO: R
	public String getPlainText() {
		return HtmlUtils.htmlToPlain(getDelegate().getText());
	}

	// NodeRO: R
	@Deprecated
	public String getPlainTextContent() {
		return getPlainText();
	}

	// NodeRO: R
	public Proxy.NodeStyle getStyle() {
		return new NodeStyleProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	public String getText() {
		return getDelegate().getText();
	}

	// NodeRO: R
	public Convertible getTo() {
		final NodeModel nodeModel = getDelegate();
		return new ConvertibleNodeText(nodeModel, getScriptContext());
	}

	// NodeRO: R
	public Convertible getValue() {
		return getTo();
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
	public void setNote(Object value) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), convertConvertibleToHtml(value));
	}

	private String convertConvertibleToHtml(Object value) {
		if (value == null)
			return null;
		final String text = Convertible.toString(value);
		return HtmlUtils.isHtmlNode(text) ? text : HtmlUtils.plainToHTML(text);
	}

	// Node: R/W
	@Deprecated
	public void setNoteText(final String text) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), text);
	}

	// Node: R/W
	public void setText(final Object value) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeText(getDelegate(), Convertible.toString(value));
	}

	// NodeRO: R
	public Proxy.Map getMap() {
		final MapModel map = getDelegate().getMap();
		return map != null ? new MapProxy(map, getScriptContext()) : null;
	}

	// NodeRO: R
	@Deprecated
	public List<Node> find(final ICondition condition) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(condition, delegate, getScriptContext());
	}

	// NodeRO: R
	public List<Node> find(final Closure closure) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(closure, delegate, getScriptContext());
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
