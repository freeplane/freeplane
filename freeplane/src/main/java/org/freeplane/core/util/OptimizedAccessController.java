package org.freeplane.core.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class OptimizedAccessController {
	private static class OptimizingPrivilegedAction<T> implements PrivilegedAction<T> {
		private final PrivilegedAction<T> delegate;

		private OptimizingPrivilegedAction(PrivilegedAction<T> delegate) {
			this.delegate = delegate;
		}

		public static <T> OptimizingPrivilegedAction<T> of(PrivilegedAction<T> delegate){
			return new OptimizingPrivilegedAction<>(delegate);
		}

		@Override
		public T run(){
			final SecurityManager securityManager = System.getSecurityManager();
			try {
				if(securityManager != null)
					System.setSecurityManager(null);
				return delegate.run();
			}
			finally {
				if(securityManager != null)
					System.setSecurityManager(securityManager);
			}

		}
	}

	private static class OptimizingPrivilegedExceptionAction<T> implements PrivilegedExceptionAction<T> {
		private final PrivilegedExceptionAction<T> delegate;

		private OptimizingPrivilegedExceptionAction(PrivilegedExceptionAction<T> delegate) {
			this.delegate = delegate;
		}

		public static <T> OptimizingPrivilegedExceptionAction<T> of(PrivilegedExceptionAction<T> delegate){
			return new OptimizingPrivilegedExceptionAction<>(delegate);
		}

		@Override
		public T run() throws Exception {
			final SecurityManager securityManager = System.getSecurityManager();
			try {
				if(securityManager != null)
					System.setSecurityManager(null);
				return delegate.run();
			}
			finally {
				if(securityManager != null)
					System.setSecurityManager(securityManager);
			}

		}
	}


	public static <T> T doPrivileged(PrivilegedAction<T> action) {
		return AccessController.doPrivileged(OptimizingPrivilegedAction.of(action));
	}
	public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
		return AccessController.doPrivileged(OptimizingPrivilegedExceptionAction.of(action));
	}
}
