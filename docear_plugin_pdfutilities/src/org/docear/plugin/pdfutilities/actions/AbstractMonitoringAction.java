package org.docear.plugin.pdfutilities.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearNodeModelExtension;
import org.docear.plugin.core.features.DocearNodeModelExtension.DocearExtensionKey;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
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
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
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
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
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
			LogUtils.warn(e);
			LogUtils.warn("ExecutionException during monitoring update.");
			LogUtils.warn(e.getCause());
		} catch (Exception e){
			LogUtils.warn(e);
			LogUtils.warn("====================================");
			LogUtils.warn(e.getCause());
			
		}
					
	}
	
	public static SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> getMonitoringThread(final List<NodeModel> targets){
		
		return new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
			List<URI> monitorFiles = new ArrayList<URI>();
			List<URI> otherFilesLinkedInMindMap = new ArrayList<URI>();
			List<MapModel> monitoredMindmaps = new ArrayList<MapModel>();
			Map<AnnotationID, List<NodeModel>> nodeIndex = new HashMap<AnnotationID, List<NodeModel>>();
			Map<AnnotationID, AnnotationModel> importedFiles = new HashMap<AnnotationID, AnnotationModel>();
			Map<AnnotationID, AnnotationModel> importedOtherFiles = new HashMap<AnnotationID, AnnotationModel>();
			List<NodeModel> widowedLinkedNode = new ArrayList<NodeModel>();
			List<AnnotationModel> newAnnotations = new ArrayList<AnnotationModel>();
			Map<String, List<NodeModel>> equalChildIndex = new HashMap<String, List<NodeModel>>();
			Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
			
			@Override
			protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {				
				for(final NodeModel target : targets){					
					if(canceled()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating against "+ target.getText() +" in progress....");
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					
					if(!cleanUpCollections()) continue;
					
					if(!setupPreconditions(target)) continue;
					
					if(!buildNodeIndex(target)) continue;
					
					if(!loadMonitoredFiles(target)) continue;
					
					if(!searchNewAndConflictedNodes()) continue;
					
					if(!searchingWidowedNodes(target)) continue;
					
					final boolean isfolded =  target.isFolded();
					if(newAnnotations.size() > 100){
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Folding node ...");		
						SwingUtilities.invokeAndWait(
					        new Runnable() {
					            public void run(){								            	
					            	target.setFolded(true);	
					            }
					        }
						);
					}	
					
					if(!pasteNewNodesAndRemoveWidowedNodes(target)){
						if(newAnnotations.size() > 100){
							fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
							fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Resetting folding ...");
							SwingUtilities.invokeAndWait(
						        new Runnable() {
						            public void run(){								            	
						            	target.setFolded(isfolded);	
						            }
						        }
							);
						}
						continue;
					}
					
					DocearEvent event = new DocearEvent(this, DocearEventType.MINDMAP_ADD_PDF_TO_NODE, true);
					DocearController.getController().dispatchDocearEvent(event);
					if(newAnnotations.size() > 100){
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Resetting folding ...");
						SwingUtilities.invokeAndWait(
					        new Runnable() {
					            public void run(){								            	
					            	target.setFolded(isfolded);	
					            }
					        }
						);
					}
					
					
				}
				return conflicts;
			}
			
			private boolean searchingWidowedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching widowed annotations ...");
				int count = 0;
				for(AnnotationID id : nodeIndex.keySet()){
					if(canceled()) return false;
					count++;
					fireProgressUpdate(100 * count / nodeIndex.keySet().size());
					if(importedFiles.containsKey(id)) continue;					
					for(NodeModel node : nodeIndex.get(id)){
						AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);
						if(annotation == null) continue;
						if(annotation.getAnnotationType() == null) continue;
						if(annotation.getAnnotationType().equals(AnnotationType.PDF_FILE)) continue;
						if(annotation.getAnnotationType().equals(AnnotationType.FILE)) continue;
						if(widowedLinkedNode.contains(node)) continue;						
						try{
							File file = Tools.getFilefromUri(Tools.getAbsoluteUri(node));
							if(file != null){
								File monitoringDirectory = Tools.getFilefromUri(Tools.getAbsoluteUri(NodeUtils.getPdfDirFromMonitoringNode(target), target.getMap()));
								if(file.getPath().startsWith(monitoringDirectory.getPath())){
									AnnotationModel annoation = new PdfAnnotationImporter().searchAnnotation(Tools.getAbsoluteUri(node), node);
									if(annoation == null){
										widowedLinkedNode.add(node);							
									}
								}
							}
							
						} catch(IOException e){
							LogUtils.info("IOexception during import file: "+ Tools.getAbsoluteUri(node));
						} catch(COSRuntimeException e){
							LogUtils.info("COSRuntimeException during import file: "+ Tools.getAbsoluteUri(node));
						} catch(COSLoadException e){
							LogUtils.info("COSLoadException during import file: "+ Tools.getAbsoluteUri(node));
						}
					}				
				}
				return true;
			}

			private boolean cleanUpCollections() {
				monitorFiles.clear();
				monitoredMindmaps.clear();
				nodeIndex.clear();
				importedFiles.clear();
				newAnnotations.clear();
				equalChildIndex.clear();
				widowedLinkedNode.clear();
				importedOtherFiles.clear();
				otherFilesLinkedInMindMap.clear();
				return true;
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
			
			private boolean canceled() throws InterruptedException{
				//Thread.sleep(1L);
				return (this.isCancelled() || Thread.currentThread().isInterrupted());
			}
			
			private boolean pasteNewNodesAndRemoveWidowedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {			
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Pasting new annotations ...");				
								
				for(AnnotationModel annotation : newAnnotations){
					if(canceled()) return false;
					try{
						fireProgressUpdate(100 * newAnnotations.indexOf(annotation) / newAnnotations.size());
						if(annotation.isInserted()) continue;
						Stack<NodeModel> treePathStack = getTreePathStack(annotation, target);
						if(canceled()) return false;
						NodeModel tempTarget = target;
						while(!treePathStack.isEmpty()){
							NodeModel insertNode = treePathStack.pop();
							NodeModel equalChild = getEqualChild(insertNode);
							if(equalChild == null){
								final NodeModel finalTarget = tempTarget;
								final NodeModel finalInsertNode = insertNode;
								if(canceled()) return false;
								SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){								            	
							            	finalTarget.insert(finalInsertNode);
							            }
							        }
								);
								tempTarget = insertNode;
								if(!equalChildIndex.containsKey(insertNode.getText())){
									equalChildIndex.put(insertNode.getText(), new ArrayList<NodeModel>());
								}				
								equalChildIndex.get(insertNode.getText()).add(insertNode);
							}
							else{
								tempTarget = equalChild;
							}
						}
					}catch(Exception e){
						LogUtils.warn(e);
					}
				}
				if(widowedLinkedNode.size() > 0){
					int result = UITools.showConfirmDialog(target, "Delete nodes with widowed links ?", "Delete nodes with widowed links ?", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.OK_OPTION){
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Deleting widowed nodes ...");
						for(final NodeModel node : widowedLinkedNode){
							SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){
							            	try{
							            		node.removeFromParent();
							            	} catch(Exception e){
							            		LogUtils.warn(e);
							            	}
							            }
							        }
								);
						}
					}
				}				
				return true;
			}			
			
			
			private NodeModel getEqualChild(NodeModel insertNode) {
				if(equalChildIndex.containsKey(insertNode.getText())){
					for(NodeModel equalChild : equalChildIndex.get(insertNode.getText())){
						if(isEqualNode(equalChild, insertNode)){
							return equalChild;
						}
					}
				}
				return null;
			}

			/*public NodeModel getEqualChild(NodeModel target, NodeModel insertNode){
				if(insertNode == null || target == null)	return null;
				
				for(NodeModel child : target.getChildren()){
					if(isEqualNode(child, insertNode)) return child;					
				}
				for(NodeModel child : target.getChildren()){
					NodeModel equalChild = getEqualChild(child, insertNode);
					if(equalChild != null) return equalChild;
				}
				return null;
			}*/
			
			private boolean isEqualNode(NodeModel node1, NodeModel node2){
				if(!node1.getText().equals(node2.getText())){
					return false;
				}
				if(Tools.getAbsoluteUri(node1, node1.getMap()) != null && Tools.getAbsoluteUri(node2, node2.getMap()) == null){
					return false;
				}
				if(Tools.getAbsoluteUri(node1, node1.getMap()) == null && Tools.getAbsoluteUri(node2, node2.getMap()) != null){
					return false;
				}
				if(node1.containsExtension(DocearNodeModelExtension.class) && !node2.containsExtension(DocearNodeModelExtension.class)){
					return false;
				}
				if(node2.containsExtension(DocearNodeModelExtension.class) && !node1.containsExtension(DocearNodeModelExtension.class)){
					return false;
				}
				if(node1.containsExtension(IAnnotation.class) && !node2.containsExtension(IAnnotation.class)){
					return false;
				}
				if(node2.containsExtension(IAnnotation.class) && !node1.containsExtension(IAnnotation.class)){
					return false;
				}
				if(node1.containsExtension(DocearNodeModelExtension.class) && !node2.containsExtension(DocearNodeModelExtension.class)){
					return false;
				}
				if(node1.containsExtension(AnnotationModel.class) && !node2.containsExtension(AnnotationModel.class)){
					return false;
				}
				if(node2.containsExtension(AnnotationModel.class) && !node1.containsExtension(AnnotationModel.class)){
					return false;
				}
				if(node1.containsExtension(AnnotationModel.class) && node2.containsExtension(AnnotationModel.class)){
					IAnnotation anno1 = AnnotationController.getAnnotationNodeModel(node1);
					IAnnotation anno2 = AnnotationController.getAnnotationNodeModel(node2);
					if(anno1.getAnnotationType() != anno2.getAnnotationType()){
						return false;
					}
					if(!anno1.getAnnotationID().equals(anno2.getAnnotationID())){
						return false;
					}
				}
				if(node1.containsExtension(IAnnotation.class) && node2.containsExtension(IAnnotation.class)){
					IAnnotation anno1 = AnnotationController.getAnnotationNodeModel(node1);
					IAnnotation anno2 = AnnotationController.getAnnotationNodeModel(node2);
					if(anno1.getAnnotationType() != anno2.getAnnotationType()){
						return false;
					}
					if(!anno1.getAnnotationID().equals(anno2.getAnnotationID())){
						return false;
					}
				}
				if(Tools.getAbsoluteUri(node1, node1.getMap()) != null && Tools.getAbsoluteUri(node2, node2.getMap()) != null){
					if(!Tools.getAbsoluteUri(node1, node1.getMap()).equals(Tools.getAbsoluteUri(node2, node2.getMap()))){
						return false;
					}
				}
				return true;
			}

			private Stack<NodeModel> getTreePathStack(AnnotationModel annotation, NodeModel target) throws InterruptedException {
				Stack<NodeModel> result = new Stack<NodeModel>();
				AnnotationModel tempAnnotation = annotation;
				do{
					if(canceled()) return result;
					// Scripting Error Bugfix
					if(tempAnnotation.getTitle() != null && tempAnnotation.getTitle().length() > 1 && tempAnnotation.getTitle().charAt(0) == '='){
						tempAnnotation.setTitle(" " + tempAnnotation.getTitle());
					}
					NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(tempAnnotation.getTitle(), target.getMap());
					AnnotationController.setModel(node, tempAnnotation);
					NodeUtils.setLinkFrom(tempAnnotation.getUri(), node);
					result.push(node);
					tempAnnotation.setInserted(true);
					tempAnnotation = tempAnnotation.getParent();
				} while(tempAnnotation != null);
				if(!isFlattenSubfolders(target)){
					URI pdfDirURI = Tools.getAbsoluteUri(NodeUtils.getPdfDirFromMonitoringNode(target));
					
					File pdfDirFile = Tools.getFilefromUri(pdfDirURI);		
					File parent = Tools.getFilefromUri(annotation.getUri()).getParentFile();
					while(parent != null && !parent.equals(pdfDirFile)){
						if(canceled()) return result;
						NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(parent.getName(), target.getMap());
						DocearNodeModelExtensionController.setEntry(node, DocearExtensionKey.MONITOR_PATH, null);
						result.push(node);
						parent = parent.getParentFile();						
					}
				}
				return result;
			}

			private boolean searchNewAndConflictedNodes() throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching new or conflicted annotations ...");
				int count = 0;
				for(AnnotationID id : importedFiles.keySet()){
					if(canceled()) return false;
					fireProgressUpdate(100 * count / importedFiles.keySet().size());
					if(!nodeIndex.containsKey(id)){
						importedFiles.get(id).setNew(true);
						newAnnotations.add(importedFiles.get(id));
					}
					else{
						AnnotationModel importedAnnotation = importedFiles.get(id);
						for(NodeModel node : nodeIndex.get(id)){
							AnnotationNodeModel oldAnnotation = AnnotationController.getAnnotationNodeModel(node);
							if(oldAnnotation != null){
								if(oldAnnotation.getAnnotationType() == null) continue;
								if(oldAnnotation.getAnnotationType().equals(AnnotationType.PDF_FILE)) continue;
								if(oldAnnotation.getAnnotationType().equals(AnnotationType.FILE)) continue;
								if(!importedAnnotation.getTitle().trim().equals(oldAnnotation.getTitle().trim())){
									importedAnnotation.setConflicted(true);
									AnnotationController.addConflictedAnnotation(importedAnnotation, conflicts);
									for(NodeModel conflictedNode : nodeIndex.get(id)){
										AnnotationNodeModel conflictedAnnotation = AnnotationController.getAnnotationNodeModel(conflictedNode);
										if(conflictedAnnotation != null){
											AnnotationController.addConflictedAnnotation(conflictedAnnotation, conflicts);
										}
									}
									break;
								}
							}
						}
						
					}
					count++;
				}
				return true;
			}

			private boolean loadMonitoredFiles(NodeModel target) throws InterruptedException, InvocationTargetException{
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Loading monitored files ...");
				for(URI uri : monitorFiles){
					if(canceled()) return false;
					try{
						fireProgressUpdate(100 * monitorFiles.indexOf(uri) / monitorFiles.size());
						fireStatusUpdate(SwingWorkerDialog.NEW_FILE, null, Tools.getFilefromUri(uri).getName());
						if(new PdfFileFilter().accept(uri)){
							AnnotationModel pdf = new PdfAnnotationImporter().importPdf(uri);							
							addAnnotationsToImportedFiles(pdf, target);							
						}
						else{						
							AnnotationID id = new AnnotationID(uri, 0);
							AnnotationModel annotation = new AnnotationModel(id, AnnotationType.FILE);	
							annotation.setTitle(Tools.getFilefromUri(uri).getName());
							annotation.setUri(uri);
							if(!importedFiles.containsKey(id)){
								importedFiles.put(id, annotation);
							}							
						}
					} catch(IOException e){
						LogUtils.info("IOexception during update file: "+ uri);
					} catch(COSRuntimeException e){
						LogUtils.info("COSRuntimeException during update file: "+ uri);
					} catch(COSLoadException e){
						LogUtils.info("COSLoadException during update file: "+ uri);
					}
				}			
				return true;
			}

			private void addAnnotationsToImportedFiles(AnnotationModel annotation, NodeModel target) throws InterruptedException {
				if(canceled()) return;
				AnnotationID id = annotation.getAnnotationID();
				if(!importedFiles.containsKey(id)){
					importedFiles.put(id, annotation);
				}				
				for(AnnotationModel child : annotation.getChildren()){
					child.setParent(annotation);
					addAnnotationsToImportedFiles(child, target);
				}				
			}			

			private boolean buildNodeIndex(NodeModel target) throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Building node index ...");
				for(MapModel map : monitoredMindmaps){
					if(canceled()) return false;
					buildAnnotationNodeIndex(map.getRootNode());
				}
				buildEqualChildIndex(target.getChildren());
				if(canceled()) return false;
				return true;
			}

			private void buildAnnotationNodeIndex(NodeModel node) throws InterruptedException {
				if(canceled()) return;
				File file = Tools.getFilefromUri(Tools.getAbsoluteUri(node));
				if(file != null && !file.exists()){
					widowedLinkedNode.add(node);
				}				
				AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);				
				if(annotation != null && annotation.getAnnotationID() != null){
					AnnotationID id = annotation.getAnnotationID();
					if(!nodeIndex.containsKey(id)){
						nodeIndex.put(id, new ArrayList<NodeModel>());
					}
					nodeIndex.get(id).add(node);
				}
				for(NodeModel child : node.getChildren()){
					buildAnnotationNodeIndex(child);
				}
			}
			
			private void buildEqualChildIndex(List<NodeModel> children) throws InterruptedException {
				if(canceled()) return;
				for(NodeModel child : children){
					if(!equalChildIndex.containsKey(child.getText())){
						equalChildIndex.put(child.getText(), new ArrayList<NodeModel>());
					}				
					equalChildIndex.get(child.getText()).add(child);
					buildEqualChildIndex(child.getChildren());
				}
			}

			private boolean setupPreconditions(NodeModel target) throws InterruptedException, InvocationTargetException {
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored files ...");
					if(canceled()) return false;
					URI monitoringDirectory = Tools.getAbsoluteUri(NodeUtils.getPdfDirFromMonitoringNode(target), target.getMap());
					if(monitoringDirectory == null || Tools.getFilefromUri(monitoringDirectory) == null || !Tools.getFilefromUri(monitoringDirectory).exists()){
						UITools.informationMessage("Monitoring directory does not exist.");
						return false;
					}
					CustomFileListFilter monitorFileFilter = new CustomFileListFilter(ResourceController.getResourceController().getProperty("docear_files_to_import"));
					monitorFiles = Tools.getFilteredFileList(monitoringDirectory, monitorFileFilter, isMonitorSubDirectories(target));
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Searching monitored mindmaps ...");
					if(canceled()) return false;
					List<URI> mindmapDirectories = NodeUtils.getMindmapDirFromMonitoringNode(target);
					Collection<URI> mindmapFiles = new ArrayList<URI>();
					for(URI uri : mindmapDirectories){
						uri = Tools.getAbsoluteUri(uri, target.getMap());
						if(Tools.getFilefromUri(uri) == null || !Tools.getFilefromUri(uri).exists()) continue;
						if(Tools.getFilefromUri(uri).isDirectory()){
							mindmapFiles.addAll(Tools.getFilteredFileList(uri, new CustomFileFilter(".*[.][mM][mM]"), isMonitorSubDirectories(target)));
						}
						else{
							mindmapFiles.add(uri);
						}
					}
					if(!mindmapFiles.contains(target.getMap().getFile().toURI())){
						mindmapFiles.add(target.getMap().getFile().toURI());
					}
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Loading monitored mindmaps ...");	
					if(canceled()) return false;
					monitoredMindmaps = NodeUtils.getMapsFromUris(mindmapFiles);
					if(updateMindmaps(monitoredMindmaps)){
						monitoredMindmaps = NodeUtils.getMapsFromUris(mindmapFiles);
					}				
				
				return true;
			}

			private boolean updateMindmaps(Collection<MapModel> maps) throws InterruptedException, InvocationTargetException {
				List<MapModel> mapsToUpdate = new ArrayList<MapModel>();
				for(MapModel map : maps){
					if(DocearMapModelController.getModel(map) == null){							
						mapsToUpdate.add(map);
					}
				}
				
				
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
					return true;
				}
				else{
					return false;
				}
			}
			
			private boolean isFlattenSubfolders(NodeModel target) {
				int value = (Integer)NodeUtils.getAttributeValue(target, PdfUtilitiesController.MON_FLATTEN_DIRS);
				switch(value){					
					default:
						return false;					
					case 1:
						return true;									
				}
			}

			private boolean isMonitorSubDirectories(NodeModel target) {
				int value = (Integer)NodeUtils.getAttributeValue(target, PdfUtilitiesController.MON_SUBDIRS);
				switch(value){
					
					default:
						return false;					
					case 1:
						return true;						
					case 2:
						return ResourceController.getResourceController().getBooleanProperty("docear_subdir_monitoring");			
				}
			}
			
			private String getMessage(List<MapModel> mapsToConvert){
				if(mapsToConvert.size() > 1){
					return mapsToConvert.size() + " of your monitored maps, need to be updated.\n Update now?\n\n"+TextUtils.getText("update_splmm_to_docear_explanation");
				}
				else if (mapsToConvert.size() == 1){
					return mapsToConvert.get(0).getTitle() + " needs to be updated.\n Update now?\n\n"+TextUtils.getText("update_splmm_to_docear_explanation");
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
			
		};
		
	}
	
	/*
	
	public static SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> getMonitoringThread(final List<NodeModel> targets){
		
		return new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
			List<NodeModel> deletableNodes = new ArrayList<NodeModel>();
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
					deletableNodes.clear();
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
					
						SwingUtilities.invokeAndWait(
						        new Runnable() {
						            public void run(){
						            	for(NodeModel node : deletableNodes){
						            		node.removeFromParent();	
						            	}
						            }
						        }
						   );
					
					
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
							LogUtils.info("IOexception during update file: "+ uri);
						} catch(COSRuntimeException e){
							LogUtils.info("COSRuntimeException during update file: "+ uri);
						} catch(COSLoadException e){
							LogUtils.info("COSLoadException during update file: "+ uri);
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
					            		deletableNodes.add(widowedLink);					            		
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
*/
}