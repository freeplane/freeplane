package org.freeplane.view.swing.features.filepreview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.border.MatteBorder;

import org.freeplane.core.util.LogUtils;

public class ImageRendererFactory {
	private static final int BORDER_WIDTH = 2;

	public void configureRenderer(IViewerFactory viewerFactory, URI uri, Dimension size, JComponent renderer) {
		renderer.setBorder(new MatteBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, Color.BLACK));
		renderer.setPreferredSize(size);
		renderer.setSize(size);
		Dimension viewerSize = new Dimension(size.width - 2 * BORDER_WIDTH,
		size.height - 2 * BORDER_WIDTH);
		JComponent viewer = createViewer(viewerFactory, uri, viewerSize);
		if(viewer != null) {
			renderer.add(viewer);
			viewer.setLocation(BORDER_WIDTH, BORDER_WIDTH);
		}
	}

	private JComponent createViewer(IViewerFactory viewerFactory, URI uri, Dimension size) {
		JComponent viewer = null;
		try {
			viewer = (JComponent) viewerFactory.createViewer(uri, size);
		}
		catch (final MalformedURLException e) {
			LogUtils.warn(e);
			return null;
		}
		catch (final IOException e) {
			LogUtils.warn(e);
			return null;
		}
		if (viewer == null) {
			return null;
		}
		if (viewer instanceof BitmapViewerComponent) {
			((BitmapViewerComponent) viewer).setHint(Image.SCALE_FAST);
		}
		return viewer;
	}

	public JComponent createRenderer(IViewerFactory viewerFactory, URI absoluteUri, Dimension tooltipSize) {
		JComponent renderer = new JComponent() {};
		configureRenderer(viewerFactory, absoluteUri, tooltipSize, renderer);
		return renderer;
	}

}
