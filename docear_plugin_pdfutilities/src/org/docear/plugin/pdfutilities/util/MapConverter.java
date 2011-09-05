package org.docear.plugin.pdfutilities.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.SwingWorkerDialog;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class MapConverter {
	
	public static boolean convert(final List<MapModel> maps){
		if(maps == null || maps.size() <= 0) return false;
				
		try {			
			SwingWorker<Void, Void> thread = MapConverter.getConverterThread(maps);		
			SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			workerDialog.setHeadlineText("Mindmap Converter");
			workerDialog.setSubHeadlineText("Converting of " + maps.size() + " mindmap(s) in progress....");
			workerDialog.showDialog(thread);
			if(thread.isCancelled()){
				thread = null;
				return false;
			}			
			for(MapModel map : maps){				
				DocearMapModelController.setModelWithCurrentVersion(map);
				Controller.getCurrentModeController().getMapController().fireMapChanged(new MapChangeEvent(MapConverter.class, map, "", "", ""));
			}
			new SaveAll().actionPerformed(null);
			thread = null;			
			return true;
		} catch (CancellationException e){
			LogUtils.warn("CancellationException during update of maps.");
		}
		return false;
	}
	
	public static SwingWorker<Void, Void> getConverterThread(final List<MapModel> maps){
		
		return new SwingWorker<Void, Void>(){
			
			private int totalpdfNodeCount;
			private int totalNodeCount;
			private int totalNodeProgressCount;
							
			@Override
			protected Void doInBackground() throws Exception {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Computing total node count...");
				getTotalNodeCount(maps);
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Collecting Pdf linked nodes...");
				
				Map<URI, List<NodeModel>> outdatedNodes = getOutdatedNodesNodes(maps);
				
				int count = 0;
				fireProgressUpdate(100 * count / totalpdfNodeCount);
				for(final URI uri : outdatedNodes.keySet()){
					try{
						if(this.isCancelled() || Thread.currentThread().isInterrupted()) return null;							
						fireStatusUpdate(SwingWorkerDialog.NEW_FILE, null, Tools.getFilefromUri(uri).getName());											
						PdfAnnotationImporter importer = new PdfAnnotationImporter();
						Collection<AnnotationModel> importedAnnotations = importer.importAnnotations(uri);
						AnnotationModel root = new AnnotationModel(new AnnotationID(Tools.getAbsoluteUri(uri), 0), AnnotationType.PDF_FILE);
						root.setTitle(Tools.getFilefromUri(Tools.getAbsoluteUri(uri)).getName());
						root.getChildren().addAll(importedAnnotations);							
						importedAnnotations = this.getPlainNodeList(root);
						
						for(final NodeModel node : outdatedNodes.get(uri)){
							Thread.sleep(1L);
							if(this.isCancelled() || Thread.currentThread().isInterrupted()) return null;	
							fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Updating Node: " + node.getText());							
							this.updateNodeModel(node, importedAnnotations);
							count++;
							fireProgressUpdate(100 * count / totalpdfNodeCount);
						}
						
						
						
					} catch(IOException e){
						LogUtils.severe("IOexception during update file: "+ uri);
					} catch(COSRuntimeException e){
						LogUtils.severe("COSRuntimeException during update file: "+ uri);
					} catch(COSLoadException e){
						LogUtils.severe("COSLoadException during update file: "+ uri);
					}
				}					
				return null;
			}		
			
			private void getTotalNodeCount(List<MapModel> maps) {
				for(MapModel map : maps){
					getTotalNodeCount(map.getRootNode());
				}					
			}

			private void getTotalNodeCount(NodeModel node) {
				if(node.isRoot()){
					this.totalNodeCount++;
				}
				this.totalNodeCount += node.getChildCount();
				for(NodeModel child : node.getChildren()){
					getTotalNodeCount(child);
				}					
			}

			private void updateNodeModel(final NodeModel node, Collection<AnnotationModel> importedAnnotations) throws InterruptedException, InvocationTargetException {
				for(AnnotationModel annotation : importedAnnotations){
					if(annotation.getTitle().equals(node.getText())){
						AnnotationController.setModel(node, annotation);
						fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Successfully updated annotation model on node " + node.getText());					
						
						return;
					}
				}
				fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Could not find annotation for node " + node.getText());
			}

			private Map<URI, List<NodeModel>> getOutdatedNodesNodes(List<MapModel> maps) throws InterruptedException, InvocationTargetException {
				Map<URI, List<NodeModel>> result = new HashMap<URI, List<NodeModel>>();
				for(MapModel map : maps){
					Map<URI, List<NodeModel>> tempResult = getOutdatedNodesNodes(map.getRootNode());
					for(URI key : tempResult.keySet()){
						if(result.containsKey(key)){
							result.get(key).addAll(tempResult.get(key));
						}
						else{
							result.put(key, tempResult.get(key));
						}
					}
				}
				return result;
			}
			
			private Map<URI, List<NodeModel>> getOutdatedNodesNodes(final NodeModel node) throws InterruptedException, InvocationTargetException {
				Map<URI, List<NodeModel>> result = new HashMap<URI, List<NodeModel>>();
				for(NodeModel child : node.getChildren()){
					try {
						Thread.sleep(1L);
					} catch (InterruptedException e) {							
					}
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return new HashMap<URI, List<NodeModel>>();	
					
					Map<URI, List<NodeModel>> tempResult = getOutdatedNodesNodes(child);
					for(URI key : tempResult.keySet()){
						if(result.containsKey(key)){
							result.get(key).addAll(tempResult.get(key));
						}
						else{
							result.put(key, tempResult.get(key));
						}
					}
				}
				fireStatusUpdate(SwingWorkerDialog.REPAINT, null, null);
				if(NodeUtils.isPdfLinkedNode(node) && AnnotationController.getModel(node, false) == null){
					fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Collecting Node: " + node.getText());										
											
					URI key = Tools.getAbsoluteUri(node);
					if(result.containsKey(key)){
						result.get(key).add(node);
					}
					else{
						List<NodeModel> list = new ArrayList<NodeModel>();
						list.add(node);
						result.put(key, list);
					}
					this.totalpdfNodeCount++;
				}
				totalNodeProgressCount++;
				fireProgressUpdate(100 * totalNodeProgressCount / totalNodeCount);
				return result;
			}
			
			private Collection<AnnotationModel> getPlainNodeList(AnnotationModel root){
				Collection<AnnotationModel> result = new ArrayList<AnnotationModel>();
				result.add(root);
				for(AnnotationModel child : root.getChildren()){
					result.addAll(this.getPlainNodeList(child));						
				}
				return result;
			}

			@Override
		    protected void done() {
				System.out.println("Yielding");
				Thread.currentThread().yield();
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){
					firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Update canceled.");
				}
				else{
					firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Update complete.");
				}				
			}
			
			private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue) throws InterruptedException, InvocationTargetException{
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	firePropertyChange(propertyName, oldValue, newValue);										
				            }
				        }
				   );	
			}
			
			private void fireProgressUpdate(final int progress) throws InterruptedException, InvocationTargetException{
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	setProgress(progress);						
				            }
				        }
				   );	
			}
			
			
		};
	}

}
