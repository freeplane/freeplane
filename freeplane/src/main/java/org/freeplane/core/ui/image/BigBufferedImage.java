package org.freeplane.core.ui.image;

/*
 * @author Zsolt Pocze, Dimitry Polivaev
 */

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Hashtable;

import sun.nio.ch.DirectBuffer;


public class BigBufferedImage extends BufferedImage {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	private static final int MAX_PIXELS_IN_MEMORY = 50 * 1024 * 1024;

    public static BufferedImage create(int width, int height, int imageType){
    	if(width * height > MAX_PIXELS_IN_MEMORY)
			try {
				final File tempDir = new File(TMP_DIR);
				return createBigBufferedImage(tempDir, width, height, imageType);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		else
    		return new BufferedImage(width, height, imageType);
    }


	private static BufferedImage createBigBufferedImage(File tempDir, int width, int height, int imageType)
			throws FileNotFoundException, IOException {
		FileDataBuffer buffer = new FileDataBuffer(tempDir, width * height, 4);
        ColorModel colorModel = null;
        BandedSampleModel sampleModel = null;
        switch (imageType) {
            case TYPE_INT_RGB:
                colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        new int[]{8, 8, 8, 0},
                        false,
                        false,
                        ComponentColorModel.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE);
                sampleModel = new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3);
                break;
            case TYPE_INT_ARGB:
                colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        new int[]{8, 8, 8, 8},
                        true,
                        false,
                        ComponentColorModel.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE);
                sampleModel = new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4);
                break;
            default:
                throw new IllegalArgumentException("Unsupported image type: " + imageType);
        }
        SimpleRaster raster = new SimpleRaster(sampleModel, buffer, new Point(0, 0));
        BigBufferedImage image = new BigBufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        return image;
	}


    private BigBufferedImage(ColorModel cm, SimpleRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
    }

    public void dispose(){
    	((SimpleRaster) getRaster()).dispose();
    }
    
    static public void dispose(RenderedImage image){
    	if(image instanceof BigBufferedImage){
    		((BigBufferedImage) image).dispose();
    	}
    }

    private static class SimpleRaster extends WritableRaster {

        public SimpleRaster(SampleModel sampleModel, FileDataBuffer dataBuffer, Point origin) {
            super(sampleModel, dataBuffer, origin);
        }

		public void dispose() {
			((FileDataBuffer)getDataBuffer()).dispose();
		}

    }
    
    private static class FileDataBuffer extends DataBuffer {

        private final String id = "buffer-" + System.currentTimeMillis() + "-" + ((int) (Math.random() * 1000));
        private File dir;
        private String path;
        private File[] files;
        private RandomAccessFile[] accessFiles;
        private MappedByteBuffer[] buffer;

        public FileDataBuffer(File dir, int size) throws FileNotFoundException, IOException {
            super(TYPE_BYTE, size);
            this.dir = dir;
            init();
        }

		public FileDataBuffer(File dir, int size, int numBanks) throws FileNotFoundException, IOException {
            super(TYPE_BYTE, size, numBanks);
            this.dir = dir;
            init();
        }

        public void dispose() {
        	try {
        		for(MappedByteBuffer b : buffer) {
        			((DirectBuffer) b).cleaner().clean();
        		}
        		for(RandomAccessFile file : accessFiles) {
        			file.close();
        		}
        		for(File file : files) {
        			file.delete();
        		}
				new File(path).delete();
			} catch (IOException e) {
			}
		}

        private void init() throws FileNotFoundException, IOException {
            if (dir == null) {
                dir = new File(".");
            }
            if (!dir.exists()) {
                throw new RuntimeException("FileDataBuffer constructor parameter dir does not exist: " + dir);
            }
            if (!dir.isDirectory()) {
                throw new RuntimeException("FileDataBuffer constructor parameter dir is not a directory: " + dir);
            }
            path = dir.getPath() + "/" + id;
            File subDir = new File(path);
            subDir.mkdir();
            subDir.deleteOnExit();
            buffer = new MappedByteBuffer[banks];
            accessFiles = new RandomAccessFile[banks];
            files = new File[banks];
            for (int i = 0; i < banks; i++) {
                File file = files[i] = new File(path + "/bank" + i + ".dat");
                file.deleteOnExit();
				final RandomAccessFile randomAccessFile = accessFiles[i] = new RandomAccessFile(file, "rw");
                buffer[i] = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, getSize());
            }
        }

        @Override
        public int getElem(int bank, int i) {
            return buffer[bank].get(i) & 0xff;
        }

        @Override
        public void setElem(int bank, int i, int val) {
            buffer[bank].put(i, (byte) val);
        }

    }
}
