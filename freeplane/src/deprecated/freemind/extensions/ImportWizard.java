/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package deprecated.freemind.extensions;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.freeplane.controller.Controller;

/**
 * Converts an unqualified class name to import statements by scanning through
 * the classpath.
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version 1.0 - 6 May 1999
 */
public class ImportWizard {
	/** Stores the list of all classes in the classpath */
	public Vector CLASS_LIST = new Vector(500);
	public final String lookFor = ".xml";

	public ImportWizard() {
	}

	/**
	 * Adds the classes from the supplied directory to the class list.
	 *
	 * @param classList
	 *            the Vector to add the classes to
	 * @param currentDir
	 *            the File to recursively scan as a directory
	 * @param recursionLevel
	 *            To ensure that after a certain depth the recursive directory
	 *            search stops
	 */
	public void addClassesFromDir(final Vector classList, final File rootDir,
	                              final File currentDir,
	                              final int recursionLevel) {
		if (recursionLevel >= 6) {
			return;
		}
		final String[] files = currentDir.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String current = files[i];
				if (isInteresting(current)) {
					final String rootPath = rootDir.getPath();
					final String currentPath = currentDir.getPath();
					if (!currentPath.startsWith(rootPath)) {
						Logger.global
						    .severe("currentPath doesn't start with rootPath!\n"
						            + "rootPath: "
						            + rootPath
						            + "\n"
						            + "currentPath: " + currentPath + "\n");
					}
					else {
						current = current.substring(0, current.length()
						        - lookFor.length());
						final String packageName = currentPath
						    .substring(rootPath.length());
						String fileName;
						if (packageName.length() > 0) {
							fileName = packageName.substring(1)
							        + File.separator + current;
						}
						else {
							fileName = current;
						}
						classList.addElement(fileName);
					}
				}
				else {
					final File currentFile = new File(currentDir, current);
					if (currentFile.isDirectory()) {
						addClassesFromDir(classList, rootDir, currentFile,
						    recursionLevel + 1);
					}
				}
			}
		}
	}

	/**
	 * Adds the classes from the supplied Zip file to the class list.
	 *
	 * @param classList
	 *            the Vector to add the classes to
	 * @param classPathFile
	 *            the File to scan as a zip file
	 */
	public void addClassesFromZip(final Vector classList,
	                              final File classPathFile) {
		try {
			final ZipFile zipFile = new ZipFile(classPathFile);
			final Enumeration enumeration = zipFile.entries();
			while (enumeration.hasMoreElements()) {
				final ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
				String current = zipEntry.getName();
				if (isInteresting(current)) {
					current = current.substring(0, current.length()
					        - lookFor.length());
					classList.addElement(current);
				}
			}
		}
		catch (final Exception ex) {
			org.freeplane.main.Tools.logException(ex, "Problem opening "
			        + classPathFile + " with zip.");
		}
	}

	/** Build the list of classes */
	public void buildClassList() {
		String classPath = System.getProperty("java.class.path");
		final String classPathSeparator = File.pathSeparator;
		classPath = Controller.getResourceController().getFreemindBaseDir()
		        + classPathSeparator + classPath;
		final HashSet foundPlugins = new HashSet();
		final StringTokenizer st = new StringTokenizer(classPath,
		    classPathSeparator);
		while (st.hasMoreTokens()) {
			final String classPathEntry = st.nextToken();
			final File classPathFile = new File(classPathEntry);
			try {
				final String key = classPathFile.getCanonicalPath();
				if (foundPlugins.contains(key)) {
					continue;
				}
				foundPlugins.add(key);
			}
			catch (final IOException e) {
				org.freeplane.main.Tools.logException(e);
			}
			if (classPathFile.exists()) {
				final String lowerCaseFileName = classPathEntry.toLowerCase();
				if (lowerCaseFileName.endsWith(".jar")) {
					addClassesFromZip(CLASS_LIST, classPathFile);
				}
				else if (lowerCaseFileName.endsWith(".zip")) {
					addClassesFromZip(CLASS_LIST, classPathFile);
				}
				else if (classPathFile.isDirectory()) {
					addClassesFromDir(CLASS_LIST, classPathFile, classPathFile,
					    0);
				}
			}
		}
	}

	/**
	 */
	private boolean isInteresting(final String current) {
		final int length = current.length();
		if (length < lookFor.length()) {
			return false;
		}
		final String currentPostfix = current.substring(length
		        - lookFor.length());
		return lookFor.equalsIgnoreCase(currentPostfix);
	}
}
/*
 * $Log: ImportWizard.java,v $ Revision 1.1.4.6.2.16 2008/07/28 03:06:01
 * christianfoltin Bug fix: B19 startup fails with "Mode not available: Mindmap"
 * https : FreeMind Starter: no more statics. Revision 1.1.4.6.2.15 2008/07/18
 * 16:14:25 christianfoltin Renamed zh into zh_TW (patch from willyann Reverted
 * changes to Tools. Revision 1.1.4.6.2.14 2006/12/14 16:45:00 christianfoltin
 * Search & Replace Dialog with menu and nicer. Bug fixes... Revision
 * 1.1.4.6.2.13 2006/11/28 08:25:01 dpolivaev no message Revision 1.1.4.6.2.12
 * 2006/11/26 10:20:40 dpolivaev LocalProperties, TextResources for SHTML and
 * Bug Fixes Revision 1.1.4.6.2.11 2006/11/12 21:07:06 christianfoltin Mac bug
 * fixes (class path, error messages, directories) Revision 1.1.4.6.2.10
 * 2006/09/05 21:17:58 dpolivaev SimplyHTML
 */
