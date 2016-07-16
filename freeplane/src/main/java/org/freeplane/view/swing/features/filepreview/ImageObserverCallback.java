package org.freeplane.view.swing.features.filepreview;

import java.awt.Image;
import java.awt.image.ImageObserver;

class ImageObserverCallback implements ImageObserver {
	private Runnable onSizeAvailable = null;
	private Runnable onImageAvailable = null;
	private int imageWidth = -1;
	private int imageHeight = -1;
	
	void onSizeAvailable (Runnable method){
		this.onSizeAvailable = method;
	}
	
	void onImageRendered (Runnable method){
		this.onImageAvailable = method;
	}
	
	int getImageWidth(){
		return imageWidth;
	}
	int getImageHeight(){
		return imageHeight;
	}
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if(imageWidth == -1 || imageHeight == -1) {
			if((infoflags & ImageObserver.WIDTH) != 0)
				imageWidth = width; 

			if((infoflags & ImageObserver.HEIGHT) != 0)
				imageHeight = height; 
			if(imageWidth != -1 && imageHeight != -1 && onSizeAvailable != null) {
				onSizeAvailable.run();
				onSizeAvailable = null;
			}
			return true;
		}
		if((infoflags & ImageObserver.ALLBITS) != 0){
			if(onImageAvailable != null){
				onImageAvailable.run();
				onImageAvailable = null;
			}
			return false;
		}
		return true;
	}
}