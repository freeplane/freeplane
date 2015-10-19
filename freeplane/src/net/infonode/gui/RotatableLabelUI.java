/*
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 * 
 * Fix by Dimitry Polivaev (2015) : RotatableLabelUI paint uses wrong FontMetrics in paint
 * => call BasicLabelUI for direction == RIGHT
 */


// $Id: RotatableLabelUI.java,v 1.11 2005/12/04 13:46:04 jesper Exp $
package net.infonode.gui;

import net.infonode.util.Direction;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class RotatableLabelUI extends BasicLabelUI {
  // Optimization
  private static Rectangle paintIconR = new Rectangle();
  private static Rectangle paintTextR = new Rectangle();
  private static Rectangle paintViewR = new Rectangle();

  private Direction direction;
  private boolean mirror;

  public RotatableLabelUI(Direction direction) {
    this(direction, false);
  }

  public RotatableLabelUI(Direction direction, boolean mirror) {
    this.direction = direction;
    this.mirror = mirror;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public boolean isMirror() {
    return mirror;
  }

  public void setMirror(boolean mirror) {
    this.mirror = mirror;
  }

  public void paint(Graphics g, JComponent c) {
	  if (direction == Direction.RIGHT && ! mirror){
		  super.paint(g, c);
		  return;
	  }
    JLabel label = (JLabel) c;
    String text = label.getText();
    Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

    if (icon == null && text == null)
      return;

    FontMetrics fm = g.getFontMetrics();
    Insets insets = c.getInsets();

    paintViewR.x = insets.left;
    paintViewR.y = insets.top;

    if (direction.isHorizontal()) {
      paintViewR.height = c.getHeight() - (insets.top + insets.bottom);
      paintViewR.width = c.getWidth() - (insets.left + insets.right);
    }
    else {
      paintViewR.height = c.getWidth() - (insets.top + insets.bottom);
      paintViewR.width = c.getHeight() - (insets.left + insets.right);
    }

    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

    String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

    Graphics2D g2 = (Graphics2D) g;
    AffineTransform tr = g2.getTransform();

    int m = mirror ? -1 : 1;
    g2.transform(direction == Direction.RIGHT ? new AffineTransform(1, 0, 0, m, 0, mirror ? c.getHeight() : 0) :
    	direction == Direction.DOWN ? new AffineTransform(0, 1, -m, 0, mirror ? 0 : c.getWidth(), 0) :
                 direction == Direction.LEFT ? new AffineTransform(-1,
                                                                   0,
                                                                   0,
                                                                   -m,
                                                                   c.getWidth(),
                                                                   mirror ? 0 : c.getHeight()) :
                 new AffineTransform(0, -1, m, 0, mirror ? c.getWidth() : 0, c.getHeight()));

    if (icon != null) {
      icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
    }

    if (text != null) {
      int textX = paintTextR.x;
      int textY = paintTextR.y + fm.getAscent();

      if (label.isEnabled()) {
        paintEnabledText(label, g, clippedText, textX, textY);
      }
      else {
        paintDisabledText(label, g, clippedText, textX, textY);
      }
    }

    g2.setTransform(tr);
  }
}
