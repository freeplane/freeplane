package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.util.LogUtils;
import org.freeplane.view.swing.features.filepreview.ViewerController.FactoryFileFilter;

/* ImagePreview.java by FileChooserDemo2.java. */
public class ImagePreview extends BitmapImagePreview {
	private static final long serialVersionUID = 1L;

	public ImagePreview(final JFileChooser fc) {
		super(fc);
	}

	@Override
	protected void updateView(final File file) {
		final Object fileFilter = fc.getClientProperty(FactoryFileFilter.class);
		if(! (fileFilter instanceof FactoryFileFilter))
			return;
		final FactoryFileFilter factoryFileFilter = (FactoryFileFilter) fileFilter;
		final Dimension size = getSize();
		size.width -= 2 * BORDER_WIDTH;
		size.height -= 2 * BORDER_WIDTH;
		JComponent viewer;
		try {
			viewer = (JComponent) factoryFileFilter.getFactory().createViewer(file.getAbsoluteFile().toURI(), size);
		}
		catch (final MalformedURLException e) {
			LogUtils.warn(e);
			return;
		}
		catch (final IOException e) {
			LogUtils.warn(e);
			return;
		}
		if (viewer == null) {
			return;
		}
		viewer.setLocation(BORDER_WIDTH, BORDER_WIDTH);
		if (viewer instanceof BitmapViewerComponent) {
			((BitmapViewerComponent) viewer).setHint(Image.SCALE_FAST);
		}
		add(viewer);
		viewer.revalidate();
		viewer.repaint();
	}
}
