package org.docear.plugin.pdfutilities.features;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class AnnotationModelUpdater extends AMindmapUpdater {
	
	Map<URI, List<AnnotationModel>> importedPdfs = new HashMap<URI, List<AnnotationModel>>();

	public AnnotationModelUpdater(String title) {
		super(title);		
	}

//	@Override
//	public boolean updateNode(NodeModel node) {
//		boolean changed = false;
//		if(NodeUtils.isPdfLinkedNode(node) && AnnotationController.getModel(node, false) == null){
//			try {
//				if(!importedPdfs.containsKey(Tools.getAbsoluteUri(node))){					
//					AnnotationModel pdf = new PdfAnnotationImporter().importPdf(Tools.getAbsoluteUri(node));
//					importedPdfs.put(Tools.getAbsoluteUri(node), this.getPlainAnnotationList(pdf));					
//				}			
//				for(AnnotationModel annotation : importedPdfs.get(Tools.getAbsoluteUri(node))){
//					if(annotation.getTitle().equals(node.getText())){
//						AnnotationController.setModel(node, annotation);	
//						changed = true;
//						break;
//					}
//				}
//			} catch (COSRuntimeException e) {
//				LogUtils.warn(e);
//			} catch (IOException e) {
//				LogUtils.warn(e);
//			} catch (COSLoadException e) {
//				LogUtils.warn(e);
//			}
//		}
//		return changed;
//	}
	
	private List<AnnotationModel> getPlainAnnotationList(AnnotationModel root){
		List<AnnotationModel> result = new ArrayList<AnnotationModel>();
		result.add(root);
		for(AnnotationModel child : root.getChildren()){
			result.addAll(this.getPlainAnnotationList(child));						
		}
		return result;
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		// TODO Auto-generated method stub
		return false;
	}
}
