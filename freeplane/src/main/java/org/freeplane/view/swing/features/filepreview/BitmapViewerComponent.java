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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.view.swing.map.MapView;

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
	private static boolean disabledDueToJavaBug = false;

	public BitmapViewerComponent(final URI uri) throws MalformedURLException, IOException {
		url = uri.toURL();
		originalSize = readImageSize(url);
		hint = Image.SCALE_SMOOTH;
		scaleEnabled = true;
		cachedImage = null;
	}

	static private Dimension readImageSize(URL url) throws IOException {
		try (InputStream inputStream = url.openStream();
			 ImageInputStream in = ImageIO.createImageInputStream(inputStream)){
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
	    AccessController.doPrivileged(//
	            (PrivilegedAction<Void>)() -> paintComponentPrivileged(g));
	}

    private Void paintComponentPrivileged(final Graphics g) {
        if (componentHasNoArea() || disabledDueToJavaBug) {
			return null;
		}
        if(isPrinting()) {
            paintOriginalImage(g);
            return null;
        }
		if (cachedImage == null && cachedImageWeakRef != null) {
			cachedImage = cachedImageWeakRef.get();
			cachedImageWeakRef = null;
		}
		final Graphics2D g2 = (Graphics2D) g;
		final AffineTransform transform = g2.getTransform();
		final double scaleX = transform.getScaleX();
		final double scaleY = transform.getScaleY();
		int cachedImageWidth = (int) (getWidth() * scaleX);
		int cachedImageHeight = (int) (getHeight() * scaleY);
		if (cachedImage == null && cacheFile != null) {
			loadImageFromCacheFile();
			if (!isCachedImageValid(cachedImageWidth, cachedImageHeight)) {
				cacheFile.delete();
				cacheFile = null;
			}
		}
		if (!isCachedImageValid(cachedImageWidth, cachedImageHeight)) {
			final BufferedImage image = loadImageFromURL();
			if (image == null || hasNoArea(image)) {
				return null;
			}
			BufferedImage scaledImage = null;
			try {
				scaledImage = Scalr.resize(image, Scalr.Mode.BEST_FIT_BOTH, cachedImageWidth, cachedImageHeight);
			}
			catch (final Exception e) {
				LogUtils.severe(e);
				return null;
			}
			finally {
				image.flush();
			}
			final int scaledImageHeight = scaledImage.getHeight();
			final int scaledImageWidth = scaledImage.getWidth();
			imageX = (cachedImageWidth - scaledImageWidth) / 2;
			imageY = (cachedImageHeight - scaledImageHeight) / 2;
			cachedImage = scaledImage;
			if (getCacheType().equals(CacheType.IC_FILE)) {
				writeCacheFile();
			}
		}
		try {
			if(scaleX != 1 || scaleY != 1) {
				Graphics2D gg = (Graphics2D)g.create();
				gg.setTransform(AffineTransform.getTranslateInstance(imageX + transform.getTranslateX(), imageY  + transform.getTranslateY()));
				gg.drawImage(cachedImage, 0, 0, null);
				gg.dispose();
			}
			else
				g.drawImage(cachedImage, imageX, imageY, null);
		}
		catch (ClassCastException e) {
			LogUtils.severe("Disabled bitmap image painting due to java bug https://bugs.openjdk.java.net/browse/JDK-8160328. Modify freeplane.sh to run java with option '-Dsun.java2d.xrender=false'");
			disabledDueToJavaBug = true;
		}
		flushImage();
		return null;
    }

	private void paintOriginalImage(Graphics g) {
        final BufferedImage image = loadImageFromURL();
        if (image != null && !hasNoArea(image)) {
            try {
                g.drawImage(image, imageX, imageY, getWidth(), getHeight(), null);
            }
            catch (ClassCastException e) {
                LogUtils.severe("Disabled bitmap image painting due to java bug https://bugs.openjdk.java.net/browse/JDK-8160328. Modify freeplane.sh to run java with option '-Dsun.java2d.xrender=false'");
                disabledDueToJavaBug = true;
            }
        }
    }


    private boolean isPrinting() {
	    MapView map = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
	    return map != null && map.isPrinting();
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

	private boolean isCachedImageValid(int width, int height) {
		return cachedImage != null
		        && (!scaleEnabled || componentHasSameWidthAsCachedImage()
		                && cachedImageHeightFitsComponentHeight(height) || cachedImageWidthFitsComponentWidth(width)
		                && componentHasSameHeightAsCachedImage(height));
	}

	private boolean componentHasSameHeightAsCachedImage(int height) {
		return 1 >= Math.abs(height - cachedImage.getHeight());
	}

	private boolean cachedImageWidthFitsComponentWidth(int width) {
		return width >= cachedImage.getWidth();
	}

	private boolean cachedImageHeightFitsComponentHeight(int height) {
		return height >= cachedImage.getHeight();
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
			if(cacheFile == null)
				cacheFile = File.createTempFile("cachedImage", ".image", tempDir);
			if (!ImageIO.write(cachedImage, "jpg", cacheFile))
				ImageIO.write(cachedImage, "png", cacheFile);
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
