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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.ExclusivePropertyChain;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.IPropertyGetter;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 */
public class LinkController implements IExtension {
	private static class ArrowLinkListener implements IFreeplanePropertyListener {
		public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
			if (propertyName.equals(ResourceController.RESOURCES_LINK_COLOR)) {
				standardColor = TreeXmlReader.xmlToColor(newValue);
			}
		}
	}

	private static ArrowLinkListener listener = null;
	public static final int STANDARD_WIDTH = 1;
	static Color standardColor = null;

	public static LinkController getController(final ModeController modeController) {
		return (LinkController) modeController.getExtension(LinkController.class);
	}

	public static void install(final ModeController modeController, final LinkController linkController) {
		modeController.addExtension(LinkController.class, linkController);
		final INodeSelectionListener listener = new INodeSelectionListener() {
			public void onDeselect(final NodeModel node) {
			}

			public void onSelect(final NodeModel node) {
				String link = NodeLinks.getLink(node);
				link = (link != null ? link : " ");
				node.getModeController().getController().getViewController().out(link);
			}
		};
		modeController.getMapController().addNodeSelectionListener(listener);
	}

	final private ExclusivePropertyChain<Color, ArrowLinkModel> colorHandlers;
	final private ModeController modeController;

	public LinkController(final ModeController modeController) {
		this.modeController = modeController;
		updateStandards(modeController);
		colorHandlers = new ExclusivePropertyChain<Color, ArrowLinkModel>();
		if (listener == null) {
			listener = new ArrowLinkListener();
			Controller.getResourceController().addPropertyChangeListener(listener);
		}
		addColorGetter(ExclusivePropertyChain.NODE, new IPropertyGetter<Color, ArrowLinkModel>() {
			public Color getProperty(final ArrowLinkModel model, final Color currentValue) {
				return model.getColor();
			}
		});
		addColorGetter(ExclusivePropertyChain.DEFAULT, new IPropertyGetter<Color, ArrowLinkModel>() {
			public Color getProperty(final ArrowLinkModel model, final Color currentValue) {
				return standardColor;
			}
		});
		createActions(modeController);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		new LinkBuilder().registerBy(readManager, writeManager);
	}

	public IPropertyGetter<Color, ArrowLinkModel> addColorGetter(final Integer key,
	                                                             final IPropertyGetter<Color, ArrowLinkModel> getter) {
		return colorHandlers.addGetter(key, getter);
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		modeController.addAction("followLink", new FollowLinkAction(modeController.getController()));
		modeController.addAction("gotoLinkNodeAction", new GotoLinkNodeAction(this, null));
	}

	protected void createArrowLinkPopup(final ArrowLinkModel link, final JPopupMenu arrowLinkPopup) {
		arrowLinkPopup.add(new GotoLinkNodeAction(this, link.getSource()));
		arrowLinkPopup.add(new GotoLinkNodeAction(this, link.getTarget()));
		arrowLinkPopup.addSeparator();
		final HashSet NodeAlreadyVisited = new HashSet();
		NodeAlreadyVisited.add(link.getSource());
		NodeAlreadyVisited.add(link.getTarget());
		final Collection<LinkModel> links = new LinkedList<LinkModel>();;
		links.addAll(NodeLinks.getLinks(link.getSource()));
		links.addAll(NodeLinks.getLinks(link.getTarget()));
		final Iterator<LinkModel> iterator = links.iterator();
		while (iterator.hasNext()) {
			final ArrowLinkModel foreign_link = (ArrowLinkModel) iterator.next();
			if (NodeAlreadyVisited.add(foreign_link.getTarget())) {
				arrowLinkPopup.add(new GotoLinkNodeAction(this, foreign_link.getTarget()));
			}
			if (NodeAlreadyVisited.add(foreign_link.getSource())) {
				arrowLinkPopup.add(new GotoLinkNodeAction(this, foreign_link.getSource()));
			}
		}
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
				return dest.getShortText(modeController);
			}
			catch (final Exception e) {
				return modeController.getText("link_not_available_any_more");
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
			links.setLocalHyperlink(link.substring(1));
		}
		links.setLink(link);
	}

	public void loadURL() {
		final String link = NodeLinks.getLink(modeController.getMapController().getSelectedNode());
		if (link != null) {
			modeController.getMapController().loadURL(link);
		}
	}

	public IPropertyGetter<Color, ArrowLinkModel> removeColorGetter(final Integer key) {
		return colorHandlers.removeGetter(key);
	}

	/**
	 * @param modeController
	 */
	private void updateStandards(final ModeController modeController) {
		if (standardColor == null) {
			final String stdColor = Controller.getResourceController().getProperty(
			    ResourceController.RESOURCES_LINK_COLOR);
			if (stdColor != null && stdColor.length() == 7) {
				standardColor = TreeXmlReader.xmlToColor(stdColor);
			}
			else {
				standardColor = Color.RED;
			}
		}
	}
}
