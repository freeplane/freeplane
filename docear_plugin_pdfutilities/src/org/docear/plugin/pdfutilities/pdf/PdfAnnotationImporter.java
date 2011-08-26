package org.docear.plugin.pdfutilities.pdf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import de.intarsys.pdf.cds.CDSNameTreeEntry;
import de.intarsys.pdf.cds.CDSNameTreeNode;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.cos.COSArray;
import de.intarsys.pdf.cos.COSCatalog;
import de.intarsys.pdf.cos.COSDictionary;
import de.intarsys.pdf.cos.COSName;
import de.intarsys.pdf.cos.COSNull;
import de.intarsys.pdf.cos.COSObject;
import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.cos.COSString;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDAnnotation;
import de.intarsys.pdf.pd.PDAnyAnnotation;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDExplicitDestination;
import de.intarsys.pdf.pd.PDHighlightAnnotation;
import de.intarsys.pdf.pd.PDObject;
import de.intarsys.pdf.pd.PDOutline;
import de.intarsys.pdf.pd.PDOutlineItem;
import de.intarsys.pdf.pd.PDOutlineNode;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDTextAnnotation;
import de.intarsys.pdf.pd.PDTextMarkupAnnotation;
import de.intarsys.tools.locator.FileLocator;

public class PdfAnnotationImporter {
	
	private URI currentFile;
	private MapModel map;
	private boolean importAll = false;
	
	public PdfAnnotationImporter(){
		if(Controller.getCurrentController() != null && Controller.getCurrentController().getMap() != null){
			this.map = Controller.getCurrentController().getMap();
		}
	}
	
	
	public Map<URI, List<AnnotationModel>> importAnnotations(List<URI> files) throws IOException, COSLoadException, COSRuntimeException{
		Map<URI, List<AnnotationModel>> annotationMap = new HashMap<URI, List<AnnotationModel>>();
		
		for(URI file : files){
			annotationMap.put(file, this.importAnnotations(file));
		}
		
		return annotationMap;
	}	
	
	public List<AnnotationModel> importAnnotations(URI uri) throws IOException, COSLoadException, COSRuntimeException{
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		
		this.currentFile = uri;
		PDDocument document = getPDDocument(uri);
		if(document == null){
			return annotations;
		}
		try{
			annotations.addAll(this.importAnnotations(document));					
			annotations.addAll(this.importBookmarks(document.getOutline()));
			
		} catch(ClassCastException e){			
			PDOutlineItem outline = (PDOutlineItem)PDOutline.META.createFromCos(document.getCatalog().cosGetOutline());
			annotations.addAll(this.importBookmarks(outline));			
		} finally {
			if(document != null)
			document.close();
		}
        
		return annotations;
	}
	
	public boolean renameAnnotation(IAnnotation annotation, String newTitle) throws COSRuntimeException, IOException, COSLoadException{
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		boolean ret = false;
		this.currentFile = annotation.getUri();
		PDDocument document = getPDDocument(annotation.getUri());
		if(document == null){
			ret = false;
		}
		try{
			this.setImportAll(true);
			annotations.addAll(this.importAnnotations(document));					
			annotations.addAll(this.importBookmarks(document.getOutline()));
			
		} catch(ClassCastException e){			
			PDOutlineItem outline = (PDOutlineItem)PDOutline.META.createFromCos(document.getCatalog().cosGetOutline());
			annotations.addAll(this.importBookmarks(outline));			
		} finally {
			this.setImportAll(true);
			AnnotationModel result = this.searchAnnotation(annotations, annotation);
			if(result != null){
				PDObject pdObject = result.getPDObject();
				if(pdObject != null && pdObject instanceof PDOutlineItem){
					((PDOutlineItem)pdObject).setTitle(newTitle);
					ret = true;
				}
				if(pdObject != null && pdObject instanceof PDAnnotation){
					((PDAnnotation)pdObject).setContents(newTitle);
					ret = true;
				}
				document.save();			
			}
			if(document != null)
			document.close();
		}
        
		return ret;
	}

	public PDDocument getPDDocument(URI uri) throws IOException,	COSLoadException, COSRuntimeException {
		if(uri == null || !Tools.exists(uri, this.map) || !new PdfFileFilter().accept(uri)){
			return null;
		}
		FileLocator locator = new FileLocator(Tools.getFilefromUri(Tools.getAbsoluteUri(uri, this.map)));
		PDDocument document = PDDocument.createFromLocator(locator);
		return document;
	}
	
	private List<AnnotationModel> importBookmarks(PDOutlineNode parent) throws IOException, COSLoadException, COSRuntimeException{
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		
		if(!this.importAll && !ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.IMPORT_BOOKMARKS_KEY)){
			return annotations;
		}
		if(parent == null) return annotations;
		@SuppressWarnings("unchecked")
		List<PDOutlineItem> children = parent.getChildren();
		for(PDOutlineItem child : children){
			Integer objectNumber = child.cosGetObject().getIndirectObject().getObjectNumber();
			AnnotationModel annotation = new AnnotationModel(new AnnotationID(this.currentFile, objectNumber));
			annotation.setPDObject(child);
			annotation.setTitle(child.getTitle());			
			annotation.setAnnotationType(getAnnotationType(child));			
			annotation.setGenerationNumber(child.cosGetObject().getIndirectObject().getGenerationNumber());
			annotation.getChildren().addAll(this.importBookmarks(child));
			
			
			if(annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITH_URI){
				annotation.setDestinationUri(this.getAnnotationDestinationUri(child));
			}
			if(annotation.getAnnotationType() == AnnotationType.BOOKMARK){
				annotation.setPage(this.getAnnotationDestinationPage(child));
			}			
			
			annotations.add(annotation);
		}
		
		return annotations;
	}
	
	private URI getAnnotationDestinationUri(PDOutlineItem bookmark) {
		if(bookmark != null && !(bookmark.cosGetField(PDOutlineItem.DK_A) instanceof COSNull)){
			COSDictionary cosDictionary = (COSDictionary)bookmark.cosGetField(PDOutlineItem.DK_A);
			if(!(cosDictionary.get(COSName.create("URI")) instanceof COSNull)){
				COSObject destination = cosDictionary.get(COSName.create("URI"));
		        if(destination instanceof COSString && destination.getValueString(null) != null && destination.getValueString(null).length() > 0){
		        	try {
						return new URI(destination.getValueString(null));						
					} catch (URISyntaxException e) {
						LogUtils.warn("Bookmark Destination Uri Syntax incorrect.", e);
					}
		        }
			}            
		}		
		return null;
	}

	private AnnotationType getAnnotationType(PDOutlineItem bookmark) {
		if(bookmark != null && (bookmark.cosGetField(PDOutlineItem.DK_A) instanceof COSNull)){
			return AnnotationType.BOOKMARK_WITHOUT_DESTINATION;
		}
		if(bookmark != null && !(bookmark.cosGetField(PDOutlineItem.DK_A) instanceof COSNull)){
			COSDictionary cosDictionary = (COSDictionary)bookmark.cosGetField(PDOutlineItem.DK_A);
			if(!(cosDictionary.get(COSName.create("URI")) instanceof COSNull)){
				return AnnotationType.BOOKMARK_WITH_URI;
			}            
		}
		return AnnotationType.BOOKMARK;
	}

	private List<AnnotationModel> importAnnotations(PDDocument document){
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		boolean importComments = false;
		boolean importHighlightedTexts = false;
		if(this.importAll){
			importComments = true;
			importHighlightedTexts = true;
		}
		else{
			importComments = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.IMPORT_COMMENTS_KEY);
			importHighlightedTexts = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.IMPORT_HIGHLIGHTED_TEXTS_KEY);
		}
		
		String lastString = "";
		
		@SuppressWarnings("unchecked")
		List<PDAnnotation> pdAnnotations = document.getAnnotations();
		for(PDAnnotation annotation : pdAnnotations){
			// Avoid empty entries			
            if(annotation.getContents().equals("")/* && !annotation.isMarkupAnnotation()*/) continue;
            // Avoid double entries (Foxit Reader)
            if(annotation.getContents().equals(lastString)/* && !annotation.isMarkupAnnotation()*/) continue;
            lastString = annotation.getContents();
            // Sometimes page = NULL though this is a proper annotation
            if(annotation.getPage() != null)
            {
                // Avoid entries outside the page
                if(annotation.getPage().getMediaBox() == null || annotation.getRectangle() == null) continue;
                CDSRectangle page_rec = annotation.getPage().getMediaBox();
                CDSRectangle anno_rec = annotation.getRectangle();
                if(anno_rec.getLowerLeftX() > page_rec.getUpperRightX() ||
                   anno_rec.getLowerLeftY() > page_rec.getUpperRightY() ||
                   anno_rec.getUpperRightX() < page_rec.getLowerLeftX() ||
                   anno_rec.getUpperRightY() < page_rec.getLowerLeftY())  continue;
            }
            if((annotation.getClass() == PDAnyAnnotation.class || annotation.getClass() == PDTextAnnotation.class) && importComments){
            	Integer objectNumber = annotation.cosGetObject().getIndirectObject().getObjectNumber();
    			AnnotationModel pdfAnnotation = new AnnotationModel(new AnnotationID(this.currentFile, objectNumber));            	
            	pdfAnnotation.setTitle(annotation.getContents()); 
            	pdfAnnotation.setPDObject(annotation);
            	pdfAnnotation.setAnnotationType(AnnotationType.COMMENT);            	
            	pdfAnnotation.setGenerationNumber(annotation.cosGetObject().getIndirectObject().getGenerationNumber());
            	pdfAnnotation.setPage(this.getAnnotationDestination(annotation));
    			annotations.add(pdfAnnotation);
            }
            if((annotation.getClass() == PDTextMarkupAnnotation.class || annotation.getClass() == PDHighlightAnnotation.class) && importHighlightedTexts){
            	Integer objectNumber = annotation.cosGetObject().getIndirectObject().getObjectNumber();
    			AnnotationModel pdfAnnotation = new AnnotationModel(new AnnotationID(this.currentFile, objectNumber));           	
            	if(annotation.getContents() != null && annotation.getContents().length() > 0){
            		pdfAnnotation.setTitle(annotation.getContents()); 
            	}
            	/*else{
            		String test = ((PDMarkupAnnotation)annotation).getRichContent();
            		System.out.println(test);
            	}  */ 
            	pdfAnnotation.setPDObject(annotation);            	
            	pdfAnnotation.setAnnotationType(AnnotationType.HIGHLIGHTED_TEXT);             	
            	pdfAnnotation.setGenerationNumber(annotation.cosGetObject().getIndirectObject().getGenerationNumber());
            	pdfAnnotation.setPage(this.getAnnotationDestination(annotation));
    			annotations.add(pdfAnnotation);
            }
		}
		
		return annotations;
	}	
	
	public Integer getAnnotationDestination(PDAnnotation pdAnnotation) {				
		
		if(pdAnnotation != null){
			PDPage page = pdAnnotation.getPage();			
			if(page != null)
				return page.getNodeIndex()+1;
		}
		
		return null;		
	}

	public Integer getAnnotationDestinationPage(PDOutlineItem bookmark) throws IOException, COSLoadException {
		
		PDDocument document = bookmark.getDoc();
		if(document == null || bookmark == null){
			return null;
		}		
		
		if(bookmark != null && bookmark.getDestination() != null){
            PDExplicitDestination destination = bookmark.getDestination().getResolvedDestination(document);
            if(destination != null){
                PDPage page = destination.getPage(document);
                return page.getNodeIndex()+1;
            }
        }
        if(bookmark != null && !(bookmark.cosGetField(PDOutlineItem.DK_A) instanceof COSNull)){        	
            
            COSDictionary cosDictionary = (COSDictionary)bookmark.cosGetField(PDOutlineItem.DK_A);            
            COSArray destination = getCOSArrayFromDestination(cosDictionary);
                        
            return getPageFromCOSArray(document, (COSArray)destination);           
        }
        
        return null;
	}

	private COSArray getCOSArrayFromDestination(COSDictionary cosDictionary) {
		COSObject cosObject = cosDictionary.get(COSName.create("D"));
		if(cosObject instanceof COSArray){
			return (COSArray)cosObject;
		}
		if(cosObject instanceof COSString){
			String destinationName = cosObject.getValueString(null);
			if(destinationName == null || destinationName.length() <= 0){
				return null;
			}
				
        	COSDictionary dests = cosDictionary.getDoc().getCatalog().cosGetDests();
    		if (dests != null) {
    			for (Iterator<?> i = dests.keySet().iterator(); i.hasNext();) {
    				COSName key = (COSName) i.next();
    				if(key.stringValue().equals(destinationName)){
    					cosDictionary = (COSDictionary)dests.get(key);
    					cosObject = cosDictionary.get(COSName.create("D"));
    					if(cosObject instanceof COSArray){
    						return (COSArray)cosObject;
    					}
    				}
    			}
    		}
    		
    		COSDictionary names = cosDictionary.getDoc().getCatalog().cosGetNames();
    		if (names != null) {
    			COSDictionary destsDict = names.get(COSCatalog.DK_Dests).asDictionary();
    			if (destsDict != null) {
    				CDSNameTreeNode destsTree = CDSNameTreeNode.createFromCos(destsDict);
    				for (Iterator<?> i = destsTree.iterator(); i.hasNext();) {
    					CDSNameTreeEntry entry = (CDSNameTreeEntry) i.next();        					
    					if(entry.getName().stringValue().equals(destinationName)){
    						cosDictionary = (COSDictionary)entry.getValue();
    						cosObject = cosDictionary.get(COSName.create("D"));
        					if(cosObject instanceof COSArray){
        						return (COSArray)cosObject;
        					}       					
        				}
    				}
    			}
    		}
    		
    		
		}
		return null;
	}

	private Integer getPageFromCOSArray(PDDocument document, COSArray destination) {
		
		Iterator<?> it = destination.iterator();
	    while(it.hasNext()){
	         COSObject o = (COSObject)it.next();
	         if(o.isIndirect()){  //the page is indirect referenced
	             o.dereference();
	         }
	         PDPage page = document.getPageTree().getFirstPage();
	         while(page != null){
	             if(page.cosGetObject().equals(o)){
	                 return page.getNodeIndex() + 1;
	             }
	             page = page.getNextPage();
	         }	                 
	    }
	    return null;
	}	

	public AnnotationModel searchAnnotation(URI uri, NodeModel node) throws IOException, COSLoadException, COSRuntimeException {
		this.currentFile = uri;
		if(!this.isImportAll()) this.setImportAll(true);
		List<AnnotationModel> annotations = this.importAnnotations(uri);
		this.setImportAll(false);
		return searchAnnotation(annotations, node);        
   }
	
	public AnnotationModel searchAnnotation(List<AnnotationModel> annotations, NodeModel node) {
		for(AnnotationModel annotation : annotations){           
			AnnotationModel extensionModel = AnnotationController.getModel(node, false);
			if(extensionModel == null){
				if(annotation.getTitle().equals(node.getText())){
					//TODO: DOCEAR is Update nodeModel good here??
					//TODO: DOCEAR How to deal with nodes without extension(and object number) and changed annotation title ??
					AnnotationController.setModel(node, annotation);
					return annotation;
				}
				else{
					AnnotationModel searchResult = searchAnnotation(annotation.getChildren(), node);
					if(searchResult != null) return searchResult;
				}
			}
			else{
				return searchAnnotation(annotations, extensionModel);
			}
           
       }
		return null;
	}
	
	public AnnotationModel searchAnnotation(List<AnnotationModel> annotations, IAnnotation target) {
		for(AnnotationModel annotation : annotations){ 
			if(annotation.getObjectNumber().equals(target.getObjectNumber())){
				return annotation;
			}
			else{
				AnnotationModel searchResult = searchAnnotation(annotation.getChildren(), target);
				if(searchResult != null) return searchResult;
			}
		}
		return null;
	}

	public boolean isImportAll() {
		return importAll;
	}

	public void setImportAll(boolean importAll) {
		this.importAll = importAll;
	}

	public MapModel getMap() {
		return map;
	}

	public void setMap(MapModel map) {
		this.map = map;
	}

}
