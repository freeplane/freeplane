package org.freeplane.view.swing.features.filepreview;

import java.io.File;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.view.swing.features.filepreview.ViewerController.FactoryFileFilter;

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
		final IViewerFactory factory = factoryFileFilter.getFactory();
		new ImageRendererFactory().configureRenderer(factory, file.getAbsoluteFile().toURI(), getSize(), this);
	}
}
