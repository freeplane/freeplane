package org.docear.plugin.pdfutilities.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.SwingWorkerDialog;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.CustomFileFilter;
import org.docear.plugin.pdfutilities.util.CustomFileListFilter;
import org.docear.plugin.pdfutilities.util.MapConverter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnNodeChange = true )
public abstract class AbstractMonitoringAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractMonitoringAction(String key) {
		super(key);
	}

	public abstract void setEnabled();

	public AbstractMonitoringAction(String key, String title, Icon icon) {
		super(key, title, icon);
	}
	
	public static void updateNodesAgainstMonitoringDir(NodeModel target, boolean saveall) {
		List<NodeModel> list = new ArrayList<NodeModel>();
		list.add(target);
		AbstractMonitoringAction.updateNodesAgainstMonitoringDir(list, saveall);
	}

	public static void updateNodesAgainstMonitoringDir(final List<NodeModel> targets, boolean saveall) {		
		
		if(saveall){
			new SaveAll().actionPerformed(null);
		}
		
		try {			
			SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> thread = getMonitoringThread(targets);		
			
			SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			workerDialog.setHeadlineText("Folder Monitoring");
			workerDialog.setSubHeadlineText("Updating against monitored folder in progress....");
			workerDialog.showDialog(thread);
			workerDialog = null;			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = thread.get();
			
			if(conflicts != null && conflicts.size() > 0){
				ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
				dialog.showDialog();
			}	
			thread = null;			
		} catch (CancellationException e){
			LogUtils.info("CancellationException during monitoring update.");
		} catch (InterruptedException e) {
			LogUtils.info("InterruptedException during monitoring update.");
		} catch (ExecutionException e) {
			LogUtils.info("ExecutionException during monitoring update.");
		}
					
	}
	
	public static SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> getMonitoringThread(final List<NodeModel> targets){
		
		return new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
			private int totalNodeCount;
			private int totalNodeProgressCount;
			
			
			@Override
			protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {
				for(final NodeModel target : targets){
					totalNodeCount = 0;
					totalNodeProgressCount = 0;
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating against "+ target.getText() +" in progress....");
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored files ...");
					URI pdfDir = NodeUtils.getPdfDirFromMonitoringNode(target);
					URI mindmapDir = NodeUtils.getMindmapDirFromMonitoringNode(target);
					URI monDir = Tools.getAbsoluteUri(pdfDir);
					URI mapDir = Tools.getAbsoluteUri(mindmapDir);
					if(monDir == null || Tools.getFilefromUri(Tools.getAbsoluteUri(monDir)) == null || !Tools.getFilefromUri(Tools.getAbsoluteUri(monDir)).exists()){
						UITools.informationMessage("Monitoring directory does not exist.");
						continue;
					}
					if(mapDir == null || Tools.getFilefromUri(Tools.getAbsoluteUri(mapDir)) == null || !Tools.getFilefromUri(Tools.getAbsoluteUri(mapDir)).exists()){
						UITools.informationMessage("Mindmap directory does not exist.");
						continue;
					}
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					boolean monSubdirs = false;
					int value = (Integer)NodeUtils.getAttributeValue(target, PdfUtilitiesController.MON_SUBDIRS);
					switch(value){
						
						case 0:
							monSubdirs = false;
							break;
							
						case 1:
							monSubdirs = true;
							break;
							
						case 2:
							monSubdirs = ResourceController.getResourceController().getBooleanProperty("docear_subdir_monitoring");
							break;
					}
					Collection<URI> monitorFiles = Tools.getFilteredFileList(monDir, new CustomFileListFilter(ResourceController.getResourceController().getProperty("docear_files_to_import")), monSubdirs);
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored mindmaps ...");
					Collection<URI> mindmapFiles = Tools.getFilteredFileList(mapDir, new CustomFileFilter(".*[.][mM][mM]"), true);				
					if(!mindmapFiles.contains(target.getMap().getFile().toURI())){
						mindmapFiles.add(target.getMap().getFile().toURI());
					}
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Loading monitored mindmaps ...");
					List<MapModel> maps = new NodeUtils().getMapsFromUris(mindmapFiles);
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					List<MapModel> mapsToUpdate = new ArrayList<MapModel>();
					for(MapModel map : maps){
						if(DocearMapModelController.getModel(map) == null){
							mapsToUpdate.add(map);
						}
					}
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					if(mapsToUpdate.size() > 0){
						int result = UITools.showConfirmDialog(null, getMessage(mapsToUpdate), getTitle(mapsToUpdate), JOptionPane.OK_CANCEL_OPTION);
						if(result == JOptionPane.OK_OPTION){
							fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Converting " + mapsToUpdate.size() + " monitored mindmaps ...");
							if(!MapConverter.convert(mapsToUpdate)){							
								fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, "Monitoring canceled.");
							}
						}
						else{
							fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, "Monitoring canceled.");
						}
					}
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Computing total node count...");
					getTotalNodeCount(maps);
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Collecting Pdf linked nodes...");				
					
					Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = getOldAnnotationsFromMaps(maps);
					
					int count = 0;	
					
					for(final URI uri : monitorFiles){
						try{
							if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
							fireStatusUpdate(SwingWorkerDialog.NEW_FILE, null, Tools.getFilefromUri(uri).getName());
							PdfAnnotationImporter importer = new PdfAnnotationImporter();
							Collection<AnnotationModel> annotations = importer.importAnnotations(uri);
							AnnotationModel root = new AnnotationModel(new AnnotationID(Tools.getAbsoluteUri(uri), 0), AnnotationType.PDF_FILE);
							root.setTitle(Tools.getFilefromUri(Tools.getAbsoluteUri(uri)).getName());
							root.getChildren().addAll(annotations);
							annotations = new ArrayList<AnnotationModel>();
							annotations.add(root);
							annotations = AnnotationController.markNewAnnotations(annotations, oldAnnotations);							
							AnnotationController.addConflictedAnnotations(AnnotationController.getConflictedAnnotations(annotations, oldAnnotations), conflicts);
							
							final Collection<AnnotationModel> finalAnnotations = annotations;
							if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
							SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){
							            	new NodeUtils().insertNewChildNodesFrom(uri, finalAnnotations, target.isLeft(), target);										
							            	firePropertyChange(SwingWorkerDialog.NEW_NODES, null, getInsertedNodes(finalAnnotations));										
							            }
							        }
							   );						
							count++;
							fireProgressUpdate(100 * count / monitorFiles.size());
						} catch(IOException e){
							LogUtils.severe("IOexception during update file: "+ uri);
						} catch(COSRuntimeException e){
							LogUtils.severe("COSRuntimeException during update file: "+ uri);
						} catch(COSLoadException e){
							LogUtils.severe("COSLoadException during update file: "+ uri);
						}
					}
					for(MapModel map : maps){
						NodeUtils.saveMap(map);
						map.setSaved(true);
					}
				}
				return conflicts;
			}			

			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Import canceled.");
				}
				else{
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Import complete.");
				}
				
			}
			
			private Collection<AnnotationModel> getInsertedNodes(Collection<AnnotationModel> annotations){
				Collection<AnnotationModel> result = new ArrayList<AnnotationModel>();
				for(AnnotationModel annotation : annotations){
					try {
						Thread.sleep(1L);
					} catch (InterruptedException e) {							
					}
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return new ArrayList<AnnotationModel>();	
					if(annotation.isNew()){
						result.add(annotation);
					}
					if(annotation.hasNewChildren()){
						result.addAll(this.getInsertedNodes(annotation.getChildren()));
					}
				}
				return result;
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
			
			private Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFromMaps(List<MapModel> maps) throws InterruptedException, InvocationTargetException {
				Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
				for(MapModel map : maps){					
					Map<AnnotationID, Collection<AnnotationNodeModel>> temp = getOldAnnotationsFrom(map.getRootNode());
					for(AnnotationID id : temp.keySet()){
						Thread.sleep(1L);
						if(this.isCancelled() || Thread.currentThread().isInterrupted()) return new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
						if(!result.containsKey(id)){
							result.put(id, new ArrayList<AnnotationNodeModel>());				
						}
						result.get(id).addAll(temp.get(id));
					}
				}
				return result;
			}
			
			private Map<AnnotationID, Collection<AnnotationNodeModel>> getOldAnnotationsFrom(NodeModel parent) throws InterruptedException, InvocationTargetException{
				Map<AnnotationID, Collection<AnnotationNodeModel>> result = new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
				Thread.sleep(1L);
				if(this.isCancelled() || Thread.currentThread().isInterrupted()) return new HashMap<AnnotationID, Collection<AnnotationNodeModel>>();
				totalNodeProgressCount++;
				fireProgressUpdate(100 * totalNodeProgressCount / totalNodeCount);
				fireStatusUpdate(SwingWorkerDialog.REPAINT, null, null);
				if(NodeUtils.isPdfLinkedNode(parent)){
					URI uri = Tools.getAbsoluteUri(parent);
					if(AnnotationController.getModel(parent, false) == null){
						fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Updating Node: " + parent.getText());
					}
					AnnotationNodeModel oldAnnotation = AnnotationController.getAnnotationNodeModel(parent);
					if(uri != null && oldAnnotation != null){
						fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Collecting Node: " + parent.getText());
						result.put(oldAnnotation.getAnnotationID(), new ArrayList<AnnotationNodeModel>());				
						result.get(oldAnnotation.getAnnotationID()).add(oldAnnotation);
					}		 
				}
				
				for(NodeModel child : parent.getChildren()){
					Map<AnnotationID, Collection<AnnotationNodeModel>> children = getOldAnnotationsFrom(child);
					for(AnnotationID id : children.keySet()){
						if(!result.containsKey(id)){
							result.put(id, new ArrayList<AnnotationNodeModel>());				
						}
						result.get(id).addAll(children.get(id));
					}
				}
				
				return result;
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
			
			private String getMessage(List<MapModel> mapsToConvert){
				if(mapsToConvert.size() > 1){
					return mapsToConvert.size() + " of your monitored maps, need to be updated.\n Update now ?";
				}
				else if (mapsToConvert.size() == 1){
					return mapsToConvert.get(0).getTitle() + " needs to be updated.\n Update now ?";
				}
				return "";
			}
			
			private String getTitle(List<MapModel> mapsToConvert){
				if(mapsToConvert.size() > 1){
					return mapsToConvert.size() + " maps need to be updated";
				}
				else if (mapsToConvert.size() == 1){
					return mapsToConvert.get(0).getTitle() + " needs to be updated";
				}
				return "";
			}
			
		};
		
		
	}
	
	protected void foldAll(final NodeModel node) {
		final MapController modeController = Controller.getCurrentModeController().getMapController();
		for (NodeModel child : modeController.childrenUnfolded(node)) {
			foldAll(child);
		}
		setFolded(node, true);
	}
	
	protected void setFolded(final NodeModel node, final boolean state) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (mapController.hasChildren(node) && (mapController.isFolded(node) != state)) {
			mapController.setFolded(node, state);
		}
	}

}