package org.freeplane.view.swing.features.filepreview;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;

public class ImageAdder {
    private final Image image;
	private MMapController mapController;
	private File mindmapFile;
	private File imageFile;
	public static final String IMAGE_FORMAT = "png";

	public ImageAdder(Image image, final MMapController mapController, final File mindmapFile, final File imageFile) {
		super();
		this.image = image;
		this.mapController = mapController;
		this.mindmapFile = mindmapFile;
		this.imageFile = imageFile;
	}

	public void attachImageToNode(final NodeModel node){
		final ImageObserverCallback imageObserver = new ImageObserverCallback();
		final int imageWidth = image.getWidth(imageObserver);
		final int imageHeight = image.getHeight(imageObserver);
		if(imageWidth != -1 && imageHeight != -1)
			attachImageToNode(node, imageObserver, imageWidth, imageHeight);
		else
			imageObserver.onSizeAvailable(new Runnable() {
				@Override
				public void run() {
					attachImageToNode(node, imageObserver, imageObserver.getImageWidth(), imageObserver.getImageHeight());
				}
			});
	}

	private void attachImageToNode(final NodeModel node, final ImageObserverCallback imageObserver, final int imageWidth,
			final int imageHeight){
		final BufferedImage fixedImg = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D fig = fixedImg.createGraphics();
		if (fig.drawImage(image, 0, 0, imageObserver))
			attachImageToNode(node, fixedImg, fig);
		else {
			imageObserver.onImageRendered(new Runnable() {
				@Override
				public void run() {
					attachImageToNode(node, fixedImg, fig);
				}
			});
		}
	}

	private void attachImageToNode(final NodeModel node, BufferedImage fixedImg, Graphics2D fig) {
		fig.dispose();
		fixedImg.flush();
		try {
			ImageIO.write(fixedImg, IMAGE_FORMAT, imageFile);
			final URI uri = LinkController.toLinkTypeDependantURI(mindmapFile, imageFile);
			final ExternalResource extension = new ExternalResource(uri);
			mapController.getModeController().getExtension(ViewerController.class).add(node, extension);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
