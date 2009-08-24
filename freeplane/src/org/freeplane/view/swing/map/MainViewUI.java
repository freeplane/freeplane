package org.freeplane.view.swing.map;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
/**
 * @author Dimitry Polivaev
 * 23.08.2009
 */
class MainViewUI extends BasicLabelUI {
	private boolean isPainting = false;
	static MainViewUI labelUI = new MainViewUI();
	@Override
    public Dimension getPreferredSize(JComponent c) {
	    final Dimension preferredSize = super.getPreferredSize(c);
	    if(preferredSize.height == 0){
	    	preferredSize.height = ((MainView)c).getFontMetrics().getHeight();
	    }
	    
	    if(preferredSize.width <= 4){
	    	preferredSize.width = 4;
	    }
	    
	    preferredSize.width += 4;
	    final float zoom = ((MainView) c).getZoom();
	    if(zoom != 1f){
	    	preferredSize.width = (int)(Math.ceil(zoom * preferredSize.width));
	    	preferredSize.height = (int)(Math.ceil(zoom * preferredSize.height));
	    }
		return preferredSize;
    }
	  public static ComponentUI createUI(JComponent c) {
		  return labelUI;
	    }

	@Override
    protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR,
                              Rectangle iconR, Rectangle textR) {
        final MainView mainView = (MainView) label;
		final float zoom = mainView.getZoom();
		if(isPainting){
	        Insets insets = mainView.getInsets();
	        int width = (int)(mainView.getWidth() / zoom);
			viewR.width = width - (insets.left + insets.right);
	        int height = (int)(mainView.getHeight() / zoom);
			viewR.height = height - (insets.top + insets.bottom);
		}
	    super.layoutCL(mainView, mainView.getFontMetrics(), text, icon, viewR, iconR, textR);
	    return text;
    }

	@Override
	public void paint(Graphics g, JComponent label) {
		MainView mainView = (MainView) label;
		if(! mainView.useFractionalMetrics()){
			super.paint(g, label);
			return;
		}
		final Graphics2D g2 = (Graphics2D) g;
		final Object oldRenderingHintFM = g2.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
		final Object newRenderingHintFM = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		if (oldRenderingHintFM != newRenderingHintFM) {
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, newRenderingHintFM);
		}
		final AffineTransform transform = g2.getTransform();
		final float zoom = mainView.getZoom();
		g2.scale(zoom, zoom);
		try{
			isPainting = true;
			super.paint(g, label);
		}
		finally{
			isPainting = false;
		}
		g2.setTransform(transform);
		if (oldRenderingHintFM != newRenderingHintFM) {
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, oldRenderingHintFM != null ? oldRenderingHintFM : RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		}
	}
}
