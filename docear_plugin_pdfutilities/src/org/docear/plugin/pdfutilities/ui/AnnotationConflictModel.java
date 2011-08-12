package org.docear.plugin.pdfutilities.ui;

import org.docear.plugin.pdfutilities.features.PdfAnnotationExtensionModel;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;

public class AnnotationConflictModel {
	
	private String fileName;
	private String annotationTitle;
	private Object annotation;
	
	public AnnotationConflictModel(PdfAnnotationExtensionModel annotation){
		this.fileName = annotation.getFile().getName();
		this.annotationTitle = annotation.getTitle();
		this.annotation = annotation;
	}
	
	public AnnotationConflictModel(NodeModel node){
		this.fileName = Tools.getFilefromUri(NodeLinks.getValidLink(node)).getName();
		this.annotationTitle = node.getText();
		this.annotation = node;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getAnnotationTitle() {
		return annotationTitle;
	}
	public void setAnnotationTitle(String annotationTitle) {
		this.annotationTitle = annotationTitle;
	}
	public Object getAnnotation() {
		return annotation;
	}
	public void setAnnotation(Object annotation) {
		this.annotation = annotation;
	}

}
