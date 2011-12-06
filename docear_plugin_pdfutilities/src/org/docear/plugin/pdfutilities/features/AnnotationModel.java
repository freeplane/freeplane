package org.docear.plugin.pdfutilities.features;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.intarsys.pdf.pd.PDObject;

public class AnnotationModel implements IAnnotation{
	
	private AnnotationID id;
	private AnnotationType annotationType;
	private Integer page;
	private Integer generationNumber;
	private URI destinationUri;	
	private String title;
	private boolean isNew;
	private Integer objectNumber;
	private URI uri;
	private PDObject annotationObject; 
	
	
	private boolean isConflicted;
	private List<AnnotationModel> children = new ArrayList<AnnotationModel>();
	private AnnotationModel parent;
	private boolean isInserted;
	
	public AnnotationModel(){}; //needed for serialization
	
	public AnnotationModel(AnnotationID id){
		this.setAnnotationID(id);
	}
	
	public AnnotationModel(AnnotationID id, AnnotationType type){
		this.setAnnotationID(id);
		this.annotationType = type;
	}
	
	public AnnotationID getAnnotationID() {		
		return id;
	}

	public void setAnnotationID(AnnotationID id) {
		this.id = id;
		this.objectNumber = id.getObjectNumber();
		this.uri = id.getUri();
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
		return this.objectNumber;
	}
	
	public void setObjectNumber(Integer objectNumber) {
		this.objectNumber = objectNumber;
		if(this.uri != null){
			this.id = new AnnotationID(this.getUri(), objectNumber);
		}
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
		
	public URI getDestinationUri() {
		return destinationUri;
	}
	
	public void setDestinationUri(URI uri) {
		this.destinationUri = uri;
	}	
	
	public List<AnnotationModel> getChildren() {
		return children;
	}
	
	public void setNew(boolean isNew){
		this.isNew = isNew;
	}	
	
	public boolean isNew(){
		return this.isNew;
	}	
	
	public boolean isConflicted() {
		return isConflicted;
	}
	
	public void setConflicted(boolean isConflicted) {
		this.isConflicted = isConflicted;
	}
	
	public String toString(){
		return this.getTitle();
	}
	
	public URI getUri(){
		return this.uri;
	}
	
	public void setUri(URI absoluteUri){
		this.uri = absoluteUri;
		if(this.objectNumber != null){
			this.id = new AnnotationID(absoluteUri, this.getObjectNumber());
		}		
	}
	
	public boolean hasNewChildren(){
		for(IAnnotation child : this.children){
			if(child.isNew() || child.hasNewChildren()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasConflictedChildren(){
		for(AnnotationModel child : this.children){
			if(child.isConflicted() || child.hasConflictedChildren()){
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasConflicts(Collection<AnnotationModel> annotations){
		for(AnnotationModel model : annotations){
			if(model.isConflicted || model.hasConflictedChildren()){
				return true;
			}
		}
		return false;
	}

	public PDObject getPDObject() {
		return annotationObject;
	}

	public void setPDObject(PDObject annotationObject) {
		this.annotationObject = annotationObject;
	}

	public AnnotationModel getParent() {
		return parent;
	}

	public void setParent(AnnotationModel parent) {
		this.parent = parent;
	}

	public boolean isInserted() {
		return isInserted;
	}

	public void setInserted(boolean isInserted) {
		this.isInserted = isInserted;
	}	
	
	
	
}
