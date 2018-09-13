/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.export.mindmapmode;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.image.BigBufferedImage;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection.NodePosition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author foltin
 * @author kakeda
 * @author rreppel
 */
public class ExportToImage implements IExportEngine {
	private final String imageDescripton;
	private final String imageType;

	public static ExportToImage toPNG(){
		return new ExportToImage("png", "to png");
	}
	
	ExportToImage( final String imageType, final String imageDescripton) {
		this.imageType = imageType;
		this.imageDescripton = imageDescripton;
	}

	public void export(MapModel map, ExportedXmlWriter xmlWriter, File toFile) {
		export(map, toFile);
	}
	public void export(MapModel map, File toFile) {
		export(map, null, null, null, toFile);
	}

	public void export(MapModel map, final Dimension slideSize, NodeModel placedNode, NodePosition placedNodePosition, File toFile) {
		RenderedImage image = null;
		try {
			image = placedNode != null ? new ImageCreator(getImageResolutionDPI()).createBufferedImage(map, slideSize, placedNode, placedNodePosition) : new ImageCreator(getImageResolutionDPI()).createBufferedImage(map);
			if (image != null) {
				exportToImage(image, toFile);
			}
		}
		catch (final OutOfMemoryError ex) {
			UITools.errorMessage(TextUtils.getText("out_of_memory"));
		}
		finally {
			if (image != null)
				BigBufferedImage.dispose(image);
		}
	}

	public boolean exportToImage(final RenderedImage image, File chosenFile) {
		try {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			Iterator<ImageWriter> imageWritersByFormatName = ImageIO.getImageWritersByFormatName(imageType);
			for(;;){
				ImageWriter writer = imageWritersByFormatName.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
				IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
				if ((metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) && imageWritersByFormatName.hasNext()) {
					continue;
                }
				addDpiToMetadata(metadata);
				final FileOutputStream outFile = new FileOutputStream(chosenFile);
				final ImageOutputStream stream = ImageIO.createImageOutputStream(outFile);
				try {
					writer.setOutput(ImageIO.createImageOutputStream(outFile));
					writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
					break;
				} finally {
					stream.close();
					outFile.close();
				}
			}
		}
		catch (final IOException e1) {
			LogUtils.warn(e1);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
		finally{
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
		return true;
	}

	private void addDpiToMetadata(IIOMetadata metadata) throws IIOInvalidTreeException {
	    int dpi = getImageResolutionDPI();
	    double dotsPerMilli = 1.0 * dpi / 10 / 2.54;
	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));
	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));
	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);
	    root.appendChild(dim);
	    metadata.mergeTree("javax_imageio_1.0", root);
    }

	public FileFilter getFileFilter() {
		return new ExampleFileFilter(imageType, imageDescripton);
    }

	private int getImageResolutionDPI() {
	    return ResourceController.getResourceController().getIntProperty("exported_image_resolution_dpi", 300);
    }
}
