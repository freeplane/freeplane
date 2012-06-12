package org.docear.plugin.services.recommendations.mode;

import java.util.List;

import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class DocearRecommendationsNodeModel extends NodeModel {
	private boolean isItem = true;
	
	public DocearRecommendationsNodeModel(RecommendationEntry recommendation, MapModel map) {
		super(map);
		setUserObject(recommendation);
		
	}

	private DocearRecommendationsNodeModel(MapModel map) {
		super(map);
	}

	@Override
	public List<NodeModel> getChildren() {
		if (!children.isEmpty()) {
			return super.getChildren();
		}
		return super.getChildren();
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
		DocearRecommendationsNodeModel node = new DocearRecommendationsNodeModel(mapModel);
		node.setUserObject(node.new RecommendationContainer(name));
		return node;
	}
	
	public static NodeModel getNoRecommendationsNode(DocearRecommendationsMapModel mapModel) {
		DocearRecommendationsNodeModel node = new DocearRecommendationsNodeModel(mapModel);
		node.setUserObject(node.new NoRecommendations());
		return node;
	}
	
	protected class NoRecommendations implements NodeModelItem {
		
		private final String text;

		public NoRecommendations() {
			this.text = TextUtils.getText("recommendations.error.no_recommendations");
		}
		
		public String getText() {
			return this.text;
		}
		
		public String toString() {
			return getText();
		}
		
	}
	
	protected class RecommendationContainer implements NodeModelItem {
		
		private final String title;
		
		public RecommendationContainer(String title) {
			this.title = title;
		}
		
		public String getText() {
			return title;
		}
		
		public String toString() {
			return getText();
		}
		
	}
	
	interface NodeModelItem {
		public String getText();
	}
}
