package org.docear.plugin.pdfutilities.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class PdfAnnotationExtensionModel implements IExtension{
	
	private AnnotationType annotationType;
	private Integer page;
	
	public enum AnnotationType{
		BOOKMARK, COMMENT, HIGHLIGHTED_TEXT, BOOKMARK_WITHOUT_DESTINATION, BOOKMARK_WITH_URI, PDF_FILE
	};
	
	public PdfAnnotationExtensionModel(){		
	}
	
	public PdfAnnotationExtensionModel(AnnotationType type){
		this.setAnnotationType(type);
	}
	
	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public static PdfAnnotationExtensionModel getModel(final NodeModel node) {
		return (PdfAnnotationExtensionModel) node.getExtension(PdfAnnotationExtensionModel.class);
	}

	public static PdfAnnotationExtensionModel createModel(final NodeModel node) {
		final PdfAnnotationExtensionModel extension = (PdfAnnotationExtensionModel) node.getExtension(PdfAnnotationExtensionModel.class);
		if (extension != null) {
			return extension;
		}
		final PdfAnnotationExtensionModel annotationModel = new PdfAnnotationExtensionModel();
		node.addExtension(annotationModel);
		return annotationModel;		
	}

	public static void setModel(final NodeModel node, final PdfAnnotationExtensionModel annotationModel) {
		final PdfAnnotationExtensionModel oldAnnotationModel = PdfAnnotationExtensionModel.getModel(node);
		if (annotationModel != null && oldAnnotationModel == null) {
			node.addExtension(annotationModel);
		}
		else if (annotationModel == null && oldAnnotationModel != null) {
			node.removeExtension(PdfAnnotationExtensionModel.class);
		}
	}
	
	
	
	
}
