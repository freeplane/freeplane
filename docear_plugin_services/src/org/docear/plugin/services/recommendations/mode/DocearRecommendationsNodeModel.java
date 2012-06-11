package org.docear.plugin.services.recommendations.mode;

import java.util.List;

import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class DocearRecommendationsNodeModel extends NodeModel {

	private Object userObject;
	private boolean isItem = true;
	
	public DocearRecommendationsNodeModel(RecommendationEntry recommendation, MapModel map) {
		super(map);
		this.userObject = recommendation;
		
	}

	private DocearRecommendationsNodeModel(String name, MapModel map) {
		super(map);
		userObject = name;
	}

	@Override
	public List<NodeModel> getChildren() {
		if (!children.isEmpty()) {
			return super.getChildren();
		}
		return super.getChildren();
	}

	@Override
    public Object getUserObject() {
        return userObject;
    }

    @Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public boolean isLeaf() {
		return isItem;
	}

	@Override
	public String toString() {
		return getText();
	}

	public static NodeModel getRecommendationContainer(String name, DocearRecommendationsMapModel mapModel) {
		return new DocearRecommendationsNodeModel(name, mapModel);	
	}
}
