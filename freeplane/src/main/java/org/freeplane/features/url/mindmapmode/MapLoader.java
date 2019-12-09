package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.mindmapmode.DocuMapAttribute;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.n3.nanoxml.XMLException;
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
	private InputStream inputStream;


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

	public MapLoader setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		return this;
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
			if (withView && sourceLocation != null) {
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
						final File newFile = urlToFileOrNull(newMapLocation);
						if(newFile != null && ! asDocumentation)
							fileManager().lock(map, newFile);
					}
				}
				finally {
					setWaitingCursor(false);
				}
			}
			if (withView) {
				selectNode();
			}
			if(withView)
				restartTransaction(oldMap, map);
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
			return Compat.fileToUrl(file.getCanonicalFile());
		}
		catch (IOException e) {
			return null;
		}
	}

	private MMapModel loadMap()
			throws PrivilegedActionException {
		if(newMapLocation != null) {
			final MMapModel loadedMap = mapController().getMap(newMapLocation);
			if(loadedMap != null) {
				if (newMapLocation.equals(sourceLocation))
					return loadedMap;
				else
					throw new IllegalStateException("URL is already assigned to another map");
			}
		}

		final URL actualSourceLocation = inputStream != null ? null : asDocumentation ? sourceLocation : alternativeSourceLocation();
		final MMapModel map = createMindMap();
		AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

			@Override
			public Void run() throws FileNotFoundException, XMLException, URISyntaxException, IOException {
				if(inputStream != null) {
					loadMapContent(map);
				}
				else {
					if(actualSourceLocation != null) {
						loadMap(map, actualSourceLocation);
					}
				}
				return null;
			}
		});

		if (map.getRootNode() == null)
			map.createNewRoot();

		if (actualSourceLocation == null || ! actualSourceLocation.equals(newMapLocation)) {
			final Object rootText = map.getRootNode().getUserObject();
			if (rootText instanceof TranslatedObject) {
				map.getRootNode().setText(rootText.toString());
			}
		}
		map.setURL(newMapLocation);
		if(saveAfterLoading && ! newMapLocation.equals(actualSourceLocation)) {
			fileManager().save(map, map.getFile());
		}
		else {
			map.setSaved(actualSourceLocation != null && actualSourceLocation.equals(newMapLocation));
		}
		mapController().addLoadedMap(map);
		mapController().fireMapCreated(map);
		return map;
	}

	private void loadMap(final MMapModel map, URL sourceLocation)
			throws IOException, XMLException, FileNotFoundException, XMLParseException {
		MFileManager fileManager = fileManager();
		if(sourceLocation != null) {
			final File file = Compat.urlToFile(sourceLocation);
			if (file == null) {
				fileManager.load(sourceLocation, map);
			}
			else {
				if(! file.canRead())
					throw new FileNotFoundException(file.getAbsolutePath());
				if (file.length() != 0) {
					//DOCEAR - fixed: set the file for the map before parsing the xml, necessary for some events
					fileManager.setFile(map, file);
					NodeModel root = fileManager.loadTree(map, file);
					assert (map.getRootNode() == root);
				}
			}
		}
	}

	private MMapModel createMindMap() {
		final MMapModel map = new MMapModel();
		if(asDocumentation) {
			map.setReadOnly(true);
			map.addExtension(DocuMapAttribute.instance);
		}
		return map;
	}

	private void loadMapContent(final MMapModel map) throws IOException, XMLException {
		try (InputStreamReader urlStreamReader = new InputStreamReader(inputStream)) {
			final ModeController modeController = Controller.getCurrentModeController();
			modeController.getMapController().getMapReader().createNodeTreeFromXml(map, urlStreamReader, Mode.FILE);
		}
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

	private void assertNoTransaction(final MapModel oldMap) {
		if(oldMap == null)
			return;
		final IUndoHandler oldUndoHandler = oldMap.getExtension(IUndoHandler.class);
		final int transactionLevel = oldUndoHandler.getTransactionLevel();
        if( ! (transactionLevel == 0 || withView && transactionLevel == 1))
			throw new IllegalStateException("can not create map inside transaction");
	}

	private void restartTransaction(final MapModel oldMap, final MapModel newmap) {
		if(oldMap == null)
			return;
		final IUndoHandler oldUndoHandler = oldMap.getExtension(IUndoHandler.class);
		final IUndoHandler newUndoHandler = newmap.getExtension(IUndoHandler.class);
		final int transactionLevel = oldUndoHandler.getTransactionLevel();
		if(transactionLevel == 1){
		    oldUndoHandler.commit();
		    newUndoHandler.startTransaction();
		    return;
		}
	}
}
