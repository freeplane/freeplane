package org.freeplane.plugin.script;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.freeplane.api.Script;
import org.freeplane.core.util.ClassLoaderFactory;

public final class ScriptClassLoader extends URLClassLoader {
	private static final Permission ALL_PERMISSION = new AllPermission();
	private ScriptingSecurityManager securityManager = null;

	public static ScriptClassLoader createClassLoader() {
		final List<String> classpath = ScriptResources.getClasspath();
		final List<URL> urls = new ArrayList<URL>();
		for (String path : classpath) {
			urls.add(pathToUrl(path));
		}
		urls.addAll(ClassLoaderFactory.jarsInExtDir());
		ScriptClassLoader classLoader = new ScriptClassLoader(urls.toArray(new URL[urls.size()]),
				GenericScript.class.getClassLoader());
		return classLoader;
	}

	private static URL pathToUrl(String path) {
        try {
            return new File(path).toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	private ScriptClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}


	@Override
	public URL getResource(final String name) {
		return AccessController.doPrivileged(new PrivilegedAction<URL>() {
					@Override
					public URL run(){
						return superGetResource(name);
					}
				});
	}

	private URL superGetResource(String name) {
		if(name.startsWith(Script.class.getPackage().getName().replace('.', '/') + '/'))
			return Script.class.getClassLoader().getResource(name);
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
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
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
		if(System.getSecurityManager() != null)
			AccessController.checkPermission(ALL_PERMISSION);
		this.securityManager = securityManager;
	}

	public boolean implies(Permission permission) {
		return securityManager != null && securityManager.implies(permission);
	}
}
