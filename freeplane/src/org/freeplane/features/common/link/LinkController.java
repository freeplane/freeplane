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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.ExclusivePropertyChain;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.IPropertyHandler;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.SelectionController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public class LinkController extends SelectionController implements IExtension {
	private static class ArrowLinkListener implements IFreeplanePropertyListener {
		public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
			if (propertyName.equals(LinkController.RESOURCES_LINK_COLOR)) {
				standardColor = ColorUtils.stringToColor(newValue);
			}
		}
	}

	private static ArrowLinkListener listener = null;
	public static final String RESOURCES_LINK_COLOR = "standardlinkcolor";
	public static final int STANDARD_WIDTH = 2;
	static Color standardColor = null;

	public static LinkController getController(final ModeController modeController) {
		return (LinkController) modeController.getExtension(LinkController.class);
	}

	public static void install(final Controller controller) {
		FilterController.getController(controller).getConditionFactory().addConditionController(3,
		    new LinkConditionController());
	}

	public static void install(final ModeController modeController, final LinkController linkController) {
		modeController.addExtension(LinkController.class, linkController);
		final INodeSelectionListener listener = new INodeSelectionListener() {
			public void onDeselect(final NodeModel node) {
			}

			public void onSelect(final NodeModel node) {
				final URI link = NodeLinks.getValidLink(node);
				final String linkString = (link != null ? link.toString() : null);
				if (linkString != null) {
					modeController.getController().getViewController().out(linkString);
				}
			}
		};
		modeController.getMapController().addNodeSelectionListener(listener);
		modeController.getController();
	}

	final private ExclusivePropertyChain<Color, ConnectorModel> colorHandlers;
	final private ModeController modeController;

	public LinkController(final ModeController modeController) {
		this.modeController = modeController;
		updateStandards(modeController);
		colorHandlers = new ExclusivePropertyChain<Color, ConnectorModel>();
		if (listener == null) {
			listener = new ArrowLinkListener();
			ResourceController.getResourceController().addPropertyChangeListener(listener);
		}
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, ConnectorModel>() {
			public Color getProperty(final ConnectorModel model, final Color currentValue) {
				return model.getColor();
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, ConnectorModel>() {
			public Color getProperty(final ConnectorModel model, final Color currentValue) {
				return standardColor;
			}
		});
		createActions(modeController);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		new LinkBuilder(this).registerBy(readManager, writeManager);
	}

	public IPropertyHandler<Color, ConnectorModel> addColorGetter(final Integer key,
	                                                              final IPropertyHandler<Color, ConnectorModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	private void addLinks(final JPopupMenu arrowLinkPopup, final NodeModel source) {
		final IMapSelection selection = getModeController().getController().getSelection();
		if (!selection.isSelected(source)) {
			arrowLinkPopup.add(new GotoLinkNodeAction(this, source));
		}
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		modeController.addAction(new FollowLinkAction(modeController.getController()));
	}

	protected void createArrowLinkPopup(final ConnectorModel link, final JPopupMenu arrowLinkPopup) {
		final NodeModel source = link.getSource();
		final NodeModel target = link.getTarget();
		addLinks(arrowLinkPopup, source);
		addLinks(arrowLinkPopup, target);
	}

	public Color getColor(final ConnectorModel model) {
		return colorHandlers.getProperty(model);
	}

	public String getLinkShortText(final NodeModel node) {
		final URI uri = NodeLinks.getLink(node);
		if (uri == null) {
			return null;
		}
		final String adaptedText = uri.toString();
		if (adaptedText.startsWith("#")) {
			final NodeModel dest = modeController.getMapController().getNodeFromID(adaptedText.substring(1));
			if (dest != null) {
				return dest.getShortText();
			}
			return ResourceBundles.getText("link_not_available_any_more");
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

	public ModeController getModeController() {
		return modeController;
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

	public int getWidth(final NodeLinkModel model) {
		return STANDARD_WIDTH;
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
			LogTool.warn(e1);
			UITools.errorMessage(FpStringUtils.formatText("link_error", link));
			return;
		}
	}

	public void loadURL() {
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final URI link = NodeLinks.getValidLink(selectedNode);
		if (link != null) {
			onDeselect(selectedNode);
			((UrlManager) modeController.getMapController().getModeController().getExtension(UrlManager.class))
			    .loadURL(link);
			onSelect(modeController.getController().getSelection().getSelected());
		}
	}

	public IPropertyHandler<Color, ConnectorModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	/**
	 * @param modeController
	 */
	private void updateStandards(final ModeController modeController) {
		if (standardColor == null) {
			final String stdColor = ResourceController.getResourceController().getProperty(
			    LinkController.RESOURCES_LINK_COLOR);
			if (stdColor != null && stdColor.length() == 7) {
				standardColor = ColorUtils.stringToColor(stdColor);
			}
			else {
				standardColor = Color.RED;
			}
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
}
