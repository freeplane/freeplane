package com.thebuzzmedia.imgscalr;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.thebuzzmedia.imgscalr.Scalr.Method;
import com.thebuzzmedia.imgscalr.Scalr.Mode;
import com.thebuzzmedia.imgscalr.Scalr.Rotation;

/**
 * Class used to provide the asynchronous versions of all the methods defined in
 * {@link Scalr} for the purpose of offering more control over the scaling and
 * ordering of a large number of scale operations.
 * <p/>
 * Given that image-scaling operations, especially when working with large
 * images, can be very hardware-intensive (both CPU and memory), in large-scale
 * deployments (e.g. a busy web application) it becomes increasingly important
 * that the scale operations performed by imgscalr be manageable so as not to
 * fire off too many simultaneous operations that the JVM's heap explodes and
 * runs out of memory.
 * <p/>
 * Up until now it was left to the caller to implement their own serialization
 * or limiting logic to handle these use-cases, but it was determined that this
 * requirement be common enough that it should be integrated directly into the
 * imgscalr library for everyone to benefit from.
 * <p/>
 * Every method in this class wraps the mirrored calls in the {@link Scalr}
 * class in new {@link Callable} instances that are submitted to an internal
 * {@link ExecutorService} for execution at a later date. A {@link Future} is
 * returned to the caller representing the task that will perform the scale
 * operation. {@link Future#get()} or {@link Future#get(long, TimeUnit)} can be
 * used to block on the returned <code>Future</code>, waiting for the scale
 * operation to complete and return the resultant {@link BufferedImage}.
 * <p/>
 * This design provides the following features:
 * <ul>
 * <li>Non-blocking, asynchronous scale operations that can continue execution
 * while waiting on the scaled result.</li>
 * <li>Serialize all scale requests down into a maximum number of
 * <em>simultaneous</em> scale operations with no additional/complex logic. The
 * number of simultaneous scale operations is caller-configurable so as best to
 * optimize the host system (e.g. 1 scale thread per core).</li>
 * <li>No need to worry about overloading the host system with too many scale
 * operations, they will simply queue up in this class and execute in-order.</li>
 * <li>Synchronous/blocking behavior can still be achieved by calling
 * <code>get()</code> or <code>get(long, TimeUnit)</code> immediately on the
 * returned {@link Future} from any of the methods below.</li>
 * </ul>
 * 
 * This class also allows callers to provide their own (custom)
 * {@link ExecutorService} for processing scale operations for maximum
 * flexibility; otherwise this class utilizes a fixed {@link ThreadPoolExecutor}
 * via {@link Executors#newFixedThreadPool(int)} that will create the given
 * number of threads and let them sit idle, waiting for work.
 * <h3>Performance</h3>
 * When tuning this class for optimal performance, benchmarking your particular
 * hardware is the best approach. For some rough guidelines though, there are
 * two resources you want to watch closely:
 * <ol>
 * <li>JVM Heap Memory (Assume physical machine memory is always sufficiently
 * large)</li>
 * <li># of CPU Cores</li>
 * </ol>
 * You never want to allocate more scaling threads than you have CPU cores and
 * on a sufficiently busy host where some of the cores may be busy running a
 * database or a web server, you will want to allocate even less scaling
 * threads.
 * <p/>
 * So as a maximum you would never want more scaling threads than CPU cores in
 * any situation and less so on a busy server.
 * <p/>
 * If you allocate more threads than you have available CPU cores, your scaling
 * operations will slow down as the CPU will spend a considerable amount of time
 * context-switching between threads on the same core trying to finish all the
 * tasks in parallel. You might still be tempted to do this because of the I/O
 * delay some threads will encounter reading images off disk, but when you do
 * your own benchmarking you'll likely find (as I did) that the actual disk I/O
 * necessary to pull the image data off disk is a much smaller portion of the
 * execution time than the actual scaling operations.
 * <p/>
 * If you are executing on a storage medium that is unexpectedly slow and I/O is
 * a considerable portion of the scaling operation, feel free to try using more
 * threads than CPU cores to see if that helps; but in most normal cases, it
 * will only slow down all other parallel scaling operations.
 * <p/>
 * As for memory, every time an image is scaled it is decoded into a
 * {@link BufferedImage} and stored in the JVM Heap space (decoded image
 * instances are always larger than the source images on-disk). For larger
 * images, that can use up quite a bit of memory. You will need to benchmark
 * your particular use-cases on your hardware to get an idea of where the sweet
 * spot is for this; if you are operating within tight memory bounds, you may
 * want to limit simultaneous scaling operations to 1 or 2 regardless of the
 * number of cores just to avoid having too many {@link BufferedImage} instances
 * in JVM Heap space at the same time.
 * <p/>
 * These are rough metrics and behaviors to give you an idea of how best to tune
 * this class for your deployment, but nothing can replacement writing a small
 * Java class that scales a handful of images in a number of different ways and
 * testing that directly on your deployment hardware. *
 * <h3>Resource Overhead</h3>
 * The {@link ExecutorService} utilized by this class won't be initialized until
 * the class is referenced for the first time or explicitly set with one of the
 * setter methods. More specifically, if you have no need for asynchronous image
 * processing offered by this class, you don't need to worry about wasted
 * resources or hanging/idle threads as they will never be created if you never
 * reference this class.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @since 3.2
 */
public class AsyncScalr {
	/**
	 * Default thread count used to initialize the internal
	 * {@link ExecutorService} if a count isn't specified via
	 * {@link #setServiceThreadCount(int)} before this class is used.
	 * <p/>
	 * Default value is <code>2</code>.
	 */
	public static final int DEFAULT_THREAD_COUNT = 2;

	private static ExecutorService service;

	/**
	 * Used to init the internal service with a 2-threaded, fixed thread pool if
	 * a custom one is not specified with either of the <code>init</code>
	 * methods.
	 */
	static {
		setServiceThreadCount(DEFAULT_THREAD_COUNT);
	}

	/**
	 * Used to get access to the internal {@link ExecutorService} used by this
	 * class to process scale operations.
	 * <p/>
	 * <strong>NOTE</strong>: You will need to explicitly shutdown any service
	 * currently set on this class before the host JVM exits <em>unless</em> you
	 * have passed in a custom {@link ExecutorService} that specifically
	 * creates/uses daemon threads (which will exit immediately).
	 * <p/>
	 * You can call {@link ExecutorService#shutdown()} to wait for all scaling
	 * operations to complete first or call
	 * {@link ExecutorService#shutdownNow()} to kill any in-process operations
	 * and purge all pending operations before exiting.
	 * 
	 * @return the current {@link ExecutorService} used by this class to process
	 *         scale operations.
	 */
	public static ExecutorService getService() {
		return service;
	}

	/**
	 * Used to initialize the internal {@link ExecutorService} which runs tasks
	 * generated by this class with the given service.
	 * <p/>
	 * <strong>NOTE</strong>: This operation will call
	 * {@link ExecutorService#shutdown()} on any existing
	 * {@link ExecutorService} currently set on this class. This means this
	 * operation will block until all pending (queued) scale operations are
	 * completed.
	 * 
	 * @param service
	 *            A specific {@link ExecutorService} instance that will be used
	 *            by this class to process scale operations.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>service</code> is <code>null</code>.
	 */
	public static void setService(ExecutorService service)
			throws IllegalArgumentException {
		if (service == null)
			throw new IllegalArgumentException(
					"service cannot be null; it must be a valid ExecutorService that can execute Callable tasks created by this class.");

		/*
		 * Shutdown any existing service, waiting for the last scale ops to
		 * finish first.
		 */
		if (AsyncScalr.service != null) {
			AsyncScalr.service.shutdown();
		}

		AsyncScalr.service = service;
	}

	/**
	 * Used to adjust the fixed number of threads (min/max) used by the internal
	 * {@link ThreadPoolExecutor} to executor scale operations.
	 * <p/>
	 * The following logic is used when applying thread count changes using this
	 * method:
	 * <ol>
	 * <li>If this is the first time the service is being initialized, a new
	 * {@link ThreadPoolExecutor} is created with the given fixed number of
	 * threads.</li>
	 * <li>If a service has already been set and it is of type
	 * {@link ThreadPoolExecutor} then the methods
	 * {@link ThreadPoolExecutor#setCorePoolSize(int)} and
	 * {@link ThreadPoolExecutor#setMaximumPoolSize(int)} are used to adjust the
	 * current fixed size of the thread pool without destroying the executor and
	 * creating a new one. This avoids unnecessary garbage for the GC and helps
	 * keep the task queue intact.</li>
	 * <li>If a service has already been set, but it is not of type
	 * {@link ThreadPoolExecutor}, then it will be shutdown after all pending
	 * tasks have completed and replaced with a new instance of type
	 * {@link ThreadPoolExecutor} with the given number of fixed threads.</li>
	 * </ol>
	 * 
	 * In the case where an existing {@link ThreadPoolExecutor} thread count is
	 * adjusted, if the given <code>threadCount</code> is smaller than the
	 * current number of threads in the pool, the extra threads will only be
	 * killed after they have completed their work and become idle. No scaling
	 * operations will be interrupted.
	 * 
	 * @param threadCount
	 *            The fixed number of threads (min/max) that the service will be
	 *            configured to use to process scale operations.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>threadCount</code> is &lt; 1.
	 */
	public static void setServiceThreadCount(int threadCount)
			throws IllegalArgumentException {
		if (threadCount < 1)
			throw new IllegalArgumentException("threadCount [" + threadCount
					+ "] must be > 0.");

		// Adjust the service if we can, otherwise replace it.
		if (AsyncScalr.service instanceof ThreadPoolExecutor) {
			ThreadPoolExecutor tpe = (ThreadPoolExecutor) AsyncScalr.service;

			// Set the new min/max thread counts for the pool.
			tpe.setCorePoolSize(threadCount);
			tpe.setMaximumPoolSize(threadCount);
		} else
			setService(Executors.newFixedThreadPool(threadCount));
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final int targetSize, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Rotation rotation, final int targetSize,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, rotation, targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final int targetSize,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Rotation rotation,
			final int targetSize, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, rotation, targetSize,
						ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Mode resizeMode, final int targetSize,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, resizeMode, targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Mode resizeMode, final Rotation rotation,
			final int targetSize, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, resizeMode, rotation, targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Mode resizeMode,
			final int targetSize, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, resizeMode, targetSize,
						ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Mode resizeMode,
			final Rotation rotation, final int targetSize,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, resizeMode, rotation,
						targetSize, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final int targetWidth, final int targetHeight,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, targetWidth, targetHeight, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Rotation rotation, final int targetWidth,
			final int targetHeight, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, rotation, targetWidth, targetHeight,
						ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final int targetWidth,
			final int targetHeight, final BufferedImageOp... ops) {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, targetWidth,
						targetHeight, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Rotation rotation,
			final int targetWidth, final int targetHeight,
			final BufferedImageOp... ops) {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, rotation, targetWidth,
						targetHeight, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Mode resizeMode, final int targetWidth,
			final int targetHeight, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, resizeMode, targetWidth, targetHeight,
						ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Mode resizeMode, final Rotation rotation,
			final int targetWidth, final int targetHeight,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, resizeMode, rotation, targetWidth,
						targetHeight, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Mode resizeMode,
			final int targetWidth, final int targetHeight,
			final BufferedImageOp... ops) throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, resizeMode,
						targetWidth, targetHeight, ops);
			}
		});
	}

	public static Future<BufferedImage> resize(final BufferedImage src,
			final Method scalingMethod, final Mode resizeMode,
			final Rotation rotation, final int targetWidth,
			final int targetHeight, final BufferedImageOp... ops)
			throws IllegalArgumentException {
		return service.submit(new Callable<BufferedImage>() {
			public BufferedImage call() throws Exception {
				return Scalr.resize(src, scalingMethod, resizeMode, rotation,
						targetWidth, targetHeight, ops);
			}
		});
	}
}