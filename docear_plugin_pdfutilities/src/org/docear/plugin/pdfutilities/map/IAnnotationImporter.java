package org.docear.plugin.pdfutilities.map;

import java.net.URI;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.freeplane.features.map.NodeModel;


public interface IAnnotationImporter {
	public AnnotationModel searchAnnotation(URI uri, NodeModel node) throws Exception;
	public AnnotationModel importPdf(URI uri) throws Exception;
}
