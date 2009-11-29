package org.freeplane.view.swing.addins.filepreview;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.ui.components.BitmapViewerComponent;
import org.freeplane.core.util.LogTool;
import org.freeplane.view.swing.addins.filepreview.ViewerController.FactoryFileFilter;

import java.beans.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/* ImagePreview.java by FileChooserDemo2.java. */
public class ImagePreview extends BitmapImagePreview{

    public ImagePreview(JFileChooser fc) {
    	super(fc);
    }

    protected void updateView(final File file) {
	    FactoryFileFilter filter = (FactoryFileFilter) fc.getFileFilter();
	    Dimension size = getSize();
		size.width -= 2 * BORDER_WIDTH;
		size.height -= 2 * BORDER_WIDTH;
        JComponent viewer;
        try {
	        viewer = filter.getFactory().createViewer(file.getAbsoluteFile().toURI(), size);
        }
        catch (MalformedURLException e) {
	        LogTool.warn(e);
	        return;
        }
        catch (IOException e) {
	        LogTool.warn(e);
	        return;
        }
        if(viewer == null){
        	return;
        }
		viewer.setLocation(BORDER_WIDTH, BORDER_WIDTH);
        if(viewer instanceof BitmapViewerComponent){
        	((BitmapViewerComponent) viewer).setHint(Image.SCALE_FAST);
        }
        add(viewer);
		viewer.revalidate();
		viewer.repaint();
    }
}
