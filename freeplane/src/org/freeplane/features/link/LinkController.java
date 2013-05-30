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
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
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
	public static LinkController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LinkController getController(ModeController modeController) {
		return (LinkController) modeController.getExtension(LinkController.class);
	}
	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(3,
		    new LinkConditionController());
	}

	public static void install( final LinkController linkController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LinkController.class, linkController);
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

	public static final String LINK_ICON = ResourceController.getResourceController().getProperty("link_icon");
	private static final String MAIL_ICON = ResourceController.getResourceController().getProperty("mail_icon");
	public static final String LINK_LOCAL_ICON = ResourceController.getResourceController().getProperty(
	"link_local_icon");

// 	final private ModeController modeController;

	public LinkController() {
//		this.modeController = modeController;
		createActions();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		new LinkBuilder(this).registerBy(readManager, writeManager);
		final LinkTransformer textTransformer = new LinkTransformer(modeController, 10);
		TextController.getController(modeController).addTextTransformer(textTransformer);
		textTransformer.registerListeners(modeController);
	}

	private void addLinks(final JComponent arrowLinkPopup, final NodeModel source) {
		final IMapSelection selection = Controller.getCurrentModeController().getController().getSelection();
		if (!selection.isSelected(source)) {
			GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(this, source);
            addAction(arrowLinkPopup, gotoLinkNodeAction);
		}
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
        else
            componentBox = component;
        componentBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        componentBox.setMinimumSize(new Dimension());
        componentBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        arrowLinkPopup.add(componentBox);
    }

    protected void addAction(final JComponent arrowLinkPopup, Action action) {
        JButton comp = new JButton(action);
        comp.setHorizontalAlignment(JButton.LEFT);
        comp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.getWindowAncestor(arrowLinkPopup).setVisible(false);
            }
        });
        addPopupComponent (arrowLinkPopup, null, comp);
    }

	/**
	 *
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new FollowLinkAction());
		modeController.addMenuContributor(new LinkMenuContributor("menu_navigate", "menu_goto_links"));
		modeController.addMenuContributor(new LinkMenuContributor("popup_navigate", "popup_goto_links"));
	}

    private class LinkMenuContributor implements IMenuContributor {
    	final String key;
        final String menuKey;
	    public LinkMenuContributor(String menuKey, String key) {
	        super();
	        this.menuKey = menuKey;
	        this.key = key;
        }
		public void updateMenus(final ModeController modeController, final MenuBuilder builder) {
			if(builder.contains(key)) {
	            builder.addPopupMenuListener(menuKey, new PopupMenuListener(
	            		) {
	            		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	            			final IMapSelection selection = modeController.getController().getSelection();
	            			if(selection == null)
	            				return;
							final NodeModel node = selection.getSelected();
	            			Set<LinkModel> links = new LinkedHashSet<LinkModel>( NodeLinks.getLinks(node));
	            			links.addAll(getLinksTo(node));
	            			if(links.isEmpty())
	            				return;
	            			builder.addSeparator(key, MenuBuilder.AS_CHILD);
	            			for(LinkModel link : links){
	            				final String targetID = link.getTargetID();
	            				final NodeModel target;
	            				if(node.getID().equals(targetID)){
	            					if(link instanceof ConnectorModel){
	            						ConnectorModel cm = (ConnectorModel) link;
	            						target = cm.getSource();
	            						if(node.equals(target))
	            							continue;
	            					}
	            					else
	            						continue;
	            				}
	            				else
	            					target = node.getMap().getNodeForID(targetID);
	            				final GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(LinkController.this, target);
	            				if(!(link instanceof ConnectorModel)){
	            					gotoLinkNodeAction.putValue(Action.SMALL_ICON, ICON_STORE.getUIIcon(LINK_LOCAL_ICON).getIcon());
	            				}
	            				builder.addAction(key, gotoLinkNodeAction, MenuBuilder.AS_CHILD);
	            			}
	            		}
	            		
	            		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	            			builder.removeChildElements(key);
	            		}
	            		
	            		public void popupMenuCanceled(PopupMenuEvent e) {
	            		}
	            	});
            }
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
		
		final InputMap inputMap = arrowLinkPopup.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		final ActionMap actionMap = arrowLinkPopup.getActionMap();
		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL);
		actionMap.put(CANCEL, new ClosePopupAction(CANCEL));
		final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty("el__enter_confirms_by_default");
		final KeyStroke close = KeyStroke.getKeyStroke(enterConfirms ? "ENTER" : "alt ENTER");
		inputMap.put(close, CLOSE);
		actionMap.put(CLOSE, new ClosePopupAction(CLOSE));

		final NodeModel source = link.getSource();
		final NodeModel target = link.getTarget();
		addLinks(arrowLinkPopup, source);
		addLinks(arrowLinkPopup, target);
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
				return TextController.getController().getShortText(dest);
			}
			return TextUtils.getText("link_not_available_any_more");
		}
		return adaptedText;
	}

	public Collection<LinkModel> getLinksTo(final NodeModel target) {
		if (target.hasID() == false) {
			return Collections.emptySet();
		}
		final MapLinks links = (MapLinks) target.getMap().getExtension(MapLinks.class);
		if (links == null) {
			return Collections.emptySet();
		}
		final Set<LinkModel> set = links.get(target.createID());
		if (set == null) {
			return Collections.emptySet();
		}
		return set;
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
	private static final String RESOURCES_CONNECTOR_COLOR_ALPHA = "connector_alpha";
	private static final String RESOURCES_CONNECTOR_WIDTH = "connector_width";
	
	public int getWidth(final ConnectorModel model) {
		return model.getWidth();
	}

	void loadLink(final NodeModel node, String link) {
		NodeLinks links = NodeLinks.getLinkExtension(node);
		if (links == null) {
			links = NodeLinks.createLinkExtension(node);
		}
		if (link != null && link.startsWith("#")) {
			links.setLocalHyperlink(node, link.substring(1));
		}
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
				final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				final DefaultMutableTreeNode treeNode = menuBuilder.get(LinkController.parseMenuItemLink(link));
				if (treeNode == null || !treeNode.isLeaf() || !(treeNode.getUserObject() instanceof JMenuItem)) {
					LogUtils.warn("node " + link + " should have been an executable action");
					return;
				}
				final JMenuItem menuItem = (JMenuItem) treeNode.getUserObject();
				final Action action = menuItem.getAction();
				action.actionPerformed(e);
			}
			else {
				loadURI(link);
			}
			onSelect(modeController.getController().getSelection().getSelected());
		}
    }

	public static URI toRelativeURI(final File map, final File input) {
		try {
			final URI fileUri = input.getAbsoluteFile().toURI();
			if (map == null) {
				return fileUri;
			}
			final URI mapUri = map.getAbsoluteFile().toURI();
			final String filePathAsString = fileUri.getRawPath();
			final String mapPathAsString = mapUri.getRawPath();
			int differencePos;
			final int lastIndexOfSeparatorInMapPath = mapPathAsString.lastIndexOf("/");
			final int lastIndexOfSeparatorInFilePath = filePathAsString.lastIndexOf("/");
			int lastCommonSeparatorPos = 0;
			for (differencePos = 1; differencePos <= lastIndexOfSeparatorInMapPath
			        && differencePos <= lastIndexOfSeparatorInFilePath
			        && filePathAsString.charAt(differencePos) == mapPathAsString.charAt(differencePos); differencePos++) {
				if (filePathAsString.charAt(differencePos) == '/') {
					lastCommonSeparatorPos = differencePos;
				}
			}
			if (lastCommonSeparatorPos == 0) {
				return fileUri;
			}
			final StringBuilder relativePath = new StringBuilder();
			for (int i = lastCommonSeparatorPos + 1; i <= lastIndexOfSeparatorInMapPath; i++) {
				if (mapPathAsString.charAt(i) == '/') {
					relativePath.append("../");
				}
			}
			relativePath.append(filePathAsString.substring(lastCommonSeparatorPos + 1));
			return new URI(relativePath.toString());
		}
		catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
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

	/** 
	 * the syntax of menu item URIs is
	 * <pre>
	 *   "menuitem" + ":" + "_" + <menuItemKey>
	 * </pre>
	 * Compared to <code>mailto:abc@somewhere.com</code> a "_" is added to prevent the rest being parsed
	 * as a regular path. (Menu item keys start with "/").
	 */
	public static URI createMenuItemLink(final String menuItemKey) {
		try {
			return new URI(MENUITEM_SCHEME, "_" + menuItemKey, null);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException("huh? URI should have escaped illegal characters", e);
		}
	}

	public static boolean isMenuItemLink(final URI uri) {
		final String scheme = uri.getScheme();
		return scheme != null && scheme.equals(MENUITEM_SCHEME);
	}

	// this will fail badly for non-menuitem uris!
	public static String parseMenuItemLink(final URI uri) {
		return uri.getSchemeSpecificPart().substring(1);
	}
	
	public int getStandardConnectorWidth() {
		final String standardWidth = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_WIDTH);
		final int width = Integer.valueOf(standardWidth);
		return width;
	}
	
	public void setStandardConnectorWidth(final int width) {
		final String value = Integer.toString(width);
		ResourceController.getResourceController().setProperty(RESOURCES_CONNECTOR_WIDTH, value);
	}

	public Color getStandardConnectorColor() {
        final String standardColor = ResourceController.getResourceController().getProperty(RESOURCES_LINK_COLOR);
		final Color color = ColorUtils.stringToColor(standardColor);
        return color;
    }

	public void setStandardConnectorColor(final Color color) {
		String value = ColorUtils.colorToString(color);
		ResourceController.getResourceController().setProperty(RESOURCES_LINK_COLOR, value);
	}

	public Shape getStandardConnectorShape() {
		final String standardShape = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_SHAPE);
		final Shape shape = Shape.valueOf(standardShape);
		return shape;
	}
	
	public void setStandardConnectorShape(final Shape shape) {
		String value = shape.toString();
		ResourceController.getResourceController().setProperty(RESOURCES_CONNECTOR_SHAPE, value);
	}


	public int getStandardConnectorAlpha() {
		final String standardAlpha = ResourceController.getResourceController().getProperty(RESOURCES_CONNECTOR_COLOR_ALPHA);
		final int alpha = Integer.valueOf(standardAlpha);
		return alpha;
	}
	
	public void setStandardAlpha(final int alpha) {
		final String value = Integer.toString(alpha);
		ResourceController.getResourceController().setProperty(RESOURCES_CONNECTOR_COLOR_ALPHA, value);
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

	private static final String MENUITEM_ICON = "icons/button.png";
	private static final String EXECUTABLE_ICON = ResourceController.getResourceController().getProperty("executable_icon");
	private static final IconStore ICON_STORE = IconStoreFactory.create();
	public static enum LinkType{
		LOCAL(LINK_LOCAL_ICON), MAIL(MAIL_ICON), EXECUTABLE(EXECUTABLE_ICON), MENU(MENUITEM_ICON), DEFAULT(LINK_ICON);
		LinkType(String iconPath){
			final UIIcon uiIcon = ICON_STORE.getUIIcon(iconPath);
			if(uiIcon == null)
				this.icon =  null;
			else
				this.icon =  uiIcon.getIcon();
		}
		final public Icon icon;
	}
	
	public static Icon getLinkIcon(final URI link, final NodeModel model) {
		final LinkType linkType = getLinkType(link, model);
	    if(linkType == null)
	    	return null;
	    if(linkType.equals(LinkType.MENU)){
	    	final String menuItemKey = parseMenuItemLink(link);
	    	synchronized (menuItemCache) {
	    	    Icon icon = menuItemCache.get(menuItemKey);
                if (icon == null) {
                    final Icon menuItemIcon = MenuUtils.getMenuItemIcon(menuItemKey);
                    icon = (menuItemIcon == null) ? ICON_STORE.getUIIcon(MENUITEM_ICON).getIcon() : menuItemIcon;
                    menuItemCache.put(menuItemKey, icon);
                }
	    	    return icon;
	    	}
	    }
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
	    else if (Compat.isWindowsExecutable(link)) {
	    	return LinkType.EXECUTABLE;
	    }
	    else{
	    	return LinkType.DEFAULT;
	    }
	}

	public boolean formatNodeAsHyperlink(final NodeModel node){
	 return formatNodeAsHyperlink(Controller.getCurrentModeController(), node);
	}
	
	public boolean formatNodeAsHyperlink(final ModeController modeController, final NodeModel node){
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
		final NodeLinks linkModel = NodeLinks.getModel(node);
		if(linkModel == null){
			return null;
		}
		final Boolean formatNodeAsHyperlink = linkModel.formatNodeAsHyperlink();
		return formatNodeAsHyperlink;
	}

}
