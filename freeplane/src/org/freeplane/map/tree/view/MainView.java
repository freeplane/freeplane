/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.map.tree.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.freeplane.controller.Freeplane;
import org.freeplane.main.HtmlTools;
import org.freeplane.main.Tools;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.ui.FreemindMenuBar;

/**
 * Base class for all node views.
 */
public abstract class MainView extends JLabel {
	static Dimension maximumSize = new Dimension(Integer.MAX_VALUE,
	    Integer.MAX_VALUE);
	private static final int MIN_HOR_NODE_SIZE = 10;
	static Dimension minimumSize = new Dimension(0, 0);
	static final float ZOOM_CORRECTION_FACTOR = 0.97F;
	protected int isDraggedOver = NodeView.DRAGGED_OVER_NO;
	private boolean isLong = false;
	private boolean isPainting;

	MainView() {
		isPainting = false;
		setAlignmentX(Component.CENTER_ALIGNMENT);
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
	}

	protected void convertPointFromMap(final Point p) {
		Tools.convertPointFromAncestor(getNodeView().getMap(), p, this);
	}

	protected void convertPointToMap(final Point p) {
		Tools.convertPointToAncestor(this, p, getNodeView().getMap());
	}

	public boolean dropAsSibling(final double xCoord) {
		return isInVerticalRegion(xCoord, 1. / 3);
	}

	/** @return true if should be on the left, false otherwise. */
	public boolean dropPosition(final double xCoord) {
		/* here it is the same as me. */
		return getNodeView().isLeft();
	}

	abstract int getAlignment();

	abstract Point getCenterPoint();

	/** get x coordinate including folding symbol */
	public int getDeltaX() {
		return 0;
	}

	/** get y coordinate including folding symbol */
	public int getDeltaY() {
		return 0;
	}

	public int getDraggedOver() {
		return isDraggedOver;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		if (isPainting) {
			final float zoom = getZoom();
			if (zoom != 1F) {
				return (int) (super.getHeight() / zoom);
			}
		}
		return super.getHeight();
	}

	protected int getIconWidth() {
		final Icon icon = getIcon();
		if (icon == null) {
			return 0;
		}
		return getNodeView().getMap().getZoomed(icon.getIconWidth());
	}

	public boolean getIsLong() {
		return isLong;
	}

	abstract Point getLeftPoint();

	/** get height including folding symbol */
	protected int getMainViewHeightWithFoldingMark() {
		return getHeight();
	}

	/** get width including folding symbol */
	protected int getMainViewWidthWithFoldingMark() {
		return getWidth();
	}

	@Override
	public Dimension getMaximumSize() {
		return MainView.maximumSize;
	}

	@Override
	public Dimension getMinimumSize() {
		return MainView.minimumSize;
	}

	public NodeView getNodeView() {
		return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class,
		    this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		final String text = getText();
		final boolean isEmpty = text.length() == 0
		        || HtmlTools.isHtmlNode(text) && text.indexOf("<img") < 0
		        && HtmlTools.htmlToPlain(text).length() == 0;
		if (isEmpty) {
			setText("!");
		}
		final Dimension prefSize = super.getPreferredSize();
		final float zoom = getNodeView().getMap().getZoom();
		if (zoom != 1F) {
			prefSize.width = (int) (0.99 + prefSize.width * zoom);
			prefSize.height = (int) (0.99 + prefSize.height * zoom);
		}
		if (isCurrentlyPrinting() && MapView.NEED_PREF_SIZE_BUG_FIX) {
			prefSize.width += getNodeView().getMap().getZoomed(10);
		}
		prefSize.width = Math.max(getNodeView().getMap().getZoomed(
		    MainView.MIN_HOR_NODE_SIZE), prefSize.width);
		if (isEmpty) {
			setText("");
		}
		prefSize.width += getNodeView().getMap().getZoomed(12);
		prefSize.height += getNodeView().getMap().getZoomed(4);
		return prefSize;
	}

	abstract Point getRightPoint();

	abstract String getStyle();

	public int getTextWidth() {
		return getWidth() - getIconWidth();
	}

	public int getTextX() {
		int gap = (getWidth() - getPreferredSize().width) / 2;
		final boolean isLeft = getNodeView().isLeft();
		if (isLeft) {
			gap = -gap;
		}
		return gap + (isLeft && !getNodeView().isRoot() ? 0 : getIconWidth());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		if (isPainting) {
			final float zoom = getZoom();
			if (zoom != 1F) {
				return (int) (0.99f + super.getWidth() / zoom);
			}
		}
		return super.getWidth();
	}

	private float getZoom() {
		final float zoom = getNodeView().getMap().getZoom();
		return zoom;
	}

	int getZoomedFoldingSymbolHalfWidth() {
		return getNodeView().getZoomedFoldingSymbolHalfWidth();
	}

	protected boolean isCurrentlyPrinting() {
		return getNodeView().getMap().isCurrentlyPrinting();
	}

	public boolean isInFollowLinkRegion(final double xCoord) {
		final NodeModel model = getNodeView().getModel();
		return model.getLink() != null
		        && (model.isRoot()
		                || !model.getModeController().getMapController()
		                    .hasChildren(model) || isInVerticalRegion(xCoord,
		            1. / 2));
	}

	/**
	 * Determines whether or not the xCoord is in the part p of the node: if
	 * node is on the left: part [1-p,1] if node is on the right: part[ 0,p] of
	 * the total width.
	 */
	public boolean isInVerticalRegion(final double xCoord, final double p) {
		return getNodeView().isLeft() ? xCoord > getSize().width * (1.0 - p)
		        : xCoord < getSize().width * p;
	}

	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		float zoom = getZoom();
		if (zoom != 1F) {
			zoom *= MainView.ZOOM_CORRECTION_FACTOR;
			final AffineTransform transform = g2.getTransform();
			g2.scale(zoom, zoom);
			isPainting = true;
			super.paint(g);
			isPainting = false;
			g2.setTransform(transform);
		}
		else {
			super.paint(g);
		}
	}

	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	public void paintDragOver(final Graphics2D graphics) {
		if (isDraggedOver == NodeView.DRAGGED_OVER_SON) {
			if (getNodeView().isLeft()) {
				graphics.setPaint(new GradientPaint(getWidth() * 3 / 4, 0,
				    getNodeView().getMap().getBackground(), getWidth() / 4, 0,
				    NodeView.dragColor));
				graphics.fillRect(0, 0, getWidth() * 3 / 4, getHeight() - 1);
			}
			else {
				graphics.setPaint(new GradientPaint(getWidth() / 4, 0,
				    getNodeView().getMap().getBackground(), getWidth() * 3 / 4,
				    0, NodeView.dragColor));
				graphics.fillRect(getWidth() / 4, 0, getWidth() - 1,
				    getHeight() - 1);
			}
		}
		if (isDraggedOver == NodeView.DRAGGED_OVER_SIBLING) {
			graphics.setPaint(new GradientPaint(0, getHeight() * 3 / 5,
			    getNodeView().getMap().getBackground(), 0, getHeight() / 5,
			    NodeView.dragColor));
			graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}

	void paintFoldingMark(final Graphics2D g, final Point p) {
		final int zoomedFoldingSymbolHalfWidth = getZoomedFoldingSymbolHalfWidth();
		p.translate(-zoomedFoldingSymbolHalfWidth,
		    -zoomedFoldingSymbolHalfWidth);
		final Color color = g.getColor();
		g.setColor(Color.WHITE);
		g.fillOval(p.x, p.y, zoomedFoldingSymbolHalfWidth * 2,
		    zoomedFoldingSymbolHalfWidth * 2);
		final NodeModel model = getNodeView().getModel();
		final Color edgeColor = model.getModeController().getEdgeController()
		    .getColor(model);
		g.setColor(edgeColor);
		g.drawOval(p.x, p.y, zoomedFoldingSymbolHalfWidth * 2,
		    zoomedFoldingSymbolHalfWidth * 2);
		g.setColor(color);
	}

	public void paintSelected(final Graphics2D graphics) {
		if (getNodeView().useSelectionColors()) {
			paintBackground(graphics, getNodeView().getSelectedColor());
		}
		else {
			final Color backgroundColor = getNodeView().getModel()
			    .getBackgroundColor();
			if (backgroundColor != null) {
				paintBackground(graphics, backgroundColor);
			}
		}
	}

	@Override
	protected void printComponent(final Graphics g) {
		super.paintComponent(g);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke,
	 * java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,
	                                    final int condition,
	                                    final boolean pressed) {
		if (super.processKeyBinding(ks, e, condition, pressed)) {
			return true;
		}
		final FreemindMenuBar freeMindMenuBar = Freeplane.getController()
		    .getViewController().getFreeMindMenuBar();
		return !freeMindMenuBar.isVisible()
		        && freeMindMenuBar.processKeyBinding(ks, e,
		            JComponent.WHEN_IN_FOCUSED_WINDOW, pressed);
	}

	public void setDraggedOver(final int draggedOver) {
		isDraggedOver = draggedOver;
	}

	public void setDraggedOver(final Point p) {
		setDraggedOver((dropAsSibling(p.getX())) ? NodeView.DRAGGED_OVER_SIBLING
		        : NodeView.DRAGGED_OVER_SON);
	}

	/**
	 * @return true if a link is to be displayed and the curser is the hand now.
	 */
	public boolean updateCursor(final double xCoord) {
		final boolean followLink = isInFollowLinkRegion(xCoord);
		final int requiredCursor = followLink ? Cursor.HAND_CURSOR
		        : Cursor.DEFAULT_CURSOR;
		if (getCursor().getType() != requiredCursor) {
			setCursor(requiredCursor != Cursor.DEFAULT_CURSOR ? new Cursor(
			    requiredCursor) : null);
		}
		return followLink;
	}

	public void updateFont(final NodeModel model) {
		final Font font = model.getModeController().getNodeStyleController()
		    .getFont(model);
		setFont(font);
	}

	void updateIcons(final NodeModel node) {
		setHorizontalTextPosition(node.isLeft() ? SwingConstants.LEADING
		        : SwingConstants.TRAILING);
		final MultipleImage iconImages = new MultipleImage(1.0f);
		boolean iconPresent = false;
		/* fc, 06.10.2003: images? */
		final Map stateIcons = node.getStateIcons();
		for (final Iterator i = stateIcons.keySet().iterator(); i.hasNext();) {
			final String key = (String) i.next();
			iconPresent = true;
			final ImageIcon myIcon = (ImageIcon) stateIcons.get(key);
			iconImages.addImage(myIcon);
		}
		final List icons = node.getIcons();
		for (final Iterator i = icons.iterator(); i.hasNext();) {
			final MindIcon myIcon = (MindIcon) i.next();
			iconPresent = true;
			iconImages.addImage(myIcon.getIcon());
		}
		final String link = node.getLink();
		if (link != null) {
			iconPresent = true;
			String iconPath = "images/Link.png";
			if (link.startsWith("#")) {
				iconPath = "images/LinkLocal.png";
			}
			else if (link.startsWith("mailto:")) {
				iconPath = "images/Mail.png";
			}
			else if (Tools.executableByExtension(link)) {
				iconPath = "images/Executable.png";
			}
			final ImageIcon icon = new ImageIcon(Freeplane.getController()
			    .getResourceController().getResource(iconPath));
			iconImages.addImage(icon);
		}
		setIcon((iconPresent ? iconImages : null));
	}

	void updateText(String nodeText, final MapView map) {
		final boolean isHtml = nodeText.startsWith("<html>");
		boolean widthMustBeRestricted = false;
		if (!isHtml) {
			final String[] lines = nodeText.split("\n");
			for (int line = 0; line < lines.length; line++) {
				setText(lines[line]);
				widthMustBeRestricted = getPreferredSize().width > map
				    .getZoomed(map.getMaxNodeWidth())
				        + getIconWidth();
				if (widthMustBeRestricted) {
					break;
				}
			}
			isLong = widthMustBeRestricted || lines.length > 1;
		}
		if (isHtml) {
			if (nodeText.indexOf("<img") >= 0 && nodeText.indexOf("<base ") < 0) {
				nodeText = "<html><base href=\"" + map.getModel().getURL()
				        + "\">" + nodeText.substring(6);
			}
			final String htmlLongNodeHead = Freeplane.getController()
			    .getResourceController().getProperty("html_long_node_head");
			if (htmlLongNodeHead != null && !htmlLongNodeHead.equals("")) {
				if (nodeText.matches("(?ims).*<head>.*")) {
					nodeText = nodeText.replaceFirst("(?ims).*<head>.*",
					    "<head>" + htmlLongNodeHead);
				}
				else {
					nodeText = nodeText.replaceFirst("(?ims)<html>",
					    "<html><head>" + htmlLongNodeHead + "</head>");
				}
			}
			if (nodeText.length() < 30000) {
				setText(nodeText);
				widthMustBeRestricted = getPreferredSize().width > map
				    .getZoomed(map.getMaxNodeWidth())
				        + getIconWidth();
			}
			else {
				widthMustBeRestricted = true;
			}
			if (widthMustBeRestricted) {
				nodeText = nodeText.replaceFirst("(?i)<body>", "<body width=\""
				        + map.getMaxNodeWidth() + "\">");
			}
			setText(nodeText);
		}
		else if (nodeText.startsWith("<table>")) {
			final String[] lines = nodeText.split("\n");
			lines[0] = lines[0].substring(7);
			final int startingLine = lines[0].matches("\\s*") ? 1 : 0;
			String text = "<html><table border=1 style=\"border-color: white\">";
			for (int line = startingLine; line < lines.length; line++) {
				text += "<tr><td style=\"border-color: white;\">"
				        + HtmlTools.toXMLEscapedText(lines[line]).replaceAll(
				            "\t", "<td style=\"border-color: white\">");
			}
			setText(text);
		}
		else if (isLong) {
			String text = HtmlTools.plainToHTML(nodeText);
			if (widthMustBeRestricted) {
				text = text.replaceFirst("(?i)<p>", "<p width=\""
				        + map.getMaxNodeWidth() + "\">");
			}
			setText(text);
		}
		else {
			setText(nodeText);
		}
	}

	void updateTextColor(final NodeModel model) {
		final Color color = model.getModeController().getNodeStyleController()
		    .getColor(model);
		setForeground(color);
	}
}
