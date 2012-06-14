package org.docear.plugin.services.recommendations.mode;

import java.net.UnknownHostException;
import java.util.Collection;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.MapModel;

public class DocearRecommendationsMapModel extends MapModel {

	public DocearRecommendationsMapModel(Collection<RecommendationEntry> recommendations) {
		new DocearRecommendationsMapModel();
		parseRecommendations(recommendations);
		getRootNode().setFolded(false);
	}
	
	public DocearRecommendationsMapModel(Exception e) {
		new DocearRecommendationsMapModel();
		String message = "";
		if (e instanceof UnknownHostException) {
			message = TextUtils.getText("recommendations.error.no_connection");
		}
		else {
			message = TextUtils.getText("recommendations.error.unknown");
		}
		setRoot(DocearRecommendationsNodeModel.getNoRecommendationsNode(this, message));
		getRootNode().setFolded(false);
	}
	
	public DocearRecommendationsMapModel() {
		super();
		// create empty attribute registry
		AttributeRegistry.getRegistry(this);
		
	}
	
	private void parseRecommendations(Collection<RecommendationEntry> recommendations) {		
		if(recommendations == null) {
			if(ServiceController.getController().isRecommendationsAllowed()) {
				setRoot(DocearRecommendationsNodeModel.getNoRecommendationsNode(this, TextUtils.getText("recommendations.error.no_recommendations")));
			}
			else {
				setRoot(DocearRecommendationsNodeModel.getNoServiceNode(this));
			}
			return;
		}
		setRoot(DocearRecommendationsNodeModel.getRecommendationContainer(TextUtils.getText("recommendations.container.documents"),this));
		if(recommendations.isEmpty()) {
			getRootNode().insert(DocearRecommendationsNodeModel.getNoRecommendationsNode(this, TextUtils.getText("recommendations.error.no_recommendations")));
		} 
		else {
			for(RecommendationEntry entry : recommendations) {
				getRootNode().insert(new DocearRecommendationsNodeModel(entry, this));
			}		
		}
	}

	public String getTitle() {
		String label = CommunicationsController.getController().getRegisteredUserName();
		if(label != null && label.trim().length() > 0) {
			return TextUtils.format("recommendations.map.label.forUser", label);
		}
		return TextUtils.getText("recommendations.map.label.anonymous");
	}
}
