package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;

import javax.swing.JFileChooser;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.features.filepreview.ViewerController.FactoryFileFilter;

public class MindMapPreview extends BitmapImagePreview {
    private static final String EXTENSION = ".mm";
    private static final long serialVersionUID = 1L;

    public MindMapPreview(final JFileChooser fc) {
        super(fc);
        final int previewSize = ResourceController.getResourceController().getIntProperty("image_preview_size", 300);
        setPreferredSize(new Dimension(2 * previewSize, previewSize));
    }
    public MindMapPreview() {
        super();
        final int previewSize = ResourceController.getResourceController().getIntProperty("image_preview_size", 300);
        Dimension preferredSize = new Dimension(2 * previewSize, previewSize);
        setPreferredSize(preferredSize);
        setSize(preferredSize);
    }

    @Override
    public void updateView(final File file) {
        removeView();
        if(file != null) {
            String path = file.getPath();
            updateViewByImage(previewFile(path));
        }
    }

    private void updateViewByImage(File file) {
        if(file != null) {
            final ViewerController viewerController = Controller.getCurrentModeController().getExtension(ViewerController.class);
            IViewerFactory factory = viewerController.getViewerFactory();
            URI uri = file.getAbsoluteFile().toURI();
            if(factory.accept(uri))
                new ImageRendererFactory().configureRenderer(factory, uri, getSize(), this);
        }
    }

    private File previewFile(String path) {
        if(path.endsWith(EXTENSION))
            return previewFile(path, ".svg", ".png");
        else
            return null;
    }

    private File previewFile(String path, String... extensions) {
        for(String extension : extensions) {
            File imageFile = new File(path.substring(0, path.length() - EXTENSION.length()) + extension);
            if(imageFile.canRead())
                return imageFile;
        }
        return null;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }
    @Override
    public void reshape(int x, int y, int w, int h) {
        // TODO Auto-generated method stub
        super.reshape(x, y, w, h);
    }
    
    
    
    
}
