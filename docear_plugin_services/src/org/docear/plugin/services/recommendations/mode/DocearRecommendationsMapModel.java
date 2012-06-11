package org.docear.plugin.services.recommendations.mode;

import java.util.Collection;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.MapModel;

public class DocearRecommendationsMapModel extends MapModel {

	public DocearRecommendationsMapModel(Collection<RecommendationEntry> recommendations) {
		super();
		// create empty attribute registry
		AttributeRegistry.getRegistry(this);
		parseRecommendations(recommendations);
		 
		getRootNode().setFolded(false);
	}
	
	private void parseRecommendations(Collection<RecommendationEntry> recommendations) {		
		setRoot(DocearRecommendationsNodeModel.getRecommendationContainer("documents",this));
		for(RecommendationEntry entry : recommendations) {
			getRootNode().insert(new DocearRecommendationsNodeModel(entry, this));
		}		
	}

	public String getTitle() {
		String label = CommunicationsController.getController().getRegisteredUserName();
		if(label != null) {
			return TextUtils.format("recommendations.map.label.forUser", label);
		}
		return TextUtils.getText("recommendations.map.label.anonymous");
	}

}
