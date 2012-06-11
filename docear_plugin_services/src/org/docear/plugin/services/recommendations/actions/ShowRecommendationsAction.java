package org.docear.plugin.services.recommendations.actions;

import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.DocearServiceResponse;
import org.docear.plugin.communications.features.DocearServiceResponse.Status;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.recommendations.dialog.RecommendationsResultPanel;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

@EnabledAction(checkOnPopup = true)
public class ShowRecommendationsAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;

	public ShowRecommendationsAction() {
		super("ShowRecommendationsAction");
	}
	
	public void setEnabled() {
		if(CommunicationsController.getController().getUserName() == null) {
			setEnabled(false);
		}
		else {
			setEnabled(true);
		}		
	}

	public void actionPerformed(ActionEvent e) {
		//showRecommendations();
		ServiceController.getController().getRecommenationMode().getMapController().newMap();
	}

//	public static void showRecommendations() {
//		String name = CommunicationsController.getController().getUserName();
//		if(name != null) {
//			DocearServiceResponse response = CommunicationsController.getController().get("/user/" + name + "/recommendations/documents");
//			if (response.getStatus() == Status.OK) {
//				try {
//					DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
//					IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
//					IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
//					parser.setBuilder(xmlBuilder);
//					parser.setReader(reader);
//					parser.parse();
//					DocearXmlRootElement result = (DocearXmlRootElement)xmlBuilder.getRoot();
//					Collection<DocearXmlElement> documents = result.findAll("document");
//					List<RecommendationEntry> recommandations = new ArrayList<RecommendationEntry>();
//					for(DocearXmlElement document : documents) {
//						String title = document.find("title").getContent();
//						String url = document.find("sourceid").getContent();
//						recommandations.add(new RecommendationEntry(title, url));
//					}
//					
//					RecommendationsResultPanel panel = new RecommendationsResultPanel(recommandations);
//					JOptionPane.showMessageDialog(UITools.getFrame(), panel, TextUtils.getText("docear.recommendations.dialog.title"), JOptionPane.PLAIN_MESSAGE);
//				} 
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			} else {
//				System.out.println();
//			}
//		}
//		else {
//			System.out.println("not user set");
//		}
//	}

}
