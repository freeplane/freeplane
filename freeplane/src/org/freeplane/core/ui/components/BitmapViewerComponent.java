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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

import com.thebuzzmedia.imgscalr.AsyncScalr;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapViewerComponent extends JComponent {
	/**
	 *
	 */
	static{
//		System.setProperty("imgscalr.debug", "true");
		AsyncScalr.setServiceThreadCount(1);
	}

	enum CacheType{IC_DISABLE, IC_FILE, IC_RAM};
	private static final long serialVersionUID = 1L;
	private File cacheFile;
	private int hint;
	private BufferedImage cachedImage;
	private WeakReference<BufferedImage> cachedImageWeakRef;
	private final URL url;
	private final Dimension originalSize;
	private int imageX;
	private int imageY;
	private boolean processing;
	private boolean scaleEnabled;

	public boolean isScaleEnabled() {
		return scaleEnabled;
	}

	public void setScaleEnabled(boolean scaleEnabled) {
		this.scaleEnabled = scaleEnabled;
	}

	protected int getHint() {
		return hint;
	}

	public void setHint(final int hint) {
		this.hint = hint;
	}

	public BitmapViewerComponent(final URI uri) throws MalformedURLException, IOException {
		url = uri.toURL();
		originalSize = readOriginalSize();
		hint = Image.SCALE_SMOOTH;
		processing = false;
		scaleEnabled = true;
		cachedImage = null;
	}

	private Dimension readOriginalSize() throws IOException {
		InputStream inputStream = null;
		ImageInputStream in = null;
		try {
				inputStream = url.openStream();
				in = ImageIO.createImageInputStream(inputStream);
		        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
		        if (readers.hasNext()) {
		                ImageReader reader = readers.next();
		                try {
		                        reader.setInput(in);
		                        return new Dimension(reader.getWidth(0), reader.getHeight(0));
		                } finally {
		                        reader.dispose();
		                }
		        }
		        else{
		        	throw new IOException("can not create image");
		        }
		} finally {
		        if (in != null)
		        	in.close();
		        if(inputStream != null)
		        	inputStream.close();
		}
    }

	public Dimension getOriginalSize() {
		return new Dimension(originalSize);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if(processing)
			return;
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		if(cachedImage == null && cachedImageWeakRef != null){
			cachedImage = cachedImageWeakRef.get();
			cachedImageWeakRef = null;
		}
		if(cachedImage == null && cacheFile != null)
			loadImageFromCacheFile();
		if(! isCachedImageValid()){
			BufferedImage tempImage;
	        try {
	        	tempImage = ImageIO.read(url);
	        }
	        catch (IOException e) {
				return;
	        }
	        final BufferedImage image = tempImage;
			final int imageWidth = image.getWidth();
			final int imageHeight = image.getHeight();
			if(imageWidth == 0 || imageHeight == 0){
				return;
			}
			processing = true;
			final Future<BufferedImage> result = AsyncScalr.resize(image, getWidth(),getHeight());
			AsyncScalr.getService().submit(new Runnable() {
				public void run() {
					BufferedImage scaledImage = null;
					try {
						scaledImage = result.get();
					} catch (Exception e) {
						LogUtils.severe(e);
						return;
					}
					finally{
						image.flush();
					}
					final int scaledImageHeight = scaledImage.getHeight();
					final int scaledImageWidth = scaledImage.getWidth();
					if (scaledImageHeight > getHeight()) {
						imageX = 0;
						imageY = (getHeight() - scaledImageHeight) / 2;
					}
					else {
						imageX = (getWidth() - scaledImageWidth) / 2;
						imageY = 0;
					}
					cachedImage = scaledImage;
					if(getCacheType().equals(CacheType.IC_FILE))
						writeCacheFile();
					EventQueue.invokeLater(new Runnable() {

						public void run() {
							processing = false;
							repaint();
						}
					});
				}
			});
		}
		else{
			g.drawImage(cachedImage, imageX, imageY, null);
			flushImage();
		}
	}

	private void flushImage() {
		final CacheType cacheType = getCacheType();
		if(CacheType.IC_RAM.equals(cacheType)){
			cachedImage.flush();
		}
		else{
			cachedImageWeakRef = new WeakReference<BufferedImage>(cachedImage);
			cachedImage = null;
		}
	}

	private CacheType getCacheType() {
		return ResourceController.getResourceController().getEnumProperty("image_cache", CacheType.IC_DISABLE);
	}

	private final static Object LOCK = new Object();
	private void writeCacheFile() {
		File tempDir = new File (System.getProperty("java.io.tmpdir"), "freeplane");
		tempDir.mkdirs();
		try {
			synchronized(LOCK) {
				cacheFile = File.createTempFile("cachedImage", ".jpg", tempDir);
			}
			ImageIO.write(cachedImage, "jpg", cacheFile);

		} catch (IOException e) {
			cacheFile.delete();
			cacheFile = null;
		}
	}

	private boolean isCachedImageValid() {
		return cachedImage != null &&
				(! scaleEnabled
					|| 1 >= Math.abs(getWidth() -  cachedImage.getWidth()) && getHeight() >= cachedImage.getHeight()
				    || getWidth() >=  cachedImage.getWidth() && 1 >= Math.abs(getHeight() - cachedImage.getHeight())
				 );
	}

	private void loadImageFromCacheFile() {
		try {
			cachedImage = ImageIO.read(cacheFile);
			if(isCachedImageValid())
				return;
		} catch (IOException e) {
		}
		cacheFile.delete();
		cacheFile = null;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		if(cacheFile != null){
			cacheFile.delete();
			cacheFile = null;
		}
	}


}
