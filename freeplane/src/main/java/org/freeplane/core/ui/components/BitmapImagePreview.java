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
import org.freeplane.core.util.LogUtils;
import org.freeplane.view.swing.features.filepreview.BitmapViewerComponent;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapImagePreview extends JComponent implements PropertyChangeListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected static final int BORDER_WIDTH = 2;

    public BitmapImagePreview(final JFileChooser fc) {
        this();
        fc.addPropertyChangeListener(this);
    }

	public BitmapImagePreview() {
		super();
		setBorder(new MatteBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, Color.BLACK));
		final int previewSize = ResourceController.getResourceController().getIntProperty("image_preview_size", 300);
		setPreferredSize(new Dimension(previewSize, previewSize));
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e) {
		final String prop = e.getPropertyName();
		//If the directory changed, don't show an image.
		final File file;
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			file = null;
			//If a file became selected, find out which one.
		}
		else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			file = (File) e.getNewValue();
		}
		else {
			return;
		}
		try {
			updateView(file);
		}
		catch (final MalformedURLException e1) {
			LogUtils.warn(e1);
		}
		catch (final IOException e1) {
			LogUtils.warn(e1);
		}
	}

	protected void updateView(final File file) throws MalformedURLException, IOException {
        removeView();
        repaint();
        if (file == null || !file.exists()) {
            return;
        }
		final BitmapViewerComponent viewer = new BitmapViewerComponent(file.toURI());
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

    protected void removeView() {
        if (getComponentCount() == 1) {
            remove(0);
            repaint();
        }
    }
}
