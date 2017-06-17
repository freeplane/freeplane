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
package org.freeplane.features.link;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.DashVariant;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.SelectionController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.url.UrlManager;

/**
 * @author Dimitry Polivaev
 */
public class LinkController extends SelectionController implements IExtension {
	public static final String MENUITEM_SCHEME = "menuitem";
	public static final String EXECUTE_APP_SCHEME = "execute";
	public static LinkController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LinkController getController(ModeController modeController) {
		return modeController.getExtension(LinkController.class);
	}

	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(30, new LinkConditionController());
	}

	public static void install( final LinkController linkController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LinkController.class, linkController);
		linkController.init();
	}

 	final protected ModeController modeController;

	public LinkController(ModeController modeController) {
		this.modeController = modeController;
	}

	protected void init() {
		createActions();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		LinkBuilder linkBuilder = new LinkBuilder(this);
		linkBuilder.registerBy(readManager);
		
		// this IContentTransformer is unconditional because the outcome
		// (#ID_1698830792 -> Nodename) is usually wanted
		final LinkTransformer textTransformer = new LinkTransformer(modeController, 10);
		TextController.getController(modeController).addTextTransformer(textTransformer);
		
		textTransformer.registerListeners(modeController);

		final INodeSelectionListener listener = new INodeSelectionListener() {
			public void onDeselect(final NodeModel node) {
			}

			public void onSelect(final NodeModel node) {
				final URI link = NodeLinks.getValidLink(node);
				final String linkString = (link != null ? link.toString() : null);
				if (linkString != null) {
					Controller.getCurrentController().getViewController().out(linkString);
				}
			}
		};
		Controller.getCurrentModeController().getMapController().addNodeSelectionListener(listener);
	}

	private JButton addLinks(final JComponent arrowLinkPopup, final NodeModel source) {
		GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(this, source);
		gotoLinkNodeAction.configureText("follow_graphical_link", source);
		return addAction(arrowLinkPopup, gotoLinkNodeAction);
	}

    protected void addPopupComponent(final JComponent arrowLinkPopup, final String label, final JComponent component) {
        final JComponent componentBox;
        if(label != null){
            componentBox = Box.createHorizontalBox();
            componentBox.add(Box.createHorizontalStrut(10));
            final JLabel jlabel = new JLabel(label);
            componentBox.add(jlabel);
            componentBox.add(Box.createHorizontalStrut(10));
            componentBox.add(component);
        }
        else {
            componentBox = component;
        }
        componentBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        componentBox.setMinimumSize(new Dimension());
        componentBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        arrowLinkPopup.add(componentBox);
    }

    protected void addClosingAction(final JComponent arrowLinkPopup, Action action) {
        JButton comp = addAction(arrowLinkPopup, action);
        comp.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		SwingUtilities.getWindowAncestor(arrowLinkPopup).setVisible(false);
        	}
        });
    }

    protected JButton addAction(final JComponent arrowLinkPopup, Action action) {
	    JButton comp = new JButton(action);
        comp.setHorizontalAlignment(JButton.LEFT);
        addPopupComponent (arrowLinkPopup, null, comp);
	    return comp;
    }

	/**
	 *
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new FollowLinkAction());
		modeController.addUiBuilder(Phase.ACTIONS, "clone_actions", new ClonesMenuBuilder(modeController),
				new ChildActionEntryRemover(modeController));
		modeController.addUiBuilder(Phase.ACTIONS, "link_actions", new LinkMenuBuilder(modeController), 
				new ChildActionEntryRemover(modeController));
	}

	final class LinkMenuBuilder implements EntryVisitor {
	    private final ModeController modeController;

	    LinkMenuBuilder(ModeController modeController) {
		    this.modeController = modeController;
	    }

	    @Override
	    public void visit(Entry entry) {
	    	final IMapSelection selection = modeController.getController().getSelection();
	    	if (selection == null)
	    		return;
	    	final NodeModel node = selection.getSelected();
	    	Set<NodeLinkModel> links = new LinkedHashSet<NodeLinkModel>(NodeLinks.getLinks(node));
	    	links.addAll(getLinksTo(node));
	    	boolean firstAction = true;
	    	for (NodeLinkModel link : links) {
	    		final String targetID = link.getTargetID();
	    		final NodeModel target;
	    		if (node.getID().equals(targetID)) {
	    			if (link instanceof ConnectorModel) {
	    				ConnectorModel cm = (ConnectorModel) link;
	    				target = cm.getSource();
	    				if (node.equals(target))
	    					continue;
	    			}
	    			else
	    				continue;
	    		}
	    		else
	    			target = node.getMap().getNodeForID(targetID);
	    		final GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(LinkController.this, target);
	    		gotoLinkNodeAction.configureText("follow_graphical_link", target);
	    		if (!(link instanceof ConnectorModel)) {
	    			gotoLinkNodeAction.putValue(Action.SMALL_ICON, LinkType.LOCAL.icon);
	    		}
	    		if (firstAction) {
	    			entry.addChild(new Entry().setBuilders("separator"));
	    			firstAction = false;
	    		}
	    		modeController.addActionIfNotAlreadySet(gotoLinkNodeAction);
				new EntryAccessor().addChildAction(entry, gotoLinkNodeAction);
	    	}
	    }

	    @Override
	    public boolean shouldSkipChildren(Entry entry) {
	    	return true;
	    }
    }


	private class ClonesMenuBuilder implements EntryVisitor {
		final private ModeController modeController;

		public ClonesMenuBuilder(ModeController modeController) {
	        super();
			this.modeController = modeController;
        }


		@Override
		public void visit(Entry target) {
			final IMapSelection selection = modeController.getController().getSelection();
			if (selection == null)
				return;
			final NodeModel node = selection.getSelected();
			boolean firstAction = true;
			NodeModel parentNode = node.getParentNode();
			if (parentNode != null) {
				for (NodeModel clone : node.allClones()) {
					if (!clone.equals(node)) {
						final GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(LinkController.this, clone);
						NodeModel subtreeRootParentNode = clone.getSubtreeRoot().getParentNode();
						gotoLinkNodeAction.configureText("follow_clone", subtreeRootParentNode);
						if (firstAction) {
							target.addChild(new Entry().setBuilders("separator"));
							firstAction = false;
						}
						modeController.addActionIfNotAlreadySet(gotoLinkNodeAction);
						new EntryAccessor().addChildAction(target, gotoLinkNodeAction);
					}
				}
			}

		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return true;
		}
    }
    @SuppressWarnings("serial")
    public static final class ClosePopupAction extends AbstractAction {
        final private String reason;

        public ClosePopupAction(String reason) {
            this.reason = reason;
        }

        public void actionPerformed(ActionEvent e) {
            JComponent src = (JComponent) e.getSource();
            src.putClientProperty(reason, Boolean.TRUE);
            SwingUtilities.getWindowAncestor(src).setVisible(false);
        }
    }

	protected static final String CANCEL = "CANCEL";
	protected static final String CLOSE = "CLOSE";
	protected void createArrowLinkPopup(final ConnectorModel link, final JComponent arrowLinkPopup) {

		registerCloseActions(arrowLinkPopup);

		final NodeModel source = link.getSource();
		final NodeModel target = link.getTarget();
		final IMapSelection selection = Controller.getCurrentModeController().getController().getSelection();
		final JButton sourceButton = addLinks(arrowLinkPopup, source);
		sourceButton.setEnabled(!selection.isSelected(source));
		final JButton targetButton = addLinks(arrowLinkPopup, target);
		targetButton.setEnabled(!selection.isSelected(target));
		
		sourceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceButton.setEnabled(false);
				targetButton.setEnabled(true);
			}
		});

		targetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				targetButton.setEnabled(false);
				sourceButton.setEnabled(true);
			}
		});
	}

	private void registerCloseActions(final JComponent arrowLinkPopup) {
		arrowLinkPopup.addHierarchyListener(new HierarchyListener() {
			
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(arrowLinkPopup.isDisplayable()) {
					arrowLinkPopup.removeHierarchyListener(this);
					final JRootPane rootPane = arrowLinkPopup.getRootPane();
					final InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
					final ActionMap actionMap = rootPane.getActionMap();
					final ClosePopupAction closeAction = new ClosePopupAction(CLOSE);
					final ClosePopupAction cancelAction = new ClosePopupAction(CANCEL);
					inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelAction);
					actionMap.put(cancelAction, cancelAction);
					inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), closeAction);
					final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty("el__enter_confirms_by_default");
					if(enterConfirms)
						inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), closeAction);
					actionMap.put(closeAction, closeAction);
				}
			}
		});
	}

	public Color getColor(final ConnectorModel model) {
		return model.getColor();
	}

	public int[] getDash(final ConnectorModel model) {
		return model.getDash();
	}

	public String getLinkShortText(final NodeModel node) {
		final URI uri = NodeLinks.getLink(node);
		if (uri == null) {
			return null;
		}
		final String adaptedText = uri.toString();
		if (adaptedText.startsWith("#")) {
			ModeController modeController = Controller.getCurrentModeController();
			final NodeModel dest = modeController.getMapController().getNodeFromID(adaptedText.substring(1));
			if (dest != null) {
				return TextController.getController().getShortPlainText(dest);
			}
			return TextUtils.getText("link_not_available_any_more");
		}
		return adaptedText;
	}

	public Collection<NodeLinkModel> getLinksFrom(NodeModel node) {
		return NodeLinks.getLinks(node);
	}
	
	public Collection<NodeLinkModel> getLinksTo(final NodeModel target) {
		if (target.hasID() == false) {
			return Collections.emptySet();
		}
		final MapLinks links = target.getMap().getExtension(MapLinks.class);
		if (links == null) {
			return Collections.emptySet();
		}
		
		ArrayList<NodeLinkModel> clonedLinks = null;
		for(NodeModel targetClone : target.subtreeClones()){
			final Set<NodeLinkModel> set = links.get(targetClone.createID());
			if (set == null) {
				continue;
			}
			if(target.subtreeClones().size() == 1)
				return set;
			if (clonedLinks == null)
				clonedLinks = new ArrayList<NodeLinkModel>(10);
			for(NodeLinkModel sharedLink : set){
				final Collection<NodeLinkModel> linkClones = sharedLink.clones();
				for(NodeLinkModel linkClone : linkClones)
					if(target.equals(linkClone.getTarget()))
						clonedLinks.add(linkClone);
			}
		}
		return clonedLinks != null  ? clonedLinks : Collections.<NodeLinkModel>emptySet();
	}

	/**
	 * Link implementation: If this is a link, we want to make a popup with at
	 * least removelink available.
	 */
	public Component getPopupForModel(final java.lang.Object obj) {
		if (obj instanceof ConnectorModel) {
			final ConnectorModel link = (ConnectorModel) obj;
			final Box arrowLinkPopup = Box.createVerticalBox();
			arrowLinkPopup.setName(TextUtils.getText("connector"));
			createArrowLinkPopup(link, arrowLinkPopup);
			return arrowLinkPopup;
		}
		return null;
	}

	public static final String RESOURCES_LINK_COLOR = "standardlinkcolor";
	private static final String RESOURCES_CONNECTOR_SHAPE = "connector_shape";
	private static final String RESOURCES_CONNECTOR_ARROWS = "connector_arrows";
	private static final String RESOURCES_DASH_VARIANT = "connector_dash";
	private static final String RESOURCES_CONNECTOR_COLOR_ALPHA = "connector_alpha";
	private static final String RESOURCES_CONNECTOR_WIDTH = "connector_width";

	public int getWidth(final ConnectorModel model) {
		return model.getWidth();
	}

	public void loadLink(final NodeModel node, String link) {
		NodeLinks links = NodeLinks.getLinkExtension(node);
		if (links == null) {
			links = NodeLinks.createLinkExtension(node);
		}
		if (link != null && link.startsWith("#")) {
			links.setLocalHyperlink(node, link.substring(1));
		}
		else {
			try {
				if (link.startsWith("\"") && link.endsWith("\"")) {
					link = link.substring(1, link.length() - 1);
				}
				final URI hyperlink = LinkController.createURI(link);
				links.setHyperLink(hyperlink);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
				UITools.errorMessage(TextUtils.format("link_error", link));
				return;
			}
		}
	}

	void loadLinkFormat(NodeModel node, boolean enabled) {
	    NodeLinks.createLinkExtension(node).setFormatNodeAsHyperlink(enabled);
    }


	public void loadURL(final NodeModel node, final MouseEvent e) {
		loadURL(node, new ActionEvent(e.getSource(), e.getID(), null));
	}

	public void loadURL(final MouseEvent e) {
		ModeController modeController = Controller.getCurrentModeController();
		loadURL(modeController.getMapController().getSelectedNode(), e);
	}

	@SuppressWarnings("deprecation")
    public void loadURI(URI uri) {
		UrlManager.getController().loadURL(uri);
    }

	public void loadMap(String map)
			throws URISyntaxException {
		UrlManager.getController().loadMap(map);
	}

	protected void loadURL(final NodeModel selectedNode, final ActionEvent e) {
		loadURL(selectedNode, e, NodeLinks.getValidLink(selectedNode));
	}

    public void loadURL(final NodeModel selectedNode, final ActionEvent e, final URI link) {
        if (link != null) {
			onDeselect(selectedNode);
			ModeController modeController = Controller.getCurrentModeController();
			if (LinkController.isMenuItemLink(link)) {
				if (e == null) {
					throw new IllegalArgumentException("ActionEvent is needed for menu item links");
				}
				final String actionKey = LinkController.parseSpecialLink(link);
				final Action action = modeController.getAction(actionKey);

				if (action != null) {
					action.actionPerformed(e);
				} else {
					LogUtils.warn("Trying to call a menu hyperlink action with key '" //
						+ actionKey + "'that doesn't exist.");
				}
			}
			else if (LinkController.isSpecialLink(LinkController.EXECUTE_APP_SCHEME, link)) {
				final String command = LinkController.parseSpecialLink(link);
				try {
					Controller.getCurrentController().getViewController().out(command);
					Runtime.getRuntime().exec(command);
				}
				catch (IOException e1) {
				}
			}
			else {
				loadURI(link);
			}
			onSelect(modeController.getController().getSelection().getSelected());
		}
    }

    public static int getLinkType() {
		return getController().linkType();
	}

    public static final int LINK_ABSOLUTE = 0;
	public static final int LINK_RELATIVE_TO_MINDMAP = 1;

	public int linkType() {
		String linkTypeProperty = ResourceController.getResourceController().getProperty("links");
		if ("relative".equals(linkTypeProperty)) {
			return LINK_RELATIVE_TO_MINDMAP;
		}
		return LINK_ABSOLUTE;
	}

	public static URI toLinkTypeDependantURI(final File map, final File input) {
		int type = getLinkType();
		if (type == LINK_ABSOLUTE) {
			return input.getAbsoluteFile().toURI();
		}
		return toRelativeURI(map, input, type);
	}

	public static URI toLinkTypeDependantURI(final File map, final File input, final int linkType) {
		return toRelativeURI(map, input, linkType);
	}

	public static URI toRelativeURI(final File map, final File input, final int linkType) {
		return getController().createRelativeURI(map, input, linkType);
	}

	public static URI normalizeURI(URI uri){
		final String UNC_PREFIX = "//";
		URI normalizedUri = uri.normalize();
		//Fix UNC paths that are incorrectly normalized by URI#resolve (see Java bug 4723726)
		String normalizedPath = normalizedUri.getPath();
		if ("file".equalsIgnoreCase(uri.getScheme()) && uri.getPath() != null && uri.getPath().startsWith(UNC_PREFIX) && (normalizedPath == null || !normalizedPath.startsWith(UNC_PREFIX))){
		try {
				normalizedUri = new URI(normalizedUri.getScheme(), ensureUNCPath(normalizedUri.getSchemeSpecificPart()), normalizedUri.getFragment());
			} catch (URISyntaxException e) {
				LogUtils.warn(e);
			}
		}
		return normalizedUri;
	}

	private static String ensureUNCPath(String path) {
		int len = path.length();
		StringBuffer result = new StringBuffer(len);
		for (int i = 0; i < 4; i++) {
			//    if we have hit the first non-slash character, add another leading slash
			if (i >= len || result.length() > 0 || path.charAt(i) != '/')
				result.append('/');
		}
		result.append(path);
		return result.toString();
	}

	public URI createRelativeURI(final File mapFile, final File target, final int linkType) {
		if (linkType == LINK_ABSOLUTE) {
			return null;
		}
		final URI fileUri = target.getAbsoluteFile().toURI();
		return createRelativeURI(mapFile, fileUri);
	}

	public URI createRelativeURI(final File mapFile, final URI targetUri) {
		if (mapFile != null) {
			URI mapUri = mapFile.getAbsoluteFile().toURI();
			return createRelativeURI(mapUri, targetUri);
		}
		else
			return targetUri;
	}

	public URI createRelativeURI(URI mapUri, final URI targetUri){
		boolean isUNCinput = targetUri.getPath().startsWith("//");
		boolean isUNCmap = mapUri.getPath().startsWith("//");
		if((isUNCinput != isUNCmap)) {
			return targetUri;
		}
		final String filePathAsString = targetUri.getRawPath();
		final String mapPathAsString = mapUri.getRawPath();
		int differencePos;
		final int lastIndexOfSeparatorInMapPath = mapPathAsString.lastIndexOf("/");
		final int lastIndexOfSeparatorInFilePath = filePathAsString.lastIndexOf("/");
		int lastCommonSeparatorPos = -1;
		for (differencePos = 0; differencePos <= lastIndexOfSeparatorInMapPath
		        && differencePos <= lastIndexOfSeparatorInFilePath
		        && filePathAsString.charAt(differencePos) == mapPathAsString.charAt(differencePos); differencePos++) {
			if (filePathAsString.charAt(differencePos) == '/') {
				lastCommonSeparatorPos = differencePos;
			}
		}
		if (lastCommonSeparatorPos < 0) {
			return targetUri;
		}
		final StringBuilder relativePath = new StringBuilder();
		for (int i = lastCommonSeparatorPos + 1; i <= lastIndexOfSeparatorInMapPath; i++) {
			if (mapPathAsString.charAt(i) == '/') {
				relativePath.append("../");
			}
		}
		relativePath.append(filePathAsString.substring(lastCommonSeparatorPos + 1));
		final String rawFragment = targetUri.getRawFragment();
		if(rawFragment != null)
			relativePath.append("#" + rawFragment);
		try {
			return new URI(relativePath.toString());
		}
		catch (final URISyntaxException e) {
			return null;
		}

	}

	// patterns only need to be compiled once
	static Pattern patSMB = Pattern.compile( // \\host\path[#fragement]
	    "(?:\\\\\\\\([^\\\\]+)\\\\)(.*?)(?:#([^#]*))?");
	static Pattern patFile = Pattern.compile( // [drive:]path[#fragment]
	    "((?:\\p{Alpha}:)?([/\\\\])?(?:[^:#?]*))?(?:#([^#]*))?");
	static Pattern patURI = Pattern.compile( // [scheme:]scheme-specific-part[#fragment]
	    "(?:(\\p{Alpha}[\\p{Alnum}+.-]+):)?(.*?)(?:#([^#]*))?");

	/* Function that tries to transform a not necessarily well-formed
	 * string into a valid URI. We use the fact that the single-argument
	 * URI constructor doesn't escape invalid characters (especially
	 * spaces), whereas the 3-argument constructors does do escape
	 * them (e.g. space into %20).
	 */
	public static URI createURI(final String inputValue) throws URISyntaxException {
		try { // first, we try if the string can be interpreted as URI
			return new URI(inputValue);
		}
		catch (final URISyntaxException e) {
			// [scheme:]scheme-specific-part[#fragment]
			// we check first if the string matches an SMB
			// of the form \\host\path[#fragment]
			{
				final Matcher mat = patSMB.matcher(inputValue);
				if (mat.matches()) {
					final String scheme = "smb";
					final String ssp = "//" + mat.group(1) + "/" + mat.group(2).replace('\\', '/');
					final String fragment = mat.group(3);
					return new URI(scheme, ssp, fragment);
				}
			}
			{
				final Matcher mat = patFile.matcher(inputValue);
				if (mat.matches()) {
					String ssp = mat.group(1);
					if (File.separatorChar != '/') {
						ssp = ssp.replace(File.separatorChar, '/');
					}
					final String fragment = mat.group(3);
					if (mat.group(2) == null) {
						return new URI(null, null, ssp, fragment);
					}
					final String scheme = "file";
					if (ssp.startsWith("//")) {
						ssp = "//" + ssp;
					}
					else if (!ssp.startsWith("/")) {
						ssp = "/" + ssp;
					}
					return new URI(scheme, null, ssp, fragment);
				}
			}
			// if this doesn't work out, we try to
			// recognize an URI of the form
			// [scheme:]scheme-specific-part[#fragment]
			{
				final Matcher mat = patURI.matcher(inputValue);
				if (mat.matches()) {
					final String scheme = mat.group(1);
					final String ssp = mat.group(2).replace('\\', '/');
					final String fragment = mat.group(3);
					return new URI(scheme, ssp, fragment);
				}
			}
			throw new URISyntaxException(inputValue, "This doesn't look like a valid link (URI, file, SMB or URL).");
		}
	}

	private static final Pattern urlPattern = Pattern.compile("file://[^\\s\"'<>]+|(:?https?|ftp)://[^\\s\"|<>{}]+");
	private static final Pattern mailPattern = Pattern.compile("([!+\\-/=~.\\w#]+@[\\w.\\-+?&=%]+)");
    private static final HashMap<String, Icon> menuItemCache = new HashMap<String, Icon>();

	static public String findLink(final String text) {
		final Matcher urlMatcher = urlPattern.matcher(text);
		if (urlMatcher.find()) {
			String link = urlMatcher.group();
			try {
				new URL(link).toURI();
				return link;
			}
			catch (final MalformedURLException e) {
				return null;
			}
			catch (final URISyntaxException e) {
				return null;
			}
		}
		final Matcher mailMatcher = mailPattern.matcher(text);
		if (mailMatcher.find()) {
			final String link = "mailto:" + mailMatcher.group();
			return link;
		}
		return null;
	}

	public static URI createMenuItemLink(final String content) {
		return createItemLink(MENUITEM_SCHEME, content);
	}

	/**
	 * the syntax of  item URIs is
	 * <pre>
	 *   "scheme" + ":" + "_" + <menuItemKey>
	 * </pre>
	 * Compared to <code>mailto:abc@somewhere.com</code> a "_" is added to prevent the rest being parsed
	 * as a regular path.
	 */
	public static URI createItemLink(final String scheme, final String content) {
		try {
			return new URI(scheme, "_" + content, null);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException("huh? URI should have escaped illegal characters", e);
		}
	}

	public static boolean isMenuItemLink(final URI uri) {
		return isSpecialLink(MENUITEM_SCHEME, uri);
	}

	public static boolean isSpecialLink(final String requiredScheme, final URI uri) {
		final String scheme = uri.getScheme();
		return scheme != null && scheme.equals(requiredScheme);
	}

	// this will fail badly for non-menuitem uris!
	public static String parseSpecialLink(final URI uri) {
		return convertPre15VersionStyleKeysToCurrent(uri.getSchemeSpecificPart().substring(1));
	}

	private static String convertPre15VersionStyleKeysToCurrent(final String actionKey) {
		return actionKey.startsWith("$") ? actionKey.replaceFirst("\\$(.*)\\$0", "$1") : actionKey;
	}

	public int getStandardConnectorWidth() {
		final String standardWidth = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_WIDTH);
		final int width = Integer.valueOf(standardWidth);
		return width;
	}

	public Color getStandardConnectorColor() {
        final String standardColor = ResourceController.getResourceController().getProperty(RESOURCES_LINK_COLOR);
		final Color color = ColorUtils.stringToColor(standardColor);
        return color;
    }

	public ConnectorArrows getStandardConnectorArrows() {
		final String standard = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_ARROWS);
		final ConnectorArrows arrows = ConnectorArrows.valueOf(standard);
		return arrows;
	}
	
	public DashVariant getStandardDashVariant() {
		final String standard = ResourceController.getResourceController().getProperty(RESOURCES_DASH_VARIANT);
		final DashVariant variant = DashVariant.valueOf(standard);
		return variant;
	}

	public Shape getStandardConnectorShape() {
		final String standardShape = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_SHAPE);
		final Shape shape = Shape.valueOf(standardShape);
		return shape;
	}

	public int getStandardConnectorAlpha() {
		final String standardAlpha = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_COLOR_ALPHA);
		final int alpha = Integer.valueOf(standardAlpha);
		return alpha;
	}

	public int getAlpha(ConnectorModel connectorModel) {
		return connectorModel.getAlpha();
    }

	public int getStandardLabelFontSize() {
		return ResourceController.getResourceController().getIntProperty("label_font_size", 12);
    }

	public String getStandardLabelFontFamily() {
	    return ResourceController.getResourceController().getProperty("label_font_family");
    }

	private static final String MENUITEM_ICON = "menuitem_icon";
	private static final String EXECUTABLE_ICON = "executable_icon";
	private static final String LINK_ICON = "link_icon";
	private static final String MAIL_ICON = "mail_icon";
	private static final String LINK_LOCAL_ICON = "link_local_icon";

	public static enum LinkType{
		LOCAL(LINK_LOCAL_ICON), MAIL(MAIL_ICON), EXECUTABLE(EXECUTABLE_ICON), MENU(MENUITEM_ICON), DEFAULT(LINK_ICON);
		LinkType(String iconKey){
			this.icon =  ResourceController.getResourceController().getIcon(iconKey);
		}
		final public Icon icon;
	}

	public Icon getLinkIcon(final URI link, final NodeModel model) {
		final LinkType linkType = getLinkType(link, model);
	    if(linkType == null)
	    	return null;
	    if(linkType.equals(LinkType.MENU)){
	    	final String menuItemKey = parseSpecialLink(link);
	    	synchronized (menuItemCache) {
	    	    Icon icon = menuItemCache.get(menuItemKey);
                if (icon == null) {
                    final Icon menuItemIcon = MenuUtils.getMenuItemIcon(menuItemKey);
                    icon = (menuItemIcon == null) ? LinkType.MENU.icon : menuItemIcon;
                    menuItemCache.put(menuItemKey, icon);
                }
	    	    return icon;
	    	}
	    }
	    if(LinkType.DEFAULT == linkType && formatNodeAsHyperlink(model))
	    	return null;
	    return linkType.icon;

	}

	public static LinkType getLinkType(final URI link, final NodeModel model) {
		if (link == null)
			return null;
	    final String linkText = link.toString();
	    if (linkText.startsWith("#")) {
	    	final String id = linkText.substring(1);
	    	if (model == null || model.getMap().getNodeForID(id) == null) {
	    		return null;
	    	}
	    	else{
	    		return LinkType.LOCAL;
	    	}
	    }
	    else if (linkText.startsWith("mailto:")) {
	    	return LinkType.MAIL;
	    }
	    else if (isMenuItemLink(link)) {
	    	return LinkType.MENU;
	    }
		else if (isSpecialLink(EXECUTE_APP_SCHEME, link) || Compat.isWindowsExecutable(link)) {
	    	return LinkType.EXECUTABLE;
	    }
	    else{
	    	return LinkType.DEFAULT;
	    }
	}

	public boolean formatNodeAsHyperlink(final NodeModel node){
		String text = node.getText();
		if (text.isEmpty() || HtmlUtils.isHtmlNode(text))
			return false;
		final Boolean ownFlag = ownFormatNodeAsHyperlink(node);
		if(ownFlag != null)
			return ownFlag;
		Collection<IStyle> collection = LogicalStyleController.getController(modeController).getStyles(node);
		final MapStyleModel mapStyles = MapStyleModel.getExtension(node.getMap());
		for(IStyle styleKey : collection){
			final NodeModel styleNode = mapStyles.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final Boolean styleFlag = ownFormatNodeAsHyperlink(styleNode);
			if(styleFlag != null)
				return styleFlag;

		}
		return false;
	}

	private Boolean ownFormatNodeAsHyperlink(final NodeModel node){
		final NodeLinks linkModel = NodeLinks.getLinkExtension(node);
		if(linkModel == null){
			return null;
		}
		final Boolean formatNodeAsHyperlink = linkModel.formatNodeAsHyperlink();
		return formatNodeAsHyperlink;
	}

	public void loadURI(NodeModel node, URI uri) {
		loadURI(uri);
	}
}
