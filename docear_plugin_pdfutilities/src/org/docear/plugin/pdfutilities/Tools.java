package org.docear.plugin.pdfutilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

public class Tools {
	
	public static File getFilefromUri(URI uri){
		if(uri == null) return null;
		try{
			if(!uri.isAbsolute()){
				final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
				MapModel map = Controller.getCurrentController().getMap();
				if(map == null || urlManager == null) return null;
				uri = urlManager.getAbsoluteUri(map, uri);				
			}
			return new File(uri);
		} catch(IllegalArgumentException e){
			return null;
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
