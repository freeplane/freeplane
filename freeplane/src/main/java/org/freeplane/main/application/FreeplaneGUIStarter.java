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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.explorer.mindmapmode.MapExplorerController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.NextNodeAction;
import org.freeplane.features.filter.NextPresentationItemAction;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.help.HelpController;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.QuitAction;
import org.freeplane.features.mode.filemode.FModeController;
import org.freeplane.features.mode.mindmapmode.LoadAcceleratorPresetsAction;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.print.PrintController;
import org.freeplane.features.styles.LogicalStyleFilterController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.TextController;
import org.freeplane.features.time.TimeController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.main.application.CommandLineParser.Options;
import org.freeplane.main.application.survey.FreeplaneSurveyProperties;
import org.freeplane.main.application.survey.SurveyRunner;
import org.freeplane.main.application.survey.SurveyStarter;
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

	@Override
	public void setDontLoadLastMaps() {
		dontLoadLastMaps = true;
    }

	@Override
	@SuppressWarnings("serial")
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
			final boolean supportHidpi = UITools.shouldScaleUIFonts();
			FrameController.setLookAndFeel(lookandfeel, supportHidpi);
			final JFrame frame;
			frame = new JFrame("Freeplane");
			frame.setContentPane(new JPanel(){

				@Override
				protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
					return super.processKeyBinding(ks, e, condition, pressed)
							|| MenuKeyProcessor.INSTANCE.processKeyBinding(ks, e, condition, pressed);
				}

			});
			frame.setName(UITools.MAIN_FREEPLANE_FRAME);
			final MMapViewController mapViewController = new MMapViewController(controller);
			viewController = new ApplicationViewController(controller, mapViewController, frame);
			splash = new FreeplaneSplashModern(frame);
			if (!System.getProperty("org.freeplane.nosplash", "false").equals("true")) {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						splash.setVisible(true);
					}
				});
			}
			mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
			controller.addExtension(HighlightController.class, new HighlightController());
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
			IconController.installConditionControllers();
			HelpController.install();
			controller.addAction(new NextNodeAction(Direction.FORWARD));
			controller.addAction(new NextNodeAction(Direction.BACK));
			controller.addAction(new NextNodeAction(Direction.FORWARD_N_FOLD));
			controller.addAction(new NextNodeAction(Direction.BACK_N_FOLD));
			controller.addAction(NextPresentationItemAction.createFoldingAction());
			controller.addAction(NextPresentationItemAction.createNotFoldingAction());
			controller.addAction(new ShowSelectionAsRectangleAction());
			controller.addAction(new ViewLayoutTypeAction(MapViewLayout.OUTLINE));
			FilterController.getCurrentFilterController().getConditionFactory().addConditionController(70,
			    new LogicalStyleFilterController());
			MapController.install();
			NodeHistory.install(controller);
			MapExplorerController.installFilterConditions();
			final FreeplaneSurveyProperties freeplaneSurveyProperties = new FreeplaneSurveyProperties();
			if(freeplaneSurveyProperties.mayAskUserToFillSurveys()) {
				controller.addApplicationLifecycleListener(new SurveyStarter(freeplaneSurveyProperties, new SurveyRunner(freeplaneSurveyProperties), Math.random()));
			}
			return controller;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void createModeControllers(final Controller controller) {
		MModeControllerFactory.createModeController();
		final ModeController mindMapModeController = controller.getModeController(MModeController.MODENAME);
		LastOpenedList lastOpenedList = applicationResourceController.getLastOpenedList();
		mindMapModeController.getMapController().addMapChangeListener(lastOpenedList);
		lastOpenedList.registerMenuContributor(mindMapModeController);
		mindMapModeController.addUiBuilder(Phase.ACTIONS, "filterConditions", FilterController
		    .getController(controller)
		    .getMenuBuilder(), new ChildActionEntryRemover(controller));
		FModeControllerFactory.createModeController();
    }

	@Override
	public void buildMenus(final Controller controller, final Set<String> plugins) {
		LoadAcceleratorPresetsAction.install(controller.getModeController(MModeController.MODENAME));
	    buildMenus(controller, plugins, MModeController.MODENAME, "/xml/mindmapmodemenu.xml");
	    buildMenus(controller, plugins, FModeController.MODENAME, "/xml/filemodemenu.xml");
	    ResourceController.getResourceController().getAcceleratorManager().loadAcceleratorPresets();
    }

	private void buildMenus(final Controller controller, final Set<String> plugins, String mode, String xml) {
		ModeController modeController = controller.getModeController(mode);
		controller.selectModeForBuild(modeController);
		modeController.updateMenus(xml, plugins);
		final FreeplaneMenuBar menuBar = modeController.getUserInputListenerFactory().getMenuBar();
		MnemonicSetter.INSTANCE.setComponentMnemonics(menuBar);
		controller.selectModeForBuild(null);
	}

	@Override
	public void createFrame(final String[] args) {
		Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController(MModeController.MODENAME);
		controller.selectModeForBuild(modeController);
		EventQueue.invokeLater(new Runnable() {
			@Override
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
				loadMaps(CommandLineParser.parse(args, false).getFilesToOpenAsArray());
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

		        UITools.executeWhenNodeHasFocus(new Runnable() {
					@Override
					public void run() {
		                fireStartupFinished();
		                MenuUtils.executeMenuItems(options.getMenuItemsToExecute());
		                if(options.shouldStopAfterLaunch())
		                	System.exit(0);
					}
				});
            }

			private void focusCurrentView() {
				final MapView currentMapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
				if(currentMapView != null){
					viewController.focusTo(currentMapView);
				}
            }
		});
	}

	private void fireStartupFinished() {
		Controller.getCurrentController().fireStartupFinished();
	}

	private void loadMaps( final String[] args) {
		final Controller controller = Controller.getCurrentController();
		final boolean alwaysLoadLastMaps = ResourceController.getResourceController().getBooleanProperty(
		    "always_load_last_maps");

		if (alwaysLoadLastMaps && !dontLoadLastMaps) {
			loadLastMaps();
		}
		loadMaps(controller, args);
		if (controller.getMap() == null && !alwaysLoadLastMaps && !dontLoadLastMaps) {
			final AddOnsController addonsController = AddOnsController.getController();
			addonsController.setAutoInstallEnabled(false);
			loadLastMaps();
			addonsController.setAutoInstallEnabled(true);
		}
		final ModeController modeController = Controller.getCurrentModeController();
		if(firstRun && ! dontLoadLastMaps){
			final String map = ResourceController.getResourceController().getProperty("whatsnew_map");
			((MMapController)modeController.getMapController()).newDocumentationMap(map);
		}
		if (null != controller.getMap()) {
			return;
		}
		controller.selectMode(MModeController.MODENAME);
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

	@Override
	public void loadMapsLater(final String[] args){
	    EventQueue.invokeLater(new Runnable() {

            @Override
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
				final LinkController linkController = LinkController.getController();
				linkController.loadMap(fileArgument);
			}
			catch (final Exception ex) {
				System.err.println("File " + fileArgument + " not loaded");
			}
		}
    }

   @Override
	public void stop() {
		try {
			if (EventQueue.isDispatchThread()) {
				Controller.getCurrentController().shutdown();
				return;
			}
			EventQueue.invokeAndWait(new Runnable() {
				@Override
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

	@Override
	public ResourceController getResourceController() {
	    return applicationResourceController;
    }
}
