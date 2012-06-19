package org.docear.plugin.services.recommendations.mode;

import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.DocearServiceResponse;
import org.docear.plugin.communications.features.DocearServiceResponse.Status;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.view.swing.map.MapView;

public class DocearRecommendationsMapController extends MapController {

	private DocearRecommendationsModeController modeController;
	private MapView currentMapView;

	public DocearRecommendationsMapController(DocearRecommendationsModeController modeController) {
		super(modeController);
		this.modeController = modeController;
		addMapLifeCycleListener(new IMapLifeCycleListener() {
			public void onSavedAs(MapModel map) {
			}

			public void onSaved(MapModel map) {
			}

			public void onRemove(MapModel map) {
				currentMapView = null;
			}

			public void onCreate(MapModel map) {
			}
		});
	}

	public DocearRecommendationsModeController getModeController() {
		return modeController;
	}

	public MapModel newMap() {
		DocearRecommendationsMapModel mapModel = null;		
		Collection<RecommendationEntry> recommendations = null;
		if (ServiceController.getController().isRecommendationsAllowed()) {
			try {
				recommendations = getRecommendations();
				mapModel = new DocearRecommendationsMapModel(recommendations);
			}
			catch(Exception e) {
				mapModel = getExceptionModel(e);
			}		
		} 
		else {
			mapModel = new DocearRecommendationsMapModel(recommendations);
		}
		fireMapCreated(mapModel);
		newMapView(mapModel);
		return mapModel;
	}

	private DocearRecommendationsMapModel getExceptionModel(Exception e) {
		DocearRecommendationsMapModel mapModel = new DocearRecommendationsMapModel();
		String message = "";
		if (e instanceof UnknownHostException) {
			message = TextUtils.getText("recommendations.error.no_connection");
		}
		else {
			message = TextUtils.getText("recommendations.error.unknown");
		}
		mapModel.setRoot(DocearRecommendationsNodeModel.getNoRecommendationsNode(mapModel, message));
		mapModel.getRootNode().setFolded(false);
		return mapModel;
	}

	public NodeModel newNode(final Object userObject, final MapModel map) {
		throw new UnsupportedOperationException();
	}

	public void toggleFolded(final NodeModel node) {
		if (hasChildren(node) && !node.isRoot()) {
			setFolded(node, !isFolded(node));
		}
	}

	public void newMapView(MapModel map) {
		this.currentMapView = createMapView(map);
		Controller.getCurrentController().getMapViewManager().changeToMapView(this.currentMapView);
		Controller.getCurrentController().getMapViewManager().updateMapViewName();
	}

	public void refreshRecommendations() {
		if (this.currentMapView != null) {
			Controller.getCurrentController().getMapViewManager().changeToMapView(this.currentMapView);
			Controller.getCurrentController().getMapViewManager().close(false);
		}
		newMap();
	}

	private MapView createMapView(MapModel map) {
		DocearRecommendationsMapView mapView = new DocearRecommendationsMapView(map, this.getModeController());
		return mapView;
	}

	private Collection<RecommendationEntry> getRecommendations() throws UnknownHostException, UnexpectedException {
		String name = CommunicationsController.getController().getUserName();
		if (!CoreUtils.isEmpty(name)) {
			DocearServiceResponse response = CommunicationsController.getController().get("/user/" + name + "/recommendations/documents");
			if (response.getStatus() == Status.OK) {
				try {
					DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
					IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
					IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
					parser.setBuilder(xmlBuilder);
					parser.setReader(reader);
					parser.parse();
					DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
					Collection<DocearXmlElement> documents = result.findAll("document");
					List<RecommendationEntry> recommendations = new ArrayList<RecommendationEntry>();
					for (DocearXmlElement document : documents) {
						String title = document.find("title").getContent();
						String url = document.find("sourceid").getContent();
						String click = document.getParent().getAttributeValue("fulltext");
						recommendations.add(new RecommendationEntry(title, url, click));
					}

					return recommendations;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (response.getStatus() == Status.NO_CONTENT) {
				return null;
			}
			else if (response.getStatus() == Status.UNKNOWN_HOST) {
				throw new UnknownHostException();
			}			
			else {
				throw new UnexpectedException("");
			}
		}
		else {
			System.out.println("no user set");
		}
		return Collections.emptyList();
	}
}
