/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved. Redistribution
 * and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: -Redistributions of
 * source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. -Redistribution in binary form must reproduct
 * the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution. Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. This software is provided
 * "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR
 * LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR RELATING TO USE,
 * MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
 * WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 * HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE
 * USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. You acknowledge that Software is not designed,
 * licensed or intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */
/*
 * @(#)ExampleFileFilter.java 1.14 03/01/23
 */
package org.freeplane.core.ui;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about. Extensions are of the type
 * ".foo", which is typically found on Windows and Unix boxes, but not on
 * Macinthosh. Case is ignored. Example - create a new filter that filters out
 * all files but gif and jpg image files: JFileChooser chooser = new
 * JFileChooser(); ExampleFileFilter filter = new ExampleFileFilter( new
 * String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 *
 * @version 1.14 01/23/03
 * @author Jeff Dinkins
 */
public class ExampleFileFilter extends FileFilter {
	private String description = null;
	private Hashtable<String, FileFilter> filters = null;
	private String fullDescription = null;
	private String mainExtension = null;
	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are
	 * accepted.
	 */
	public ExampleFileFilter() {
		filters = new Hashtable<String, FileFilter>();
	}

	/**
	 * Creates a file filter that accepts files with the given extension.
	 * Example: new ExampleFileFilter("jpg");
	 */
	public ExampleFileFilter(final String extension) {
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new
	 * ExampleFileFilter("jpg", "JPEG Image Images"); Note that the "." before
	 * the extension is not needed. If provided, it will be ignored.
	 */
	public ExampleFileFilter(final String extension, final String description) {
		this();
		if (extension != null) {
			addExtension(extension);
		}
		if (description != null) {
			setDescription(description);
		}
	}

	/**
	 * Creates a file filter from the given string array. Example: new
	 * ExampleFileFilter(String {"gif", "jpg"}); Note that the "." before the
	 * extension is not needed adn will be ignored.
	 */
	public ExampleFileFilter(final String[] filters) {
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description.
	 * Example: new ExampleFileFilter(String {"gif", "jpg"},
	 * "Gif and JPG Images"); Note that the "." before the extension is not
	 * needed and will be ignored.
	 */
	public ExampleFileFilter(final String[] filters, final String description) {
		this();
		for (int i = 0; i < filters.length; i++) {
			addExtension(filters[i]);
		}
		if (description != null) {
			setDescription(description);
		}
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if
	 * it shouldn't. Files that begin with "." are ignored.
	 */
	@Override
	public boolean accept(final File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			final String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) {
				return true;
			};
		}
		return false;
	}

	/**
	 * Adds a filetype "dot" extension to filter against. For example: the
	 * following code will create a filter that filters out all files except
	 * those that end in ".jpg" and ".tif": ExampleFileFilter filter = new
	 * ExampleFileFilter(); filter.addExtension("jpg");
	 * filter.addExtension("tif"); Note that the "." before the extension is not
	 * needed and will be ignored.
	 */
	public void addExtension(final String extension) {
		if (filters == null) {
			filters = new Hashtable<String, FileFilter>(5);
		}
		if (mainExtension == null) {
			mainExtension = extension;
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example:
	 * "JPEG and GIF Image Files (*.jpg, *.gif)"
	 */
	@Override
	public String getDescription() {
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				fullDescription = description == null ? "(" : description + " (";
				final Enumeration<String> extensions = filters.keys();
				if (extensions != null) {
					fullDescription += "." + extensions.nextElement();
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + extensions.nextElement();
					}
				}
				fullDescription += ")";
			}
			else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Return the extension portion of the file's name .
	 */
	private String getExtension(final File f) {
		if (f != null) {
			final String filename = f.getName();
			final int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			};
		}
		return null;
	}

	public String getExtensionProposal() {
		return mainExtension;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description. Only relevant if a description was
	 * provided in the constructor or using setDescription();
	 */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 */
	public void setDescription(final String description) {
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description. Only relevant if a description was
	 * provided in the constructor or using setDescription();
	 */
	public void setExtensionListInDescription(final boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}
}
