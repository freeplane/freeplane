/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.view.swing.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JComponent;

import org.apache.commons.lang.StringEscapeUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;

/** */
class ClickableImageCreator {
	public static class AreaHolder {
		String alt;
		Rectangle coordinates = new Rectangle();
		String href;
		String shape = "rect";
		String title;
	}

	Vector<AreaHolder> area = new Vector<AreaHolder>();
	private Rectangle innerBounds;
	final private MapView mapView;
	final private String regExpLinkReplacement;
	final private NodeModel root;

	/**
	 * @param regExpLinkReplacement
	 *            if for example the link abc must be replaced with FMabcFM,
	 *            then this string has to be FM$1FM.
	 */
	ClickableImageCreator(final NodeModel root, final ModeController modeController, final String regExpLinkReplacement) {
		super();
		this.root = root;
		this.regExpLinkReplacement = regExpLinkReplacement;
		mapView = ((MapView) modeController.getController().getMapViewManager().getMapViewComponent());
		if (mapView != null) {
			mapView.preparePrinting();
			innerBounds = mapView.getInnerBounds();
		}
		else {
			innerBounds = new Rectangle(0, 0, 100, 100);
		}
//		this.modeController = modeController;
		createArea();
		if (mapView != null)
			mapView.endPrinting();
	}

	private void createArea() {
		createArea(root);
	}

	private void createArea(final NodeModel node) {
		final NodeView nodeView = mapView.getNodeView(node);
		if (nodeView != null) {
			final AreaHolder holder = new AreaHolder();
			holder.title = TextController.getController().getShortPlainText(node);
			holder.alt = TextController.getController().getShortPlainText(node);
			holder.href = node.createID();
			final Point contentXY = mapView.getNodeContentLocation(nodeView);
			final JComponent content = nodeView.getContent();
			holder.coordinates.x = (int) (contentXY.x - innerBounds.getMinX());
			holder.coordinates.y = (int) (contentXY.y - innerBounds.getMinY());
			holder.coordinates.width = content.getWidth();
			holder.coordinates.height = content.getHeight();
			area.add(holder);
			for (final NodeModel child: node.getChildren()) {
				createArea(child);
			}
		}
	}

	public String generateHtml() {
		final StringBuilder htmlArea = new StringBuilder();
		for (final AreaHolder holder : area) {
			htmlArea.append("<area shape=\"" + holder.shape + "\" href=\"#"
			        + holder.href.replaceFirst("^(.*)$", regExpLinkReplacement) + "\" alt=\""
			        + StringEscapeUtils.escapeHtml(holder.alt) + "\" title=\""
			        + StringEscapeUtils.escapeHtml(holder.title) + "\" coords=\"" + holder.coordinates.x + ","
			        + holder.coordinates.y + "," + (holder.coordinates.width + holder.coordinates.x) + ","
			        + +(holder.coordinates.height + holder.coordinates.y) + "\" />\n");
		}
		return htmlArea.toString();
	}
}
