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
package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.JComponent;



/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapViewerComponent extends JComponent{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private int hint;
	protected int getHint() {
    	return hint;
    }

	public void setHint(int hint) {
    	this.hint = hint;
    }

	public BitmapViewerComponent(URI uri) throws MalformedURLException, IOException {
		this.image = ImageIO.read(uri.toURL());
		hint = Image.SCALE_SMOOTH;
    }
	
	public Dimension getOriginalSize(){
		return new Dimension(image.getWidth(), image.getHeight());
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		if(image == null){
			super.paintComponent(g);
			return;
		}
		final Image scaledImage;
		final int x;
		final int y;
		final int width = getWidth();
		final int height = getHeight();
		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();
		if(width == 0 || height == 0 || imageWidth == 0 || imageHeight == 0){
			return;
		}
		if(imageWidth != width || imageHeight != height){
			double kComponent = (double)height / (double)width;
			double kImage = (double)imageHeight / (double)imageWidth;
			if(kComponent >= kImage){
				final int calcHeight = (int)(width * kImage);
				scaledImage = image.getScaledInstance(width, calcHeight, hint);
				x = 0;
				y = (height - calcHeight) / 2;
			}
			else{
				final int calcWidth = (int)(height / kImage);
				scaledImage = image.getScaledInstance(calcWidth, height, hint);
				x = (width - calcWidth) / 2;
				y = 0;
			}
		}
		else{
			scaledImage = image;
			x = 0;
			y = 0;
		}
		g.drawImage(scaledImage, x, y, null);
    }
}