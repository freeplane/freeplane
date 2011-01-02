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
package org.freeplane.features.common.link;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.controller.SelectionController;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.link.ConnectorModel.Shape;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.url.UrlManager;

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

// 	final private ModeController modeController;

	public LinkController() {
//		this.modeController = modeController;
		createActions();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		new LinkBuilder(this).registerBy(readManager, writeManager);
	}

	private void addLinks(final JPopupMenu arrowLinkPopup, final NodeModel source) {
		final IMapSelection selection = Controller.getCurrentModeController().getController().getSelection();
		if (!selection.isSelected(source)) {
			arrowLinkPopup.add(new GotoLinkNodeAction(this, source));
		}
	}

	/**
	 *
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new FollowLinkAction());
	}

	protected void createArrowLinkPopup(final ConnectorModel link, final JPopupMenu arrowLinkPopup) {
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
				return dest.getShortText();
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
	public JPopupMenu getPopupForModel(final java.lang.Object obj) {
		if (obj instanceof ConnectorModel) {
			final ConnectorModel link = (ConnectorModel) obj;
			final JPopupMenu arrowLinkPopup = new JPopupMenu();
			createArrowLinkPopup(link, arrowLinkPopup);
			return arrowLinkPopup;
		}
		return null;
	}

	public static final String RESOURCES_LINK_COLOR = "standardlinkcolor";
	private static final String RESOURCES_LINK_SHAPE = "link_shape";
	private static final String RESOURCES_LINK_COLOR_ALPHA = "link_alpha";
	private static final String RESOURCES_LINK_WIDTH = "link_width";
	
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

	public void loadURL(final MouseEvent e) {
		ModeController modeController = Controller.getCurrentModeController();
		loadURL(modeController.getMapController().getSelectedNode(), new ActionEvent(e.getSource(), e.getID(), null));
	}

	// TODO: document why ActionEvent?
	void loadURL(final NodeModel selectedNode, final ActionEvent e) {
		final URI link = NodeLinks.getValidLink(selectedNode);
		if (link != null) {
			onDeselect(selectedNode);
			ModeController modeController = Controller.getCurrentModeController();
			if (LinkController.isMenuItemLink(link)) {
				if (e == null) {
					throw new IllegalArgumentException("ActionEvent is needed for menu item links");
				}
				final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				final DefaultMutableTreeNode treeNode = menuBuilder.get(LinkController.parseMenuItemLink(link));
				if (!treeNode.isLeaf() || !(treeNode.getUserObject() instanceof JMenuItem)) {
					throw new RuntimeException("node " + treeNode + " should have been an executable action");
				}
				final JMenuItem menuItem = (JMenuItem) treeNode.getUserObject();
				final Action action = menuItem.getAction();
				action.actionPerformed(e);
			}
			else {
				getURLManager().loadURL(link);
			}
			onSelect(modeController.getController().getSelection().getSelected());
		}
	}

	private UrlManager getURLManager() {
		ModeController modeController = Controller.getCurrentModeController();
		return (UrlManager) modeController.getExtension(UrlManager.class);
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
			// TODO Auto-generated catch block
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
		final String standardWidth = ResourceController.getResourceController().getProperty(RESOURCES_LINK_WIDTH);
		final int width = Integer.valueOf(standardWidth);
		return width;
	}
	
	public void setStandardConnectorWidth(final int width) {
		final String value = Integer.toString(width);
		ResourceController.getResourceController().setProperty(RESOURCES_LINK_WIDTH, value);
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
		final String standardShape = ResourceController.getResourceController().getProperty(RESOURCES_LINK_SHAPE);
		final Shape shape = Shape.valueOf(standardShape);
		return shape;
	}
	
	public void setStandardConnectorShape(final Shape shape) {
		String value = shape.toString();
		ResourceController.getResourceController().setProperty(RESOURCES_LINK_SHAPE, value);
	}


	public int getStandardAlpha() {
		final String standardAlpha = ResourceController.getResourceController().getProperty(RESOURCES_LINK_COLOR_ALPHA);
		final int alpha = Integer.valueOf(standardAlpha);
		return alpha;
	}
	
	public void setStandardAlpha(final int alpha) {
		final String value = Integer.toString(alpha);
		ResourceController.getResourceController().setProperty(RESOURCES_LINK_COLOR_ALPHA, value);
	}

	public int getAlpha(ConnectorModel connectorModel) {
		return connectorModel.getAlpha();
    }

}
