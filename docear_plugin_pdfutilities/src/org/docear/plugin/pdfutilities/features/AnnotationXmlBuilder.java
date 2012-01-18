package org.docear.plugin.pdfutilities.features;

import java.io.IOException;

import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
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

public class AnnotationXmlBuilder implements IElementDOMHandler, IExtensionElementWriter {
	
	private static final String ANNOTATION_PAGE_XML_TAG = "page"; //$NON-NLS-1$
	private static final String ANNOTATION_TYPE_XML_TAG = "type"; //$NON-NLS-1$
	private static final String ANNOTATION_OBJECT_NUMBER_XML_TAG = "object_number"; //$NON-NLS-1$
	private static final String ANNOTATION_GENERATION_NUMBER_XML_TAG = "generation_number"; //$NON-NLS-1$
	private static final String PDF_ANNOTATION_XML_TAG = "pdf_annotation"; //$NON-NLS-1$
	
	
	public AnnotationXmlBuilder(){		
	}
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(PDF_ANNOTATION_XML_TAG, this);
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(AnnotationModel.class, this);		
	}

	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				annotation.setAnnotationType(AnnotationModel.AnnotationType.valueOf(value));				
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_PAGE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try{
					annotation.setPage(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Page Number."); //$NON-NLS-1$
				}
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_OBJECT_NUMBER_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try{
					annotation.setObjectNumber(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Page Number."); //$NON-NLS-1$
				}
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_GENERATION_NUMBER_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try{
					annotation.setGenerationNumber(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Page Number."); //$NON-NLS-1$
				}
			}
			
		});
		
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (tag.equals(PDF_ANNOTATION_XML_TAG)) {
			final AnnotationModel oldAnnotationModel = AnnotationController.getModel((NodeModel) parent, false);
			if(oldAnnotationModel != null){
				return oldAnnotationModel;
			}
			else{
				AnnotationModel model = new AnnotationModel();
				model.setUri(Tools.getAbsoluteUri((NodeModel) parent));
				return new AnnotationModel();				
			}
		}
		return null;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof AnnotationModel) {
				final AnnotationModel annotation = (AnnotationModel) userObject;
				AnnotationController.setModel(node, annotation);
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}

	public void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension) throws IOException {
		
		final AnnotationModel model = extension != null ? (AnnotationModel) extension : AnnotationController.getModel(node, false);
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
			pdfAnnotation.setAttribute(ANNOTATION_PAGE_XML_TAG, "" + page); //$NON-NLS-1$
		}
		
		final Integer objectNumber = model.getObjectNumber();
		if (objectNumber != null) {
			pdfAnnotation.setAttribute(ANNOTATION_OBJECT_NUMBER_XML_TAG, "" + objectNumber); //$NON-NLS-1$
		}
		
		final Integer generationNumber = model.getGenerationNumber();
		if (generationNumber != null) {
			pdfAnnotation.setAttribute(ANNOTATION_GENERATION_NUMBER_XML_TAG, "" + generationNumber); //$NON-NLS-1$
		}
		
		writer.addElement(model, pdfAnnotation);
		
	}

}
