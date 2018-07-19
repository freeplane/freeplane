package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.n3.nanoxml.XMLParseException;

public class MapLoader{

	private final ModeController modeController;

	private URL sourceLocation;
	private URL newMapLocation;
	private boolean saveAfterLoading;
	private boolean withView;
	private boolean unsetMapLocation;
	private boolean asDocumentation;
	private String selectedNodeId;

	public MapLoader(ModeController modeController) {
		super();
		this.modeController = modeController;
	}

	public MapLoader load(URL url) {
		this.sourceLocation = url;
		return this;

	}

	public MapLoader load(File file) {
		URL url = fileToUrlOrNull(file);
		return load(url);

	}

	public MapLoader newMapLocation(File associatedFile) {
		this.newMapLocation = fileToUrlOrNull(associatedFile);
		this.saveAfterLoading = false;
		return this;
	}


	public MapLoader unsetMapLocation() {
		this.unsetMapLocation = true;
		return this;
	}


	public MapLoader saveAfterLoading() {
		this.saveAfterLoading = true;
		return this;
	}

	public MapLoader withView() {
		this.withView = true;
		return this;
	}

	public MapLoader asDocumentation() {
		asDocumentation = true;
		return this;
	}

	public MapLoader selectNodeById(String nodeId) {
		withView = true;
		selectedNodeId = nodeId;
		return this;
	}

	public MapModel getMap() {
		final MapModel oldMap = controller().getMap();
		if(unsetMapLocation) {
			newMapLocation = null;
			saveAfterLoading = false;
		}
		else if(newMapLocation == null)
			newMapLocation = sourceLocation;
		asDocumentation = asDocumentation || modeController.containsExtension(DocuMapAttribute.class);

		try {
			MMapModel map = null;
			if (withView) {
				assertNoTransaction(oldMap);
				map = selectMapViewByUrl();
			}
			if (map == null) {
				setWaitingCursor(true);
				try{
					map = loadMap();
					if (withView) {
						createMapView(map);
						enableAutosave(map);
					}
				}
				finally {
					setWaitingCursor(false);
				}
			}
			if (withView) {
				selectNode();
			}
			return map;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setWaitingCursor(boolean isSet) {
		controller().getViewController().setWaitingCursor(isSet);
	}

	private void enableAutosave(MMapModel map) {
		if (! asDocumentation)
			map.enableAutosave();
	}

	private void selectNode() {
		if(selectedNodeId != null) {
			mapController().select(selectedNodeId);
		}
	}

	private void createMapView(MMapModel map) {
		mapController().createMapView(map);
	}

	private MMapModel selectMapViewByUrl() throws MalformedURLException {
		if (newMapLocation != null) {
			final IMapViewManager mapViewManager = controller().getMapViewManager();
			if (mapViewManager.tryToChangeToMapView( newMapLocation != null ? newMapLocation : sourceLocation)) {
				selectNode();
				final MapModel map = controller().getMap();
				if(map instanceof MMapModel)
					return (MMapModel) map;
				else
					throw new IllegalStateException("URL is already assigned to map with another type");
			}
		}
		return null;
	}

	private static URL fileToUrlOrNull(final File file) {
		try {
			return Compat.fileToUrl(file);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	private void assertNoTransaction(final MapModel oldMap) {
		if(oldMap == null)
			return;
		final IUndoHandler oldUndoHandler = oldMap.getExtension(IUndoHandler.class);
		final int transactionLevel = oldUndoHandler.getTransactionLevel();
        if(transactionLevel != 0)
			throw new RuntimeException("can not create map inside transaction");
	}


	private MMapModel loadMap()
			throws FileNotFoundException, IOException, XMLParseException, URISyntaxException {
		if(newMapLocation != null) {
			final MMapModel loadedMap = mapController().getMap(newMapLocation);
			if(loadedMap != null) {
				if (newMapLocation.equals(sourceLocation))
					return loadedMap;
				else
					throw new IllegalStateException("URL is already assigned to another map");
			}
		}

		URL sourceLocation = asDocumentation ? this.sourceLocation : alternativeSourceLocation();

		final MMapModel map = new MMapModel();
		MFileManager fileManager = fileManager();
		if(asDocumentation) {
			map.setReadOnly(true);
			map.addExtension(DocuMapAttribute.instance);
		}
		else {
			final File newFile = urlToFileOrNull(newMapLocation);
			if(newFile != null)
				fileManager.lock(map, newFile);
		}

		final File file = Compat.urlToFile(sourceLocation);
		if (file == null) {
			fileManager.loadCatchExceptions(sourceLocation, map);
		}
		else {
			if (file.length() != 0) {
				//DOCEAR - fixed: set the file for the map before parsing the xml, necessary for some events
				fileManager.setFile(map, file);
				NodeModel root = fileManager.loadTree(map, file);
				assert (map.getRootNode() == root);
			}
			if (map.getRootNode() == null)
				map.createNewRoot();
		}
		if (sourceLocation == null || ! sourceLocation.equals(newMapLocation)) {
			final Object rootText = map.getRootNode().getUserObject();
			if (rootText instanceof TranslatedObject) {
				map.getRootNode().setText(rootText.toString());
			}
		}
		map.setURL(newMapLocation);
		if(saveAfterLoading && ! newMapLocation.equals(sourceLocation)) {
			fileManager().save(map, map.getFile());
		}
		map.setSaved(sourceLocation != null && sourceLocation.equals(newMapLocation));
		mapController().addLoadedMap(map);
		mapController().fireMapCreated(map);
		return map;
	}

	private File urlToFileOrNull(URL url) throws URISyntaxException {
		return url != null ? Compat.urlToFile(url) : null;
	}

	private URL alternativeSourceLocation() {
		URL alternativeURL = null;
		try {
			final File file = urlToFileOrNull(sourceLocation);
			if(file == null){
				alternativeURL =  sourceLocation;
			}
			else{
				if(file.exists()){
					File alternativeFile = fileManager().getAlternativeFile(file, AlternativeFileMode.AUTOSAVE);
					if(alternativeFile != null){
						if (alternativeFile.getAbsoluteFile().equals(file.getAbsoluteFile()) )
							alternativeURL =  sourceLocation;
						else
							alternativeURL = Compat.fileToUrl(alternativeFile);
					}
					else
						return null;
				}
				else{
					alternativeURL = sourceLocation;
				}
			}
		}
		catch (MalformedURLException e) {
		}
		catch (URISyntaxException e) {
		}
		return alternativeURL;
	}

	private MFileManager fileManager() {
		return MFileManager.getController(modeController);
	}

	private MMapController mapController() {
		return (MMapController) modeController.getMapController();
	}

	private Controller controller() {
		return modeController.getController();
	}

}
