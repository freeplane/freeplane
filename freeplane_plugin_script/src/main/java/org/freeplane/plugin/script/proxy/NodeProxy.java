/**
 *
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.NumberMath;
import org.freeplane.api.Attributes;
import org.freeplane.api.Cloud;
import org.freeplane.api.Connector;
import org.freeplane.api.Node;
import org.freeplane.api.NodeCondition;
import org.freeplane.api.NodeRO;
import org.freeplane.api.NodeStyle;
import org.freeplane.api.NodeToComparableMapper;
import org.freeplane.api.Reminder;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.Quantity;
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
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
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

import groovy.lang.Closure;

class NodeProxy extends AbstractProxy<NodeModel> implements Proxy.Node {
	private static final Integer ONE = 1;
	private static final Integer ZERO = 0;

	public NodeProxy(final NodeModel node, final ScriptContext scriptContext) {
		super(node, scriptContext);
		if (scriptContext != null)
			scriptContext.accessNode(node);
	}

	// Node: R/W
	@Override
	public Proxy.Connector addConnectorTo(final Node target) {
		return addConnectorTo(target.getId());
	}

	// Node: R/W
	@Override
	public Proxy.Connector addConnectorTo(final String targetNodeID) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		final ConnectorModel connectorModel = linkController.addConnector(getDelegate(), targetNodeID);
		return new ConnectorProxy(connectorModel, getScriptContext());
	}

	// Node: R/W
	@Override
	public Node createChild() {
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		getMapController().insertNode(newNodeModel, getDelegate());
		return new NodeProxy(newNodeModel, getScriptContext());
	}

	private MMapController getMapController() {
		return (MMapController) getModeController().getMapController();
	}

	// Node: R/W
	@Override
	public Node createChild(final Object value) {
		final Node child = createChild();
		child.setObject(value);
		return child;
	}

	// Node: R/W
	@Override
	public Node createChild(final int position) {
		final NodeModel newNodeModel = new NodeModel(getDelegate().getMap());
		getMapController().insertNode(newNodeModel, getDelegate(), position);
		return new NodeProxy(newNodeModel, getScriptContext());
	}

	// Node: R/W
	@Override
	public Node appendChild(NodeRO node) {
		return appendBranchImpl(node, false);
	}

	// Node: R/W
	@Override
	public Node appendBranch(NodeRO node) {
		return appendBranchImpl(node, true);
	}

	private Node appendBranchImpl(NodeRO node, boolean withChildren) {
	    final MClipboardController clipboardController = (MClipboardController) ClipboardController.getController();
		final NodeModel newNodeModel = clipboardController.duplicate(((NodeProxy) node).getDelegate(), withChildren);
		getMapController().insertNode(newNodeModel, getDelegate());
		return new NodeProxy(newNodeModel, getScriptContext());
    }

	// Node: R/W
	@Override
	public void delete() {
		getMapController().deleteNode(getDelegate());
	}

	// NodeRO: R
	@Override
	public Proxy.Attributes getAttributes() {
		return AttributesProxy.withRawValues(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Override
	public Convertible getAt(final String attributeName) {
		final Object value = getAttributes().getFirst(attributeName);
		return ProxyUtils.attributeValueToConvertible(getDelegate(), getScriptContext(), value);
	}

	// Node: R/W
	@Override
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
	@Override
	public void setAttributes(Map<String, Object> attributeMap) {
		final Attributes attributes = getAttributes();
		attributes.clear();
		for (Entry<String, Object> entry : attributeMap.entrySet()) {
			attributes.set(entry.getKey(), entry.getValue());
		}
	}

	// Node: R/W
	@Override
	public void setDetails(Object details) {
		setDetailsText(convertConvertibleToHtml(details));
	}

	// Node: R/W
    @Override
	public void setDetailsText(String html) {
        final MTextController textController = (MTextController) TextController.getController();
		if (html == null) {
			textController.setDetailsHidden(getDelegate(), false);
			textController.setDetails(getDelegate(), null);
		}
		else{
			textController.setDetails(getDelegate(), html);
		}
    }

	// Node: R/W
	@Override
	public void setHideDetails(boolean hide) {
		MTextController controller = MTextController.getController();
		controller.setDetailsHidden(getDelegate(), hide);
    }

	// NodeRO: R
	@Override
	public int getChildPosition(final Node childNode) {
		final NodeModel childNodeModel = ((NodeProxy) childNode).getDelegate();
		return getDelegate().getIndex(childNodeModel);
	}

	// NodeRO: R
	@Override
	public List<Proxy.Node> getChildren() {
		return ProxyUtils.createListOfChildren(getDelegate(), getScriptContext());
	}

    // NodeRO: R
    @Override
	public Cloud getCloud() {
        return new CloudProxy(this);
    }

	// NodeRO: R
	@Override
	public Collection<Proxy.Connector> getConnectorsIn() {
		return new ConnectorInListProxy(this);
	}

	// NodeRO: R
	@Override
	public Collection<Proxy.Connector> getConnectorsOut() {
		return new ConnectorOutListProxy(this);
	}

	// NodeRO: R
	@Override
	public Convertible getDetails() {
		final String detailsText = DetailTextModel.getDetailTextText(getDelegate());
		return (detailsText == null) ? null : new ConvertibleHtmlText(getDelegate(), getScriptContext(), detailsText);
	}

	// NodeRO: R
	@Override
	public String getDetailsText() {
		return DetailTextModel.getDetailTextText(getDelegate());
	}

	// NodeRO: R
	@Override
	public boolean getHideDetails() {
		final DetailTextModel detailText = DetailTextModel.getDetailText(getDelegate());
		return detailText != null && detailText.isHidden();
    }

	// NodeRO: R
	@Override
	public Proxy.ExternalObject getExternalObject() {
		return new ExternalObjectProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Override
	public Proxy.Icons getIcons() {
		return new IconsProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Override
	public Proxy.Link getLink() {
		return new LinkProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
    @Override
	public Reminder getReminder() {
        return new ReminderProxy(getDelegate(), getScriptContext());
    }

	// NodeRO: R
	@Override
	public String getId() {
		return getDelegate().createID();
	}

	// NodeRO: R
	@Override
	@Deprecated
	public String getNodeID() {
		return getId();
	}

	// NodeRO: R
	@Override
	public int getNodeLevel(final boolean countHidden) {
		return getDelegate().getNodeLevel(countHidden);
	}

	// NodeRO: R
	public String getPlainNote() {
		final String noteText = NoteModel.getNoteText(getDelegate());
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText);
	}

	// NodeRO: R
	@Override
	public String getNoteText() {
		return NoteModel.getNoteText(getDelegate());
	}

	// NodeRO: R
	@Override
	public Convertible getNote() {
		final String noteText = getNoteText();
		return (noteText == null) ? null : new ConvertibleNoteText(getDelegate(), getScriptContext(), noteText);
	}

	// NodeRO: R
	@Override
	public Node getParent() {
		final NodeModel parentNode = getDelegate().getParentNode();
		return parentNode != null ? new NodeProxy(parentNode, getScriptContext()) : null;
	}

	// NodeRO: R
	@Override
	@Deprecated
	public Node getParentNode() {
		return getParent();
	}

    // NodeRO: R
    @Override
	public List<? extends Node> getPathToRoot() {
        return ProxyUtils.createNodeList(Arrays.asList(getDelegate().getPathToRoot()), getScriptContext());
    }

    // NodeRO: R
    @Override
	public Node getNext() {
        final NodeModel node = MapNavigationUtils.findNext(Direction.FORWARD, getDelegate(), null);
        return node == null ? null : new NodeProxy(node, getScriptContext());
    }

    // NodeRO: R
    @Override
	public Node getPrevious() {
        final NodeModel node = MapNavigationUtils.findPrevious(Direction.BACK, getDelegate(), null);
        return node == null ? null : new NodeProxy(node, getScriptContext());
    }

	// NodeRO: R
	@Override
	public String getPlainText() {
		return HtmlUtils.htmlToPlain(getDelegate().getText());
	}

	// NodeRO: R
	@Override
	@Deprecated
	public String getPlainTextContent() {
		return getPlainText();
	}

	// NodeRO: R
	@Override
	public String getHtmlText() {
		final String nodeText = getDelegate().getText();
		if (HtmlUtils.isHtmlNode(nodeText))
			return nodeText;
		else
			return HtmlUtils.plainToHTML(nodeText);
	}

	// NodeRO: R
	@Override
	public NodeStyle getStyle() {
		return new NodeStyleProxy(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Override
	public boolean hasStyle(String styleName) {
		return NodeStyleProxy.hasStyle(getDelegate(), styleName);
	}

	// NodeRO: R
	@Override
	public String getText() {
		return getDelegate().getText();
	}

	// NodeRO: R
	@Override
	public String getTransformedText() {
		final TextController textController = TextController.getController();
		return textController.getTransformedTextNoThrow(getDelegate());
	}

	// NodeRO: R
	@Override
	public String getShortText() {
		final TextController textController = TextController.getController();
		return textController.getShortPlainText(getDelegate());
	}

	// NodeRO: R
	@Override
	public String getDisplayedText(){
		if(isMinimized())
			return getShortText();
		else
			return getTransformedText();
	}

	// NodeRO: R
	@Override
	public boolean isMinimized(){
		final TextController textController = TextController.getController();
		return textController.isMinimized(getDelegate());
	}

	// NodeRO: R
	@Override
	public Object getObject() {
		final Object userObject = getDelegate().getUserObject();
		if (userObject instanceof IFormattedObject)
			return ((IFormattedObject) userObject).getObject();
		return userObject;
	}

	// NodeRO: R
	@Override
	public byte[] getBinary() {
		return Base64Coding.decode64(getDelegate().getText().replaceAll("\\s", ""));
	}

	// NodeRO: R
	@Override
	public String getFormat() {
		final NodeModel nodeModel = getDelegate();
		final String format = TextController.getController().getNodeFormat(nodeModel);
		if (format == null && nodeModel.getUserObject() instanceof IFormattedObject)
			return ((IFormattedObject) nodeModel.getUserObject()).getPattern();
		return format;
	}

	// NodeRO: R
	@Override
	public Convertible getTo() {
		return ProxyUtils.nodeModelToConvertible(getDelegate(), getScriptContext());
	}

	// NodeRO: R
	@Override
	public Convertible getValue() {
		return getTo();
	}

	// NodeRO: R
	@Override
	public boolean isDescendantOf(final Node otherNode) {
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
	@Override
	public boolean isFolded() {
		return getDelegate().isFolded();
	}

    // NodeRO: R
    @Override
	public boolean isFree() {
        final FreeNode freeNode = Controller.getCurrentModeController().getExtension(FreeNode.class);
        return freeNode.isActive(getDelegate());
    }

	// NodeRO: R
	@Override
	public boolean isLeaf() {
		return getDelegate().isLeaf();
	}

	// NodeRO: R
	@Override
	public boolean isLeft() {
		return getDelegate().isLeft();
	}

	// NodeRO: R
	@Override
	public boolean isRoot() {
		return getDelegate().isRoot();
	}

	// NodeRO: R
	@Override
	public boolean isVisible() {
		return getDelegate().hasVisibleContent();
	}

	// Node: R/W
	@Override
	public void moveTo(final Node parentNodeProxy) {
		final NodeModel parentNode = ((NodeProxy) parentNodeProxy).getDelegate();
        final NodeModel movedNode = getDelegate();
		boolean oldSide = movedNode.isLeft();
		boolean newSide = parentNode.isRoot() ? oldSide : parentNode.isLeft();
        getMapController().moveNodesAsChildren(Arrays.asList(movedNode), parentNode, newSide, newSide != oldSide);
	}

	// Node: R/W
	@Override
	public void moveTo(final Node parentNodeProxy, final int position) {
        final NodeModel parentNode = ((NodeProxy) parentNodeProxy).getDelegate();
        final NodeModel movedNode = getDelegate();
		Controller.getCurrentModeController().getExtension(FreeNode.class).undoableDeactivateHook(movedNode);
		boolean oldSide = movedNode.isLeft();
		boolean newSide = parentNode.isRoot() ? oldSide : parentNode.isLeft();
		getMapController().moveNodes(Arrays.asList(movedNode), parentNode, position, newSide, newSide != oldSide);
	}

	// Node: R/W
	@Override
	public void removeConnector(final Connector connectorToBeRemoved) {
		final ConnectorProxy connectorProxy = (ConnectorProxy) connectorToBeRemoved;
		final ConnectorModel link = connectorProxy.getConnector();
		final MLinkController linkController = (MLinkController) LinkController.getController();
		linkController.removeArrowLink(link);
	}

	// Node: R/W
	@Override
	public void setFolded(final boolean folded) {
		getMapController().setFolded(getDelegate(), folded);
	}

    // Node: R/W
    @Override
	public void setFree(boolean free) {
        final FreeNode freeNode = Controller.getCurrentModeController().getExtension(FreeNode.class);
        if (free != freeNode.isActive(getDelegate()))
            freeNode.undoableToggleHook(getDelegate());
    }

	// Node: R/W
	@Override
	public void setMinimized(boolean shortened){
		final MTextController textController = (MTextController) TextController.getController();
		textController.setIsMinimized(getDelegate(), shortened);
	}

	// Node: R/W
	@Override
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
	@Override
	public void setNoteText(final String html) {
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setNoteText(getDelegate(), html);
	}

	// Node: R/W
	@Override
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
	@Override
	public void setObject(final Object object) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeObject(getDelegate(), ProxyUtils.transformObject(object, null));
	}

	// Node: R/W
	@Override
	public void setDateTime(final Date date) {
		final MTextController textController = (MTextController) TextController.getController();
		textController.setNodeObject(getDelegate(), ProxyUtils.createDefaultFormattedDateTime(date));
	}

	// Node: R/W
	@Override
	public void setBinary(final byte[] data) {
		setObject(Base64Coding.encode64(data).replaceAll("(.{74})", "$1\n"));
	}

	@Override
	public void setFormat(final String format) {
		final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
		    .getExtension(NodeStyleController.class);
		styleController.setNodeFormat(getDelegate(), format);
	}

	@Override
	public void setLeft(final boolean isLeft) {
		getDelegate().setLeft(isLeft);
	}

	// NodeRO: R
	@Override
	public Proxy.Map getMap() {
		final MapModel map = getDelegate().getMap();
		return map != null ? new MapProxy(map, getScriptContext()) : null;
	}

	// NodeRO: R
	@Override
	@Deprecated
	public List<? extends Node> find(final ICondition condition) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(condition, delegate, getScriptContext());
	}

	// NodeRO: R
	@Override
	public List<? extends Node> find(final Closure<Boolean> closure) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(closure, delegate, getScriptContext());
	}

	@Override
	public List<? extends Node> find(NodeCondition condition) {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.find(condition, delegate, getScriptContext());
	}

	// NodeRO: R
	@Override
	public List<? extends Node> findAll() {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.findAll(delegate, getScriptContext(), true);
    }

	// NodeRO: R
	@Override
	public List<? extends Node> findAllDepthFirst() {
		final NodeModel delegate = getDelegate();
		if (getScriptContext() != null)
			getScriptContext().accessBranch(delegate);
		return ProxyUtils.findAll(delegate, getScriptContext(), false);
    }

	// NodeRO: R
	@Override
	public Date getLastModifiedAt() {
		return getDelegate().getHistoryInformation().getLastModifiedAt();
	}

	// Node: R/W
	@Override
	public void setLastModifiedAt(final Date date) {
		final Date oldDate = getDelegate().getHistoryInformation().getLastModifiedAt();
		final IActor actor = new IActor() {
			@Override
			public void act() {
				getDelegate().getHistoryInformation().setLastModifiedAt(date);
			}

			@Override
			public String getDescription() {
				return "setLastModifiedAt";
			}

			@Override
			public void undo() {
				getDelegate().getHistoryInformation().setLastModifiedAt(oldDate);
			}
		};
		getModeController().execute(actor, getDelegate().getMap());
	}

	// NodeRO: R
	@Override
	public Date getCreatedAt() {
		return getDelegate().getHistoryInformation().getCreatedAt();
	}

	// Node: R/W
	@Override
	public void setCreatedAt(final Date date) {
		final Date oldDate = getDelegate().getHistoryInformation().getCreatedAt();
		final IActor actor = new IActor() {
			@Override
			public void act() {
				getDelegate().getHistoryInformation().setCreatedAt(date);
			}

			@Override
			public String getDescription() {
				return "setCreatedAt";
			}

			@Override
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

	public Number and(final Node node) {
		return NumberMath.and(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number div(final Number number) {
		return NumberMath.divide(this.getTo().getNum0(), number);
	}

	public Number div(final Node node) {
		return NumberMath.divide(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number minus(final Number number) {
		return NumberMath.subtract(this.getTo().getNum0(), number);
	}

	public Number minus(final Node node) {
		return NumberMath.subtract(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number mod(final Number number) {
		return NumberMath.mod(this.getTo().getNum0(), number);
	}

	public Number mod(final Node node) {
		return NumberMath.mod(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number multiply(final Number number) {
		return NumberMath.multiply(this.getTo().getNum0(), number);
	}

	public Number multiply(final Node node) {
		return NumberMath.multiply(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number or(final Number number) {
		return NumberMath.or(this.getTo().getNum0(), number);
	}

	public Number or(final Node node) {
		return NumberMath.or(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number plus(final Number number) {
		return NumberMath.add(this.getTo().getNum0(), number);
	}

	public Number plus(final Node node) {
		return NumberMath.add(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number power(final Number number) {
		return DefaultGroovyMethods.power(this.getTo().getNum0(), number);
	}

	public Number power(final Node node) {
		return DefaultGroovyMethods.power(this.getTo().getNum0(), node.getTo().getNum0());
	}

	public Number xor(final Number number) {
		return NumberMath.xor(this.getTo().getNum0(), number);
	}

	public Number xor(final Node node) {
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

    @Override
	public boolean hasEncryption() {
        return getEncryptionModel() != null;
    }

    @Override
	public boolean isEncrypted() {
        final EncryptionModel encryptionModel = getEncryptionModel();
        return encryptionModel != null && !encryptionModel.isAccessible();
    }

    @Override
	public void encrypt(String password) {
        if (!isEncrypted())
            getEncryptionController().toggleCryptState(getDelegate(), makePasswordStrategy(password));
    }

    @Override
	public void decrypt(String password) {
        if (isEncrypted())
            getEncryptionController().toggleCryptState(getDelegate(), makePasswordStrategy(password));
    }

    @Override
	public void removeEncryption(String password) {
        getEncryptionController().removeEncryption(getDelegate(), makePasswordStrategy(password));
    }

    private PasswordStrategy makePasswordStrategy(final String password) {
        return new PasswordStrategy() {
            @Override
			public StringBuilder getPassword() {
                return new StringBuilder(password);
            }

            @Override
			public StringBuilder getPasswordWithConfirmation() {
                return getPassword();
            }

            @Override
			public void onWrongPassword() {
                LogUtils.info("wrong password for node " + getDelegate());
                setStatusInfo(TextUtils.getText("accessories/plugins/EncryptNode.properties_wrong_password"));
            }

            @Override
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


	@Override
	public int getHorizontalShift(){
		return LocationController.getController().getHorizontalShift(getDelegate()).toBaseUnitsRounded();
	}

	@Override
	public int getVerticalShift(){
		return LocationController.getController().getVerticalShift(getDelegate()).toBaseUnitsRounded();
	}

	@Override
	public void setHorizontalShift(final int horizontalShift){
		final Quantity<LengthUnits> horizontalShiftQuantity = new Quantity<LengthUnits>(horizontalShift, LengthUnits.px);
		((MLocationController) LocationController.getController()).setHorizontalShift(getDelegate(),horizontalShiftQuantity);
	}

	@Override
	public void setHorizontalShift(Quantity<LengthUnits> verticalShift) {
		((MLocationController) LocationController.getController()).setHorizontalShift(getDelegate(), verticalShift);
	}

	@Override
	public void setHorizontalShift(String verticalShift) {
		((MLocationController) LocationController.getController()).setHorizontalShift(getDelegate(), Quantity.fromString(verticalShift, LengthUnits.px));
	}

	@Override
	public void setVerticalShift(final int verticalShift){
		final Quantity<LengthUnits> verticalShiftQuantity = new Quantity<LengthUnits>(verticalShift, LengthUnits.px);
		((MLocationController) LocationController.getController()).setVerticalShift(getDelegate(), verticalShiftQuantity);
	}

	@Override
	public void setVerticalShift(Quantity<LengthUnits> verticalShift) {
		((MLocationController) LocationController.getController()).setVerticalShift(getDelegate(), verticalShift);
	}

	@Override
	public void setVerticalShift(String verticalShift) {
		((MLocationController) LocationController.getController()).setVerticalShift(getDelegate(), Quantity.fromString(verticalShift, LengthUnits.px));
	}

	@Override
	public int getMinimalDistanceBetweenChildren(){
		return LocationController.getController().getMinimalDistanceBetweenChildren(getDelegate()).toBaseUnitsRounded();
	}

	@Override
	public void setMinimalDistanceBetweenChildren(final int minimalDistanceBetweenChildren){
		final Quantity<LengthUnits> minimalDistanceBetweenChildrenQuantity = new Quantity<LengthUnits>(minimalDistanceBetweenChildren, LengthUnits.px);
		((MLocationController) LocationController.getController()).setMinimalDistanceBetweenChildren(getDelegate(), minimalDistanceBetweenChildrenQuantity);
	}

	@Override
	public void setMinimalDistanceBetweenChildren(Quantity<LengthUnits> minimalDistanceBetweenChildren) {
		((MLocationController) LocationController.getController()).setMinimalDistanceBetweenChildren(getDelegate(), minimalDistanceBetweenChildren);
	}

	@Override
	public void setMinimalDistanceBetweenChildren(String minimalDistanceBetweenChildren) {
		((MLocationController) LocationController.getController()).setMinimalDistanceBetweenChildren(getDelegate(), Quantity.fromString(minimalDistanceBetweenChildren, LengthUnits.px));
	}

	@Override
	public void sortChildrenBy(Closure<Comparable<Object>> closure) {
		final Comparator<NodeModel> comparator = comparatorByClosureResult(closure);
		sortChildrenBy(comparator);
	}

	@Override
	public void sortChildrenBy(NodeToComparableMapper mapper) {
		final Comparator<NodeModel> comparator = comparatorByMapper(mapper);
		sortChildrenBy(comparator);
	}

	private Comparator<NodeModel> comparatorByMapper(final NodeToComparableMapper mapper) {
		return new Comparator<NodeModel>() {
			@Override
			public int compare(NodeModel o1, NodeModel o2) {
				final NodeProxy p1 = new NodeProxy(o1, getScriptContext());
				final NodeProxy p2 = new NodeProxy(o1, getScriptContext());
				return mapper.toComparable(p1).compareTo(mapper.toComparable(p2));
			}
		};
	}
	private void sortChildrenBy(final Comparator<NodeModel> comparator) {
		getScriptContext().accessNode(getDelegate());
		NodeModel node = getDelegate();
		final ArrayList<NodeModel> children = new ArrayList<NodeModel>(node.getChildren());
		Collections.sort(children, comparator);
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		int i = 0;
		for (final NodeModel child : children) {
			Controller.getCurrentModeController().getExtension(FreeNode.class)
			    .undoableDeactivateHook(child);
			mapController.moveNode(child, i++);
		}
	}


	private Comparator<NodeModel> comparatorByClosureResult(final Closure<Comparable<Object>> closure) {
		return new Comparator<NodeModel>() {
			@Override
			public int compare(NodeModel o1, NodeModel o2) {
				final NodeProxy p1 = new NodeProxy(o1, getScriptContext());
				final NodeProxy p2 = new NodeProxy(o1, getScriptContext());
				return closure.call(p1).compareTo(closure.call(p2));
			}
		};
	}

	@Override
	public int getCountNodesSharingContent() {
		return getDelegate().allClones().size() - 1;
	}

	@Override
	public int getCountNodesSharingContentAndSubtree() {
		return getDelegate().subtreeClones().size() - 1;
	}

	@Override
	public List<? extends Node> getNodesSharingContent() {
		final ArrayList<NodeModel> nodeModels = new ArrayList<NodeModel>(getDelegate().allClones().toCollection());
		nodeModels.remove(getDelegate());
		return ProxyUtils.createNodeList(nodeModels, getScriptContext());
	}

	@Override
	public List<? extends Node> getNodesSharingContentAndSubtree() {
		final ArrayList<NodeModel> nodeModels = new ArrayList<NodeModel>(getDelegate().subtreeClones().toCollection());
		nodeModels.remove(getDelegate());
		return ProxyUtils.createNodeList(nodeModels, getScriptContext());
	}

	@Override
	public Node appendAsCloneWithSubtree(NodeRO toBeCloned) {
		return appendAsCloneImpl(((NodeProxy) toBeCloned).getDelegate(), true);
	}

	@Override
	public Node appendAsCloneWithoutSubtree(NodeRO toBeCloned) {
		return appendAsCloneImpl(((NodeProxy) toBeCloned).getDelegate(), false);
	}

	private Node appendAsCloneImpl(NodeModel toBeCloned, boolean withSubtree) {
		final NodeModel target = getDelegate();
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		if (toBeCloned.getParentNode() == null || toBeCloned.isRoot())
			throw new IllegalArgumentException("can't clone root node or node without parent");
		if (!toBeCloned.getMap().equals(getDelegate().getMap()))
			throw new IllegalArgumentException("can't clone a node from another map");
		if (toBeCloned.subtreeContainsCloneOf(target))
			throw new IllegalArgumentException("can't clone a node which has this node as child");
		final NodeModel clone = withSubtree ? toBeCloned.cloneTree() : toBeCloned.cloneContent();
		mapController.addNewNode(clone, target, target.getChildCount(), target.isNewChildLeft());
		return new NodeProxy(clone, getScriptContext());
	}

	@Override
	public void pasteAsClone() {
		final MClipboardController clipboardController = (MClipboardController) ClipboardController
			    .getController();
		clipboardController.addClone(clipboardController.getClipboardContents(), getDelegate());
	}

}
