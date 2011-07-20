package org.freeplane.plugin.workspace.imageviewer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.freeplane.features.mode.Controller;

public class ImageViewer extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static int MIN_WIDTH = 50;
	private final static int MIN_HEIGHT = 50;
	
	private Image image;
	
	public ImageViewer(Image image, boolean scaled, String title) {
		this.image = image;
		this.setSize(getStartingDimensions());
		
		ImageComponent imageComp = new ImageComponent(image, scaled);
		if (scaled)
			this.getContentPane().add(imageComp);
		else
			this.getContentPane().add(new JScrollPane(imageComp));
	}
	
	private Dimension getStartingDimensions() {
		//TODO: returns -1
		int imageWidth = image.getWidth(this);
		int imageHeight = image.getHeight(this);
		
		System.out.println("imageWidth: "+imageWidth);
		int width = Math.max(MIN_WIDTH, imageWidth);
		width = Math.min(getMaxWidth(), width);
		double scalex = width/imageWidth;
		System.out.println("width: "+width);
		
		System.out.println("imageHeight: "+imageHeight);
		int height = Math.max(MIN_WIDTH, imageWidth);
		height = Math.min(getMaxHeight(), height);
		double scaley = height/imageWidth;
		System.out.println("heigth: "+height);
		
		double scale;
		if (scalex > 0 && scaley > 0) {
			scale = Math.max(scalex, scaley);
		}
		else {
			scale = Math.min(scalex, scaley);
		}
		
		return new Dimension((int) scale * imageWidth, (int) scale * imageHeight);
		
	}
	
	private int getMaxWidth() {
		int maxWidth = Controller.getCurrentController().getViewController().getJFrame().getWidth();
		System.out.println("MAXWIDTH: "+maxWidth);
		return maxWidth;
	}

	private int getMaxHeight() {
		int maxHeight = Controller.getCurrentController().getViewController().getJFrame().getHeight();
		System.out.println("MAXHEIGHT: "+maxHeight);
		return maxHeight;
	}
	
	
}
