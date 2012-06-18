package org.docear.plugin.services.recommendations.mode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

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
	
	public static NodeModel getNoRecommendationsNode(DocearRecommendationsMapModel mapModel, String message) {
		DocearRecommendationsNodeModel node = new DocearRecommendationsNodeModel(mapModel);
		node.setUserObject(node.new NoRecommendations(message));
		return node;
	}
	
	public static NodeModel getNoServiceNode(DocearRecommendationsMapModel mapModel) {
		DocearRecommendationsNodeModel node = new DocearRecommendationsNodeModel(mapModel);
		node.setUserObject(node.new NoService());
		return node;
	}
	
	protected class NoRecommendations implements NodeModelItem {
		
		private final String text;

		public NoRecommendations(String message) {
			this.text = message;
		}
		
		public String getText() {
			return this.text;
		}
		
		public String toString() {
			return getText();
		}
		
	}
	
	protected class NoService extends JPanel implements NodeModelItem {
		private static final long serialVersionUID = 1L;
		private final String text;

		public NoService() {
			this.text = TextUtils.getText("recommendations.error.no_service");
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("fill:default"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"),}));
			
			JLabel lblText = new JLabel(text);
			add(lblText, "2, 2, 3, 1");
			
			JButton btnNewButton = new JButton(TextUtils.getText("recommendations.error.no_service.button"));
			add(btnNewButton, "2, 4");
			btnNewButton.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					if(DocearAllowUploadChooserAction.showDialog(false)) {
						ServiceController.getController().getRecommenationMode().getMapController().refreshRecommendations();
					}
				}
			});
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
