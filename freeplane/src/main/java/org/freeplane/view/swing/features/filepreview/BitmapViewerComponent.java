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
package org.freeplane.view.swing.features.filepreview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

import com.thebuzzmedia.imgscalr.Scalr;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
public class BitmapViewerComponent extends JComponent implements ScalableComponent {

	enum CacheType {
		IC_DISABLE, IC_FILE, IC_RAM
	}

	private static final long serialVersionUID = 1L;
	private File cacheFile;
	private int hint;
	private BufferedImage cachedImage;
	private WeakReference<BufferedImage> cachedImageWeakRef;
	private final URL url;
	private final Dimension originalSize;
	private int imageX;
	private int imageY;
	private boolean scaleEnabled;
	private Dimension maximumSize = null;
	private final static Object LOCK = new Object();
	private static boolean disabledDueToJavaBug = false;

	public BitmapViewerComponent(final URI uri) throws MalformedURLException, IOException {
		url = uri.toURL();
		originalSize = readImageSize(url);
		hint = Image.SCALE_SMOOTH;
		scaleEnabled = true;
		cachedImage = null;
	}

	static private Dimension readImageSize(URL url) throws IOException {
		InputStream inputStream = null;
		ImageInputStream in = null;
		try {
			inputStream = url.openStream();
			in = ImageIO.createImageInputStream(inputStream);
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				final ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					return new Dimension(reader.getWidth(0), reader.getHeight(0));
				}
				finally {
					reader.dispose();
				}
			}
			else {
				throw new IOException("can not create image");
			}
		}
		finally {
			if (in != null) {
				in.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public boolean isScaleEnabled() {
		return scaleEnabled;
	}

	public void setScaleEnabled(final boolean scaleEnabled) {
		this.scaleEnabled = scaleEnabled;
	}

	protected int getHint() {
		return hint;
	}

	public void setHint(final int hint) {
		this.hint = hint;
	}

	@Override
	public Dimension getOriginalSize() {
		return new Dimension(originalSize);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (componentHasNoArea() || disabledDueToJavaBug) {
			return;
		}
		if (cachedImage == null && cachedImageWeakRef != null) {
			cachedImage = cachedImageWeakRef.get();
			cachedImageWeakRef = null;
		}
		if (cachedImage == null && cacheFile != null) {
			loadImageFromCacheFile();
			if (!isCachedImageValid()) {
				cacheFile.delete();
				cacheFile = null;
			}
		}
		if (!isCachedImageValid()) {
			final BufferedImage image = loadImageFromURL();
			if (image == null || hasNoArea(image)) {
				return;
			}
			BufferedImage scaledImage = null;
			try {
				scaledImage = Scalr.resize(image, Scalr.Mode.BEST_FIT_BOTH, getWidth(), getHeight());
			}
			catch (final Exception e) {
				LogUtils.severe(e);
				return;
			}
			finally {
				image.flush();
			}
			final int scaledImageHeight = scaledImage.getHeight();
			final int scaledImageWidth = scaledImage.getWidth();
			centerImagePosition(scaledImageWidth, scaledImageHeight);
			cachedImage = scaledImage;
			if (getCacheType().equals(CacheType.IC_FILE)) {
				writeCacheFile();
			}
		}
		try {
			g.drawImage(cachedImage, imageX, imageY, null);
		}
		catch (ClassCastException e) {
			LogUtils.severe("Disabled bitmap image painting due to java bug https://bugs.openjdk.java.net/browse/JDK-8160328. Modify freeplane.sh to run java with option '-Dsun.java2d.xrender=false'");
			disabledDueToJavaBug = true;
		}
		flushImage();
	}

	private void centerImagePosition(final int scaledImageWidth, final int scaledImageHeight) {
		imageX = (getWidth() - scaledImageWidth) / 2;
		imageY = (getHeight() - scaledImageHeight) / 2;
	}

	private boolean componentHasNoArea() {
		return getWidth() == 0 || getHeight() == 0;
	}

	private boolean hasNoArea(final BufferedImage image) {
		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();
		if (imageWidth == 0 || imageHeight == 0) {
			return true;
		}
		return false;
	}

	private void loadImageFromCacheFile() {
		try {
			cachedImage = ImageIO.read(cacheFile);
		}
		catch (final IOException e) {
			logImageReadingException(e);
		}
	}

	private boolean isCachedImageValid() {
		return cachedImage != null
		        && (!scaleEnabled || componentHasSameWidthAsCachedImage()
		                && cachedImageHeightFitsComponentHeight() || cachedImageWidthFitsComponentWidth()
		                && componentHasSameHeightAsCachedImage());
	}

	private boolean componentHasSameHeightAsCachedImage() {
		return 1 >= Math.abs(getHeight() - cachedImage.getHeight());
	}

	private boolean cachedImageWidthFitsComponentWidth() {
		return getWidth() >= cachedImage.getWidth();
	}

	private boolean cachedImageHeightFitsComponentHeight() {
		return getHeight() >= cachedImage.getHeight();
	}

	private boolean componentHasSameWidthAsCachedImage() {
		return 1 >= Math.abs(getWidth() - cachedImage.getWidth());
	}

	private BufferedImage loadImageFromURL() {
		BufferedImage tempImage = null;
		try {
			tempImage = ImageIO.read(url);
		}
		catch (final IOException e) {
			logImageReadingException(e);
		}
		return tempImage;
	}

	private void logImageReadingException(final IOException e) {
		if((e instanceof FileNotFoundException) || (e instanceof IIOException)) {
			LogUtils.warn(e.getMessage());
			return;
		}
		final Throwable cause = e.getCause();
		if((cause instanceof FileNotFoundException) || (cause instanceof IIOException)) {
			LogUtils.warn(cause.getMessage());
			return;
		}

		LogUtils.severe(e);
	}

	private void flushImage() {
		final CacheType cacheType = getCacheType();
		if (CacheType.IC_RAM.equals(cacheType)) {
			cachedImage.flush();
		}
		else {
			cachedImageWeakRef = new WeakReference<BufferedImage>(cachedImage);
			cachedImage = null;
		}
	}

	private CacheType getCacheType() {
		return ResourceController.getResourceController().getEnumProperty("image_cache", CacheType.IC_DISABLE);
	}

	private void writeCacheFile() {
		final File tempDir = new File(System.getProperty("java.io.tmpdir"), "freeplane");
		tempDir.mkdirs();
		try {
			synchronized(LOCK) {
				if(cacheFile == null)
					cacheFile = File.createTempFile("cachedImage", ".jpg", tempDir);
				else{
					cacheFile.delete();
					cacheFile.createNewFile();
				}

			}
			ImageIO.write(cachedImage, "jpg", cacheFile);
		}
		catch (final IOException e) {
			if(cacheFile != null) {
				cacheFile.delete();
				cacheFile = null;
			}
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		if (cacheFile != null) {
			cacheFile.delete();
			cacheFile = null;
		}
	}

	@Override
	public void setFinalViewerSize(final Dimension size) {
		final Dimension sizeWithScaleCorrection = fitToMaximumSize(size);
		setPreferredSize(sizeWithScaleCorrection);
		setSize(sizeWithScaleCorrection);
		setScaleEnabled(true);
	}

	@Override
	public void setFinalViewerSize(final float zoom) {
		final int scaledWidth = (int) (originalSize.width * zoom);
		final int scaledHeight = (int) (originalSize.height * zoom);
		setFinalViewerSize(new Dimension(scaledWidth, scaledHeight));
	}

	@Override
	public void setDraftViewerSize(final Dimension size) {
		setPreferredSize(size);
		setSize(size);
		setScaleEnabled(false);
	}

	@Override
	public void setMaximumComponentSize(final Dimension size) {
		maximumSize = size;
	}

	private Dimension fitToMaximumSize(final Dimension size) {
		if (maximumSize == null || isUnderMaximumSize(size)) {
			return size;
		}
		else {
			return maximumSize;
		}
	}

	private boolean isUnderMaximumSize(final Dimension size) {
		return maximumSize.getWidth() >= size.width || maximumSize.getHeight() >= size.height;
	}
}
