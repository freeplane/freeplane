package org.freeplane.view.swing.addins.filepreview;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.ui.components.BitmapViewerComponent;
import org.freeplane.core.util.LogTool;
import org.freeplane.view.swing.addins.filepreview.ViewerController.FactoryFileFilter;

/* ImagePreview.java by FileChooserDemo2.java. */
public class ImagePreview extends BitmapImagePreview {
	public ImagePreview(final JFileChooser fc) {
		super(fc);
	}

	@Override
	protected void updateView(final File file) {
		final FactoryFileFilter filter = (FactoryFileFilter) fc.getFileFilter();
		final Dimension size = getSize();
		size.width -= 2 * BORDER_WIDTH;
		size.height -= 2 * BORDER_WIDTH;
		JComponent viewer;
		try {
			viewer = filter.getFactory().createViewer(file.getAbsoluteFile().toURI(), size);
		}
		catch (final MalformedURLException e) {
			LogTool.warn(e);
			return;
		}
		catch (final IOException e) {
			LogTool.warn(e);
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
