package org.freeplane.plugin.script;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;

final class PrivilegedURLClassLoader extends URLClassLoader {
	PrivilegedURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public URL getResource(final String name) {
		return AccessController.doPrivileged(
				new PrivilegedAction<URL>() {
					public URL run(){
						return superGetResource(name);
					}
				});
	}

	private URL superGetResource(String name) {
		return super.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(final String name) throws IOException {
		try {
			return AccessController.doPrivileged(
			        new PrivilegedExceptionAction<Enumeration<URL>>() {
			            public Enumeration<URL> run() throws IOException{
							return superGetResources(name);
			            }
			        });
		} catch (PrivilegedActionException e) {
			throw (IOException)e.getCause();
		}
	}

	private Enumeration<URL> superGetResources(String name) throws IOException {
		return super.getResources(name);
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		try {
			return AccessController.doPrivileged(
			        new PrivilegedExceptionAction<Class<?>>() {
			            public Class<?> run() throws ClassNotFoundException{
							return superLoadClass(name);
			            }
			        });
		} catch (PrivilegedActionException e) {
			throw (ClassNotFoundException)e.getCause();
		}
	}

	private Class<?> superLoadClass(final String name) throws ClassNotFoundException {
		return super.loadClass(name);
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		try {
			return AccessController.doPrivileged(
			        new PrivilegedExceptionAction<Class<?>>() {
			            public Class<?> run() throws ClassNotFoundException{
			        		return superLoadClass(name, resolve);
			            }
			        });
		} catch (PrivilegedActionException e) {
			throw (ClassNotFoundException)e.getCause();
		}
	}
	
	
	private Class<?> superLoadClass(String name, boolean resolve) throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}
	
	
}