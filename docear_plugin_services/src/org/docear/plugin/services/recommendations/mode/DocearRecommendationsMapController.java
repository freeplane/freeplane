package org.docear.plugin.services.recommendations.mode;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.DocearServiceResponse;
import org.docear.plugin.communications.features.DocearServiceResponse.Status;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
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

	public DocearRecommendationsMapController(DocearRecommendationsModeController modeController) {
		super(modeController);
		this.modeController = modeController;
	}

	public DocearRecommendationsModeController getModeController() {
		return modeController;
	}

	public MapModel newMap() {
		if(ServiceController.getController().isRecommendationsAllowed()) {
			final DocearRecommendationsMapModel mapModel = new DocearRecommendationsMapModel(getRecommendations());
			fireMapCreated(mapModel);
			newMapView(mapModel);
			// FIXME: setSaved(true) necessary? (it's removed from newMapView())
			return mapModel;
		}
		return new DocearRecommendationsMapModel(null);
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
		Controller.getCurrentController().getMapViewManager().changeToMapView(createMapView(map));
		Controller.getCurrentController().getMapViewManager().updateMapViewName();
	}

	private MapView createMapView(MapModel map) {
		DocearRecommendationsMapView mapView = new DocearRecommendationsMapView(map, this.getModeController());
		return mapView;
	}

	private Collection<RecommendationEntry> getRecommendations() {
		String name = CommunicationsController.getController().getUserName();
		if (name != null) {
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
					List<RecommendationEntry> recommandations = new ArrayList<RecommendationEntry>();
					for (DocearXmlElement document : documents) {
						String title = document.find("title").getContent();
						String url = document.find("sourceid").getContent();
						recommandations.add(new RecommendationEntry(title, url));
					}

					return recommandations;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println();
			}
		} else {
			System.out.println("no user set");
		}
		return Collections.emptyList();
	}
}
