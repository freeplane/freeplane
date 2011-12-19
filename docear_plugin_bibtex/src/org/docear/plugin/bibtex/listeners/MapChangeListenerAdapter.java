package org.docear.plugin.bibtex.listeners;

import java.net.URI;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.AMapChangeListenerAdapter;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

import spl.filter.PdfFileFilter;

public class MapChangeListenerAdapter extends AMapChangeListenerAdapter {

	public void mapChanged(MapChangeEvent event) {
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {	
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void nodeChanged(NodeChangeEvent event) {
		if (event.getProperty().equals(NodeModel.HYPERLINK_CHANGED)) {
			URI newUri = (URI) event.getNewValue();
			if (newUri != null) {
				try{
					if(new PdfFileFilter().accept(Tools.getFilefromUri(Tools.getAbsoluteUri(newUri)))){
						if(AnnotationController.getModel(event.getNode(), false) == null){
							AnnotationModel model = new AnnotationModel();
							model.setAnnotationID(new AnnotationID(newUri, 0));
							model.setAnnotationType(AnnotationType.PDF_FILE);
							AnnotationController.setModel(event.getNode(), model);
						}
					}
				}
				catch(Exception e){
					LogUtils.warn(e);
				}
				JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
				BibtexEntry entry = jabRefAttributes.findBibtexEntryForPDF(newUri, event.getNode());

				if (entry != null) {
					jabRefAttributes.setReferenceToNode(entry, event.getNode());
				}
			}
		}
	}

	public void onCreate(MapModel map) {
	}

	public void onRemove(MapModel map) {
	}

	public void onSavedAs(MapModel map) {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame();
		try {
			saveJabrefDatabase();
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}

	}

	public void onSaved(MapModel map) {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame();
		try {
			saveJabrefDatabase();
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private void saveJabrefDatabase() {
		BasePanel basePanel = ReferencesController.getController().getJabrefWrapper().getBasePanel();
		basePanel.runCommand("save");

	}
}
