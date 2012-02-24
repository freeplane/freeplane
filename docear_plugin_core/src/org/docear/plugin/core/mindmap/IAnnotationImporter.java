package org.docear.plugin.core.mindmap;

import java.net.URI;

import org.docear.plugin.core.features.AnnotationModel;
import org.freeplane.features.map.NodeModel;


public interface IAnnotationImporter {
	public AnnotationModel searchAnnotation(URI uri, NodeModel node) throws Exception;
	public AnnotationModel importPdf(URI uri) throws Exception;
}
