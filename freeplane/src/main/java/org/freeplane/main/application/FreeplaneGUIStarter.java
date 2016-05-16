/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.application;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.NextNodeAction;
import org.freeplane.features.filter.NextPresentationItemAction;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.help.HelpController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.QuitAction;
import org.freeplane.features.mode.browsemode.BModeController;
import org.freeplane.features.mode.filemode.FModeController;
import org.freeplane.features.mode.mindmapmode.LoadAcceleratorPresetsAction;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.print.PrintController;
import org.freeplane.features.styles.LogicalStyleFilterController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.TextController;
import org.freeplane.features.time.TimeController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.url.FreeplaneUriConverter;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.main.application.CommandLineParser.Options;
import org.freeplane.main.browsemode.BModeControllerFactory;
import org.freeplane.main.filemode.FModeControllerFactory;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.ViewLayoutTypeAction;
import org.freeplane.view.swing.map.mindmapmode.MMapViewController;

public class FreeplaneGUIStarter implements FreeplaneStarter {

	private static String RESOURCE_BASE_DIRECTORY;
	private static String INSTALLATION_BASE_DIRECTORY;
	static {
		try {
			RESOURCE_BASE_DIRECTORY = new File(System.getProperty(ApplicationResourceController.FREEPLANE_GLOBALRESOURCEDIR_PROPERTY,
			ApplicationResourceController.DEFAULT_FREEPLANE_GLOBALRESOURCEDIR)).getCanonicalPath();
			INSTALLATION_BASE_DIRECTORY = new File(System.getProperty(ApplicationResourceController.FREEPLANE_BASEDIRECTORY_PROPERTY, RESOURCE_BASE_DIRECTORY + "/..")).getCanonicalPath();
		} catch (IOException e) {
		}
	}
	

	static{
		Compat.fixMousePointerForLinux();
	}

	public static String getResourceBaseDir() {
		return RESOURCE_BASE_DIRECTORY;
	}

	public static String getInstallationBaseDir() {
		return INSTALLATION_BASE_DIRECTORY;
	}

	public static void showSysInfo() {
		final StringBuilder info = new StringBuilder();
		info.append("freeplane_version = ");
		info.append(FreeplaneVersion.getVersion());
		String revision = FreeplaneVersion.getVersion().getRevision();

		info.append("; freeplane_xml_version = ");
		info.append(FreeplaneVersion.XML_VERSION);
		if(! revision.equals("")){
			info.append("\ngit revision = ");
			info.append(revision);
		}
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
		LogUtils.info(info.toString());
	}

	private final ApplicationResourceController applicationResourceController;
// // 	private Controller controller;
	private FreeplaneSplashModern splash = null;
    private boolean startupFinished = false;
	private ApplicationViewController viewController;
	/** allows to disable loadLastMap(s) if there already is a second instance running. */
	private boolean dontLoadLastMaps;
	final private boolean firstRun;
	private static final String LOAD_LAST_MAPS = "load_last_maps";
	private static final String LOAD_LAST_MAP = "load_last_map";
	final private Options options;

	public FreeplaneGUIStarter(String[] args) {
		super();
		options = CommandLineParser.parse(args, true);
		final File userPreferencesFile = ApplicationResourceController.getUserPreferencesFile();
		firstRun = !userPreferencesFile.exists();
		new UserPropertiesUpdater().importOldProperties();
		applicationResourceController = new ApplicationResourceController();
	}

	public void setDontLoadLastMaps() {
		dontLoadLastMaps = true;
    }

	public Controller createController() {
		try {
			Controller controller = new Controller(applicationResourceController);
			Controller.setCurrentController(controller);
			Compat.macAppChanges();
			controller.addAction(new QuitAction());
			applicationResourceController.init();
			LogUtils.createLogger();
			FreeplaneGUIStarter.showSysInfo();
			final String lookandfeel = System.getProperty("lookandfeel", applicationResourceController
			    .getProperty("lookandfeel"));
			FrameController.setLookAndFeel(lookandfeel);
			final JFrame frame;
			frame = new JFrame("Freeplane");
			frame.setName(UITools.MAIN_FREEPLANE_FRAME);
			splash = new FreeplaneSplashModern(frame);
			if (!System.getProperty("org.freeplane.nosplash", "false").equals("true")) {
				splash.setVisible(true);
			}
			final MMapViewController mapViewController = new MMapViewController(controller);
			viewController = new ApplicationViewController(controller, mapViewController, frame);
			mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
			FilterController.install();
			PrintController.install();
			FormatController.install(new FormatController());
	        final ScannerController scannerController = new ScannerController();
	        ScannerController.install(scannerController);
	        scannerController.addParsersForStandardFormats();
			ModelessAttributeController.install();
			TextController.install();
			TimeController.install();
			LinkController.install();
			IconController.install();
			HelpController.install();
			controller.addAction(new NextNodeAction(Direction.FORWARD));
			controller.addAction(new NextNodeAction(Direction.BACK));
			controller.addAction(new NextNodeAction(Direction.FORWARD_N_FOLD));
			controller.addAction(new NextNodeAction(Direction.BACK_N_FOLD));
			controller.addAction(new NextPresentationItemAction());
			controller.addAction(new ShowSelectionAsRectangleAction());
			controller.addAction(new ViewLayoutTypeAction(MapViewLayout.OUTLINE));
			FilterController.getCurrentFilterController().getConditionFactory().addConditionController(70,
			    new LogicalStyleFilterController());
			MapController.install();

			NodeHistory.install(controller);
			return controller;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			throw new RuntimeException(e);
		}
	}

	private void initIcons(ResourceController resourceController) {
        resourceController.setDefaultProperty("ResetNodeLocationAction.icon", "/images/ribbons/nodes/NodesSettings-ResetPosition.png");
        resourceController.setDefaultProperty("SetBooleanPropertyAction.edit_on_double_click.icon", "/images/ribbons/nodes/Nodes-EditOnDblClick.png");
        resourceController.setDefaultProperty("NewFreeNodeAction.icon", "/images/ribbons/nodes/Nodes-NewFreenode.png");
        resourceController.setDefaultProperty("FreeNodeAction.icon", "/images/ribbons/nodes/NodesSettings-Freenode.png");
        resourceController.setDefaultProperty("NewSummaryAction.icon", "/images/ribbons/nodes/Nodes-NewSummaryNode.png");
        resourceController.setDefaultProperty("ChangeNodeLevelLeftsAction.icon", "/images/ribbons/nodes/Nodes-MoveLeft.png");
        resourceController.setDefaultProperty("ChangeNodeLevelRightsAction.icon", "/images/ribbons/nodes/nodes-MoveRight.png");
        resourceController.setDefaultProperty("NodeUpAction.icon", "/images/ribbons/nodes/nodes-MoveUp.png");
        resourceController.setDefaultProperty("NodeDownAction.icon", "/images/ribbons/nodes/nodes-MoveDown.png");
        resourceController.setDefaultProperty("AlwaysUnfoldedNodeAction.icon", "/images/ribbons/nodes/nodes-AlwaysUnfolded.png");

        resourceController.setDefaultProperty("ToggleFoldedAction.icon", "/images/ribbons/navigate/navigate-NodesUn-fold.png");

        resourceController.setDefaultProperty("LatexEditLatexAction.icon", "/images/ribbons/resources/Resources-LaTeXFormulaEdit.png");
        resourceController.setDefaultProperty("LatexDeleteLatexAction.icon", "/images/ribbons/resources/Resources-LaTeXFormulaRemove.png");
        resourceController.setDefaultProperty("ExternalImageAddAction.icon", "/images/ribbons/resources/Resources-AddImage.png");

        resourceController.setDefaultProperty("freeplaneAddOnLocationAction.icon", "/images/ribbons/tools/ToolsAndSettings-DocearAddOns.png");
        resourceController.setDefaultProperty("SetAcceleratorOnNextClickAction.icon", "/images/ribbons/tools/Tools-AssignHotkey.png");
        resourceController.setDefaultProperty("OpenMapsAddLocation.icon", "/images/ribbons/tools/tools-AddOpenMaps.png");
        resourceController.setDefaultProperty("OpenMapsRemoveLocation.icon", "/images/ribbons/tools/tools-RemoveOpenMaps.png");
        resourceController.setDefaultProperty("OpenMapsViewLocation.icon", "/images/ribbons/tools/tools-ViewOpenMaps.png");
        resourceController.setDefaultProperty("formula.menuname.icon", "/images/ribbons/tools/tools-formulas.png");
        resourceController.setDefaultProperty("menu_encryption.icon", "/images/ribbons/tools/tools-PasswordProtection.png");
        resourceController.setDefaultProperty("menu_time.icon", "/images/ribbons/tools/tools-TimeManagement.png");
        resourceController.setDefaultProperty("scripting.icon", "/images/ribbons/tools/tools-Scripting.png");

        resourceController.setDefaultProperty("attribute_options.icon", "/images/ribbons/view/view-AttributeOptions.png");
        resourceController.setDefaultProperty("ShowHideNoteAction.icon", "/images/ribbons/view/view-showNotePanel.png");
        resourceController.setDefaultProperty("ToggleDetailsAction.icon", "/images/ribbons/view/view-hideNoteDetails.png");
        resourceController.setDefaultProperty("note_window_location.icon", "/images/ribbons/view/view-NotePanelPosition.png");
        resourceController.setDefaultProperty("menu_noteView.icon", "/images/ribbons/view/view-NotesSettings.png");
        resourceController.setDefaultProperty("SetBooleanPropertyAction.highlight_formulas.icon", "/images/ribbons/view/view-HighlightFormulas.png");
        resourceController.setDefaultProperty("SetBooleanPropertyAction.show_node_tooltips.icon", "/images/ribbons/view/view-DisplayTooltips.png");
        resourceController.setDefaultProperty("SetBooleanPropertyAction.show_styles_in_tooltip.icon", "/images/ribbons/view/view-displayNodeStylesTooltips.png");
        resourceController.setDefaultProperty("ToggleFBarAction.icon", "/images/ribbons/view/view-FBar.png");
        resourceController.setDefaultProperty("ToggleStatusAction.icon", "/images/ribbons/view/view-Statusline.png");
        resourceController.setDefaultProperty("ToggleScrollbarsAction.icon", "/images/ribbons/view/view-Scrollbars.png");
        resourceController.setDefaultProperty("ToggleLeftToolbarAction.icon", "/images/ribbons/view/view-IconToolbar.png");
        resourceController.setDefaultProperty("ToggleFullScreenAction.icon", "/images/ribbons/view/view-FullScreen.png");
        resourceController.setDefaultProperty("ToggleRibbonAction.icon", "/images/ribbons/view/view-MinimizeRibbon.png");
        resourceController.setDefaultProperty("SetBooleanPropertyAction.presentation_mode.icon", "/images/ribbons/view/view-PresentationMode.png");


        resourceController.setDefaultProperty("OpenFreeplaneSiteAction.icon", "/images/ribbons/help/help-Homepage.png");
        resourceController.setDefaultProperty("AboutAction.icon", "/images/ribbons/help/help-about.png");
        resourceController.setDefaultProperty("AskForHelp.icon", "/images/ribbons/help/help-ask4help.png");
        resourceController.setDefaultProperty("RequestFeatureAction.icon", "/images/ribbons/help/help-requestFeature.png");
        resourceController.setDefaultProperty("ReportBugAction.icon", "/images/ribbons/help/help-bugReport.png");
        resourceController.setDefaultProperty("HotKeyInfoAction.icon", "/images/ribbons/help/help-keyReference.png");
        resourceController.setDefaultProperty("UpdateCheckAction.icon", "/images/ribbons/help/help-check4updates.png");
        resourceController.setDefaultProperty("OpenUserDirAction.icon", "/images/ribbons/help/help-openUserDirectory.png");
        resourceController.setDefaultProperty("GettingStartedAction.icon", "/images/ribbons/help/help-tutorial.png");

        resourceController.setDefaultProperty("OpenLogsFolderAction.icon", "/images/ribbons/help/help-showSystemLog.png");
        resourceController.setDefaultProperty("ManualAction.icon", "/images/ribbons/help/help-Manual.png");
        resourceController.setDefaultProperty("ContactAction.icon", "/images/ribbons/help/help-contact.png");
        resourceController.setDefaultProperty("FAQAction.icon", "/images/ribbons/help/help-faq.png");

	}

	public void createModeControllers(final Controller controller) {
		MModeControllerFactory.createModeController();
		final ModeController mindMapModeController = controller.getModeController(MModeController.MODENAME);
		LastOpenedList lastOpenedList = applicationResourceController.getLastOpenedList();
		mindMapModeController.getMapController().addMapChangeListener(lastOpenedList);
		lastOpenedList.registerMenuContributor(mindMapModeController);
		mindMapModeController.addUiBuilder(Phase.ACTIONS, "filterConditions", FilterController
		    .getController(controller)
		    .getMenuBuilder(), new ChildActionEntryRemover(controller));
		BModeControllerFactory.createModeController();
		FModeControllerFactory.createModeController();
    }

	public void buildMenus(final Controller controller, final Set<String> plugins) {
		LoadAcceleratorPresetsAction.install(controller.getModeController(MModeController.MODENAME));
	    buildMenus(controller, plugins, MModeController.MODENAME, "/xml/mindmapmodemenu.xml");
	    buildMenus(controller, plugins, BModeController.MODENAME, "/xml/browsemodemenu.xml");
	    buildMenus(controller, plugins, FModeController.MODENAME, "/xml/filemodemenu.xml");
    }

	private void buildMenus(final Controller controller, final Set<String> plugins, String mode, String xml) {
		ModeController modeController = controller.getModeController(mode);
		controller.selectModeForBuild(modeController);
		modeController.updateMenus(xml, plugins);
		final FreeplaneMenuBar menuBar = modeController.getUserInputListenerFactory().getMenuBar();
		MnemonicSetter.INSTANCE.setComponentMnemonics(menuBar);
		controller.selectModeForBuild(null);
	}

	public void createFrame(final String[] args) {
		Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController(MModeController.MODENAME);
		controller.selectModeForBuild(modeController);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				viewController.init(Controller.getCurrentController());
				splash.toBack();
				final JFrame frame = (JFrame) viewController.getMenuComponent();
				final int extendedState = frame.getExtendedState();
				Container contentPane = frame.getContentPane();
				contentPane.setVisible(false);
				splash.dispose();
				splash = null;
				ToolTipManager.sharedInstance().setDismissDelay(12000);
				frame.setVisible(true);
				if (extendedState != frame.getExtendedState()) {
					frame.setExtendedState(extendedState);
				}
				loadMaps(options.getFilesToOpenAsArray());
				focusCurrentView();
				contentPane.setVisible(true);
				frame.toFront();
				startupFinished = true;
		        System.setProperty("nonInteractive", Boolean.toString(options.isNonInteractive()));
		        try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fireStartupFinished();
                MenuUtils.executeMenuItems(options.getMenuItemsToExecute());
            }

			private void focusCurrentView() {
				final MapView currentMapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
				if(currentMapView != null){
					viewController.focusTo(currentMapView);
				}
            }
		});
	}

	protected void fireStartupFinished() {
		for (ApplicationLifecycleListener listener : Controller.getCurrentController().getApplicationLifecycleListeners()) {
			listener.onStartupFinished();
		}
	}

	private void loadMaps( final String[] args) {
		final Controller controller = Controller.getCurrentController();
		final boolean alwaysLoadLastMaps = ResourceController.getResourceController().getBooleanProperty(
		    "always_load_last_maps");

		if (alwaysLoadLastMaps && !dontLoadLastMaps) {
			loadLastMaps();
		}
		loadMaps(controller, args);
		if(controller.getMap() != null) {
			return;
		}
		if (!alwaysLoadLastMaps && !dontLoadLastMaps) {
			final AddOnsController addonsController = AddOnsController.getController();
			addonsController.setAutoInstallEnabled(false);
			loadLastMaps();
			addonsController.setAutoInstallEnabled(true);
		}
		if(firstRun && ! dontLoadLastMaps){
			final File baseDir = new File(FreeplaneGUIStarter.getResourceBaseDir()).getAbsoluteFile().getParentFile();
			final String map = ResourceController.getResourceController().getProperty("whatsnew_map");
			final File absolutFile = ConfigurationUtils.getLocalizedFile(new File[]{baseDir}, map, Locale.getDefault().getLanguage());
			if(absolutFile != null)
				loadMaps(controller, new String[]{absolutFile.getAbsolutePath()});
		}
		if (null != controller.getMap()) {
			return;
		}
		controller.selectMode(MModeController.MODENAME);
		final MModeController modeController = (MModeController) controller.getModeController();
		MFileManager.getController(modeController).newMapFromDefaultTemplate();
	}

	private void loadLastMaps() {
	    final boolean loadLastMap = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAP);
	    final boolean loadLastMaps = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAPS);
	    if(loadLastMaps)
	    	viewController.openMapsOnStart();
	    if(loadLastMaps || loadLastMap)
	    	applicationResourceController.getLastOpenedList().openLastMapOnStart();
    }

	public void loadMapsLater(final String[] args){
	    EventQueue.invokeLater(new Runnable() {

            public void run() {
                if(startupFinished && EventQueue.isDispatchThread()){
                    loadMaps(Controller.getCurrentController(), args);
                    toFront();
                    return;
                }
                EventQueue.invokeLater(this);
            }
        });
	}

    private void toFront() {
    	final Component menuComponent = UITools.getMenuComponent();
    	if(menuComponent instanceof Frame) {
    		final Frame frame = (Frame) menuComponent;
    		final int state = frame.getExtendedState();
    		if ((state & Frame.ICONIFIED) != 0)
    			frame.setExtendedState(state & ~Frame.ICONIFIED);
    	}
    	if(menuComponent instanceof Window) {
    		Window window = (Window) menuComponent;
    		if (!window.isVisible())
    			window.setVisible(true);
    		window.toFront();
    		window.requestFocus();
    	}
    }


    private void loadMaps(final Controller controller, final String[] args) {
		controller.selectMode(MModeController.MODENAME);
		for (int i = 0; i < args.length; i++) {
			String fileArgument = args[i];
			try {
				final URL url;
				if(fileArgument.startsWith("http://")) {
					LinkController.getController().loadURI(new URI(fileArgument));
				}
                else if (fileArgument.startsWith(UrlManager.FREEPLANE_SCHEME + ':')) {
					String fixedUri = new FreeplaneUriConverter().fixPartiallyDecodedFreeplaneUriComingFromInternetExplorer(fileArgument);
					LinkController.getController().loadURI(new URI(fixedUri));
				}
                else {
					if (!FileUtils.isAbsolutePath(fileArgument)) {
						fileArgument = System.getProperty("user.dir") + System.getProperty("file.separator") + fileArgument;
					}
					url = Compat.fileToUrl(new File(fileArgument));
					if (url.getPath().toLowerCase().endsWith(
						org.freeplane.features.url.UrlManager.FREEPLANE_FILE_EXTENSION)) {
						final MModeController modeController = (MModeController) controller.getModeController();
						MapController mapController = modeController.getMapController();
						mapController.openMapSelectReferencedNode(url);
					}
				}
			}
			catch (final Exception ex) {
				System.err.println("File " + fileArgument + " not loaded");
			}
		}
    }

	/**
	 */
	public void run(final String[] args) {
		try {
			if (null == System.getProperty("org.freeplane.core.dir.lib", null)) {
				System.setProperty("org.freeplane.core.dir.lib", "/lib/");
			}
			final Controller controller = createController();
			createModeControllers(controller);
			FilterController.getController(controller).loadDefaultConditions();
			final Set<String> emptySet = Collections.emptySet();
			buildMenus(controller, emptySet);
			createFrame(args);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			JOptionPane.showMessageDialog(UITools.getMenuComponent(), "freeplane.main.Freeplane can't be started",
			    "Startup problem", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public void stop() {
		try {
			if (EventQueue.isDispatchThread()) {
				Controller.getCurrentController().shutdown();
				return;
			}
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					Controller.getCurrentController().shutdown();
				}
			});
		}
		catch (final InterruptedException e) {
			LogUtils.severe(e);
		}
		catch (final InvocationTargetException e) {
			LogUtils.severe(e);
		}
	}

	public ResourceController getResourceController() {
	    return applicationResourceController;
    }
}
