package org.freeplane.core.security;

import java.security.AllPermission;
import java.security.Permission;

public class QuickSecurityManager extends SecurityManager {
	
	private static final String SECURITY_QUICKCHECKS_DISABLED_PROPERTY = //
			"org.freeplane.core.security.quickchecks.disabled";

	private static final boolean QUICK_CHECKS_ENABLED = ! Boolean.getBoolean(SECURITY_QUICKCHECKS_DISABLED_PROPERTY);
	
	public static final AllPermission ALL_PERMISSION = new AllPermission();
	private static final ThreadLocal<Boolean> checksActive = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.TRUE;
		}
	};
	
	@FunctionalInterface
	public interface PrivilegedSupplier<T, E extends Exception> {
		T get() throws E;
	}

	@FunctionalInterface
	public interface PrivilegedRunnable<E extends Exception>{
		void run() throws E;
	}
	
	public static <E extends Exception> Void skipChecks(PrivilegedRunnable<E> runnable) throws E {
		return skipChecks(() -> {runnable.run(); return null;});
	}

	public static <T, E extends Exception> T skipChecks(PrivilegedSupplier<T, E> supplier) throws E {
		SecurityManager securityManager = System.getSecurityManager();
		if(QUICK_CHECKS_ENABLED && securityManager != null && QuickSecurityManager.checksActive.get()) {
			securityManager.checkPermission(ALL_PERMISSION);
			try {
				QuickSecurityManager.checksActive.set(Boolean.FALSE);
				return supplier.get();
			}
			finally {
				QuickSecurityManager.checksActive.set(Boolean.TRUE);
			}
		}
		else
			return supplier.get();
	}

	public QuickSecurityManager() {
		super();
	}
	
	public void checkPermission(Permission perm) {
		if(checksActive.get())
			super.checkPermission(perm);
	}

	public void checkPermission(Permission perm, Object context) {
		if(checksActive.get())
			super.checkPermission(perm, context);
	}

	public void checkRead(String file) {
		if(checksActive.get())
			super.checkRead(file);
	}

	public void checkRead(String file, Object context) {
		if(checksActive.get())
			super.checkRead(file, context);
	}
	
}
