package org.docear.plugin.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.docear.plugin.core.features.DocearMapModelController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MapView;

public class MapItem {
	private URI uri;
	private MapModel map;
	private Boolean mapIsOpen;

	public MapItem(URI mapUri) {
		this.uri = mapUri;
	}

	public MapItem(MapModel mapModel) {
		this.map = mapModel;
	}

	public MapModel getModel() {
		if (this.map == null) {
			URL url;
			String mapExtensionKey;
			try {
				url = WorkspaceUtils.resolveURI(uri).toURL();
				mapExtensionKey = Controller.getCurrentController()
						.getMapViewManager().checkIfFileIsAlreadyOpened(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}

			if (mapExtensionKey != null) {
				map = Controller.getCurrentController().getViewController()
						.getMapViewManager().getMaps().get(mapExtensionKey);
				if (map != null) {
					return map;
				}
			}

			map = new MMapModel();
			try {
				File f = WorkspaceUtils.resolveURI(uri);
				if (f.exists()) {
					UrlManager.getController().load(url, map);
					// do not work on non-docear-mindmaps
					if (DocearMapModelController.getModel(map) == null) {
						return null;
					}
					map.setURL(url);
					map.setSaved(true);
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	public String getIdentifierForDialog() {
		if (this.uri != null) {
			return this.uri.getPath();
		}

		if (this.map != null && this.map.getTitle() != null) {
			return TextUtils.getText("unsaved_map") + " \""
					+ this.map.getTitle() + "\"";
		}

		return TextUtils.getText("unknown_unsaved_map");
	}

	@SuppressWarnings("unchecked")
	public boolean isMapOpen() {
		if (this.mapIsOpen == null) {
			if (this.uri == null) {
				this.mapIsOpen = true;
			} 
			else {
				for (MapView view : (List<MapView>) Controller
						.getCurrentController().getViewController()
						.getMapViewManager().getMapViewVector()) {
					File mapFile = view.getModel().getFile();
					if (mapFile != null) {
						URI mapUri = mapFile.toURI();

						if (uri.equals(mapUri)) {
							this.mapIsOpen = true;
						}

					}
				}
			}
		}

		return this.mapIsOpen;
	}
}
