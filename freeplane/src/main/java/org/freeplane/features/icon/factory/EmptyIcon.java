package org.freeplane.features.icon.factory;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

class EmptyIcon implements Icon {

	  private int width;
	  private int height;
	  
	  public EmptyIcon() {
	    this(0, 0);
	  }
	  
	  public EmptyIcon(int width, int height) {
	    this.width = width;
	    this.height = height;
	  }

	  public int getIconHeight() {
	    return height;
	  }

	  public int getIconWidth() {
	    return width;
	  }

	  public void paintIcon(Component c, Graphics g, int x, int y) {
	  }

	}