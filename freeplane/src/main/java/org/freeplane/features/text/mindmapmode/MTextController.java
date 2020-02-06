/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.text.mindmapmode;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.ConditionPredicate;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.IContentTransformer;
import org.freeplane.features.text.NodeItemRelation;
import org.freeplane.features.text.ShortenedTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;

import com.jgoodies.common.base.Objects;
import com.lightdev.app.shtm.ActionBuilder;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.SHTMLPanelImpl;
import com.lightdev.app.shtm.UIResources;

/**
 * @author Dimitry Polivaev
 */
public class MTextController extends TextController {
	private static final String PARSE_DATA_PROPERTY = "parse_data";
	public static final String NODE_TEXT = "NodeText";
	private static Pattern FORMATTING_PATTERN = null;
	private EditNodeBase mCurrentEditor = null;
	private final Collection<IEditorPaneListener> editorPaneListeners;
	private final EventBuffer eventQueue;
	static {
		final UIResources defaultResources = SHTMLPanel.getResources();
		SHTMLPanel.setResources(new UIResources() {
			@Override
			public String getString(final String key) {
				if (key.equals("approximate_search_threshold")) {
					return new Double(StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB).toString();
				}
				String freeplaneKey = "simplyhtml." + key;
				String resourceString = ResourceController.getResourceController().getText(freeplaneKey, null);
				if (resourceString == null) {
					resourceString = ResourceController.getResourceController().getProperty(freeplaneKey);
				}
				if (resourceString == null && key.equals("splashImage"))
					return defaultResources.getString(key);
				return resourceString;
			}

			@Override
			public Icon getIcon(String name) {
				String freeplaneKey = "simplyhtml." + name;
				final Icon freeplaneIcon = ResourceController.getResourceController().getIcon(freeplaneKey);
				return freeplaneIcon != null ? freeplaneIcon : defaultResources.getIcon(name);
			}
		});
	}
	private static final ConditionPredicate DEPENDS_ON_PARENT = new ConditionPredicate() {
		@Override
		public boolean test(ICondition condition) {
			return condition instanceof NodeItemRelation &&
			        FILTER_PARENT.equals(((NodeItemRelation) condition).getNodeItem());
		}
	};

	public static MTextController getController() {
		return (MTextController) TextController.getController();
	}

	public MTextController(ModeController modeController) {
		super(modeController);
		eventQueue = new EventBuffer();
		editorPaneListeners = new LinkedList<IEditorPaneListener>();
		createActions();
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			@Override
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if (PARSE_DATA_PROPERTY.equals(propertyName)) {
					parseData = null;
					@SuppressWarnings("unused")
					boolean dummy = parseData();
				}
			}
		});
	}

	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new EditAction());
		modeController.addAction(new UsePlainTextAction());
		modeController.addAction(new EditLongAction());
		modeController.addAction(new SetImageByFileChooserAction());
		modeController.addAction(new EditDetailsAction(false));
		modeController.addAction(new EditDetailsAction(true));
		modeController.addAction(new DeleteDetailsAction());
		modeController.addUiBuilder(Phase.ACTIONS, "splitToWordsActions", new EntryVisitor() {
			@Override
			public void visit(Entry target) {
				final String[] nodeNumbersInLine = ResourceController.getResourceController()
				    .getProperty("SplitToWordsAction.nodeNumbersInLine").split("[^\\d]+");
				for (String nodeNumberInLineAsString : nodeNumbersInLine) {
					try {
						final int nodeNumberInLine = Integer.parseInt(nodeNumberInLineAsString);
						if (nodeNumberInLine > 0) {
							final SplitToWordsAction action = new SplitToWordsAction(nodeNumberInLine);
							new EntryAccessor().addChildAction(target, action);
							modeController.addAction(action);
						}
					}
					catch (NumberFormatException e) {
					}
				}
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		});
		modeController.addUiBuilder(Phase.ACTIONS, "joinNodesActions", new EntryVisitor() {
			@Override
			public void visit(Entry target) {
				final String textSeparators = ResourceController.getResourceController()
				    .getProperty("JoinNodesAction.textSeparators");
				final Pattern JOIN_NODES_ACTION_SEPARATORS = Pattern.compile("\\{\\{.*?\\}\\}");
				final Matcher matcher = JOIN_NODES_ACTION_SEPARATORS.matcher(textSeparators);
				if (matcher.find()) {
					do {
						String textSeparator = textSeparators.substring(matcher.start() + 2, matcher.end() - 2);
						addAction(modeController, target, textSeparator);
					} while (matcher.find());
				}
				else {
					addAction(modeController, target, textSeparators);
				}
			}

			private void addAction(final ModeController modeController, Entry target, String textSeparator) {
				final JoinNodesAction action = new JoinNodesAction(textSeparator);
				new EntryAccessor().addChildAction(target, action);
				modeController.addAction(action);
				if (target.getChildCount() == 1)
					target.getChild(0).setAttribute(EntryAccessor.ACCELERATOR,
					    target.getAttribute(EntryAccessor.ACCELERATOR));
			}

			@Override
			public boolean shouldSkipChildren(Entry entry) {
				return true;
			}
		});
	}

	@Override
	public void install(final ModeController modeController) {
		super.install(modeController);
		modeController.getMapController().addUINodeChangeListener(new INodeChangeListener() {
			@Override
			public void nodeChanged(NodeChangeEvent event) {
				if (event.getProperty().equals(NodeModel.NODE_TEXT)) {
					NodeModel node = event.getNode();
					if (LogicalStyleController.getController().conditionalStylesOf(node)
					    .dependOnCondition(DEPENDS_ON_PARENT)) {
						for (NodeModel child : node.getChildren())
							modeController.getMapController().delayedNodeRefresh(child, NodeModel.UNKNOWN_PROPERTY,
							    null, null);
					}
				}
			}
		});
	}

	private String[] getContent(final String text, final int pos) {
		if (pos <= 0) {
			return null;
		}
		final String[] strings = new String[2];
		if (text.startsWith("<html>")) {
			final HTMLEditorKit kit = new HTMLEditorKit();
			final HTMLDocument doc = new HTMLDocument();
			final StringReader buf = new StringReader(text);
			try {
				kit.read(buf, doc, 0);
				final char[] firstText = doc.getText(0, pos).toCharArray();
				int firstStart = 0;
				int firstLen = pos;
				while ((firstStart < firstLen) && (firstText[firstStart] <= ' ')) {
					firstStart++;
				}
				while ((firstStart < firstLen) && (firstText[firstLen - 1] <= ' ')) {
					firstLen--;
				}
				int secondStart = 0;
				int secondLen = doc.getLength() - pos;
				if (secondLen <= 0)
					return null;
				final char[] secondText = doc.getText(pos, secondLen).toCharArray();
				while ((secondStart < secondLen) && (secondText[secondStart] <= ' ')) {
					secondStart++;
				}
				while ((secondStart < secondLen) && (secondText[secondLen - 1] <= ' ')) {
					secondLen--;
				}
				if (firstStart == firstLen || secondStart == secondLen) {
					return null;
				}
				StringWriter out = new StringWriter();
				new FixedHTMLWriter(out, doc, firstStart, firstLen - firstStart).write();
				strings[0] = out.toString();
				out = new StringWriter();
				new FixedHTMLWriter(out, doc, pos + secondStart, secondLen - secondStart).write();
				strings[1] = out.toString();
				return strings;
			}
			catch (final IOException e) {
				LogUtils.severe(e);
			}
			catch (final BadLocationException e) {
				LogUtils.severe(e);
			}
		}
		else {
			if (pos >= text.length()) {
				return null;
			}
			strings[0] = text.substring(0, pos);
			strings[1] = text.substring(pos);
		}
		return strings;
	}

	private String addContent(String joinedContent, final boolean isHtml, String nodeContent, final boolean isHtmlNode,
	                          String separator) {
		if (isHtml) {
			final String joinedContentParts[] = JoinNodesAction.BODY_END.split(joinedContent, 2);
			joinedContent = joinedContentParts[0];
			if (!isHtmlNode) {
				final String end[] = JoinNodesAction.BODY_START.split(joinedContent, 2);
				if (end.length == 1) {
					end[0] = "<html>";
				}
				nodeContent = end[0] + "<body><p>" + HtmlUtils.toXMLEscapedTextExpandingWhitespace(nodeContent)
				        + "</p>";
			}
		}
		if (isHtmlNode & !joinedContent.equals("")) {
			final String nodeContentParts[] = JoinNodesAction.BODY_START.split(nodeContent, 2);
			// if no <body> tag is found
			if (nodeContentParts.length == 1) {
				nodeContent = nodeContent.substring(6);
				nodeContentParts[0] = "<html>";
			}
			else {
				nodeContent = nodeContentParts[1];
			}
			if (!isHtml) {
				joinedContent = nodeContentParts[0] + "<body><p>"
				        + HtmlUtils.toXMLEscapedTextExpandingWhitespace(joinedContent) + "</p>";
			}
		}
		if (joinedContent.equals("")) {
			return nodeContent;
		}
		joinedContent += isHtml ? HtmlUtils.toXMLEscapedTextExpandingWhitespace(separator) : separator;
		joinedContent += nodeContent;
		return joinedContent;
	}

	public void joinNodes(final List<NodeModel> selectedNodes, final String separator) {
		if (selectedNodes.isEmpty())
			return;
		final NodeModel selectedNode = selectedNodes.get(0);
		for (final NodeModel node : selectedNodes) {
			if (node != selectedNode && node.subtreeContainsCloneOf(selectedNode)) {
				UITools.errorMessage(TextUtils.getText("cannot_move_into_child_node"));
				return;
			}
		}
		String joinedContent = "";
		final Controller controller = Controller.getCurrentController();
		boolean isHtml = false;
		final LinkedHashSet<MindIcon> icons = new LinkedHashSet<MindIcon>();
		for (final NodeModel node : selectedNodes) {
			final String nodeContent = node.getText();
			icons.addAll(node.getIcons());
			final boolean isHtmlNode = HtmlUtils.isHtmlNode(nodeContent);
			joinedContent = addContent(joinedContent, isHtml, nodeContent, isHtmlNode, separator);
			if (node != selectedNode) {
				final MMapController mapController = (MMapController) Controller.getCurrentModeController()
				    .getMapController();
				mapController.moveNodes(node.getChildren(), selectedNode, selectedNode.getChildCount());
				mapController.deleteNode(node);
			}
			isHtml = isHtml || isHtmlNode;
		}
		controller.getSelection().selectAsTheOnlyOneSelected(selectedNode);
		setNodeText(selectedNode, joinedContent);
		final MIconController iconController = (MIconController) IconController.getController();
		iconController.removeAllIcons(selectedNode);
		for (final MindIcon icon : icons) {
			iconController.addIcon(selectedNode, icon);
		}
	}

	public void setImageByFileChooser() {
		boolean picturesAmongSelecteds = false;
		final ModeController modeController = Controller.getCurrentModeController();
		for (final NodeModel node : modeController.getMapController().getSelectedNodes()) {
			final URI link = NodeLinks.getLink(node);
			if (link != null) {
				final String linkString = link.toString();
				final String lowerCase = linkString.toLowerCase();
				if (lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")
				        || lowerCase.endsWith(".gif")) {
					picturesAmongSelecteds = true;
					final String encodedLinkString = HtmlUtils.unicodeToHTMLUnicodeEntity(linkString);
					final String strText = "<html><img src=\"" + encodedLinkString + "\">";
					((MLinkController) LinkController.getController()).setLink(node, (URI) null,
					    LinkController.LINK_ABSOLUTE);
					setNodeText(node, strText);
				}
			}
		}
		if (picturesAmongSelecteds) {
			return;
		}
		final Controller controller = modeController.getController();
		final ViewController viewController = controller.getViewController();
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final MapModel map = selectedNode.getMap();
		final File file = map.getFile();
		if (file == null && LinkController.getLinkType() == LinkController.LINK_RELATIVE_TO_MINDMAP) {
			JOptionPane.showMessageDialog(viewController.getCurrentRootComponent(), TextUtils
			    .getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("png");
		filter.addExtension("gif");
		filter.setDescription(TextUtils.getText("bitmaps"));
		final UrlManager urlManager = modeController.getExtension(UrlManager.class);
		final JFileChooser chooser = urlManager.getFileChooser(null, false);
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setAccessory(new BitmapImagePreview(chooser));
		final int returnVal = chooser.showOpenDialog(viewController.getCurrentRootComponent());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File input = chooser.getSelectedFile();
		URI uri = input.toURI();
		if (uri == null) {
			return;
		}
		// bad hack: try to interpret file as http link
		if (!input.exists()) {
			uri = LinkController.toRelativeURI(map.getFile(), input, LinkController.LINK_RELATIVE_TO_MINDMAP);
			if (uri == null || !"http".equals(uri.getScheme())) {
				UITools.errorMessage(TextUtils.format("file_not_found", input.toString()));
				return;
			}
		}
		else if (LinkController.getLinkType() != LinkController.LINK_ABSOLUTE) {
			uri = LinkController.toLinkTypeDependantURI(map.getFile(), input);
		}
		String uriString = uri.toString();
		if (uriString.startsWith("http:/")) {
			uriString = "http://" + uriString.substring("http:/".length());
		}
		final String strText = "<html><img src=\"" + uriString + "\">";
		setNodeText(selectedNode, strText);
	}

	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	private EditEventDispatcher keyEventDispatcher;
	private Boolean parseData;

	public void setGuessedNodeObject(final NodeModel node, final String newText) {
		if (HtmlUtils.isHtmlNode(newText))
			setNodeObject(node, newText);
		else {
			final Object guessedObject = guessObject(newText, NodeStyleModel.getNodeFormat(node));
			if (guessedObject instanceof IFormattedObject)
				setNodeObject(node, ((IFormattedObject) guessedObject).getObject());
			else
				setNodeObject(node, newText);
		}
	}

	public Object guessObject(final Object text, final String oldFormat) {
		if (parseData() && text instanceof String) {
			if (PatternFormat.getIdentityPatternFormat().getPattern().equals(oldFormat))
				return text;
			final Object parseResult = ScannerController.getController().parse((String) text);
			if (oldFormat != null) {
				final Object formatted = FormatController.format(parseResult, oldFormat, null);
				return (formatted == null) ? text : formatted;
			}
			return parseResult;
		}
		return text;
	}

	@Override
	public boolean parseData() {
		if (parseData == null)
			parseData = ResourceController.getResourceController().getBooleanProperty(PARSE_DATA_PROPERTY);
		return parseData;
	}

	public void setNodeText(final NodeModel node, final String newText) {
		setNodeObject(node, newText);
	}

	public void setNodeObject(final NodeModel node, final Object newObject) {
		if (newObject == null) {
			setNodeObject(node, "");
			return;
		}
		final Object oldText = node.getUserObject();
		if (oldText.equals(newObject)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				if (!oldText.equals(newObject)) {
					node.setUserObject(newObject);
					Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT,
					    oldText, newObject);
				}
			}

			@Override
			public String getDescription() {
				return "setNodeText";
			}

			@Override
			public void undo() {
				if (!oldText.equals(newObject)) {
					node.setUserObject(oldText);
					Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT,
					    newObject, oldText);
				}
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void splitNode(final NodeModel node, final int caretPosition, final String newText) {
		if (node.isRoot()) {
			return;
		}
		final String oldText = node.getText();
		final String futureText = newText != null ? newText : oldText;
		final String[] strings = getContent(futureText, caretPosition);
		if (strings == null) {
			final String mayBePlainText = makePlainIfNoFormattingFound(futureText);
			if (!Objects.equals(mayBePlainText, oldText))
				setNodeObject(node, mayBePlainText);
			return;
		}
		final String newUpperContent = makePlainIfNoFormattingFound(strings[0]);
		final String newLowerContent = makePlainIfNoFormattingFound(strings[1]);
		setNodeObject(node, newUpperContent);
		final NodeModel parent = node.getParentNode();
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel lowerNode = ((MMapController) modeController.getMapController()).addNewNode(parent,
		    parent.getIndex(node) + 1, node.isLeft());
		final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
		    .getController();
		nodeStyleController.copyStyle(node, lowerNode);
		setNodeObject(lowerNode, newLowerContent);
	}

	public boolean useRichTextInEditor(String key) {
		final int showResult = OptionalDontShowMeAgainDialog.show(
		    "OptionPanel." + key, "edit.decision", key,
		    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
		return showResult == JOptionPane.OK_OPTION;
	}

	public void editDetails(final NodeModel nodeModel, InputEvent e, final boolean editLong) {
		final Controller controller = Controller.getCurrentController();
		stopEditing();
		Controller.getCurrentModeController().setBlocked(true);
		String text = DetailTextModel.getDetailTextText(nodeModel);
		final boolean isNewNode = text == null;
		if (isNewNode) {
			final MTextController textController = MTextController.getController();
			textController.setDetails(nodeModel, "<html>");
			text = "";
		}
		final EditNodeBase.IEditControl editControl = new EditNodeBase.IEditControl() {
			@Override
			public void cancel() {
				if (isNewNode) {
					final String detailText = DetailTextModel.getDetailTextText(nodeModel);
					final MModeController modeController = (MModeController) Controller.getCurrentModeController();
					if (detailText != null)
						modeController.undo();
					modeController.resetRedo();
				}
				stop();
			}

			@Override
			public void ok(final String newText) {
				if (HtmlUtils.isEmpty(newText))
					if (isNewNode) {
						final MModeController modeController = (MModeController) Controller.getCurrentModeController();
						modeController.undo();
						modeController.resetRedo();
					}
					else {
						keepNodePosition();
						setDetailsHtmlText(nodeModel, null);
					}
				else {
					keepNodePosition();
					setDetailsHtmlText(nodeModel, newText);
				}
				stop();
			}

			private void keepNodePosition() {
				Controller.getCurrentController().getSelection().keepNodePosition(nodeModel, 0, 0);
			}

			@Override
			public void split(final String newText, final int position) {
			}

			private void stop() {
				Controller.getCurrentModeController().setBlocked(false);
				mCurrentEditor = null;
			}

			@Override
			public boolean canSplit() {
				return false;
			}

			@Override
			public EditedComponent getEditType() {
				return EditedComponent.DETAIL;
			}
		};
		mCurrentEditor = createEditor(nodeModel, editControl, text, false, editLong, true);
		final RootPaneContainer frame = (RootPaneContainer) SwingUtilities
		    .getWindowAncestor(controller.getMapViewManager().getMapViewComponent());
		mCurrentEditor.show(frame);
	}

	private void setDetailsHtmlText(final NodeModel node, final String newText) {
		if (newText != null) {
			final String body = removeHtmlHead(newText);
			setDetails(node, body.replaceFirst("\\s+$", ""));
		}
		else
			setDetails(node, null);
	}

	public void setDetails(final NodeModel node, final String newText) {
		final String oldText = DetailTextModel.getDetailTextText(node);
		if (oldText == newText || null != oldText && oldText.equals(newText)) {
			return;
		}
		final IActor actor = new IActor() {
			boolean hidden = false;

			@Override
			public void act() {
				setText(newText);
			}

			@Override
			public String getDescription() {
				return "setDetailText";
			}

			private void setText(final String text) {
				final boolean containsDetails = !(text == null || text.equals(""));
				if (containsDetails) {
					final DetailTextModel details = DetailTextModel.createDetailText(node);
					details.setHtml(text);
					details.setHidden(hidden);
					node.addExtension(details);
				}
				else {
					final DetailTextModel details = node.getExtension(DetailTextModel.class);
					if (null != details) {
						hidden = details.isHidden();
						node.removeExtension(DetailTextModel.class);
					}
				}
				Controller.getCurrentModeController().getMapController().nodeChanged(node, DetailTextModel.class,
				    oldText, text);
			}

			@Override
			public void undo() {
				setText(oldText);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	@Override
	public void setDetailsHidden(final NodeModel node, final boolean isHidden) {
		stopEditing();
		DetailTextModel details = node.getExtension(DetailTextModel.class);
		if (details == null || details.isHidden() == isHidden) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public boolean isReadonly() {
				return true;
			}
			@Override
			public void act() {
				setHidden(isHidden);
			}

			@Override
			public String getDescription() {
				return "setDetailsHidden";
			}

			private void setHidden(final boolean isHidden) {
				final DetailTextModel details = DetailTextModel.createDetailText(node);
				details.setHidden(isHidden);
				node.addExtension(details);
				final NodeChangeEvent nodeChangeEvent = new NodeChangeEvent(node, DETAILS_HIDDEN, !isHidden, isHidden, true, false);
				Controller.getCurrentModeController().getMapController().nodeRefresh(nodeChangeEvent);
			}

			@Override
			public void undo() {
				setHidden(!isHidden);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	@Override
	public void setIsMinimized(final NodeModel node, final boolean state) {
		ShortenedTextModel details = node.getExtension(ShortenedTextModel.class);
		if (details == null && state == false || details != null && state == true) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public boolean isReadonly() {
				return true;
			}

			@Override
			public void act() {
				setShortener(state);
			}

			@Override
			public String getDescription() {
				return "setShortener";
			}

			private void setShortener(final boolean state) {
				if (state) {
					final ShortenedTextModel details = ShortenedTextModel.createShortenedTextModel(node);
					node.addExtension(details);
				}
				else {
					node.removeExtension(ShortenedTextModel.class);
				}
				Controller.getCurrentModeController().getMapController().nodeChanged(node, ShortenedTextModel.SHORTENER,
				    !state, state);
			}

			@Override
			public void undo() {
				setShortener(!state);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void edit(final FirstAction action, final boolean editLong) {
		final Controller controller = Controller.getCurrentController();
		final IMapSelection selection = controller.getSelection();
		if (selection == null)
			return;
		final NodeModel selectedNode = selection.getSelected();
		if (selectedNode == null)
			return;
		if (FirstAction.EDIT_CURRENT.equals(action)) {
			edit(selectedNode, selectedNode, false, false, editLong);
		}
		else if (!Controller.getCurrentModeController().isBlocked()) {
			final int mode = FirstAction.ADD_CHILD.equals(action) ? MMapController.NEW_CHILD
			        : MMapController.NEW_SIBLING_BEHIND;
			((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(mode);
		}
	}

	public boolean containsFormatting(final String text) {
		if (FORMATTING_PATTERN == null) {
			FORMATTING_PATTERN = Pattern.compile("<(?!/|html>|head|body|p/?>|!--|style type=\"text/css\">)",
			    Pattern.CASE_INSENSITIVE);
		}
		final Matcher matcher = FORMATTING_PATTERN.matcher(text);
		return matcher.find();
	}

	private class EditEventDispatcher implements KeyEventDispatcher, INodeChangeListener, INodeSelectionListener {
		private final boolean editLong;
		private final boolean parentFolded;
		private final boolean isNewNode;
		private final NodeModel prevSelectedModel;
		private final NodeModel nodeModel;
		private final ModeController modeController;

		private EditEventDispatcher(ModeController modeController, NodeModel nodeModel, NodeModel prevSelectedModel,
		                            boolean isNewNode,
		                            boolean parentFolded, boolean editLong) {
			this.modeController = modeController;
			this.editLong = editLong;
			this.parentFolded = parentFolded;
			this.isNewNode = isNewNode;
			this.prevSelectedModel = prevSelectedModel;
			this.nodeModel = nodeModel;
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_RELEASED || e.getID() == KeyEvent.KEY_TYPED)
				return false;
			switch (e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:
				case KeyEvent.VK_CONTROL:
				case KeyEvent.VK_CAPS_LOCK:
				case KeyEvent.VK_ALT:
				case KeyEvent.VK_ALT_GRAPH:
					return false;
			}
			uninstall();
			if (isMenuEvent(e)) {
				return false;
			}
			eventQueue.activate(e);
			edit(nodeModel, prevSelectedModel, isNewNode, parentFolded, editLong);
			return true;
		}

		private boolean isMenuEvent(KeyEvent e) {
			if (!editLong) {
				AFreeplaneAction editLongAcction = modeController.getAction("EditLongAction");
				KeyStroke accelerator = ResourceController.getResourceController().getAcceleratorManager()
				    .getAccelerator(editLongAcction);
				if (accelerator != null) {
					final KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
					if (accelerator.equals(keyStroke)) {
						return true;
					}
				}
			}
			return false;
		}

		public void uninstall() {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
			MapController mapController = modeController.getMapController();
			mapController.removeNodeChangeListener(this);
			mapController.removeNodeSelectionListener(this);
			keyEventDispatcher = null;
		}

		public void install() {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
			MapController mapController = modeController.getMapController();
			mapController.addUINodeChangeListener(this);
			mapController.addNodeSelectionListener(this);
		}

		@Override
		public void onDeselect(NodeModel node) {
			uninstall();
		}

		@Override
		public void onSelect(NodeModel node) {
			uninstall();
		}

		@Override
		public void nodeChanged(NodeChangeEvent event) {
			uninstall();
		}
	}

	public void edit(final NodeModel nodeModel, final NodeModel prevSelectedModel, final boolean isNewNode,
	                 final boolean parentFolded, final boolean editLong) {
		if (nodeModel == null || mCurrentEditor != null) {
			return;
		}
		final Controller controller = Controller.getCurrentController();
		if (controller.getMap() != nodeModel.getMap()) {
			return;
		}
		final IMapViewManager viewController = controller.getMapViewManager();
		final Component map = viewController.getMapViewComponent();
		map.validate();
		map.invalidate();
		final Component node = viewController.getComponent(nodeModel);
		if (node == null) {
			return;
		}
		node.requestFocus();
		stopEditing();
		if (isNewNode && !eventQueue.isActive()
		        && !ResourceController.getResourceController()
		            .getBooleanProperty("display_inline_editor_for_all_new_nodes")) {
			keyEventDispatcher = new EditEventDispatcher(Controller.getCurrentModeController(), nodeModel,
			    prevSelectedModel, isNewNode, parentFolded, editLong);
			keyEventDispatcher.install();
			return;
		} ;
		final IEditControl editControl = new IEditControl() {
			@Override
			public void cancel() {
				if (isNewNode && nodeModel.getMap().equals(controller.getMap())) {
					if (nodeModel.getParentNode() != null) {
						controller.getSelection().selectAsTheOnlyOneSelected(nodeModel);
						final MModeController modeController = (MModeController) Controller.getCurrentModeController();
						modeController.undo();
						modeController.resetRedo();
					}
					final MapController mapController = Controller.getCurrentModeController().getMapController();
					if (parentFolded) {
						mapController.fold(prevSelectedModel);
					}
				}
				stop();
			}

			private void stop() {
				Controller.getCurrentModeController().setBlocked(false);
				viewController.obtainFocusForSelected();
				mCurrentEditor = null;
			}

			@Override
			public void ok(final String text) {
				String processedText = makePlainIfNoFormattingFound(text);
				keepNodePosition();
				setGuessedNodeObject(nodeModel, processedText);
				stop();
			}

			private void keepNodePosition() {
				Controller.getCurrentController().getSelection().keepNodePosition(nodeModel, 0, 0);
			}

			@Override
			public void split(final String text, final int position) {
				String processedText = HtmlUtils.isHtmlNode(text) ? removeHtmlHead(text) : text;
				splitNode(nodeModel, position, processedText);
				viewController.obtainFocusForSelected();
				stop();
			}

			@Override
			public boolean canSplit() {
				return true;
			}

			@Override
			public EditedComponent getEditType() {
				return EditedComponent.TEXT;
			}
		};
		mCurrentEditor = createEditor(nodeModel, editControl, nodeModel.getText(), isNewNode, editLong, true);
		final RootPaneContainer frame = (RootPaneContainer) UITools.getCurrentRootComponent();
		mCurrentEditor.show(frame);
	}

	private EditNodeBase createEditor(final NodeModel nodeModel, final IEditControl editControl,
	                                  String text, final boolean isNewNode, final boolean editLong,
	                                  boolean internal) {
		Controller.getCurrentModeController().setBlocked(true);
		EditNodeBase base = getEditNodeBase(nodeModel, text, editControl, editLong);
		if (base != null || !internal) {
			return base;
		}
		final IEditBaseCreator textFieldCreator = (IEditBaseCreator) Controller.getCurrentController()
		    .getMapViewManager();
		return textFieldCreator.createEditor(nodeModel, editControl, text, editLong);
	}

	public EditNodeBase getEditNodeBase(final NodeModel nodeModel, final String text, final IEditControl editControl,
	                                    final boolean editLong) {
		final List<IContentTransformer> textTransformers = getTextTransformers();
		for (IContentTransformer t : textTransformers) {
			if (t instanceof IEditBaseCreator) {
				final EditNodeBase base = ((IEditBaseCreator) t).createEditor(nodeModel, editControl, text, editLong);
				if (base != null) {
					return base;
				}
			}
		}
		return null;
	}

	public void stopEditing() {
		if (keyEventDispatcher != null) {
			keyEventDispatcher.uninstall();
		}
		if (mCurrentEditor != null) {
			// Ensure that setText from the edit and the next action
			// are parts of different transactions
			mCurrentEditor.closeEdit();
			modeController.forceNewTransaction();
			mCurrentEditor = null;
		}
	}

	public void addEditorPaneListener(IEditorPaneListener l) {
		editorPaneListeners.add(l);
	}

	public void removeEditorPaneListener(IEditorPaneListener l) {
		editorPaneListeners.remove(l);
	}

	private void fireEditorPaneCreated(JEditorPane editor, Object purpose) {
		for (IEditorPaneListener l : editorPaneListeners) {
			l.editorPaneCreated(editor, purpose);
		}
	}

	/**
	 * Note: when creating an SHTMLPanel using this method, you must make sure to attach
	 * a FreeplaneToSHTMLPropertyChangeAdapter to the panel (see for example EditNodeWYSIWYG.HTMLDialog.createEditorPanel(String))
	 * @param purpose
	 * @return
	 */
	public SHTMLPanel createSHTMLPanel(String purpose) {
		com.lightdev.app.shtm.ScaledStyleSheet.FONT_SCALE_FACTOR = UITools.FONT_SCALE_FACTOR;
		SHTMLPanel.setActionBuilder(new ActionBuilder() {
			@Override
			public void initActions(SHTMLPanel panel) {
				panel.addAction("editLink", new SHTMLEditLinkAction((SHTMLPanelImpl) panel));
				panel.addAction("setLinkByFileChooser", new SHTMLSetLinkByFileChooserAction((SHTMLPanelImpl) panel));
			}
		});
		final SHTMLPanel shtmlPanel = SHTMLPanel.createSHTMLPanel();
		final JEditorPane sourceEditorPane = shtmlPanel.getSourceEditorPane();
		final Font originalFont = sourceEditorPane.getFont();
		final Font scaledFont = UITools.scale(originalFont);
		sourceEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		sourceEditorPane.setFont(scaledFont);
		shtmlPanel.setOpenHyperlinkHandler(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent pE) {
				try {
					UrlManager.getController().loadURL(new URI(pE.getActionCommand()));
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		final JEditorPane editorPane = shtmlPanel.getEditorPane();
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		fireEditorPaneCreated(editorPane, purpose);
		return shtmlPanel;
	}

	public JEditorPane createEditorPane(Object purpose) {
		@SuppressWarnings("serial")
		final JEditorPane editorPane = new JEditorPane() {
			@Override
			public String getSelectedText() {
				final String selectedText = super.getSelectedText();
				return selectedText != null ? selectedText.replace('\u00a0', ' ') : null;
			}

			@Override
			protected void paintComponent(Graphics g) {
				try {
					super.paintComponent(g);
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		};
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		fireEditorPaneCreated(editorPane, purpose);
		return editorPane;
	}

	public EventBuffer getEventQueue() {
		return eventQueue;
	}

	private String makePlainIfNoFormattingFound(String text) {
		if (HtmlUtils.isHtmlNode(text)) {
			text = removeHtmlHead(text);
			if (!containsFormatting(text)) {
				text = HtmlUtils.htmlToPlain(text);
			}
		}
		text = text.replaceFirst("\\s+$", "");
		return text;
	}

	private String removeHtmlHead(String text) {
		return HTML_HEAD.matcher(text).replaceFirst("");
	}

	@Override
	public boolean canEdit() {
		return true;
	}
}
