/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.mapio.mindmapmode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 * Jan 14, 2012
 */
@SuppressWarnings("deprecation")
public class MMapIO extends MapIO{


	private static MMapIO INSTANCE;

	public static MMapIO getInstance() {
		return INSTANCE;
	}

	final private MFileManager fileManager;
	final private MMapController mapController;
	private MMapIO(MFileManager urlManager, MMapController mapController) {
	    super(urlManager, mapController);
	    this.fileManager = urlManager;
	    this.mapController = mapController;
    }

	public static void install(MModeController modeController){
		if(INSTANCE != null) {
			throw new IllegalStateException("Should be used only in one mode");
		}
		MFileManager urlManager = (MFileManager) modeController.getExtension(UrlManager.class);
		MMapController mapController = (MMapController) modeController.getMapController();
		INSTANCE = new MMapIO(urlManager, mapController);
		modeController.addExtension(MapIO.class, INSTANCE);
	}
    @Override
	public void load(URL url, MapModel map) throws FileNotFoundException, IOException, XMLException, XMLParseException,
    URISyntaxException {
    	fileManager.load(url, map);
    }
    @Override
	public boolean loadCatchExceptions(URL url, MapModel map)
    {
    	return fileManager.loadCatchExceptions(url, map);
    }

    public void loadSafeAndLock(URL url, MapModel map) throws FileNotFoundException, IOException, XMLParseException,
    URISyntaxException {
    	fileManager.loadAndLock(url, map);
    }
	public void open() {
	    fileManager.open();
    }
	public MapModel openUntitledMap(File startFile) {
	    return fileManager.openUntitledMap(startFile);
    }
	public boolean save(MapModel map) {
	    return fileManager.save(map);
    }
	/**
	 * @return false is the action was cancelled
	 */
	public boolean save(MapModel map, File file) {
	    return fileManager.save(map, file);
    }
	/**
	 * @return false is the action was cancelled
	 */
	public boolean saveAs(MapModel map) {
	    return fileManager.saveAs(map);
    }
	public void writeToFile(MapModel map, File file) throws FileNotFoundException, IOException {
	    fileManager.writeToFile(map, file);
    }
	public String tryToLock(MapModel map, File file) throws Exception {
	    return fileManager.tryToLock(map, file);
    }
	public NodeModel loadTree(MapModel map, File file) throws XMLParseException, IOException {
		return fileManager.loadTree(map, file);
    }
	public MapModel newMapFromDefaultTemplate() {
		return fileManager.newMapFromDefaultTemplate();
    }
	public void newMap(URL url) throws FileNotFoundException, IOException,
	URISyntaxException, XMLException {
		mapController.newMap(url);
	}
	public MapModel createUntitledMap(URL url){
		try {
			return mapController.createUntitledMap(url);
		}
		catch (Exception e) {
			fileManager.handleLoadingException(e);
			return null;
		}
	}
	public MapModel readMap(URL url){
		try {
			return mapController.readMap(url);
		}
		catch (Exception e) {
			fileManager.handleLoadingException(e);
			return null;
		}
	}
	@Override
	public void openMap(URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
		mapController.openMap(url);
    }
	public void openDocumentationMap(URL url) throws FileNotFoundException, IOException,
            URISyntaxException, XMLException {
	    mapController.openDocumentationMap(url);
    }
	public void restoreCurrentMap() throws FileNotFoundException, IOException, URISyntaxException, XMLException {
	    mapController.restoreCurrentMap();
    }

}
