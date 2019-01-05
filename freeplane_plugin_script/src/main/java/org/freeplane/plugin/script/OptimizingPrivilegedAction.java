package org.freeplane.plugin.script;

import java.security.PrivilegedAction;

class OptimizingPrivilegedAction<T> implements PrivilegedAction<T> {
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
