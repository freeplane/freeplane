package org.docear.plugin.pdfutilities;

import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.net.URL;

import javax.swing.JMenu;

import org.docear.plugin.pdfutilities.actions.DocearPasteAction;
import org.docear.plugin.pdfutilities.actions.ImportAllAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.ImportNewAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.RadioButtonAction;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.INodeViewLifeCycleListener;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.NodeView;

public class PdfUtilitiesController {
	
	public static final String MENU_BAR = "/menu_bar";
	public static final String STYLES_MENU = "/styles";
	public static final String PDF_MANAGEMENT_MENU = "/pdf_management";
	public static final String AUTO_IMPORT_PROP_KEY = "docear_automatic_annotation_import";
	public static final String AUTO_IMPORT_LANG_KEY = "menu_auto_import_annotations";
	public static final String PDF_MANAGEMENT_MENU_LANG_KEY = "menu_pdf_utilities";
	public static final String IMPORT_ALL_ANNOTATIONS_LANG_KEY = "menu_import_all_annotations";
	public static final String IMPORT_NEW_ANNOTATIONS_LANG_KEY = "menu_import_new_annotations";
	
	
	private ModeController modecontroller;
	private ImportAllAnnotationsAction importAllAnnotationsAction;
	private ImportNewAnnotationsAction importNewAnnotationsAction;
	
	
	public PdfUtilitiesController(ModeController modeController){
		
		LogUtils.info("starting DocearPdfUtilitiesStarter(ModeController)");
		this.modecontroller = modeController;
		this.addPluginDefaults();
		this.addPluginLangResources();
		this.registerActions();		
		this.registerListener();		
		this.addMenuEntries();
	}
	
	private void registerActions() {
		this.importAllAnnotationsAction = new ImportAllAnnotationsAction(IMPORT_ALL_ANNOTATIONS_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(importAllAnnotationsAction);
		this.importNewAnnotationsAction = new ImportNewAnnotationsAction(IMPORT_NEW_ANNOTATIONS_LANG_KEY);
		this.modecontroller.getMapController().addListenerForAction(importNewAnnotationsAction);
		
		this.modecontroller.removeAction("PasteAction");
		this.modecontroller.addAction(new DocearPasteAction());
	}

	private void addMenuEntries() {
		this.modecontroller.addMenuContributor(new IMenuContributor() {
			
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
			    ResourceController resourceController =  ResourceController.getResourceController();
			    
				builder.addMenuItem(MENU_BAR + STYLES_MENU, new JMenu(TextUtils.getText(PDF_MANAGEMENT_MENU_LANG_KEY)), MENU_BAR + PDF_MANAGEMENT_MENU, MenuBuilder.AFTER);
				builder.addRadioItem(MENU_BAR + PDF_MANAGEMENT_MENU, new RadioButtonAction(AUTO_IMPORT_LANG_KEY, AUTO_IMPORT_PROP_KEY), resourceController.getBooleanProperty(AUTO_IMPORT_PROP_KEY));
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importAllAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importNewAnnotationsAction, MenuBuilder.AS_CHILD);
			}
		});
	}

	private void registerListener() {
		this.modecontroller.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener(){
			
			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView)nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new DocearNodeDropListener());
				dropTarget.setActive(true);			
			}

			public void onViewRemoved(Container nodeView) {
				// TODO Auto-generated method stub				
			}
			
		});
	}	

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
	
	private void addPluginLangResources(){
		ResourceBundles resBundle = ((ResourceBundles)ResourceController.getResourceController().getResources());
		
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+lang+".properties");
		resBundle.addResources(resBundle.getLanguageCode(), res);		
	}

	
}
