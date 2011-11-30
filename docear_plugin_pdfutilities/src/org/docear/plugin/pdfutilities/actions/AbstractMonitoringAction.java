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
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.CustomFileFilter;
import org.docear.plugin.pdfutilities.util.CustomFileListFilter;
import org.docear.plugin.pdfutilities.util.MapConverter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
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
			System.out.println(Tools.getStackTraceAsString(e));
			LogUtils.info("ExecutionException during monitoring update.");
		}
					
	}
	
	public static SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> getMonitoringThread(final List<NodeModel> targets){
		
		return new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
			private int totalNodeCount;
			private int totalNodeProgressCount;
			private int totalMonitorNodeCount;
			private int monitorNodeProgressCount;
			private boolean deleteWidowedLinks = true;
			private boolean askDeleteWidowedLinks = true;
			
			
			@Override
			protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {
				for(final NodeModel target : targets){
					totalNodeCount = 0;
					totalNodeProgressCount = 0;
					monitorNodeProgressCount = 0;
					totalMonitorNodeCount = 1;
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating against "+ target.getText() +" in progress....");
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored files ...");
					URI pdfDir = NodeUtils.getPdfDirFromMonitoringNode(target);
					
					List<URI> uriList = NodeUtils.getMindmapDirFromMonitoringNode(target);
					URI monDir = Tools.getAbsoluteUri(pdfDir);
					
					if(monDir == null || Tools.getFilefromUri(Tools.getAbsoluteUri(monDir)) == null || !Tools.getFilefromUri(Tools.getAbsoluteUri(monDir)).exists()){
						UITools.informationMessage("Monitoring directory does not exist.");
						continue;
					}
					List<URI> mindmaps = new ArrayList<URI>();
					for(URI uri : uriList){
						URI mapDir = Tools.getAbsoluteUri(uri);
						if(mapDir == null || Tools.getFilefromUri(Tools.getAbsoluteUri(mapDir)) == null || !Tools.getFilefromUri(Tools.getAbsoluteUri(mapDir)).exists()){							
							continue;
						}
						else{
							mindmaps.add(mapDir);
						}
					}
					if(mindmaps.size() <= 0){
						UITools.informationMessage("No mindmaps to monitor found.");
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
					boolean tempFlattenSubdirs = false;
					value = (Integer)NodeUtils.getAttributeValue(target, PdfUtilitiesController.MON_FLATTEN_DIRS);
					switch(value){
						
						case 0:
							tempFlattenSubdirs = false;
							break;
							
						case 1:
							tempFlattenSubdirs = true;
							break;						
					}
					final boolean flattenSubdirs = tempFlattenSubdirs;
					Collection<URI> monitorFiles = Tools.getFilteredFileList(monDir, new CustomFileListFilter(ResourceController.getResourceController().getProperty("docear_files_to_import")), monSubdirs);
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored mindmaps ...");
					Collection<URI> mindmapFiles = new ArrayList<URI>();
					for(URI uri : mindmaps){
						if(Tools.getFilefromUri(uri) != null && Tools.getFilefromUri(uri).isDirectory()){
							mindmapFiles.addAll(Tools.getFilteredFileList(uri, new CustomFileFilter(".*[.][mM][mM]"), true));
						}
						else{
							mindmapFiles.add(uri);
						}
					}
					if(!mindmapFiles.contains(target.getMap().getFile().toURI())){
						mindmapFiles.add(target.getMap().getFile().toURI());
					}
					
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Loading monitored mindmaps ...");					
					List<MapModel> maps = NodeUtils.getMapsFromUris(mindmapFiles);
					
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
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching for widowed links...");
					totalMonitorNodeCount = getTotalChildCount(target);
					deleteWidowedLinkNodes(target);
					
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
							if(Tools.getFilefromUri(Tools.getAbsoluteUri(uri)) != null){ 
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
												NodeUtils.insertNewChildNodesFrom(uri, finalAnnotations, target.isLeft(), flattenSubdirs,  target);
												for(AnnotationModel annotation : getInsertedNodes(finalAnnotations)){
													firePropertyChange(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Imported " + annotation.getTitle() +"\n");												
												}							            											
								            }
								        }
								   );
							}
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
			
			private void deleteWidowedLinkNodes(NodeModel parent) throws COSRuntimeException, IOException, COSLoadException, InterruptedException, InvocationTargetException{
				ArrayList<NodeModel> children = new ArrayList<NodeModel>();
				ArrayList<NodeModel> widowedLinks = new ArrayList<NodeModel>();
				for(NodeModel child : parent.getChildren()){
					Thread.sleep(1L);
					if(this.isCancelled() || Thread.currentThread().isInterrupted()) return;
					monitorNodeProgressCount++;
					fireProgressUpdate(100 * monitorNodeProgressCount / totalMonitorNodeCount);
					fireStatusUpdate(SwingWorkerDialog.REPAINT, null, null);
					AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(child);
					if(annotation != null){
						if(Tools.getAbsoluteUri(child) != null && Tools.getFilefromUri(Tools.getAbsoluteUri(child)) != null){
							if(!Tools.getFilefromUri(Tools.getAbsoluteUri(child)).exists()){
								widowedLinks.add(child);
								break;
							}
							if(annotation.getAnnotationType() != null && annotation.getAnnotationType() != AnnotationType.PDF_FILE){
								AnnotationModel annoation = new PdfAnnotationImporter().searchAnnotation(Tools.getAbsoluteUri(child), child);
								if(annoation == null){
									widowedLinks.add(child);
									break;
								}
							}
						}
					}
					children.add(child);
				}
				for(final NodeModel widowedLink : widowedLinks){
					SwingUtilities.invokeAndWait(
					        new Runnable() {
					            public void run(){							            	
					            	if(askDeleteWidowedLinks){
					            		int result = UITools.showConfirmDialog(widowedLink, "Delete nodes with widowed links ?", "Delete nodes with widowed links ?", JOptionPane.YES_NO_OPTION);
					            		if(result == JOptionPane.YES_OPTION){
					            			deleteWidowedLinks = true;
					            		}
					            		else{
					            			deleteWidowedLinks = false;
					            		}
					            		askDeleteWidowedLinks = false;
					            	}
					            	if(deleteWidowedLinks){
					            		widowedLink.removeFromParent();
					            	}
					            }
					        }
					   );
				}
				for(NodeModel child : children){
					deleteWidowedLinkNodes(child);
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
				            	if(progress < 0){
				            		setProgress(0);
				            		return;
				            	}
				            	if(progress > 100){
				            		setProgress(100);
				            		return;
				            	}
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
			
			private int getTotalChildCount(NodeModel node){
				int result = node.getChildCount();
				for(NodeModel child : node.getChildren()){
					result += getTotalChildCount(child);
				}
				return result;
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