package org.freeplane.core.util;

/**
 * LogUtils stub for unit tests - replaces the real LogUtils that depends on
 * ResourceController and AWT classes unavailable in the test classpath.
 * <p>
 * All log calls are silently swallowed (or printed to stdout for visibility).
 */
public class LogUtils {

    public static void info(final String message) {
        // no-op in tests; uncomment below to see output during debugging
        // System.out.println("[INFO] " + message);
    }

    public static void warn(final String message) {
        // no-op in tests
    }

    public static void warn(final String message, final Throwable t) {
        // no-op in tests
    }

    public static void severe(final String message) {
        // no-op in tests
    }

    public static void severe(final String message, final Throwable t) {
        // no-op in tests
    }
}
