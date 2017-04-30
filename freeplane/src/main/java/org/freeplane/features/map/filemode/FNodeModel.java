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
package org.freeplane.features.map.filemode;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

/**
 * This class represents a single Node of a Tree. It contains direct handles to
 * its parent and children and to its view.
 */
class FNodeModel extends NodeModel {
	final private File file;
	final private File[] directoryFiles;
	private boolean hasChildren = false;
	private boolean hasNoChildren = false;

	public FNodeModel(final File file, final MapModel map) {
		super(map);
		this.file = file;
		directoryFiles = null;
		final String[] children = file.list();
		setFolded(children != null && children.length > 0);
	}

	public FNodeModel(final File[] directoryFiles, final MapModel map) {
		super(map);
		this.file = null;
		this.directoryFiles = directoryFiles;
		setFolded(directoryFiles.length > 0);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	private void initializeChildNodes() {
		if (super.getChildrenInternal().isEmpty() && hasChildren()) {
			try {
				final File[] files = file != null ? file.listFiles() : directoryFiles;
				if (files != null) {
					int childCount = 0;
					for (File childFile : files) {
						if (!childFile.isHidden() || file == null) {
							final FNodeModel fileNodeModel = new FNodeModel(childFile, getMap());
							NodeLinks.createLinkExtension(fileNodeModel).setHyperLink(childFile.toURI());
							fileNodeModel.setLeft(isRoot() ?  (childCount % 2 == 1) : isLeft());
							super.getChildrenInternal().add(childCount, fileNodeModel);
							childCount++;
							fileNodeModel.setParent(this);
						}
					}
				}
			}
			catch (final SecurityException se) {
			}
		}
	}

	public File getFile() {
		return file;
	}

	@Override
    public Object getUserObject() {
		if(file == null)
			return "Files";
        String name = file.getName();
        if (name.equals("")) {
            name = file.getPath();
        }
        return name;
    }
	
	@Override
	public int getChildCount(){
		if(directoryFiles != null)
			return directoryFiles.length;
		else if(hasChildren()) {
			initializeChildNodes();
			return super.getChildCount();
		}
		else {
			return 0;
		}
	}
	
	

    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
	}

	@Override
	public boolean hasChildren() {
    	if(hasChildren)
    		return true;
    	if(hasNoChildren)
    		return false;
		if (directoryFiles != null || containsFiles()){
			hasChildren = true;
			return true;
		}
		else{
			hasNoChildren = true;
			return false;
		}
	}
    
    private boolean containsFiles(){
    	if(file.isFile())
    		return false;
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(file.toURI()))) {
            return dirStream.iterator().hasNext();
        } catch (IOException e) {
			return false;
		}
    }

	@Override
	public String toString() {
		return getText();
	}
}
