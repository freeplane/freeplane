package org.freeplane.plugin.script;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.freeplane.plugin.script.RestrictingPolicy.RestrictingClassLoader;

final class ScriptClassLoader extends URLClassLoader {
	final private RestrictingClassLoader parent;

	ScriptClassLoader(URL[] urls, RestrictingClassLoader parent) {
		super(urls, parent);
		this.parent = parent;
	}

	@Override
	public URL getResource(final String name) {
		return AccessController.doPrivileged(
				new PrivilegedAction<URL>() {
					@Override
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
			            @Override
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
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		try {
			return AccessController.doPrivileged(
			        new PrivilegedExceptionAction<Class<?>>() {
			            @Override
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

	public void setSecurityManager(ScriptingSecurityManager securityManager) {
		parent.setSecurityManager(securityManager);
	}

	public static ScriptClassLoader createClassLoader() {
		final List<URL> urls = new ArrayList<URL>();
		for (String path : ScriptResources.getClasspath()) {
			urls.add(GenericScript.pathToUrl(path));
		}
		urls.addAll(GenericScript.jarsInExtDir());
		RestrictingClassLoader restrictingClassLoader = new RestrictingClassLoader(
		    GenericScript.class.getClassLoader());
		ScriptClassLoader classLoader = new ScriptClassLoader(urls.toArray(new URL[urls.size()]),
		    restrictingClassLoader);
		return classLoader;
	}
	
	
}