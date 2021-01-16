package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.security.AccessControlException;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;

@SuppressWarnings("serial")
public class ZoomableLabel extends JLabel {
	private static final String TEXT_RENDERING_ICON = "TextRenderingIcon";

	protected static final Graphics2D fmg;
	static {
		fmg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		fmg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	private int minimumWidth;
	private int maximumWidth;

	public int getIconWidth() {
		final Icon icon = getIcon();
		if (icon == null) {
			return 0;
		}
		return getMap().getZoomed(icon.getIconWidth());
	}

	public NodeView getNodeView() {
		return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
	}


	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			Dimension preferredSize = super.getPreferredSize();
			return preferredSize;
		}
		return ((ZoomableLabelUI)getUI()).getPreferredSize(this);
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
			default:
				break;
		}
		super.paint(g);
	}

	protected void updateText(String text) {
		try{
			updateTextUnsafe(text);
		}
        catch (Exception e1) {
        	if(e1 instanceof AccessControlException)
        		LogUtils.warn(e1.getMessage());
        	else
        		LogUtils.severe(e1);
	        final String localizedMessage = e1.getLocalizedMessage();
	        if(text.length() > 603)
	        	text = text.substring(0, 600) + "...";
			try{
				updateTextUnsafe(localizedMessage + '\n' + text);
			}
			catch (Exception e2){
			}
        }
	}

	private void updateTextUnsafe(String nodeText) throws Exception{
	    final NodeView node = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
		final MapView map = node.getMap();
		if (map == null || nodeText == null) {
			return;
		}
		final boolean isHtml = nodeText.startsWith("<html>");
		boolean widthMustBeRestricted = ! areInsetsFixed();
		boolean isLong = false;
		if (!isHtml) {
			final String[] lines = nodeText.split("\n");
			for (int line = 0; line < lines.length; line++) {
				if (widthMustBeRestricted)
					break;
				setText(lines[line]);
				final int oldMaximumWidth = getMaximumWidth();
				try{
					final ModeController modeController = map.getModeController();
					final NodeStyleController nsc = NodeStyleController.getController(modeController);
					final double maxNodeWidth = nsc.getMaxWidth(node.getModel()).toBaseUnits();
					setMaximumWidth(Integer.MAX_VALUE);
					widthMustBeRestricted = getPreferredSize().width > map.getZoomed(maxNodeWidth);
				}
				finally{
					setMaximumWidth(oldMaximumWidth);
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
			setText(text);
		}
		else {
			setText(nodeText);
		}
    }
	
	protected boolean areInsetsFixed() {
		return true;
	}

	public ZoomableLabel() {
		setUI(ZoomableLabelUI.createUI(this));
	}

	@Override
    public void updateUI() {
    }

	@Override
	public FontMetrics getFontMetrics(final Font font) {
		try {
			if (!useFractionalMetrics()) {
				return super.getFontMetrics(font);
			}
			fmg.setFont(font);
			final FontMetrics fontMetrics = fmg.getFontMetrics();
			return fontMetrics;
		} catch (Exception e) {
			return super.getFontMetrics(font);
		}
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
	
	public String getLink(Point p){
		View view = (View)getClientProperty(BasicHTML.propertyKey);
		if(view == null)
			return null;
		Rectangle textR = ((ZoomableLabelUI)getUI()).getTextR(this);
		if(textR == null)
			return null;
		if(!textR.contains(p))
			return null;
		int x = (int) (p.x / getZoom());
		int y = (int) (p.y / getZoom());
		final int pos = view.viewToModel(x, y, textR);
		final HTMLDocument document = (HTMLDocument) view.getDocument();
		final String linkURL = HtmlUtils.getURLOfExistingLink(document, pos);
		return linkURL;
	}

	public Insets getZoomedInsets() {
		Insets unzoomedInsets = getInsets();
		float zoom = getZoom();
		Insets zoomedInsets = new Insets((int) (unzoomedInsets.top * zoom), 
				(int) (unzoomedInsets.left * zoom), 
				(int) (unzoomedInsets.bottom * zoom), 
				(int) (unzoomedInsets.right * zoom));
		return zoomedInsets;
	}

	public int getMinimumWidth() {
		return minimumWidth;
	}

	public int getMaximumWidth() {
		return maximumWidth;
	}

	public void setMaximumWidth(int maximumWidth) {
		this.maximumWidth = maximumWidth;
	}
	public void setMinimumWidth(int minimumWidth) {
		this.minimumWidth = minimumWidth;
	}

	protected int limitWidth(int width) {
		if(width < getMinimumWidth())
			return getMinimumWidth();
		else if(width > getMaximumWidth())
			return getMaximumWidth();
		else
			return width;
	}

	protected double limitWidth(double width) {
		if(width < getMinimumWidth())
			return getMinimumWidth();
		else if(width > getMaximumWidth())
			return getMaximumWidth();
		else
			return width;
	}

	public Icon getTextRenderingIcon() {
		return (Icon) getClientProperty(ZoomableLabel.TEXT_RENDERING_ICON);
	}
	
	public void setTextRenderingIcon(Icon icon) {
		putClientProperty(TEXT_RENDERING_ICON, icon);
	}



}