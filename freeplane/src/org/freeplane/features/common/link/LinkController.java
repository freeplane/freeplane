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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.ColorUtils;

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
	public static final int STANDARD_WIDTH = 1;
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
				String link = NodeLinks.getLink(node);
				link = (link != null ? link : " ");
				modeController.getController().getViewController().out(link);
			}
		};
		modeController.getMapController().addNodeSelectionListener(listener);
		modeController.getController();
	}

	final private ExclusivePropertyChain<Color, ArrowLinkModel> colorHandlers;
	final private ModeController modeController;

	public LinkController(final ModeController modeController) {
		this.modeController = modeController;
		updateStandards(modeController);
		colorHandlers = new ExclusivePropertyChain<Color, ArrowLinkModel>();
		if (listener == null) {
			listener = new ArrowLinkListener();
			ResourceController.getResourceController().addPropertyChangeListener(listener);
		}
		addColorGetter(IPropertyHandler.NODE, new IPropertyHandler<Color, ArrowLinkModel>() {
			public Color getProperty(final ArrowLinkModel model, final Color currentValue) {
				return model.getColor();
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, ArrowLinkModel>() {
			public Color getProperty(final ArrowLinkModel model, final Color currentValue) {
				return standardColor;
			}
		});
		createActions(modeController);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		new LinkBuilder(this).registerBy(readManager, writeManager);
	}

	public IPropertyHandler<Color, ArrowLinkModel> addColorGetter(final Integer key,
	                                                              final IPropertyHandler<Color, ArrowLinkModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	private void addForeignLinks(final JPopupMenu arrowLinkPopup, final HashSet<NodeModel> nodeAlreadyVisited,
	                             final Collection<LinkModel> sourceLinks) {
		final Collection<LinkModel> links = new LinkedList<LinkModel>();;
		links.addAll(sourceLinks);
		for (final LinkModel foreign_link : links) {
			if (!(foreign_link instanceof ArrowLinkModel)) {
				continue;
			}
			final ArrowLinkModel arrowLink = (ArrowLinkModel) foreign_link;
			final NodeModel foreignTarget = arrowLink.getTarget();
			if (nodeAlreadyVisited.add(foreignTarget)) {
				arrowLinkPopup.add(new GotoLinkNodeAction(this, foreignTarget));
			}
			final NodeModel foreignSource = arrowLink.getSource();
			if (nodeAlreadyVisited.add(foreignSource)) {
				arrowLinkPopup.add(new GotoLinkNodeAction(this, foreignSource));
			}
		}
	}

	private void addLinks(final JPopupMenu arrowLinkPopup, final NodeModel source,
	                      final HashSet<NodeModel> nodeAlreadyVisited) {
		boolean actionsAdded = false;
		final IMapSelection selection = getModeController().getController().getSelection();
		if (!selection.isSelected(source)) {
			arrowLinkPopup.add(new GotoLinkNodeAction(this, source));
			actionsAdded = true;
		}
		addForeignLinks(arrowLinkPopup, nodeAlreadyVisited, NodeLinks.getLinks(source));
		if (actionsAdded) {
			actionsAdded = false;
			arrowLinkPopup.addSeparator();
		}
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		modeController.addAction(new FollowLinkAction(modeController.getController()));
		modeController.addAction(new GotoLinkNodeAction(this, null));
	}

	protected void createArrowLinkPopup(final ArrowLinkModel link, final JPopupMenu arrowLinkPopup) {
		final NodeModel source = link.getSource();
		final NodeModel target = link.getTarget();
		final HashSet<NodeModel> nodeAlreadyVisited = new HashSet<NodeModel>();
		nodeAlreadyVisited.add(source);
		nodeAlreadyVisited.add(target);
		addLinks(arrowLinkPopup, source, nodeAlreadyVisited);
		addLinks(arrowLinkPopup, target, nodeAlreadyVisited);
	}

	public Color getColor(final ArrowLinkModel model) {
		return colorHandlers.getProperty(model);
	}

	public String getLinkShortText(final NodeModel node) {
		final String adaptedText = NodeLinks.getLink(node);
		if (adaptedText == null) {
			return null;
		}
		if (adaptedText.startsWith("#")) {
			try {
				final NodeModel dest = modeController.getMapController().getNodeFromID(adaptedText.substring(1));
				return dest.getShortText();
			}
			catch (final Exception e) {
				return ResourceBundles.getText("link_not_available_any_more");
			}
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
		if (obj instanceof ArrowLinkModel) {
			final ArrowLinkModel link = (ArrowLinkModel) obj;
			final JPopupMenu arrowLinkPopup = new JPopupMenu();
			createArrowLinkPopup(link, arrowLinkPopup);
			return arrowLinkPopup;
		}
		return null;
	}

	public int getWidth(final ArrowLinkModel model) {
		return STANDARD_WIDTH;
	}

	public void loadLink(final NodeModel node, final String link) {
		NodeLinks links = NodeLinks.getLinkExtension(node);
		if (links == null) {
			links = NodeLinks.createLinkExtension(node);
		}
		if (link != null && link.startsWith("#")) {
			links.setLocalHyperlink(node, link.substring(1));
		}
		links.setHyperLink(link);
	}

	public void loadURL() {
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final String link = NodeLinks.getLink(selectedNode);
		if (link != null) {
			onDeselect(selectedNode);
			modeController.getMapController().loadURL(link);
			onSelect(modeController.getController().getSelection().getSelected());
		}
	}

	public IPropertyHandler<Color, ArrowLinkModel> removeColorGetter(final Integer key) {
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
}
