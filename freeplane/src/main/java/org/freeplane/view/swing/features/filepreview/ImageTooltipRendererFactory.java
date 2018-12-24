package org.freeplane.view.swing.features.filepreview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.border.MatteBorder;

import org.freeplane.core.util.LogUtils;

public class ImageTooltipRendererFactory {
	private static final int BORDER_WIDTH = 2;
	private final JComponent renderer;

	public ImageTooltipRendererFactory(IViewerFactory viewerFactory, URI uri, Dimension size) {
 		this.renderer = new JComponent() {};
 		renderer.setOpaque(false);
		renderer.setBorder(new MatteBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, Color.BLACK));

		renderer.setPreferredSize(size);
		renderer.setSize(size);
		Dimension viewerSize = new Dimension(size.width - 2 * BORDER_WIDTH,
		size.height - 2 * BORDER_WIDTH);
		renderer.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(renderer.isDisplayable()) {
					JComponent viewer = createViewer(viewerFactory, uri, viewerSize);
					renderer.add(viewer);
					viewer.setLocation(BORDER_WIDTH, BORDER_WIDTH);
					renderer.removeHierarchyListener(this);
				}
			}
		});
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

	public JComponent getTooltipRenderer() {
		return renderer;
	}
}
