package org.freeplane.core.resources.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.JButton;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;

import com.formdev.flatlaf.util.HiDPIUtils;

public class JColorButton extends JButton {

	private static final long serialVersionUID = 1L;
	private static final int COLOR_ICON_BORDER_SIZE = (int) (4 * UITools.FONT_SCALE_FACTOR);
	private static final Paint TEXTURE = createTexture();
	private static TexturePaint createTexture() {
		int cellSize = (int) (6 * UITools.FONT_SCALE_FACTOR);
		BufferedImage image = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, cellSize/2, cellSize/2);
		graphics.fillRect(cellSize/2, cellSize/2, cellSize/2, cellSize/2);
		return new TexturePaint(image,  new Rectangle(cellSize, cellSize));
	}
	private Color color;
	private String text;
	int textWidth;
	int textHeight;

	public JColorButton(Action a) {
        super(a);
        setText(" ");
        this.color = null;
        this.text = " ";
    }

    public JColorButton() {
		super(" ");
		this.color = null;
		this.text = " ";
	}
	public void setColor(Color color) {
		this.color = color;
	    if (color != null) {
	        this.text = ColorUtils.colorToRGBPercentString(color);
	    }
	    else {
	    	this.text = " ";
	    }

		this.textWidth = this.textHeight = 0;
		revalidate();
		repaint();
	}



	@Override
    public String getText() {
	    return text;
    }

    @Override
	protected void paintComponent(Graphics g) {
        String textBackup = text;
        try {
            text = null;
            super.paintComponent(g);
        }
        finally {
            text = textBackup;
        }
		paintColor(g);
	}
	private void paintColor(Graphics g) {
	    if (color != null) {
	        Graphics2D g2 = (Graphics2D) g;
	    	if(color.getAlpha() < 255){
	    		g2.setPaint(TEXTURE);
	    		g.fillRect(COLOR_ICON_BORDER_SIZE ,
	    				COLOR_ICON_BORDER_SIZE,
	    				getWidth() - COLOR_ICON_BORDER_SIZE*2,
	    				getHeight() - COLOR_ICON_BORDER_SIZE*2);
	    	}
    		g.setColor(color);
    		g.setFont(getFont());
    		g.fillRect(COLOR_ICON_BORDER_SIZE ,
    				COLOR_ICON_BORDER_SIZE,
    				getWidth() - COLOR_ICON_BORDER_SIZE*2,
    				getHeight() - COLOR_ICON_BORDER_SIZE*2);
	        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    	calculateTextSize();
			int xText = (getWidth() - textWidth) / 2;
			int yText = (getHeight() + 3 * textHeight/4) / 2;
	        /* Draw the Text */
			final Color backgroundColor = ColorUtils.makeNonTransparent(color);
			if(color.getAlpha() < 255){
	    		g.setColor(backgroundColor);
	    		g.fillRect(xText - COLOR_ICON_BORDER_SIZE/2,  (getHeight() - textHeight - COLOR_ICON_BORDER_SIZE)/2,
	    				textWidth + COLOR_ICON_BORDER_SIZE, textHeight +  COLOR_ICON_BORDER_SIZE);
			}
			final Color textColor = getModel().isEnabled() ? UITools.getTextColorForBackground(backgroundColor) : UITools.getDisabledTextColorForBackground(backgroundColor);;
			g.setColor(textColor);
			HiDPIUtils.drawStringUnderlineCharAtWithYCorrection( this, g2, text, -1, xText, yText);
	    }
	}
	private void calculateTextSize() {
		if(textWidth == 0) {
	        Graphics2D g = (Graphics2D) getGraphics();
	        if(g != null) {
	        	Rectangle2D textBounds = g.getFont().getStringBounds(text, g.getFontRenderContext());
	        	textWidth = (int) (textBounds.getWidth());
	        	textHeight = (int) (textBounds.getHeight());
	        }
		}
	}
}