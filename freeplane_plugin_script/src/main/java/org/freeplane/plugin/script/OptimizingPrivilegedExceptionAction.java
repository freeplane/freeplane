package org.freeplane.plugin.script;

import java.security.PrivilegedExceptionAction;

class OptimizingPrivilegedExceptionAction<T> implements PrivilegedExceptionAction<T> {
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
