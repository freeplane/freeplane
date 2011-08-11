package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class PdfAnnotationExtensionModel implements IExtension{
	
	private AnnotationType annotationType;
	private Integer page;
	private Integer objectNumber;
	private Integer generationNumber;
	private URI destinationUri;
	
	private File file;
	private String title;
	private boolean isNew;
	private List<PdfAnnotationExtensionModel> children = new ArrayList<PdfAnnotationExtensionModel>();
	
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

	public Integer getObjectNumber() {
		return objectNumber;
	}

	public void setObjectNumber(Integer objectNumber) {
		this.objectNumber = objectNumber;
	}

	public Integer getGenerationNumber() {
		return generationNumber;
	}

	public void setGenerationNumber(Integer generationNumber) {
		this.generationNumber = generationNumber;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public URI getDestinationUri() {
		return destinationUri;
	}

	public void setDestinationUri(URI uri) {
		this.destinationUri = uri;
	}
	
	public List<PdfAnnotationExtensionModel> getChildren() {
		return children;
	}
	
	private void setNew(boolean isNew){
		this.isNew = isNew;
	}
	
	public boolean isNew(){
		return this.isNew;
	}
	
	public URI getAbsoluteUri(){
		if(this.file != null){
			return this.file.getAbsoluteFile().toURI();
		}
		else{
			return null;
		}
	}
	
	public boolean hasNewChildren(){
		for(PdfAnnotationExtensionModel child : this.children){
			if(child.isNew() || child.hasNewChildren()){
				return true;
			}
		}
		return false;
	}
	
	public void markNewAnnotations(Map<URI, Collection<NodeModel>> pdfLinkedNodes){
		for(PdfAnnotationExtensionModel child : this.getChildren()){
			child.markNewAnnotations(pdfLinkedNodes);
		}
		if(pdfLinkedNodes.containsKey(this.getAbsoluteUri())){
			for(NodeModel node : pdfLinkedNodes.get(this.getAbsoluteUri())){
				PdfAnnotationExtensionModel model = PdfAnnotationExtensionModel.getModel(node);
				if(model != null && model.getObjectNumber() != null && this.getObjectNumber() != null){
					if(model.getObjectNumber().equals(this.getObjectNumber())){
						//TODO: DOCEAR Update Model values like text etc ???
						this.setNew(false);
						return;
					}
				}
				else if(node.getText().equals(this.getTitle())){
					this.setNew(false);
					return;
				}
			}
		}
		this.setNew(true);
	}
	
	public static Collection<PdfAnnotationExtensionModel> markNewAnnotations(Collection<PdfAnnotationExtensionModel> annotations, 
										  Map<URI, Collection<NodeModel>> pdfLinkedNodes){
		for(PdfAnnotationExtensionModel annotation : annotations){
			annotation.markNewAnnotations(pdfLinkedNodes);
		}
		return annotations;
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
