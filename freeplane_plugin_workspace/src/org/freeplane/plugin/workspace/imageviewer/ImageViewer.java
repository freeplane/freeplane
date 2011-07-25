package org.freeplane.plugin.workspace.imageviewer;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.freeplane.features.mode.Controller;

public class ImageViewer extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static int MIN_WIDTH = 100;
	private final static int MIN_HEIGHT = 100;
	
	private Image image;
	
	public ImageViewer(Image image, boolean scaled, String title) {
		this.image = image;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(getStartingDimensions());
		
		ImageComponent imageComp = new ImageComponent(image, scaled);
		if (scaled)
			this.getContentPane().add(imageComp);
		else
			this.getContentPane().add(new JScrollPane(imageComp));
		setVisible(true);
		
		
	}
	
	private Dimension getStartingDimensions() {
		//TODO: returns -1
		int imageWidth = image.getWidth(this);
		int imageHeight = image.getHeight(this);
		
		int width = Math.max(MIN_WIDTH, imageWidth);
		width = Math.min(getMaxWidth(), width);
		double scalex = width/imageWidth;
		scalex = (scalex < 0 ? 1 : scalex);
		
		
		int height = Math.max(MIN_HEIGHT, imageHeight);
		height = Math.min(getMaxHeight(), height);
		double scaley = height/imageHeight;
		scaley = (scaley < 0 ? 1 : scaley);
		
		double scale;
		if (scalex > 0 && scaley > 0) {
			scale = Math.abs(Math.max(scalex, scaley));
		}
		else {
			scale = Math.abs(Math.min(scalex, scaley));
		}
		
		System.out.println(scale + "    " + new Dimension((int) scale * width, (int) scale * height)); 
		return new Dimension((int) scale * width, (int) scale * height);
		
	}
	
	private int getMaxWidth() {
		int maxWidth = Controller.getCurrentController().getViewController().getJFrame().getContentPane().getWidth();
		System.out.println("MAXWIDTH: "+maxWidth);
		return maxWidth;
	}

	private int getMaxHeight() {
		int maxHeight = Controller.getCurrentController().getViewController().getJFrame().getContentPane().getHeight();
		System.out.println("MAXHEIGHT: "+maxHeight);
		return maxHeight;
	}
	
	
}
