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
package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JComponent;

import org.freeplane.core.util.TextUtils;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapViewerFactory implements IViewerFactory {
	@Override
	public boolean accept(final URI uri) {
		final Iterator<ImageReader> readers = getImageReaders(uri);
		return readers.hasNext();
	}

	private Iterator<ImageReader> getImageReaders(final URI uri) {
        String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getRawPath();
		final int suffixPos = path.lastIndexOf('.') + 1;
		if (suffixPos == 0) {
			final List<ImageReader> empty = Collections.emptyList();
			return empty.iterator();
		}
		final String suffix = path.substring(suffixPos);
		final Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(suffix);
		return readers;
	}

	@Override
	public ScalableComponent createViewer(final ExternalResource resource,
			final URI uri, int maximumWidth, float viewZoom) throws MalformedURLException,
	        IOException {
		final BitmapViewerComponent bitmapViewerComponent = new BitmapViewerComponent(uri);
		final Dimension originalSize = bitmapViewerComponent.getOriginalSize();
		float resourceZoom = resource.getZoom();
		if(resourceZoom == -1){
			resourceZoom = resource.setZoom(originalSize.width, maximumWidth);
		}
		final ViewerLayoutManager viewerLayoutManager = new ViewerLayoutManager(viewZoom, resource, originalSize);
		((JComponent) bitmapViewerComponent).setLayout(viewerLayoutManager);

		Dimension zoomedSize = viewerLayoutManager.calculatePreferredSize();
		bitmapViewerComponent.setFinalViewerSize(zoomedSize );
		return bitmapViewerComponent;
	}

	@Override
	public ScalableComponent createViewer(final URI uri,
			final Dimension preferredSize) throws MalformedURLException,
	        IOException {
		final BitmapViewerComponent bitmapViewerComponent = new BitmapViewerComponent(uri);
		bitmapViewerComponent.setFinalViewerSize(preferredSize);
		return bitmapViewerComponent;
	}

	@Override
	public String getDescription() {
		return TextUtils.getText("bitmaps");
	}

	@Override
	public ScalableComponent createViewer(URI uri, float zoom)
			throws MalformedURLException, IOException {
		final BitmapViewerComponent bitmapViewerComponent = new BitmapViewerComponent(uri);
		bitmapViewerComponent.setFinalViewerSize(zoom);
		return bitmapViewerComponent;
	}

	/** extracted to {@link ScalableComponent} but still used in add-ons. */
	@Deprecated
	public Dimension getOriginalSize(final JComponent viewer) {
		return ((BitmapViewerComponent) viewer).getOriginalSize();
	}
}
