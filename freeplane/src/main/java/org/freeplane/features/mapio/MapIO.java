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
package org.freeplane.features.mapio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 * Jan 14, 2012
 */
@SuppressWarnings("deprecation")
public class MapIO implements IExtension{
	final private UrlManager urlManager;
	final private MapController mapController;
	protected MapIO(UrlManager urlManager, MapController mapController) {
	    super();
	    this.urlManager = urlManager;
	    this.mapController = mapController;
    }
	public static void install(ModeController modeController){
		UrlManager urlManager = modeController.getExtension(UrlManager.class);
		MapController mapController = modeController.getMapController();
		final MapIO mapIO = new MapIO(urlManager, mapController);
		modeController.addExtension(MapIO.class, mapIO);
	}
	public boolean loadCatchExceptions(URL url, MapModel map){
		return urlManager.loadCatchExceptions(url, map);
	}
	
	public void load(URL url, MapModel map) throws FileNotFoundException, IOException, XMLException, XMLParseException,
	URISyntaxException {
		urlManager.load(url, map);
	}
	public boolean openMap(URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
	    return mapController.openMap(url);
    }
}
