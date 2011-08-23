package org.docear.plugin.pdfutilities;

import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JRadioButton;

import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.pdfutilities.actions.AbstractMonitoringAction;
import org.docear.plugin.pdfutilities.actions.AddMonitoringFolderAction;
import org.docear.plugin.pdfutilities.actions.DocearPasteAction;
import org.docear.plugin.pdfutilities.actions.ImportAllAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.ImportNewAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.RadioButtonAction;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.docear.plugin.pdfutilities.listener.DocearNodeMouseMotionListener;
import org.docear.plugin.pdfutilities.listener.DocearNodeSelectionListener;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.docear.plugin.pdfutilities.util.ExeFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.resources.components.RadioButtonProperty;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.view.swing.map.NodeView;

public class PdfUtilitiesController extends ALanguageController{

	public static final String OPEN_ON_PAGE_READER_PATH_KEY = "docear_open_on_page_reader_path"; //$NON-NLS-1$
	public static final String OPEN_PDF_VIEWER_ON_PAGE_KEY = "docear_open_on_page"; //$NON-NLS-1$
	public static final String OPEN_INTERNAL_PDF_VIEWER_KEY = "docear_open_internal"; //$NON-NLS-1$
	public static final String OPEN_STANDARD_PDF_VIEWER_KEY = "docear_open_standard"; //$NON-NLS-1$
	public static final String AUTO_IMPORT_ANNOTATIONS_KEY = "docear_automatic_annotation_import"; //$NON-NLS-1$
	public static final String IMPORT_BOOKMARKS_KEY = "docear_import_bookmarks"; //$NON-NLS-1$
	public static final String IMPORT_COMMENTS_KEY = "docear_import_comments"; //$NON-NLS-1$
	public static final String IMPORT_HIGHLIGHTED_TEXTS_KEY = "docear_import_highlighted_text"; //$NON-NLS-1$
	public static final String OPEN_ON_PAGE_WARNING_KEY = "OptionPanel.docear_open_on_page_reader_path_warning"; //$NON-NLS-1$
	public static final String OPEN_ON_PAGE_ERROR_KEY = "OptionPanel.docear_open_on_page_reader_path_error"; //$NON-NLS-1$
	
	public static final String MENU_BAR = "/menu_bar"; //$NON-NLS-1$
	public static final String NODE_POPUP_MENU = "/node_popup"; //$NON-NLS-1$
	public static final String NODE_FEATURES_MENU = "/node_features"; //$NON-NLS-1$
	public static final String STYLES_MENU = "/styles"; //$NON-NLS-1$
	public static final String PDF_MANAGEMENT_MENU = "/pdf_management"; //$NON-NLS-1$
	public static final String MONITORING_MENU = "/monitoring"; //$NON-NLS-1$
	public static final String AUTO_IMPORT_LANG_KEY = "menu_auto_import_annotations"; //$NON-NLS-1$
	public static final String PDF_MANAGEMENT_MENU_LANG_KEY = "menu_pdf_utilities"; //$NON-NLS-1$
	public static final String MONITORING_MENU_LANG_KEY = "menu_monitoring_utilities"; //$NON-NLS-1$
	public static final String IMPORT_ALL_ANNOTATIONS_LANG_KEY = "menu_import_all_annotations"; //$NON-NLS-1$
	public static final String IMPORT_NEW_ANNOTATIONS_LANG_KEY = "menu_import_new_annotations"; //$NON-NLS-1$
	public static final String ADD_MONITORING_FOLDER_LANG_KEY = "menu_import_add_monitoring_folder"; //$NON-NLS-1$
	public static final String UPDATE_MONITORING_FOLDER_LANG_KEY = "menu_import_update_monitoring_folder"; //$NON-NLS-1$

	private ModeController modecontroller;
	private ImportAllAnnotationsAction importAllAnnotationsAction;
	private ImportNewAnnotationsAction importNewAnnotationsAction;
	private AbstractMonitoringAction addMonitoringFolderAction;
	private UpdateMonitoringFolderAction updateMonitoringFolderAction;

	public PdfUtilitiesController(ModeController modeController) {
		super();

		LogUtils.info("starting DocearPdfUtilitiesStarter(ModeController)"); //$NON-NLS-1$
		this.modecontroller = modeController;
		
		this.addPropertiesToOptionPanel();
		this.addPluginDefaults();
		this.registerController();
		this.registerActions();
		this.registerListener();
		this.addMenuEntries();		
	}

	private void registerController() {
		AnnotationController.install(new AnnotationController(modecontroller));		
	}

	private void registerActions() {
		this.importAllAnnotationsAction = new ImportAllAnnotationsAction(IMPORT_ALL_ANNOTATIONS_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(importAllAnnotationsAction);
		this.importNewAnnotationsAction = new ImportNewAnnotationsAction(IMPORT_NEW_ANNOTATIONS_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(importNewAnnotationsAction);
		this.addMonitoringFolderAction = new AddMonitoringFolderAction(ADD_MONITORING_FOLDER_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(addMonitoringFolderAction);
		this.updateMonitoringFolderAction = new UpdateMonitoringFolderAction(UPDATE_MONITORING_FOLDER_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(updateMonitoringFolderAction);

		this.modecontroller.removeAction("PasteAction");
		this.modecontroller.addAction(new DocearPasteAction());
	}

	private void addMenuEntries() {
		this.modecontroller.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				ResourceController resourceController = ResourceController.getResourceController();

				builder.addMenuItem(MENU_BAR + STYLES_MENU, new JMenu(TextUtils.getText(PDF_MANAGEMENT_MENU_LANG_KEY)), MENU_BAR
						+ PDF_MANAGEMENT_MENU, MenuBuilder.AFTER);				
				
				builder.addRadioItem(MENU_BAR + PDF_MANAGEMENT_MENU, new RadioButtonAction(AUTO_IMPORT_LANG_KEY,
						AUTO_IMPORT_ANNOTATIONS_KEY), resourceController.getBooleanProperty(AUTO_IMPORT_ANNOTATIONS_KEY));				
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importAllAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importNewAnnotationsAction, MenuBuilder.AS_CHILD);
				
				builder.addMenuItem(MENU_BAR + PDF_MANAGEMENT_MENU,
						new JMenu(TextUtils.getText(MONITORING_MENU_LANG_KEY)), MENU_BAR + PDF_MANAGEMENT_MENU + MONITORING_MENU,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU + MONITORING_MENU, addMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU + MONITORING_MENU, updateMonitoringFolderAction, MenuBuilder.AS_CHILD);
				
				builder.addMenuItem(NODE_POPUP_MENU + NODE_FEATURES_MENU,
						new JMenu(TextUtils.getText(MONITORING_MENU_LANG_KEY)), NODE_POPUP_MENU + MONITORING_MENU,
						MenuBuilder.BEFORE);
				builder.addAction(NODE_POPUP_MENU + MONITORING_MENU, addMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + MONITORING_MENU, updateMonitoringFolderAction, MenuBuilder.AS_CHILD);
				
				builder.addMenuItem(NODE_POPUP_MENU + NODE_FEATURES_MENU,
						new JMenu(TextUtils.getText(PDF_MANAGEMENT_MENU_LANG_KEY)), NODE_POPUP_MENU + PDF_MANAGEMENT_MENU,
						MenuBuilder.BEFORE);
				builder.addSeparator(NODE_POPUP_MENU + "/DeleteAction", MenuBuilder.AFTER);
				builder.addAction(NODE_POPUP_MENU + PDF_MANAGEMENT_MENU, importAllAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(NODE_POPUP_MENU + PDF_MANAGEMENT_MENU, importNewAnnotationsAction, MenuBuilder.AS_CHILD);				
			}
		});
	}

	private void registerListener() {
		this.modecontroller.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener() {

			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView) nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new DocearNodeDropListener());
				dropTarget.setActive(true);
				
				IMouseListener defaultMouseListener = modecontroller.getUserInputListenerFactory().getNodeMouseMotionListener();
				IMouseListener docearMouseListener = new DocearNodeMouseMotionListener(defaultMouseListener);
				node.getMainView().removeMouseMotionListener(defaultMouseListener);
				node.getMainView().addMouseMotionListener(docearMouseListener);
				node.getMainView().removeMouseListener(defaultMouseListener);
				node.getMainView().addMouseListener(docearMouseListener);
			}

			public void onViewRemoved(Container nodeView) {
				// TODO Auto-generated method stub
			}

		});

		final OptionPanelController optionController = Controller.getCurrentController().getOptionPanelController();
		optionController.addButtonListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source != null && source instanceof JRadioButton) {
					JRadioButton radioButton = (JRadioButton) event.getSource();
					if (radioButton.getName().equals(OPEN_STANDARD_PDF_VIEWER_KEY)) {
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(true);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(false);
						((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY)).setEnabled(false);
					}
					if (radioButton.getName().equals(OPEN_INTERNAL_PDF_VIEWER_KEY)) {
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(true);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(false);
						((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY)).setEnabled(false);
					}
					if (radioButton.getName().equals(OPEN_PDF_VIEWER_ON_PAGE_KEY)) {
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) optionController.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(true);
						((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY)).setEnabled(true);
					}
				}
			}
		});

		optionController.addPropertyLoadListener(new PropertyLoadListener() {
			public void propertiesLoaded(Collection<IPropertyControl> properties) {

				((RadioButtonProperty) optionController.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals("true")) { //$NON-NLS-1$
									((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY))
											.setEnabled(false);
								}
							}
						});

				((RadioButtonProperty) optionController.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals(true)) {
									((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY))
											.setEnabled(false);
								}
							}
						});

				((RadioButtonProperty) optionController.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals(true)) {
									((IPropertyControl) optionController.getPropertyControl(OPEN_ON_PAGE_READER_PATH_KEY))
											.setEnabled(true);
								}
							}
						});

			}
		});
		
		this.modecontroller.getMapController().addNodeSelectionListener(new DocearNodeSelectionListener());
		
		DocearController.getController().addDocearEventListener(new IDocearEventListener() {
			
			public void handleEvent(DocearEvent event) {
				if(event.getType().equals(DocearEventType.NEW_LITERATURE_MAP)){
					MapModel map = (MapModel)event.getEventObject();
					try {
						NodeUtils.addMonitoringDir(map.getRootNode(), new URI("property:/workspace_pdf_location"));
						NodeUtils.addMindmapDir(map.getRootNode(), new URI("workspace:/mindmaps"));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});

	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE); //$NON-NLS-1$
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	private void addPropertiesToOptionPanel() {
		final URL preferences = this.getClass().getResource("preferences.xml");
		if (preferences == null)
			throw new RuntimeException("cannot open docear.pdf_utilities plugin preferences"); //$NON-NLS-1$
		MModeController modeController = (MModeController) Controller.getCurrentModeController();

		modeController.getOptionPanelBuilder().load(preferences);
		modeController.getOptionPanelBuilder().addValidator(new IValidator(){

			public ValidationResult validate(Properties properties) {
				ValidationResult result = new ValidationResult();
				boolean openOnPageActivated = Boolean.parseBoolean(properties.getProperty(OPEN_PDF_VIEWER_ON_PAGE_KEY));
				if(openOnPageActivated){
					String readerPath = properties.getProperty(OPEN_ON_PAGE_READER_PATH_KEY, ""); //$NON-NLS-1$
					if(!(new PdfReaderFileFilter().accept(new File(readerPath)))){
						if(new ExeFileFilter().accept(new File(readerPath))){
							result.addWarning(TextUtils.getText(OPEN_ON_PAGE_WARNING_KEY));
						}
						else{
							result.addWarning(TextUtils.getText(OPEN_ON_PAGE_ERROR_KEY));
						}
					}
				}
				return result;
			}
			
		});
	}

}
