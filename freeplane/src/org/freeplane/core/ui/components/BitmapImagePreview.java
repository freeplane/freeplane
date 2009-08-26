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
package org.freeplane.core.ui.components;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.border.MatteBorder;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;


public class BitmapImagePreview extends JComponent implements PropertyChangeListener {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	protected static final int BORDER_WIDTH = 2;
	protected final JFileChooser fc;

	public BitmapImagePreview(JFileChooser fc) {
		super();
        this.fc = fc;
		setBorder(new MatteBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, Color.BLACK));
		int previewSize = ResourceController.getResourceController().getIntProperty("image_preview_size", 300);
        setPreferredSize(new Dimension(previewSize, previewSize));
        fc.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
    
        //If the directory changed, don't show an image.
        final File file;
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
    
        //If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
        	file = (File) e.getNewValue();
        }
        else{
        	return;
        }
        if(getComponentCount() == 1){
        	remove(0);
        }
        repaint();
        if(file == null){
        	return;
        }
        try {
	        updateView(file);
        }
        catch (MalformedURLException e1) {
	        LogTool.warn(e1);
        }
        catch (IOException e1) {
	        LogTool.warn(e1);
        }
    }

	protected void updateView(File file) throws MalformedURLException, IOException {
		BitmapViewerComponent viewer = new BitmapViewerComponent(file.toURI());
		viewer.setHint(Image.SCALE_FAST);
		final Dimension size = getSize();
		size.width -= 2 * BORDER_WIDTH;
		size.height -= 2 * BORDER_WIDTH;
		viewer.setPreferredSize(size);
		viewer.setSize(size);
		viewer.setLocation(BORDER_WIDTH, BORDER_WIDTH);
        add(viewer);
		viewer.revalidate();
		viewer.repaint();
    }
}
