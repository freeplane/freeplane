package org.docear.plugin.pdfutilities.features;

import java.io.IOException;

import org.docear.plugin.pdfutilities.features.PdfAnnotationExtensionModel.AnnotationType;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class PdfAnnotationExtensionBuilder implements IElementDOMHandler, IExtensionElementWriter {
	
	private static final String ANNOTATION_PAGE_XML_TAG = "page";
	private static final String ANNOTATION_TYPE_XML_TAG = "type";
	private static final String PDF_ANNOTATION_XML_TAG = "pdf_annotation";
	
	
	public PdfAnnotationExtensionBuilder(){		
	}
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(PDF_ANNOTATION_XML_TAG, this);
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(PdfAnnotationExtensionModel.class, this);		
	}

	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final PdfAnnotationExtensionModel annotationModel = (PdfAnnotationExtensionModel) node;
				annotationModel.setAnnotationType(PdfAnnotationExtensionModel.AnnotationType.valueOf(value));				
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_PAGE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final PdfAnnotationExtensionModel annotationModel = (PdfAnnotationExtensionModel) node;
				try{
					annotationModel.setPage(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Page Number.");
				}
			}
			
		});
		
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (tag.equals(PDF_ANNOTATION_XML_TAG)) {
			final PdfAnnotationExtensionModel oldAnnotationModel = PdfAnnotationExtensionModel.getModel((NodeModel) parent);
			if(oldAnnotationModel != null){
				return oldAnnotationModel;
			}
			else{
				return new PdfAnnotationExtensionModel();				
			}
		}
		return null;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof PdfAnnotationExtensionModel) {
				final PdfAnnotationExtensionModel annotationModel = (PdfAnnotationExtensionModel) userObject;
				PdfAnnotationExtensionModel.setModel(node, annotationModel);
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}

	public void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension) throws IOException {
		
		final PdfAnnotationExtensionModel model = extension != null ? (PdfAnnotationExtensionModel) extension : PdfAnnotationExtensionModel.getModel(node);
		if (model == null) {
			return;
		}
		final XMLElement pdfAnnotation = new XMLElement();
		pdfAnnotation.setName(PDF_ANNOTATION_XML_TAG);
		
		final AnnotationType annotationType = model.getAnnotationType();
		if (annotationType != null) {
			pdfAnnotation.setAttribute(ANNOTATION_TYPE_XML_TAG, annotationType.toString());
		}
		
		final Integer page = model.getPage();
		if (page != null) {
			pdfAnnotation.setAttribute(ANNOTATION_PAGE_XML_TAG, "" + page);
		}
		
		writer.addElement(model, pdfAnnotation);
		
	}

}
