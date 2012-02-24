package org.docear.plugin.pdfutilities.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.docear.plugin.core.features.AnnotationID;
import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.AnnotationNodeModel;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearNodeModelExtension;
import org.docear.plugin.core.features.IAnnotation;
import org.docear.plugin.core.features.DocearNodeModelExtension.DocearExtensionKey;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.features.DocearNodeModelExtensionController;
import org.docear.plugin.core.mindmap.AnnotationController;
import org.docear.plugin.core.mindmap.MapConverter;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.CustomFileFilter;
import org.docear.plugin.pdfutilities.util.CustomFileListFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceUtils;
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
			workerDialog.setHeadlineText(TextUtils.getText("AbstractMonitoringAction.0")); //$NON-NLS-1$
			workerDialog.setSubHeadlineText(TextUtils.getText("AbstractMonitoringAction.1")); //$NON-NLS-1$
			workerDialog.showDialog(thread);
			workerDialog = null;			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = thread.get();
			
			if(conflicts != null && conflicts.size() > 0){
				ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
				dialog.showDialog();
			}	
			thread = null;			
		} catch (CancellationException e){
			LogUtils.info("CancellationException during monitoring update."); //$NON-NLS-1$
		} catch (InterruptedException e) {
			LogUtils.info("InterruptedException during monitoring update."); //$NON-NLS-1$
		} catch (ExecutionException e) {
			LogUtils.warn(e);
			LogUtils.warn("ExecutionException during monitoring update."); //$NON-NLS-1$
			LogUtils.warn(e.getCause());
		} catch (Exception e){
			LogUtils.warn(e);
			LogUtils.warn("===================================="); //$NON-NLS-1$
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
			List<NodeModel> orphanedNodes = new ArrayList<NodeModel>();
			List<AnnotationModel> newAnnotations = new ArrayList<AnnotationModel>();
			Map<String, List<NodeModel>> equalChildIndex = new HashMap<String, List<NodeModel>>();
			Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
			
			protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {				
				for(final NodeModel target : targets){					
					if(canceled()) return conflicts;
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, TextUtils.getText("AbstractMonitoringAction.6")+ target.getText() +TextUtils.getText("AbstractMonitoringAction.7")); //$NON-NLS-1$ //$NON-NLS-2$
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					
					if(!cleanUpCollections()) continue;
					
					if(!setupPreconditions(target)) continue;
					
					if(!buildNodeIndex(target)) continue;
					
					if(!loadMonitoredFiles(target)) continue;
					
					if(!searchNewAndConflictedNodes()) continue;
					
					if(!searchingOrphanedNodes(target)) continue;
					
					final boolean isfolded =  target.isFolded();
					if(newAnnotations.size() > 100){
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.8"));		 //$NON-NLS-1$
						SwingUtilities.invokeAndWait(
					        new Runnable() {
					            public void run(){								            	
					            	target.setFolded(true);	
					            }
					        }
						);
					}	
					
					if(!pasteNewNodesAndRemoveOrphanedNodes(target)){
						if(newAnnotations.size() > 100){
							fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
							fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.9")); //$NON-NLS-1$
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
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.9")); //$NON-NLS-1$
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
			
			private boolean searchingOrphanedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.11")); //$NON-NLS-1$
				int count = 0;
				for(AnnotationID id : nodeIndex.keySet()){
					if(canceled()) return false;
					count++;
					fireProgressUpdate(100 * count / nodeIndex.keySet().size());
					if(importedFiles.containsKey(id)) continue;					
					for(NodeModel node : nodeIndex.get(id)){
						if(!isMonitoringNodeChild(target, node)) continue;						
						AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);
						if(annotation == null) continue;
						if(annotation.getAnnotationType() == null) continue;						
						if(annotation.getAnnotationType().equals(AnnotationType.FILE)) continue;
						if(orphanedNodes.contains(node)) continue;						
						try{							
							File file = WorkspaceUtils.resolveURI(NodeLinks.getValidLink(node), node.getMap());
							if(file != null && !file.exists()){
								orphanedNodes.add(node);
								continue;
							}
							else if(file != null){								
								File monitoringDirectory = WorkspaceUtils.resolveURI(NodeUtils.getPdfDirFromMonitoringNode(target), target.getMap());
								if(file.getPath().startsWith(monitoringDirectory.getPath())){
									AnnotationModel annoation = new PdfAnnotationImporter().searchAnnotation(Tools.getAbsoluteUri(node), node);
									if(annoation == null){
										orphanedNodes.add(node);										
									}
								}
							}
							
						} catch(Exception e) {
							LogUtils.info("Exception during import file: "+ Tools.getAbsoluteUri(node)); //$NON-NLS-1$
						}
					}				
				}
				return true;
			}

			private boolean isMonitoringNodeChild(NodeModel monitoringNode, NodeModel node) {
				List<NodeModel> pathToRoot = Arrays.asList(node.getPathToRoot());
				return pathToRoot.contains(monitoringNode);
			}

			private boolean cleanUpCollections() {
				monitorFiles.clear();
				monitoredMindmaps.clear();
				nodeIndex.clear();
				importedFiles.clear();
				newAnnotations.clear();
				equalChildIndex.clear();
				orphanedNodes.clear();
				importedOtherFiles.clear();
				otherFilesLinkedInMindMap.clear();
				return true;
			}

			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("AbstractMonitoringAction.15")); //$NON-NLS-1$
				}
				else{
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("AbstractMonitoringAction.16")); //$NON-NLS-1$
				}
				
			}
			
			private boolean canceled() throws InterruptedException{
				//Thread.sleep(1L);
				return (this.isCancelled() || Thread.currentThread().isInterrupted());
			}
			
			private boolean pasteNewNodesAndRemoveOrphanedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {			
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.17"));				 //$NON-NLS-1$
								
				for(AnnotationModel annotation : newAnnotations){
					if(canceled()) return false;
					try{
						fireProgressUpdate(100 * newAnnotations.indexOf(annotation) / newAnnotations.size());
						if(annotation.isInserted()) continue;
						// PDF's with no new annotations should not be imported, see Ticket #283
						if(annotation.getAnnotationType().equals(AnnotationType.PDF_FILE) && annotation.getChildren().size() > 0 && !annotation.hasNewChildren()) continue;
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
							            	int newNodePostion = AnnotationController.getAnnotationPosition(finalInsertNode);
							            	boolean pasted = false;
						            		for(NodeModel child : finalTarget.getChildren()){
						            			int childPosition =  AnnotationController.getAnnotationPosition(child);
						            			if(childPosition > newNodePostion){
						            				//finalTarget.insert(finalInsertNode, finalTarget.getChildPosition(child));	
						            				((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(finalInsertNode, finalTarget, finalTarget.getChildPosition(child), finalTarget.isNewChildLeft());
						            				pasted = true;
						            				break;
						            			}						            				
						            		}
						            		if(!pasted){
						            			((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(finalInsertNode, finalTarget, finalTarget.getChildCount(), finalTarget.isNewChildLeft());						            			
						            		}							            							            	
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
				if(orphanedNodes.size() > 0){
					int result = UITools.showConfirmDialog(target, TextUtils.getText("AbstractMonitoringAction.18"), TextUtils.getText("AbstractMonitoringAction.18"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
					if(result == JOptionPane.OK_OPTION){
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.20")); //$NON-NLS-1$
						for(final NodeModel node : orphanedNodes){
							SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){
							            	try{
							            		if(node.getParentNode() != null){
							            			node.removeFromParent();
							            		}
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
						tempAnnotation.setTitle(" " + tempAnnotation.getTitle()); //$NON-NLS-1$
					}
					NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(tempAnnotation.getTitle(), target.getMap());
					AnnotationController.setModel(node, tempAnnotation);
					NodeUtils.setLinkFrom(tempAnnotation.getUri(), node);
					result.push(node);
					tempAnnotation.setInserted(true);
					tempAnnotation = tempAnnotation.getParent();
				} while(tempAnnotation != null);
				if(!isFlattenSubfolders(target)){					
					File pdfDirFile = WorkspaceUtils.resolveURI(NodeUtils.getPdfDirFromMonitoringNode(target));
					File annoFile = WorkspaceUtils.resolveURI(annotation.getUri());
					if(annoFile != null) {
						File parent = annoFile.getParentFile();
						while(parent != null && !parent.equals(pdfDirFile)){
							if(canceled()) return result;
							NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(parent.getName(), target.getMap());
							DocearNodeModelExtensionController.setEntry(node, DocearExtensionKey.MONITOR_PATH, null);
							NodeUtils.setLinkFrom(WorkspaceUtils.getURI(parent), node);
							result.push(node);
							parent = parent.getParentFile();						
						}
					}
				}
				return result;
			}

			private boolean searchNewAndConflictedNodes() throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.22")); //$NON-NLS-1$
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
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.23")); //$NON-NLS-1$
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
						LogUtils.info("IOexception during update file: "+ uri); //$NON-NLS-1$
					} catch(COSRuntimeException e){
						LogUtils.info("COSRuntimeException during update file: "+ uri); //$NON-NLS-1$
					} catch(COSLoadException e){
						LogUtils.info("COSLoadException during update file: "+ uri); //$NON-NLS-1$
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
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.27")); //$NON-NLS-1$
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
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.28")); //$NON-NLS-1$
					if(canceled()) return false;
					URI monitoringDirectory = Tools.getAbsoluteUri(NodeUtils.getPdfDirFromMonitoringNode(target), target.getMap());
					if(monitoringDirectory == null || Tools.getFilefromUri(monitoringDirectory) == null || !Tools.getFilefromUri(monitoringDirectory).exists()){
						UITools.informationMessage(TextUtils.getText("AbstractMonitoringAction.29")); //$NON-NLS-1$
						return false;
					}
					CustomFileListFilter monitorFileFilter = new CustomFileListFilter(ResourceController.getResourceController().getProperty(TextUtils.getText("AbstractMonitoringAction.30"))); //$NON-NLS-1$
					monitorFiles = Tools.getFilteredFileList(monitoringDirectory, monitorFileFilter, isMonitorSubDirectories(target));
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.31")); //$NON-NLS-1$
					if(canceled()) return false;
					List<URI> mindmapDirectories = NodeUtils.getMindmapDirFromMonitoringNode(target);
					Collection<URI> mindmapFiles = new ArrayList<URI>();
					for(URI uri : mindmapDirectories){
						uri = Tools.getAbsoluteUri(uri, target.getMap());
						if(Tools.getFilefromUri(uri) == null || !Tools.getFilefromUri(uri).exists()) continue;
						if(Tools.getFilefromUri(uri).isDirectory()){
							mindmapFiles.addAll(Tools.getFilteredFileList(uri, new CustomFileFilter(".*[.][mM][mM]"), isMonitorSubDirectories(target))); //$NON-NLS-1$
						}
						else{
							mindmapFiles.add(uri);
						}
					}
					if(!mindmapFiles.contains(target.getMap().getFile().toURI())){
						mindmapFiles.add(target.getMap().getFile().toURI());
					}
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.33"));	 //$NON-NLS-1$
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
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.34") + mapsToUpdate.size() + TextUtils.getText("AbstractMonitoringAction.35")); //$NON-NLS-1$ //$NON-NLS-2$
						if(!MapConverter.convert(mapsToUpdate)){							
							fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, TextUtils.getText("AbstractMonitoringAction.36")); //$NON-NLS-1$
						}
					}
					else{
						fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, TextUtils.getText("AbstractMonitoringAction.36")); //$NON-NLS-1$
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
						return ResourceController.getResourceController().getBooleanProperty("docear_subdir_monitoring");			 //$NON-NLS-1$
				}
			}
			
			private String getMessage(List<MapModel> mapsToConvert){
				if(mapsToConvert.size() > 1){
					return mapsToConvert.size() + TextUtils.getText("AbstractMonitoringAction.39")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else if (mapsToConvert.size() == 1){
					return mapsToConvert.get(0).getTitle() + TextUtils.getText("AbstractMonitoringAction.41")+TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ""; //$NON-NLS-1$
			}
			
			private String getTitle(List<MapModel> mapsToConvert){
				if(mapsToConvert.size() > 1){
					return mapsToConvert.size() + TextUtils.getText("AbstractMonitoringAction.44"); //$NON-NLS-1$
				}
				else if (mapsToConvert.size() == 1){
					return mapsToConvert.get(0).getTitle() + TextUtils.getText("AbstractMonitoringAction.45"); //$NON-NLS-1$
				}
				return ""; //$NON-NLS-1$
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
}