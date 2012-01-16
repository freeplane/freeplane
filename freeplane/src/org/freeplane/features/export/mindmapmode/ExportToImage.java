/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.export.mindmapmode;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

/**
 * @author foltin
 * @author kakeda
 * @author rreppel
 */
public class ExportToImage extends AExportEngine {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private final String imageDescripton;
	private final String imageType;

	ExportToImage( final String imageType, final String imageDescripton) {
		this.imageType = imageType;
		this.imageDescripton = imageDescripton;
	}

	public void export(MapModel map, File toFile) {
		try {
			final RenderedImage image = createBufferedImage(map);
			if (image != null) {
				exportToImage(image, toFile);
			}
		}
		catch (final OutOfMemoryError ex) {
			UITools.errorMessage(TextUtils.getText("out_of_memory"));
		}
	}

	/**
	 * Export image.
	 * @param toFile 
	 */
	public boolean exportToImage(final RenderedImage image, File chosenFile) {
		try {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			final FileOutputStream out = new FileOutputStream(chosenFile);
			ImageIO.write(image, imageType, out);
			out.close();
		}
		catch (final IOException e1) {
			LogUtils.warn(e1);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
		finally{
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
		return true;
	}

	public FileFilter getFileFilter() {
		return new ExampleFileFilter(imageType, imageDescripton);
    }

}
