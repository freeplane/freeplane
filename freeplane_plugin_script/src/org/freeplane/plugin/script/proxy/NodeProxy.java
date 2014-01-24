/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.encrypt.Base64Coding;
import org.freeplane.features.encrypt.EncryptionController;
import org.freeplane.features.encrypt.PasswordStrategy;
import org.freeplane.features.encrypt.mindmapmode.MEncryptionController;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapNavigationUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Attributes;
import org.freeplane.plugin.script.proxy.Proxy.Cloud;
import org.freeplane.plugin.script.proxy.Proxy.Node;
import org.freeplane.plugin.script.proxy.Proxy.Reminder;

class NodeProxy extends AbstractProxy<NodeModel> implements Node {
	private static final Integer ONE = 1;
	private static final Integer ZERO = 0;

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
	public Proxy.Node createChild(final Object value) {
		final Node child = createChild();
		child.setObject(value);
		return child;
	}

	// Node: R/W
	public Proxy.Node createChild(final int position) {
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		mapController.insertNode(newNodeModel, getDelegate(), position);
		return new NodeProxy(newNodeModel, getScriptContext());
	}

	// Node: R/W
	public Proxy.Node appendChild(Proxy.NodeRO node) {
		return appendBranchImpl(node, false);
	}
	
	// Node: R/W
	public Proxy.Node appendBranch(Proxy.NodeRO node) {
		return appendBranchImpl(node, true);
	}

	private Proxy.Node appendBranchImpl(Proxy.NodeRO node, boolean withChildren) {
	    final MClipboardController clipboardController = (MClipboardController) ClipboardController.getController();
		final NodeModel newNodeModel = clipboardController.duplicate(((NodeProxy) node).getDelegate(), withChildren);
		final MMapController mapController = (MMapController) getModeController().getMapController();
		mapController.insertNode(newNodeModel, getDelegate());
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
	public Convertible getAt(final String attributeName) {
		final Object value = getAttributes().getFirst(attributeName);
		return ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), value);
	}

	// Node: R/W
	public Object putAt(final String attributeName, final Object value) {
		final Attributes attributes = getAttributes();
		if (value == null) {
			final int index = attributes.findFirst(attributeName);
			if (index != -1)
				attributes.remove(index);
			// else: ignore request
		}
		else {
			attributes.set(attributeName, value);
		}
		return value;
	}

	// Node: R/W
	public void setAttributes(Map<String, Object> attributeMap) {
		final Attributes attributes = getAttributes();
		attributes.clear();
		for (Entry<String, Object> entry : attributeMap.entrySet()) {
			attributes.set(entry.getKey(), entry.getValue());
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
    public Cloud getCloud() {
        return new CloudProxy(this);
    }

	// NodeRO: R
	public Collection<Proxy.Connector> getConnectorsIn() {
		return new ConnectorInListProxy(this);
	}

	// NodeRO: R
	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(this);
	}

	// NodeRO: R
	public Convertible getDetails() {
		final String detailsText = DetailTextModel.getDetailTextText(getDelegate());
		return (detailsText == null) ? null : new ConvertibleText(getDelegate(), getScriptContext(), detailsText);
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
    public Reminder getReminder() {
        return new ReminderProxy(getDelegate(), getScriptContext());
    }

	// NodeRO: R
	public String getId() {
		return getDelegate().createID();
	}

	// NodeRO: R
	@Deprecated
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
	public String getNoteText() {
		return NoteModel.getNoteText(getDelegate());
	}

	// NodeRO: R
	public Convertible getNote() {
		final String noteText = getNoteText();
		return (noteText == null) ? null : new ConvertibleNoteText(getDelegate(), getScriptContext());
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
    public List<Node> getPathToRoot() {
        return ProxyUtils.createNodeList(Arrays.asList(getDelegate().getPathToRoot()), getScriptContext());
    }

    // NodeRO: R
    public Node getNext() {
        final NodeModel node = MapNavigationUtils.findNext(Direction.FORWARD, getDelegate(), null);
        return node == null ? null : new NodeProxy(node, getScriptContext());
    }
    
    // NodeRO: R
    public Node getPrevious() {
        final NodeModel node = MapNavigationUtils.findPrevious(Direction.BACK, getDelegate(), null);
        return node == null ? null : new NodeProxy(node, getScriptContext());
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
	public boolean hasStyle(String styleName) {
		return NodeStyleProxy.hasStyle(getDelegate(), styleName);
	}

	// NodeRO: R
	public String getText() {
		return getDelegate().getText();
	}
	
	// NodeRO: R
	public String getTransformedText() {
		final TextController textController = TextController.getController();
		return textController.getTransformedTextNoThrow(getDelegate());
	}
	
	// NodeRO: R
	public String getShortText() {
		final TextController textController = TextController.getController();
		return textController.getShortText(getDelegate());
	}
	
	// NodeRO: R
	public String getDisplayedText(){
		if(isMinimized())
			return getShortText();
		else
			return getTransformedText();
	}
	
	// NodeRO: R
	public boolean isMinimized(){
		final TextController textController = TextController.getController();
		return textController.isMinimized(getDelegate());
	}
	
	// NodeRO: R
	public Object getObject() {
		final Object userObject = getDelegate().getUserObject();
		if (userObject instanceof IFormattedObject)
			return ((IFormattedObject) userObject).getObject();
		return userObject;
	}

	// NodeRO: R
	public byte[] getBinary() {
		return Base64Coding.decode64(getDelegate().getText().replaceAll("\\s", ""));
	}

	// NodeRO: R
	public String getFormat() {
		final NodeModel nodeModel = getDelegate();
		final String format = TextController.getController().getNodeFormat(nodeModel);
		if (format == null && nodeModel.getUserObject() instanceof IFormattedObject)
			return ((IFormattedObject) nodeModel.getUserObject()).getPattern();
		return format;
	}

	// NodeRO: R
	public Convertible getTo() {
		return ProxyUtils.nodeModelToConvertible(getDelegate(), getScriptContext());
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
    public boolean isFree() {
        final FreeNode freeNode = Controller.getCurrentModeController().getExtension(FreeNode.class);
        return freeNode.isActive(getDelegate());
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
	public void moveTo(final Proxy.Node parentNodeProxy) {
		final NodeModel parentNode = ((NodeProxy) parentNodeProxy).getDelegate();
        final NodeModel movedNode = getDelegate();
        final MMapController mapController = (MMapController) getModeController().getMapController();
        mapController.moveNodeAsChild(movedNode, parentNode, movedNode.isLeft(), parentNode.isLeft() != movedNode.isLeft());
	}

	// Node: R/W
	public void moveTo(final Proxy.Node parentNodeProxy, final int position) {
        final NodeModel parentNode = ((NodeProxy) parentNodeProxy).getDelegate();
        final NodeModel movedNode = getDelegate();
		final MMapController mapController = (MMapController) getModeController().getMapController();
		((FreeNode)Controller.getCurrentModeController().getExtension(FreeNode.class)).undoableDeactivateHook(movedNode);
		mapController.moveNode(movedNode, parentNode, position, getDelegate().isLeft(), parentNode.isLeft() != movedNode.isLeft());
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
    public void setFree(boolean free) {
        final FreeNode freeNode = Controller.getCurrentModeController().getExtension(FreeNode.class);
        if (free != freeNode.isActive(getDelegate()))
            freeNode.undoableToggleHook(getDelegate());
    }
	
	// Node: R/W
	public void setMinimized(boolean shortened){
		final MTextController textController = (MTextController) TextController.getController();
		textController.setIsMinimized(getDelegate(), shortened);
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
		// the text content of a Convertible object might be null
		if (text == null)
			return null;
		return HtmlUtils.isHtmlNode(text) ? text : HtmlUtils.plainToHTML(text);
	}

	// Node: R/W
	public void setNoteText(final String text) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), text);
	}

	// Node: R/W
	public void setText(final Object value) {
		if (value instanceof String) {
			final MTextController textController = (MTextController) TextController.getController();
			textController.setNodeText(getDelegate(), (String) value);
		}
		else {
			setObject(value);
		}
	}
	
	// Node: R/W
	public void setObject(final Object object) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeObject(getDelegate(), ProxyUtils.transformObject(object, null));
	}

	// Node: R/W
	public void setDateTime(final Date date) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeObject(getDelegate(), ProxyUtils.createDefaultFormattedDateTime(date));
	}

	// Node: R/W
	public void setBinary(final byte[] data) {
		setObject(Base64Coding.encode64(data).replaceAll("(.{74})", "$1\n"));
	}

	public void setFormat(final String format) {
		final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
		    .getExtension(NodeStyleController.class);
		styleController.setNodeFormat(getDelegate(), format);
	}
	
	public void setLeft(final boolean isLeft) {
		getDelegate().setLeft(isLeft);
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
	public List<Node> find(final Closure<Boolean> closure) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(closure, delegate, getScriptContext());
	}

	// NodeRO: R
	public List<Node> findAll() {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.findAll(delegate, getScriptContext(), true);
    }

	// NodeRO: R
	public List<Node> findAllDepthFirst() {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.findAll(delegate, getScriptContext(), false);
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

	//
	// Node arithmetics for
	//     Node <operator> Number
	//     Node <operator> Node
	// See NodeArithmeticsCategory for 
	//     Number <operator> Node
	//
	public Number and(final Number number) {
		return NumberMath.and(this.getTo().getNum0(), number);
	}

	public Number and(final Proxy.Node node) {
		return NumberMath.and(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number div(final Number number) {
		return NumberMath.divide(this.getTo().getNum0(), number);
	}

	public Number div(final Proxy.Node node) {
		return NumberMath.divide(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number minus(final Number number) {
		return NumberMath.subtract(this.getTo().getNum0(), number);
	}

	public Number minus(final Proxy.Node node) {
		return NumberMath.subtract(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number mod(final Number number) {
		return NumberMath.mod(this.getTo().getNum0(), number);
	}

	public Number mod(final Proxy.Node node) {
		return NumberMath.mod(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number multiply(final Number number) {
		return NumberMath.multiply(this.getTo().getNum0(), number);
	}

	public Number multiply(final Proxy.Node node) {
		return NumberMath.multiply(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number or(final Number number) {
		return NumberMath.or(this.getTo().getNum0(), number);
	}

	public Number or(final Proxy.Node node) {
		return NumberMath.or(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number plus(final Number number) {
		return NumberMath.add(this.getTo().getNum0(), number);
	}

	public Number plus(final Proxy.Node node) {
		return NumberMath.add(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number power(final Number number) {
		return DefaultGroovyMethods.power(this.getTo().getNum0(), number);
	}

	public Number power(final Proxy.Node node) {
		return DefaultGroovyMethods.power(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number xor(final Number number) {
		return NumberMath.xor(this.getTo().getNum0(), number);
	}

	public Number xor(final Proxy.Node node) {
		return NumberMath.xor(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number negative() {
		return NumberMath.subtract(ZERO, this.getTo().getNum0());
	}

	public Number next() {
		return NumberMath.add(this.getTo().getNum0(), ONE);
	}

	public Number positive() {
		return this.getTo().getNum0();
	}

	public Number previous() {
		return NumberMath.subtract(this.getTo().getNum0(), ONE);
	}

    public boolean hasEncryption() {
        return getEncryptionModel() != null;
    }

    public boolean isEncrypted() {
        final EncryptionModel encryptionModel = getEncryptionModel();
        return encryptionModel != null && !encryptionModel.isAccessible();
    }

    public void encrypt(String password) {
        if (!isEncrypted())
            getEncryptionController().toggleCryptState(getDelegate(), makePasswordStrategy(password));
    }

    public void decrypt(String password) {
        if (isEncrypted())
            getEncryptionController().toggleCryptState(getDelegate(), makePasswordStrategy(password));
    }
    
    public void removeEncryption(String password) {
        getEncryptionController().removeEncryption(getDelegate(), makePasswordStrategy(password));
    }

    private PasswordStrategy makePasswordStrategy(final String password) {
        return new PasswordStrategy() {
            public StringBuilder getPassword() {
                return new StringBuilder(password);
            }

            public StringBuilder getPasswordWithConfirmation() {
                return getPassword();
            }

            public void onWrongPassword() {
                LogUtils.info("wrong password for node " + getDelegate());
                setStatusInfo(TextUtils.getText("accessories/plugins/EncryptNode.properties_wrong_password"));
            }

            public boolean isCancelled() {
                return false;
            }
        };
    }

    private void setStatusInfo(String text) {
        final ViewController viewController = Controller.getCurrentController().getViewController();
        viewController.out(text);
    }
    
    private MEncryptionController getEncryptionController() {
        return (MEncryptionController) Controller.getCurrentModeController().getExtension(EncryptionController.class);
    }

    private EncryptionModel getEncryptionModel() {
        return EncryptionModel.getModel(getDelegate());
    }
}
