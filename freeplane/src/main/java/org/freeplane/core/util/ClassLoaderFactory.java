package org.freeplane.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ClassLoaderFactory {
	private static final FilenameFilter jarFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".jar");
		}
	};

	public static URLClassLoader getClassLoaderForUserLib(){
		final List<URL> userJars = findJars(new String[]{Compat.getApplicationUserDirectory() + "/lib"});
		final URLClassLoader urlClassLoader = new URLClassLoader(userJars.toArray(new URL[userJars.size()]),
			ClassLoaderFactory.class.getClassLoader());
		return urlClassLoader;
	}

	public static List<URL> jarsInExtDir() {
		String extDirsProperty = System.getProperty("java.ext.dirs");
		final String[] strings = extDirsProperty == null ? new String[]{}: extDirsProperty.split(File.pathSeparator);
	    return findJars(strings);
	}

	public static List<URL> findJars(final String[] directories) {
		try {
	        final List<URL> urls = new ArrayList<URL>();
			for (String path : directories) {
	            File dir = new File(path);
	            if (dir.isDirectory()) {
	                for (File file : dir.listFiles(jarFileFilter)) {
	                    urls.add(file.toURI().toURL());
	                }
	            }
	        }
	        return urls;
	    } catch (MalformedURLException e) {
	        throw new RuntimeException(e);
	    }
	}

}