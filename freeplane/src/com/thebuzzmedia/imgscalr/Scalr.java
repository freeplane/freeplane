/**
 * Copyright 2011 The Buzz Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is modified by Dimitry Polivaev
 * 
 */
package com.thebuzzmedia.imgscalr;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;

import javax.imageio.ImageIO;

/**
 * Class used to implement performant, good-quality and intelligent image
 * scaling algorithms in native Java 2D. This class utilizes the Java2D
 * "best practices" for image-scaling, ensuring that images are hardware
 * accelerated at all times if provided by the platform and host-VM.
 * <p/>
 * Hardware acceleration also includes execution of optional caller-supplied
 * {@link BufferedImageOp}s that are applied to the resultant images before
 * returning them as well as any optional rotations specified.
 * <h3>Image Proportions</h3>
 * All scaling operations implemented by this class maintain the proportion of
 * the original image. If image-cropping is desired the caller will need to
 * perform those edits before calling one of the <code>resize</code> methods
 * provided by this class.
 * <p/>
 * In order to maintain the proportionality of the original images, this class
 * implements the following behavior:
 * <ol>
 * <li>If the image is LANDSCAPE-oriented or SQUARE, treat the
 * <code>targetWidth</code> as the primary dimension and re-calculate the
 * <code>targetHeight</code> regardless of what is passed in.</li>
 * <li>If image is PORTRAIT-oriented, treat the <code>targetHeight</code> as the
 * primary dimension and re-calculate the <code>targetWidth</code> regardless of
 * what is passed in.</li>
 * <li>If a {@link Mode} value of {@link Mode#FIT_TO_WIDTH} or
 * {@link Mode#FIT_TO_HEIGHT} is passed in to the <code>resize</code> method,
 * the image's orientation is ignored and the scaled image is fit to the
 * dimension the user specified with the {@link Mode}.</li>
 * </ol>
 * Recalculation of the secondary dimensions is extremely cheap and this
 * approach provides users with better expected-behavior from the library.
 * <h3>Image Quality</h3>
 * This class implements a few different methods for scaling an image, providing
 * either the best-looking result, the fastest result or a balanced result
 * between the two depending on the scaling hint provided (see {@link Method}).
 * <p/>
 * This class also implements the incremental scaling algorithm presented by
 * Chris Campbell in his <a href="http://today.java
 * .net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html">Perils of
 * Image.getScaledInstance()</a> article in order to give the best-looking
 * results to images scaled down below roughly 800px in size where using a
 * single scaling operation (even with
 * {@link RenderingHints#VALUE_INTERPOLATION_BICUBIC} interpolation) would
 * produce a much worse-looking result.
 * <p/>
 * Only when scaling using the {@link Method#AUTOMATIC} method will this class
 * look at the size of the image before selecting an approach to scaling the
 * image. If {@link Method#QUALITY} is specified, the best-looking algorithm
 * possible is always used.
 * <p/>
 * Minor modifications are made to Campbell's original implementation in the
 * form of:
 * <ol>
 * <li>Instead of accepting a user-supplied interpolation method,
 * {@link RenderingHints#VALUE_INTERPOLATION_BICUBIC} interpolation is always
 * used. This was done after A/B comparison testing with large images
 * down-scaled to thumbnail sizes showed noticeable "blurring" when BILINEAR
 * interpolation was used. Given that Campbell's algorithm is only used in
 * QUALITY mode when down-scaling, it was determined that the user's expectation
 * of a much less blurry picture would require that BICUBIC be the default
 * interpolation in order to meet the QUALITY expectation.</li>
 * <li>After each iteration of the do-while loop that incrementally scales the
 * source image down, an explicit effort is made to call
 * {@link BufferedImage#flush()} on the interim temporary {@link BufferedImage}
 * instances created by the algorithm in an attempt to ensure a more complete GC
 * cycle by the VM when cleaning up the temporary instances (this is in addition
 * to disposing of the temporary {@link Graphics2D} references as well).</li>
 * <li>Extensive comments have been added to increase readability of the code.</li>
 * <li>Variable names have been expanded to increase readability of the code.</li>
 * </ol>
 * <p/>
 * <strong>NOTE</strong>: This class does not call {@link BufferedImage#flush()}
 * on any of the <em>source images</em> passed in by calling code; it is up to
 * the original caller to dispose of their source images when they are no longer
 * needed so the VM can most efficiently GC them.
 * <h3>Generated Image Types</h3>
 * Java2D provides support for a number of different image types defined as
 * <code>BufferedImage.TYPE_*</code> variables, unfortunately not all image
 * types are supported equally in Java2D. Some more obscure image types either
 * have poor or no support, leading to severely degraded quality when an attempt
 * is made by imgscalr to create a scaled instance <em>of the same type</em> as
 * the source image.
 * <p/>
 * To avoid imgscalr generating significantly worse-looking results than
 * alternative scaling approaches (e.g.
 * {@link Image#getScaledInstance(int, int, int)}), all resultant images
 * generated by imgscalr are one of two types:
 * <ol>
 * <li>{@link BufferedImage#TYPE_INT_RGB}</li>
 * <li>{@link BufferedImage#TYPE_INT_ARGB}</li>
 * </ol>
 * depending on if the source image utilizes transparency or not.
 * <p/>
 * This is a recommended approach by the Java2D team for dealing with poorly (or
 * non) supported image types. More can be read about this issue <a href=
 * "http://www.mail-archive.com/java2d-interest@capra.eng.sun.com/msg05621.html"
 * >here</a>.
 * <h3>Logging</h3>
 * This class implements all its debug logging via the
 * {@link #log(String, Object...)} method. At this time logging is done directly
 * to <code>System.out</code> via the <code>printf</code> method. This allows
 * the logging to be light weight and easy to capture while adding no
 * dependencies to the library.
 * <p/>
 * Implementation of logging in this class is as efficient as possible; avoiding
 * any calls to the logger or passing of arguments if logging is not enabled to
 * avoid the (hidden) cost of constructing the Object[] argument for the varargs
 * method call.
 * <h3>GIF Transparency</h3>
 * Unfortunately in Java 6 and earlier, support for GIF's
 * {@link IndexColorModel} is sub-par, both in accurate color-selection and in
 * maintaining transparency when moving to an image of type
 * {@link BufferedImage#TYPE_INT_ARGB}; because of this issue when a GIF image
 * is processed by imgscalr and the result saved as a GIF file, it is possible
 * to lose the alpha channel of a transparent image or in the case of applying
 * an optional {@link BufferedImageOp}, lose the entire picture all together in
 * the result (long standing JDK bugs are filed for these).
 * <p/>
 * imgscalr currently does nothing to work around this manually because it is a
 * defect in the native platform code itself. Fortunately it looks like the
 * issues are half-fixed in Java 7 and any manual workarounds we could attempt
 * internally are relatively expensive, in the form of hand-creating and setting
 * RGB values pixel-by-pixel with a custom {@link ColorModel} in the scaled
 * image. This would lead to a very measurable negative impact on performance
 * without the caller understanding why.
 * <p>
 * <strong>Workaround</strong>: A workaround to this issue with all version of
 * Java is to simply save a GIF as a PNG; no change to your code needs to be
 * made except when the image is saved out, e.g. using {@link ImageIO}. When a
 * file type of "PNG" is used, both the transparency and high color quality will
 * be maintained as the PNG code path in Java2D is superior to the GIF
 * implementation.
 * <p>
 * If the issue with optional {@link BufferedImageOp}s destroying GIF image
 * content is ever fixed in the platform, saving out resulting images as GIFs
 * should suddenly start working.
 * <p>
 * More can be read about the issue <a
 * href="http://gman.eichberger.de/2007/07/transparent-gifs-in-java.html"
 * >here</a> and <a
 * href="http://ubuntuforums.org/archive/index.php/t-1060128.html">here</a>.
 *
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 1.1
 */
public class Scalr {
	/**
	 * Flag used to indicate if debugging output has been enabled by setting the
	 * "imgscalr.debug" system property to <code>true</code>. This value will be
	 * <code>false</code> if the "imgscalr.debug" system property is undefined
	 * or set to <code>false</code>.
	 * <p/>
	 * This system property can be set on startup with:<br/>
	 * <code>
	 * -Dimgscalr.debug=true
	 * </code> or by calling {@link System#setProperty(String, String)} before
	 * this class is loaded.
	 * <p/>
	 * Default value is <code>false</code>.
	 */
	public static final boolean DEBUG;

	static {
		boolean debug = false;
		try{
		 debug = Boolean.getBoolean("imgscalr.debug");
		}
		catch(Exception e) {
		}
		DEBUG = debug;
        }
	/**
	 * Prefix to every log message this library logs. Using a well-defined
	 * prefix helps make it easier both visually and programmatically to scan
	 * log files for messages produced by this library.
	 * <p/>
	 * The value is "[imgscalr] " (including the space).
	 */
	public static final String LOG_PREFIX = "[imgscalr] ";

	/**
	 * A {@link ConvolveOp} using a very light "blur" kernel that acts like an
	 * anti-aliasing filter (softens the image a bit) when applied to an image.
	 * <p/>
	 * A common request by users of the library was that they wished to "soften"
	 * resulting images when scaling them down drastically. After quite a bit of
	 * A/B testing, the kernel used by this Op was selected as the closest match
	 * for the target which was the softer results from the deprecated
	 * {@link AreaAveragingScaleFilter} (which is used internally by the
	 * deprecated {@link Image#getScaledInstance(int, int, int)} method in the
	 * JDK that imgscalr is meant to replace).
	 * <p/>
	 * This ConvolveOp uses a 3x3 kernel with the values:
	 * <table cellpadding="4" border="1">
	 * <tr>
	 * <td>.0f</td>
	 * <td>.08f</td>
	 * <td>.0f</td>
	 * </tr>
	 * <tr>
	 * <td>.08f</td>
	 * <td>.68f</td>
	 * <td>.08f</td>
	 * </tr>
	 * <tr>
	 * <td>.0f</td>
	 * <td>.08f</td>
	 * <td>.0f</td>
	 * </tr>
	 * </table>
	 * <p/>
	 * For those that have worked with ConvolveOps before, this Op uses the
	 * {@link ConvolveOp#EDGE_NO_OP} instruction to not process the pixels along
	 * the very edge of the image (otherwise EDGE_ZERO_FILL would create a
	 * black-border around the image). If you have not worked with a ConvolveOp
	 * before, it just means this default OP will "do the right thing" and not
	 * give you garbage results.
	 * <p/>
	 * This ConvolveOp uses no {@link RenderingHints} values as internally the
	 * {@link ConvolveOp} class only uses hints when doing a color conversion
	 * between the source and destination {@link BufferedImage} targets.
	 * imgscalr allows the {@link ConvolveOp} to create its own destination
	 * image every time, so no color conversion is ever needed and thus no
	 * hints.
	 * <h3>Performance</h3>
	 * Use of this (and other) {@link ConvolveOp}s are hardware accelerated when
	 * possible. For more information on if your image op is hardware
	 * accelerated or not, check the source code of the underlying JDK class
	 * that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 * <h3>Known Issues</h3>
	 * In all versions of Java (tested up to Java 7 preview Build 131), running
	 * this op against a GIF with transparency and attempting to save the
	 * resulting image as a GIF results in a corrupted/empty file. The file must
	 * be saved out as a PNG to maintain the transparency.
	 */
	public static final ConvolveOp OP_ANTIALIAS = new ConvolveOp(
			new Kernel(3, 3, new float[] { .0f, .08f, .0f, .08f, .68f, .08f,
					.0f, .08f, .0f }), ConvolveOp.EDGE_NO_OP, null);

	/**
	 * Static initializer used to prepare some of the variables used by this
	 * class.
	 */
	static {
		log("Debug output ENABLED");
	}

	/**
	 * Used to define the different scaling hints that the algorithm can use.
	 */
	public static enum Method {
		/**
		 * Used to indicate that the scaling implementation should decide which
		 * method to use in order to get the best looking scaled image in the
		 * least amount of time.
		 * <p/>
		 * The scaling algorithm will use the
		 * {@link Scalr#THRESHOLD_QUALITY_BALANCED} or
		 * {@link Scalr#THRESHOLD_BALANCED_SPEED} thresholds as cut-offs to
		 * decide between selecting the <code>QUALITY</code>,
		 * <code>BALANCED</code> or <code>SPEED</code> scaling algorithms.
		 * <p/>
		 * By default the thresholds chosen will give nearly the best looking
		 * result in the fastest amount of time. We intend this method to work
		 * for 80% of people looking to scale an image quickly and get a good
		 * looking result.
		 */
		AUTOMATIC,
		/**
		 * Used to indicate that the scaling implementation should scale as fast
		 * as possible and return a result. For smaller images (800px in size)
		 * this can result in noticeable aliasing but it can be a few magnitudes
		 * times faster than using the QUALITY method.
		 */
		SPEED,
		/**
		 * Used to indicate that the scaling implementation should use a scaling
		 * operation balanced between SPEED and QUALITY. Sometimes SPEED looks
		 * too low quality to be useful (e.g. text can become unreadable when
		 * scaled using SPEED) but using QUALITY mode will increase the
		 * processing time too much. This mode provides a "better than SPEED"
		 * quality in a "less than QUALITY" amount of time.
		 */
		BALANCED,
		/**
		 * Used to indicate that the scaling implementation should do everything
		 * it can to create as nice of a result as possible. This approach is
		 * most important for smaller pictures (800px or smaller) and less
		 * important for larger pictures as the difference between this method
		 * and the SPEED method become less and less noticeable as the
		 * source-image size increases. Using the AUTOMATIC method will
		 * automatically prefer the QUALITY method when scaling an image down
		 * below 800px in size.
		 */
		QUALITY;
	}

	/**
	 * Used to define the different modes of resizing that the algorithm can
	 * use.
	 */
	public static enum Mode {
		/**
		 * Used to indicate that the scaling implementation should calculate
		 * dimensions for the resultant image by looking at the image's
		 * orientation and generating proportional dimensions that best fit into
		 * the target width and height given
		 *
		 * See "Image Proportions" in the {@link Scalr} class description for
		 * more detail.
		 */
		AUTOMATIC,
		/**
		 * Used to indicate that the scaling implementation should calculate
		 * dimensions for the resultant image that best-fit within the given
		 * width, regardless of the orientation of the image.
		 */
		FIT_TO_WIDTH,
		/**
		 * Used to indicate that the scaling implementation should calculate
		 * dimensions for the resultant image that best-fit within the given
		 * height, regardless of the orientation of the image.
		 */
		FIT_TO_HEIGHT;
	}

	/**
	 * Used to define the different types of rotations that can be applied to an
	 * image during a resize operation.
	 */
	public static enum Rotation {
		/**
		 * No rotation should be applied to the image.
		 */
		NONE,
		/**
		 * Rotate the image 90-degrees clockwise (to the right). This is
		 * equivalent to a quarter-turn of the image to the right.
		 */
		CLOCKWISE,
		/**
		 * Rotate the image negative 90-degrees counter-clockwise (to the left).
		 * This is equivalent to a quarter-turn of the image to the left. This
		 * is also equivalent to a 270-degree rotation to the right.
		 */
		COUNTER_CLOCKWISE,
		/**
		 * Flip the image. This is equivalent to rotating an image 180 degrees
		 * (right or left, it doesn't matter).
		 */
		FLIP;
	}

	/**
	 * Threshold (in pixels) at which point the scaling operation using the
	 * {@link Method#AUTOMATIC} method will decide if a {@link Method#BALANCED}
	 * method will be used (if smaller than or equal to threshold) or a
	 * {@link Method#SPEED} method will be used (if larger than threshold).
	 * <p/>
	 * The bigger the image is being scaled to, the less noticeable degradations
	 * in the image becomes and the faster algorithms can be selected.
	 * <p/>
	 * The value of this threshold (1600) was chosen after visual, by-hand, A/B
	 * testing between different types of images scaled with this library; both
	 * photographs and screenshots. It was determined that images below this
	 * size need to use a {@link Method#BALANCED} scale method to look decent in
	 * most all cases while using the faster {@link Method#SPEED} method for
	 * images bigger than this threshold showed no noticeable degradation over a
	 * <code>BALANCED</code> scale.
	 */
	public static final int THRESHOLD_BALANCED_SPEED = 1600;

	/**
	 * Threshold (in pixels) at which point the scaling operation using the
	 * {@link Method#AUTOMATIC} method will decide if a {@link Method#QUALITY}
	 * method will be used (if smaller than or equal to threshold) or a
	 * {@link Method#BALANCED} method will be used (if larger than threshold).
	 * <p/>
	 * The bigger the image is being scaled to, the less noticeable degradations
	 * in the image becomes and the faster algorithms can be selected.
	 * <p/>
	 * The value of this threshold (800) was chosen after visual, by-hand, A/B
	 * testing between different types of images scaled with this library; both
	 * photographs and screenshots. It was determined that images below this
	 * size need to use a {@link Method#QUALITY} scale method to look decent in
	 * most all cases while using the faster {@link Method#BALANCED} method for
	 * images bigger than this threshold showed no noticeable degradation over a
	 * <code>QUALITY</code> scale.
	 */
	public static final int THRESHOLD_QUALITY_BALANCED = 800;

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> and apply the given
	 * {@link BufferedImageOp}s (if any) to the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC}, mode of
	 * {@link Mode#AUTOMATIC} and rotation of {@link Rotation#NONE} are used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, int targetSize,
			BufferedImageOp... ops) throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, Rotation.NONE,
				targetSize, targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code>, apply the given
	 * {@link BufferedImageOp}s (if any) and then apply the given rotation to
	 * the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} and mode of
	 * {@link Mode#AUTOMATIC} are used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Rotation rotation,
			int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, rotation,
				targetSize, targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> using the given scaling
	 * method and apply the given {@link BufferedImageOp}s (if any) to the
	 * result before returning it.
	 * <p/>
	 * A mode of {@link Mode#AUTOMATIC} and rotation of {@link Rotation#NONE}
	 * are used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Method
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, scalingMethod, Mode.AUTOMATIC, Rotation.NONE,
				targetSize, targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> using the given scaling
	 * method, apply the given {@link BufferedImageOp}s (if any) and then apply
	 * the given rotation to the result before returning it.
	 * <p/>
	 * A mode of {@link Mode#AUTOMATIC} is used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Rotation rotation, int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, scalingMethod, Mode.AUTOMATIC, rotation, targetSize,
				targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> (or fitting the image to
	 * the given WIDTH or HEIGHT explicitly, depending on the {@link Mode}
	 * specified) and then apply the given {@link BufferedImageOp}s (if any) to
	 * the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} and rotation of
	 * {@link Rotation#NONE} are used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Mode
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Mode resizeMode,
			int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, resizeMode, Rotation.NONE,
				targetSize, targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> (or fitting the image to
	 * the given WIDTH or HEIGHT explicitly, depending on the {@link Mode}
	 * specified), apply the given {@link BufferedImageOp}s (if any) and then
	 * apply the given rotation to the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} is used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Mode
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Mode resizeMode,
			Rotation rotation, int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, resizeMode, rotation, targetSize,
				targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> (or fitting the image to
	 * the given WIDTH or HEIGHT explicitly, depending on the {@link Mode}
	 * specified) using the given scaling method and apply the given
	 * {@link BufferedImageOp}s (if any) to the result before returning it.
	 * <p/>
	 * A rotation of {@link Rotation#NONE} is used.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throw IllegalArgumentException if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Mode
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Mode resizeMode, int targetSize, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, scalingMethod, resizeMode, Rotation.NONE,
				targetSize, targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to a width and
	 * height no bigger than <code>targetSize</code> (or fitting the image to
	 * the given WIDTH or HEIGHT explicitly, depending on the {@link Mode}
	 * specified) using the given scaling method, apply the given
	 * {@link BufferedImageOp}s (if any) and apply the given rotation to the
	 * result before returning it.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetSize
	 *            The target width and height (square) that you wish the image
	 *            to fit within.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throw IllegalArgumentException if <code>targetSize</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Mode
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Mode resizeMode, Rotation rotation, int targetSize,
			BufferedImageOp... ops) throws IllegalArgumentException {
		return resize(src, scalingMethod, resizeMode, rotation, targetSize,
				targetSize, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height and apply the given {@link BufferedImageOp}s (if any) to
	 * the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC}, mode of
	 * {@link Mode#AUTOMATIC} and rotation of {@link Rotation#NONE} are used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, int targetWidth,
			int targetHeight, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, Rotation.NONE,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height, apply the given {@link BufferedImageOp}s (if any) and
	 * apply the given rotation to the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} and mode of
	 * {@link Mode#AUTOMATIC} are used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Rotation rotation,
			int targetWidth, int targetHeight, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, Mode.AUTOMATIC, rotation,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height using the given scaling method and apply the given
	 * {@link BufferedImageOp}s (if any) to the result before returning it.
	 * <p/>
	 * A mode of {@link Mode#AUTOMATIC} and rotation of {@link Rotation#NONE}
	 * are used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image no bigger than the given width
	 *         and height.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Method
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			int targetWidth, int targetHeight, BufferedImageOp... ops) {
		return resize(src, scalingMethod, Mode.AUTOMATIC, Rotation.NONE,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height using the given scaling method, apply the given
	 * {@link BufferedImageOp}s (if any) and apply the given rotation to the
	 * result before returning it.
	 * <p/>
	 * A mode of {@link Mode#AUTOMATIC} is used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image no bigger than the given width
	 *         and height.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Rotation rotation, int targetWidth, int targetHeight,
			BufferedImageOp... ops) {
		return resize(src, scalingMethod, Mode.AUTOMATIC, rotation,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height (or fitting the image to the given WIDTH or HEIGHT
	 * explicitly, depending on the {@link Mode} specified) and then apply the
	 * given {@link BufferedImageOp}s (if any) to the result before returning
	 * it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} and rotation of
	 * {@link Rotation#NONE} are used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Mode
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Mode resizeMode,
			int targetWidth, int targetHeight, BufferedImageOp... ops)
			throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, resizeMode, Rotation.NONE,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height (or fitting the image to the given WIDTH or HEIGHT
	 * explicitly, depending on the {@link Mode} specified), apply the given
	 * {@link BufferedImageOp}s (if any) and then apply the given rotation to
	 * the result before returning it.
	 * <p/>
	 * A scaling method of {@link Method#AUTOMATIC} is used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image with either a width or height of
	 *         the given target size.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Mode
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Mode resizeMode,
			Rotation rotation, int targetWidth, int targetHeight,
			BufferedImageOp... ops) throws IllegalArgumentException {
		return resize(src, Method.AUTOMATIC, resizeMode, rotation, targetWidth,
				targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height (or fitting the image to the given WIDTH or HEIGHT
	 * explicitly, depending on the {@link Mode} specified) using the given
	 * scaling method and apply the given {@link BufferedImageOp}s (if any) to
	 * the result before returning it.
	 * <p/>
	 * A rotation of {@link Rotation#NONE} is used.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image no bigger than the given width
	 *         and height.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Mode
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Mode resizeMode, int targetWidth, int targetHeight,
			BufferedImageOp... ops) throws IllegalArgumentException {
		return resize(src, scalingMethod, resizeMode, Rotation.NONE,
				targetWidth, targetHeight, ops);
	}

	/**
	 * Resize a given image (maintaining its original proportion) to the target
	 * width and height (or fitting the image to the given WIDTH or HEIGHT
	 * explicitly, depending on the {@link Mode} specified) using the given
	 * scaling method, apply the given {@link BufferedImageOp}s (if any) and
	 * apply the given rotation to the result before returning it.
	 * <p/>
	 * <strong>TIP</strong>: See the class description to understand how this
	 * class handles recalculation of the <code>targetWidth</code> or
	 * <code>targetHeight</code> depending on the image's orientation in order
	 * to maintain the original proportion.
	 * <p/>
	 * <strong>Performance</strong>: Not all {@link BufferedImageOp}s are
	 * hardware accelerated operations, but many of the most popular (like
	 * {@link ConvolveOp}) are. For more information on if your image op is
	 * hardware accelerated or not, check the source code of the underlying JDK
	 * class that actually executes the Op code, <a href=
	 * "http://www.docjar.com/html/api/sun/awt/image/ImagingLib.java.html"
	 * >sun.awt.image.ImagingLib</a>.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param scalingMethod
	 *            The method used for scaling the image; preferring speed to
	 *            quality or a balance of both.
	 * @param resizeMode
	 *            Used to indicate how imgscalr should calculate the final
	 *            target size for the image, either fitting the image to the
	 *            given width ({@link Mode#FIT_TO_WIDTH}) or fitting the image
	 *            to the given height ({@link Mode#FIT_TO_HEIGHT}). If
	 *            {@link Mode#AUTOMATIC} is passed in, imgscalr will calculate
	 *            proportional dimensions for the scaled image based on its
	 *            orientation (landscape, square or portrait). Unless you have
	 *            very specific size requirements, most of the time you just
	 *            want to use {@link Mode#AUTOMATIC} to "do the right thing".
	 * @param rotation
	 *            The rotation to be applied to the scaled image right before it
	 *            is returned.
	 * @param targetWidth
	 *            The target width that you wish the image to have.
	 * @param targetHeight
	 *            The target height that you wish the image to have.
	 * @param ops
	 *            Zero or more optional image operations (e.g. sharpen, blur,
	 *            etc.) that can be applied to the final result before returning
	 *            the image.
	 *
	 * @return the proportionally scaled image no bigger than the given width
	 *         and height.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>src</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>scalingMethod</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>resizeMode</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>rotation</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>targetWidth</code> is &lt; 0 or if
	 *             <code>targetHeight</code> is &lt; 0.
	 *
	 * @see Method
	 * @see Mode
	 * @see Rotation
	 * @see #OP_ANTIALIAS
	 */
	public static BufferedImage resize(BufferedImage src, Method scalingMethod,
			Mode resizeMode, Rotation rotation, int targetWidth,
			int targetHeight, BufferedImageOp... ops)
			throws IllegalArgumentException {
		if (src == null)
			throw new IllegalArgumentException(
					"src cannot be null, a valid BufferedImage instance must be provided.");
		if (scalingMethod == null)
			throw new IllegalArgumentException(
					"scalingMethod cannot be null. A good default value is Method.AUTOMATIC.");
		if (resizeMode == null)
			throw new IllegalArgumentException(
					"resizeMode cannot be null. A good default value is Mode.AUTOMATIC.");
		if (rotation == null)
			throw new IllegalArgumentException(
					"rotation cannot be null. A good default value is Rotation.NONE.");
		if (targetWidth < 0)
			throw new IllegalArgumentException("targetWidth must be >= 0");
		if (targetHeight < 0)
			throw new IllegalArgumentException("targetHeight must be >= 0");

		BufferedImage result = null;

		long startTime = System.currentTimeMillis();

		// Clear the 'null' ops arg passed in from other API methods
		if (ops != null && ops.length == 1 && ops[0] == null)
			ops = null;

		int currentWidth = src.getWidth();
		int currentHeight = src.getHeight();

		// <= 1 is a square or landscape-oriented image, > 1 is a portrait.
		float ratio = ((float) currentHeight / (float) currentWidth);

		if (DEBUG)
			log("START Resizing Source Image [size=%dx%d, mode=%s, orientation=%s, ratio(H/W)=%f] to [targetSize=%dx%d]",
					currentWidth, currentHeight, resizeMode,
					(ratio <= 1 ? "Landscape/Square" : "Portrait"), ratio,
					targetWidth, targetHeight);

		/*
		 * The proportion of the picture must be honored, the way that is done
		 * is to figure out if the image is in a LANDSCAPE/SQUARE or PORTRAIT
		 * orientation and depending on its orientation, use the primary
		 * dimension (width for LANDSCAPE/SQUARE and height for PORTRAIT) to
		 * recalculate the alternative (height and width respectively) value
		 * that adheres to the existing ratio. This helps make life easier for
		 * the caller as they don't need to pre-compute proportional dimensions
		 * before calling the API, they can just specify the dimensions they
		 * would like the image to roughly fit within and it will do the right
		 * thing without mangling the result.
		 */
		if ((ratio <= 1 && resizeMode == Mode.AUTOMATIC)
				|| (resizeMode == Mode.FIT_TO_WIDTH)) {
			// First make sure we need to do any work in the first place
			if (targetWidth == src.getWidth())
				return src;

			// Save for detailed logging (this is cheap).
			int originalTargetHeight = targetHeight;

			/*
			 * Landscape or Square Orientation: Ignore the given height and
			 * re-calculate a proportionally correct value based on the
			 * targetWidth.
			 */
			targetHeight = Math.round((float) targetWidth * ratio);

			if (DEBUG && originalTargetHeight != targetHeight)
				log("Auto-Corrected targetHeight [from=%d to=%d] to honor image proportions",
						originalTargetHeight, targetHeight);
		} else {
			// First make sure we need to do any work in the first place
			if (targetHeight == src.getHeight())
				return src;

			// Save for detailed logging (this is cheap).
			int originalTargetWidth = targetWidth;

			/*
			 * Portrait Orientation: Ignore the given width and re-calculate a
			 * proportionally correct value based on the targetHeight.
			 */
			targetWidth = Math.round((float) targetHeight / ratio);

			if (DEBUG && originalTargetWidth != targetWidth)
				log("Auto-Corrected targetWidth [from=%d to=%d] to honor image proportions",
						originalTargetWidth, targetWidth);
		}

		// If AUTOMATIC was specified, determine the real scaling method.
		if (scalingMethod == Scalr.Method.AUTOMATIC)
			scalingMethod = determineScalingMethod(targetWidth, targetHeight,
					ratio);

		if (DEBUG)
			log("Scaling Image to [size=%dx%d] using the %s method...",
					targetWidth, targetHeight, scalingMethod);

		// Now we scale the image
		if (scalingMethod == Scalr.Method.SPEED) {
			result = scaleImage(src, targetWidth, targetHeight,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		} else if (scalingMethod == Scalr.Method.BALANCED) {
			result = scaleImage(src, targetWidth, targetHeight,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		} else if (scalingMethod == Scalr.Method.QUALITY) {
			/*
			 * If we are scaling up (in either width or height - since we know
			 * the image will stay proportional we just check if either are
			 * being scaled up), directly using a single BICUBIC will give us
			 * better results then using Chris Campbell's incremental scaling
			 * operation (and take a lot less time). If we are scaling down, we
			 * must use the incremental scaling algorithm for the best result.
			 */
			if (targetWidth > currentWidth || targetHeight > currentHeight) {
				log("\tQUALITY Up-scale, single BICUBIC scaling will be used...");

				/*
				 * BILINEAR and BICUBIC look similar the smaller the scale jump
				 * upwards is, if the scale is larger BICUBIC looks sharper and
				 * less fuzzy. But most importantly we have to use BICUBIC to
				 * match the contract of the QUALITY rendering method. This note
				 * is just here for anyone reading the code and wondering how
				 * they can speed their own calls up.
				 */
				result = scaleImage(src, targetWidth, targetHeight,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			} else {
				log("\tQUALITY Down-scale, incremental scaling will be used...");

				/*
				 * Originally we wanted to use BILINEAR interpolation here
				 * because it takes 1/3rd the time that the BICUBIC
				 * interpolation does, however, when scaling large images down
				 * to most sizes bigger than a thumbnail we witnessed noticeable
				 * "softening" in the resultant image with BILINEAR that would
				 * be unexpectedly annoying to a user expecting a "QUALITY"
				 * scale of their original image. Instead BICUBIC was chosen to
				 * honor the contract of a QUALITY scale of the original image.
				 */
				result = scaleImageIncrementally(src, targetWidth,
						targetHeight,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			}
		}

		// Apply the image ops if any were provided
		if (ops != null && ops.length > 0) {
			if (DEBUG)
				log("Applying %d Image Ops to Result", ops.length);

			for (BufferedImageOp op : ops) {
				// In case a null op was passed in, skip it instead of dying
				if (op == null)
					continue;

				long opStartTime = System.currentTimeMillis();
				Rectangle2D dims = op.getBounds2D(result);

				/*
				 * We must manually create the target image; we cannot rely on
				 * the null-dest filter() method to create a valid destination
				 * for us thanks to this JDK bug that has been filed for almost
				 * a decade:
				 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4965606
				 */
				BufferedImage dest = new BufferedImage((int) Math.round(dims
						.getWidth()), (int) Math.round(dims.getHeight()),
						result.getType());

				result = op.filter(result, dest);

				if (DEBUG)
					log("\tOp Applied in %d ms, Resultant Image [width=%d, height=%d], Op: %s",
							(System.currentTimeMillis() - opStartTime),
							result.getWidth(), result.getHeight(), op);
			}
		}

		// Perform the rotation if one was requested
		if (rotation != Rotation.NONE) {
			if (DEBUG)
				log("Applying %s rotation to image...", rotation);

			long rotStartTime = System.currentTimeMillis();

			/*
			 * A 90 or -90 degree rotation will cause the height and width to
			 * flip-flop from the original image to the rotated one.
			 *
			 * Given that MOST rotations will typically be some form of a
			 * 90-degree rotation (portrait to landscape, etc.) just assume that
			 * here and correct it below in the switch statement if need be.
			 */
			int newWidth = result.getHeight();
			int newHeight = result.getWidth();

			/*
			 * We create a transform per operation request as (oddly enough) it
			 * ends up being faster for the VM to create, use and destroy these
			 * instances than it is to re-use a single AffineTransform
			 * per-thread via the AffineTransform.setTo(...) methods which was
			 * my first choice (less object creation).
			 *
			 * Unfortunately this introduces the need for ThreadLocal instances
			 * of AffineTransforms to avoid race conditions where two or more
			 * resize threads are manipulating the same transform before
			 * applying it.
			 *
			 * ThreadLocals are one of the #1 reasons for memory leaks in server
			 * applications and since we have no nice way to hook into the
			 * init/destroy Servlet cycle or any other initialization cycle for
			 * this library to automatically call ThreadLocal.remove() to avoid
			 * the memory leak, it would have made using this library *safely*
			 * on the server side much harder.
			 *
			 * So we opt for creating individual transforms per rotation op and
			 * let the VM clean them up in a GC.
			 */
			AffineTransform tx = new AffineTransform();

			switch (rotation) {
			case CLOCKWISE:
				// Reminder: newWidth == result.getHeight() at this point
				tx.translate(newWidth, 0);
				tx.rotate(Math.toRadians(90));

				break;

			case COUNTER_CLOCKWISE:
				// Reminder: newHeight == result.getWidth() at this point
				tx.translate(0, newHeight);
				tx.rotate(Math.toRadians(-90));
				break;

			case FLIP:
				/*
				 * This is the one rotation case where the new width and height
				 * will be the same as the original image, so reset the values
				 * from the defaults we set above.
				 */
				newWidth = result.getWidth();
				newHeight = result.getHeight();

				tx.translate(newWidth, newHeight);
				tx.rotate(Math.toRadians(180));
				break;
			}

			/*
			 * Create our target image we will render the rotated result to. At
			 * this point the resultant image has already been put into the best
			 * image type so we can just copy that without trying to
			 * re-determine the most effective image type like scaleImage(...)
			 * has to do.
			 */
			BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight,
					result.getType());
			Graphics2D g2d = (Graphics2D) rotatedImage.createGraphics();

			/*
			 * Render the resultant image to our new rotatedImage buffer,
			 * applying the AffineTransform that we calculated above during
			 * rendering so the pixels from the old position to the new
			 * transposed positions are mapped correctly.
			 */
			g2d.drawImage(result, tx, null);
			g2d.dispose();

			/*
			 * Before re-assigning the new result to be returned to our rotated
			 * image, explicitly notify the VM that you are done with any
			 * resources being used by the old resultant image that we don't
			 * need anymore.
			 */
			result.flush();

			// Reassign the result to our rotated image before returning it.
			result = rotatedImage;

			if (DEBUG)
				log("\t%s Rotation Applied in %d ms, Resultant Image [width=%d, height=%d]",
						rotation, (System.currentTimeMillis() - rotStartTime),
						result.getWidth(), result.getHeight());
		}

		if (DEBUG) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			log("END Source Image Scaled from [%dx%d] to [%dx%d] and %d BufferedImageOp(s) Applied in %d ms",
					currentWidth, currentHeight, result.getWidth(),
					result.getHeight(), (ops == null ? 0 : ops.length),
					elapsedTime);
		}

		return result;
	}

	/**
	 * Helper method used to ensure a message is loggable before it is logged
	 * and then pre-pend a universal prefix to all log messages generated by
	 * this library to make the log entries easy to parse visually or
	 * programmatically.
	 * <p/>
	 * If a message cannot be logged (logging is disabled) then this method
	 * returns immediately.
	 * <p/>
	 * <strong>NOTE</strong>: Because Java will auto-box primitive arguments
	 * into Objects when building out the <code>params</code> array, care should
	 * be taken not to call this method with primitive values unless
	 * {@link #DEBUG} is <code>true</code>; otherwise the VM will be spending
	 * time performing unnecessary auto-boxing calculations.
	 *
	 * @param message
	 *            The log message in <a href=
	 *            "http://download.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax"
	 *            >format string syntax</a> that will be logged.
	 * @param params
	 *            The parameters that will be swapped into all the place holders
	 *            in the original messages before being logged.
	 *
	 * @see #LOG_PREFIX
	 */
	protected static void log(String message, Object... params) {
		if (DEBUG)
			System.out.printf(LOG_PREFIX + message + '\n', params);
	}

	/**
	 * Used to determine the scaling {@link Method} that is best suited for
	 * scaling the image to the targeted dimensions.
	 * <p/>
	 * This method is intended to be used to select a specific scaling
	 * {@link Method} when a {@link Method#AUTOMATIC} method is specified. This
	 * method utilizes the {@link #THRESHOLD_QUALITY_BALANCED} and
	 * {@link #THRESHOLD_BALANCED_SPEED} thresholds when selecting which method
	 * should be used by comparing the primary dimension (width or height)
	 * against the threshold and seeing where the image falls. The primary
	 * dimension is determined by looking at the orientation of the image:
	 * landscape or square images use their width and portrait-oriented images
	 * use their height.
	 *
	 * @param targetWidth
	 *            The target width for the scaled image.
	 * @param targetHeight
	 *            The target height for the scaled image.
	 * @param ratio
	 *            A height/width ratio used to determine the orientation of the
	 *            image so the primary dimension (width or height) can be
	 *            selected to test if it is greater than or less than a
	 *            particular threshold.
	 *
	 * @return the fastest {@link Method} suited for scaling the image to the
	 *         specified dimensions while maintaining a good-looking result.
	 */
	protected static Method determineScalingMethod(int targetWidth,
			int targetHeight, float ratio) {
		// Get the primary dimension based on the orientation of the image
		int length = (ratio <= 1 ? targetWidth : targetHeight);

		// Default to speed
		Method result = Method.SPEED;

		// Figure out which method should be used
		if (length <= THRESHOLD_QUALITY_BALANCED)
			result = Method.QUALITY;
		else if (length <= THRESHOLD_BALANCED_SPEED)
			result = Method.BALANCED;

		if (DEBUG)
			log("AUTOMATIC Scaling Method Selected [%s] for Image [size=%dx%d]",
					result.name(), targetWidth, targetHeight);

		return result;
	}

	/**
	 * Used to implement a straight-forward image-scaling operation using Java
	 * 2D.
	 * <p/>
	 * This method uses the Snoracle-encouraged method of
	 * <code>Graphics2D.drawImage(...)</code> to scale the given image with the
	 * given interpolation hint.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param targetWidth
	 *            The target width for the scaled image.
	 * @param targetHeight
	 *            The target height for the scaled image.
	 * @param interpolationHintValue
	 *            The {@link RenderingHints} interpolation value used to
	 *            indicate the method that {@link Graphics2D} should use when
	 *            scaling the image.
	 *
	 * @return the result of scaling the original <code>src</code> to the given
	 *         dimensions using the given interpolation method.
	 */
	protected static BufferedImage scaleImage(BufferedImage src,
			int targetWidth, int targetHeight, Object interpolationHintValue) {
		/*
		 * Determine the RGB-based TYPE of image (plain RGB or RGB + Alpha) that
		 * we want to render the scaled instance into. We force all rendering
		 * results into one of these two types, avoiding the case where a source
		 * image is of an unsupported (or poorly supported) format by Java2D and
		 * the written results, when attempting to re-create and write out that
		 * format, is garbage.
		 *
		 * Originally reported by Magnus Kvalheim from Movellas when scaling
		 * certain GIF and PNG images.
		 *
		 * More information about Java2D and poorly supported image types:
		 * http:/
		 * /www.mail-archive.com/java2d-interest@capra.eng.sun.com/msg05621.html
		 *
		 * Thanks to Morten Nobel for the implementation hint:
		 * http://code.google
		 * .com/p/java-image-scaling/source/browse/trunk/src/main
		 * /java/com/mortennobel/imagescaling/MultiStepRescaleOp.java
		 */
		int imageType = (src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB);

		// Setup the rendering resources to match the source image's
		BufferedImage result = new BufferedImage(targetWidth, targetHeight,
				imageType);
		Graphics2D resultGraphics = result.createGraphics();

		// Scale the image to the new buffer using the specified rendering hint.
		resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				interpolationHintValue);
		resultGraphics.drawImage(src, 0, 0, targetWidth, targetHeight, null);

		// Just to be clean, explicitly dispose our temporary graphics object
		resultGraphics.dispose();

		// Return the scaled image to the caller.
		return result;
	}

	/**
	 * Used to implement Chris Campbell's incremental-scaling algorithm: <a
	 * href="http://today.java.net/pub/a/today/2007/04/03/perils
	 * -of-image-getscaledinstance
	 * .html">http://today.java.net/pub/a/today/2007/04/03/perils
	 * -of-image-getscaledinstance.html</a>.
	 * <p/>
	 * Modifications to the original algorithm are variable names and comments
	 * added for clarity and the hard-coding of using BICUBIC interpolation as
	 * well as the explicit "flush()" operation on the interim BufferedImage
	 * instances to avoid resource leaking.
	 *
	 * @param src
	 *            The image that will be scaled.
	 * @param targetWidth
	 *            The target width for the scaled image.
	 * @param targetHeight
	 *            The target height for the scaled image.
	 * @param interpolationHintValue
	 *            The {@link RenderingHints} interpolation value used to
	 *            indicate the method that {@link Graphics2D} should use when
	 *            scaling the image.
	 *
	 * @return an image scaled to the given dimensions using the given rendering
	 *         hint.
	 */
	protected static BufferedImage scaleImageIncrementally(BufferedImage src,
			int targetWidth, int targetHeight, Object interpolationHintValue) {
		boolean hasReassignedSrc = false;
		int incrementCount = 0;
		int currentWidth = src.getWidth();
		int currentHeight = src.getHeight();

		do {
			/*
			 * If the current width is bigger than our target, cut it in half
			 * and sample again.
			 */
			if (currentWidth > targetWidth) {
				currentWidth /= 2;

				/*
				 * If we cut the width too far it means we are on our last
				 * iteration. Just set it to the target width and finish up.
				 */
				if (currentWidth < targetWidth)
					currentWidth = targetWidth;
			}

			/*
			 * If the current height is bigger than our target, cut it in half
			 * and sample again.
			 */

			if (currentHeight > targetHeight) {
				currentHeight /= 2;

				/*
				 * If we cut the height too far it means we are on our last
				 * iteration. Just set it to the target height and finish up.
				 */

				if (currentHeight < targetHeight)
					currentHeight = targetHeight;
			}

			// Render the incremental scaled image.
			BufferedImage incrementalImage = scaleImage(src, currentWidth,
					currentHeight, interpolationHintValue);

			/*
			 * Before re-assigning our interim (partially scaled)
			 * incrementalImage to be the new src image before we iterate around
			 * again to process it down further, we want to flush() the previous
			 * src image IF (and only IF) it was one of our own temporary
			 * BufferedImages created during this incremental down-sampling
			 * cycle. If it wasn't one of ours, then it was the original
			 * caller-supplied BufferedImage in which case we don't want to
			 * flush() it and just leave it alone.
			 */
			if (hasReassignedSrc)
				src.flush();

			/*
			 * Now treat our incremental partially scaled image as the src image
			 * and cycle through our loop again to do another incremental
			 * scaling of it (if necessary).
			 */
			src = incrementalImage;

			/*
			 * Keep track of us re-assigning the original caller-supplied source
			 * image with one of our interim BufferedImages so we know when to
			 * explicitly flush the interim "src" on the next cycle through.
			 */
			if (!hasReassignedSrc)
				hasReassignedSrc = true;

			// Track how many times we go through this cycle to scale the image.
			incrementCount++;
		} while (currentWidth != targetWidth || currentHeight != targetHeight);

		if (DEBUG)
			log("\tScaled Image in %d steps", incrementCount);

		/*
		 * Once the loop has exited, the src image argument is now our scaled
		 * result image that we want to return.
		 */
		return src;
	}
}