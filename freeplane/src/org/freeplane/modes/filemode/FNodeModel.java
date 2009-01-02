/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes.filemode;

import java.io.File;
import java.util.List;

import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.NodeModel;

/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 */
public class FNodeModel extends NodeModel {
	final private File file;

	public FNodeModel(final File file, final MapModel map) {
		super(map);
		this.file = file;
		setFolded(!file.isFile());
	}

	@Override
	public List getChildren() {
		final List<NodeModel> children = super.getChildren();
		if (!children.isEmpty()) {
			return children;
		}
		try {
			final String[] files = file.list();
			if (files != null) {
				final String path = file.getPath();
				for (int i = 0; i < files.length; i++) {
					final File childFile = new File(path, files[i]);
					if (!childFile.isHidden()) {
						final FNodeModel fileNodeModel = new FNodeModel(childFile, getMap());
						fileNodeModel.setLeft(isNewChildLeft());
						insert(fileNodeModel, getChildCount());
					}
				}
			}
		}
		catch (final SecurityException se) {
		}
		return children;
	}


	public File getFile() {
		return file;
	}

	@Override
	public String getText() {
		String name = file.getName();
		if (name.equals("")) {
			name = "Root";
		}
		return name;
	}

	@Override
	public boolean hasChildren() {
		return !file.isFile() && !super.getChildren().isEmpty();
	}

	@Override
	public boolean isLeaf() {
		return file.isFile();
	}

	@Override
	public String toString() {
		return getText();
	}
}
