package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;

@SuppressWarnings("serial")
public class ZoomableLabel extends JLabel {

	protected static final Graphics2D fmg;
	static {
		fmg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		fmg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	protected int getIconWidth() {
		final Icon icon = getIcon();
		if (icon == null) {
			return 0;
		}
		return getMap().getZoomed(icon.getIconWidth());
	}

	public NodeView getNodeView() {
		return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		if (isPreferredSizeSet()) {
			return preferredSize;
		}
		final float zoom = getZoom();
		final int d = 2 * (int) (Math.floor(zoom));
		preferredSize.width += d;
		preferredSize.height += d;
		return preferredSize;
	}

	protected float getZoom() {
		final float zoom = getMap().getZoom();
		return zoom;
	}

	protected MapView getMap() {
		return getNodeView().getMap();
	}

	@Override
	public void paint(final Graphics g) {
		switch (getMap().getPaintingMode()) {
			case CLOUDS:
				return;
		}
		super.paint(g);
	}

	protected void updateText(String nodeText) {
		final MapView map = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		if (map == null || nodeText == null) {
			return;
		}
		final boolean isHtml = nodeText.startsWith("<html>");
		boolean widthMustBeRestricted = false;
		boolean isLong = false;
		int iconWidth = getIconWidth();
		if (iconWidth != 0) {
			iconWidth += map.getZoomed(getIconTextGap());
		}
		if (!isHtml) {
			final String[] lines = nodeText.split("\n");
			for (int line = 0; line < lines.length; line++) {
				setText(lines[line]);
				widthMustBeRestricted = getPreferredSize().width > map.getZoomed(map.getMaxNodeWidth()) + iconWidth;
				if (widthMustBeRestricted) {
					break;
				}
			}
			isLong = widthMustBeRestricted || lines.length > 1;
		}
		if (isHtml) {
			if (nodeText.indexOf("<img") >= 0 && nodeText.indexOf("<base ") < 0) {
				nodeText = "<html><base href=\"" + map.getModel().getURL() + "\">" + nodeText.substring(6);
			}
			final String htmlLongNodeHead = ResourceController.getResourceController().getProperty(
			    "html_long_node_head");
			if (htmlLongNodeHead != null && !htmlLongNodeHead.equals("")) {
				if (nodeText.matches("(?ims).*<head>.*")) {
					nodeText = nodeText.replaceFirst("(?ims).*<head>.*", "<head>" + htmlLongNodeHead);
				}
				else {
					nodeText = nodeText.replaceFirst("(?ims)<html>", "<html><head>" + htmlLongNodeHead + "</head>");
				}
			}
			if (nodeText.length() < 30000) {
				setText(nodeText);
				widthMustBeRestricted = getPreferredSize().width > map.getZoomed(map.getMaxNodeWidth()) + iconWidth;
			}
			else {
				widthMustBeRestricted = true;
			}
			if (widthMustBeRestricted) {
				nodeText = nodeText.replaceFirst("(?i)<body>", "<body width=\"" + map.getMaxNodeWidth() + "\">");
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
				        + HtmlUtils.toXMLEscapedText(lines[line])
				            .replaceAll("\t", "<td style=\"border-color: white\">");
			}
			setText(text);
		}
		else if (isLong) {
			String text = HtmlUtils.plainToHTML(nodeText);
			if (widthMustBeRestricted) {
				text = text.replaceFirst("(?i)<p>", "<p width=\"" + map.getMaxNodeWidth() + "\">");
			}
			setText(text);
		}
		else {
			setText(nodeText);
		}
	}

	public ZoomableLabel() {
		setUI(ZoomableLabelUI.createUI(this));
	}

	@Override
	public FontMetrics getFontMetrics(final Font font) {
		if (!useFractionalMetrics()) {
			return super.getFontMetrics(font);
		}
		fmg.setFont(font);
		final FontMetrics fontMetrics = fmg.getFontMetrics();
		return fontMetrics;
	}

	protected boolean useFractionalMetrics() {
		final MapView map = getMap();
		if (map.isPrinting()) {
			return true;
		}
		final float zoom = map.getZoom();
		return 1f != zoom;
	}

	protected FontMetrics getFontMetrics() {
		if (!useFractionalMetrics()) {
			return super.getFontMetrics(getFont());
		}
		fmg.setFont(getFont());
		final FontMetrics fontMetrics = fmg.getFontMetrics();
		return fontMetrics;
	}

}