/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  Created on 05.11.2005
 */
package com.inet.jortho;

import java.awt.*;

import javax.swing.text.*;

/**
 * @author Volker Berlin
 */
class RedZigZagPainter extends DefaultHighlighter.DefaultHighlightPainter {
	
	private static final java.awt.BasicStroke STROKE1 = new java.awt.BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{1,3}, 0);
    private static final java.awt.BasicStroke STROKE2 = new java.awt.BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{1,1}, 1);
    private static final java.awt.BasicStroke STROKE3 = new java.awt.BasicStroke(0.01F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{1,3}, 2);


    public RedZigZagPainter(){
        super(Color.red);
    }
	

    /**
     * {@inheritDoc}
     */
	@Override
    public Shape paintLayer(Graphics g, int i, int j, Shape shape, JTextComponent jtext, View view){
	    if(jtext.isEditable()){
            g.setColor(Color.red);
            try{
                Shape sh = view.modelToView(i, Position.Bias.Forward, j, Position.Bias.Backward, shape);
                Rectangle rect = (sh instanceof Rectangle) ? (Rectangle)sh : sh.getBounds();
                drawZigZagLine(g, rect);
                return rect;
            }catch(BadLocationException badlocationexception){
                return null;
            }
	    }
	    return null;
    }
	

    private void drawZigZagLine(Graphics g, Rectangle rect){
        int x1 = rect.x;
        int x2 = x1 + rect.width - 1;
        int y = rect.y + rect.height - 1;
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(STROKE1);
        g2.drawLine(x1, y, x2, y);
        y--;
        g2.setStroke(STROKE2);
        g2.drawLine(x1, y, x2, y);
        y--;
        g2.setStroke(STROKE3);
        g2.drawLine(x1, y, x2, y);
    }

}
