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
package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;



/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
final class BitmapViewerComponent extends JComponent{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private BufferedImage image;
	public BitmapViewerComponent(BufferedImage image) {
        this.image = image;
    }
	
	protected Dimension getOriginalSize(){
		return new Dimension(image.getWidth(), image.getHeight());
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		final Image scaledImage;
		if(image.getWidth() != getWidth() || image.getHeight() != getHeight()){
			scaledImage = image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
		}
		else{
			scaledImage = image;
		}
		g.drawImage(scaledImage, 0, 0, null);
    }
}