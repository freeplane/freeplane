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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.logging.internal.LogInitializer;
import org.freeplane.features.attribute.ModelessAttributeController;
import org.freeplane.features.explorer.MapExplorerConditionController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.FreeSelectNodeAction;
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
import org.freeplane.features.map.MapModel;
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
import org.freeplane.features.url.mindmapmode.ExternalMapChangeMonitor;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.main.addons.AddOnsController;
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
	private static boolean ARE_SURVEYS_ENABLED = false;

	enum UserPropertiesStatus{
	    CURRENT_VERSION_FOUND, OLD_VERSION_FOUND, NOT_FOUND
	}


	private final ApplicationResourceController applicationResourceController;
// // 	private Controller controller;
	private FreeplaneSplashModern splash = null;
    private boolean startupFinished = false;
	private ApplicationViewController viewController;
	/** allows to disable loadLastMap(s) if there already is a second instance running. */
	private boolean dontLoadLastMaps;
	private static final String LOAD_LAST_MAPS = "load_last_maps";
	private static final String LOAD_LAST_MAP = "load_last_map";
	private static final String CREATE_NEW_MAP_IF_NO_MAPS_ARE_LOADED = "create_new_map_if_no_maps_are_loaded";
	final private CommandLineOptions options;

	private final UserPropertiesStatus userPropertiesStatus;

	private static void fixX11AppName() {
		if(! System.getProperty("java.version").startsWith("1."))
			return;
		try {
			Toolkit xToolkit = Toolkit.getDefaultToolkit();
			if (xToolkit.getClass().getName().equals("sun.awt.X11.XToolkit"))
			{
				java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
				awtAppClassNameField.setAccessible(true);
				awtAppClassNameField.set(xToolkit, "Freeplane");
			}
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			System.err.format("Couldn't set awtAppClassName: %s%n", e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	private static void fixMousePointerForLinux(){
		if (isX11WindowManager()) {
			try {
				Class<?> xwm = Class.forName("sun.awt.X11.XWM");
				Field awt_wmgr = xwm.getDeclaredField("awt_wmgr");
				awt_wmgr.setAccessible(true);
				Field other_wm = xwm.getDeclaredField("OTHER_WM");
				other_wm.setAccessible(true);
				if (awt_wmgr.get(null).equals(other_wm.get(null))) {
					Field metacity_wm = xwm.getDeclaredField("METACITY_WM");
					metacity_wm.setAccessible(true);
					awt_wmgr.set(null, metacity_wm.get(null));
				}
			}
			catch (Exception x) {
			}
		}
	}

	private static boolean isX11WindowManager() {
		return Arrays.asList("gnome-shell", "mate", "other...").contains(System.getenv("DESKTOP_SESSION"));
    }

	public FreeplaneGUIStarter(CommandLineOptions options) {
		super();
		this.options = options;
		userPropertiesStatus = new UserPropertiesUpdater().importOldProperties();
		applicationResourceController = new ApplicationResourceController();
		fixMousePointerForLinux();
		fixX11AppName();
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
			LogInitializer.createLogger();
			ApplicationResourceController.showSysInfo();
			final String systemPropertyLookandfeel = System.getProperty("lookandfeel");
			final String lookandfeel;
			if(systemPropertyLookandfeel == null) {
				applicationResourceController.addPropertyChangeListener((propertyName, newValue, oldValue) -> {
					if("lookandfeel".equals(propertyName)) {
						FrameController.setLookAndFeel(newValue);
						if(FrameController.VAQUA_LAF_CLASS_NAME.equals(newValue)) {
							final Component currentRootComponent = UITools.getMenuComponent();
							Stream.of(SwingUtilities.getRootPane(currentRootComponent).getComponents())
							.forEach(SwingUtilities::updateComponentTreeUI);
						}
						SwingUtilities.updateComponentTreeUI(UITools.getFrame());
					}
				});
				lookandfeel =  applicationResourceController.getProperty("lookandfeel");
			}
			else
				lookandfeel = systemPropertyLookandfeel;
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					FrameController.setLookAndFeel(lookandfeel);
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
					controller.addAction(new CloseAllMapsAction(mapViewController));
					controller.addAction(new CloseAllOtherMapsAction(mapViewController));


					viewController = new ApplicationViewController(controller, mapViewController, frame);
					splash = new FreeplaneSplashModern(frame);
					mapViewController.addMapViewChangeListener(applicationResourceController.getLastOpenedList());
				}
			});

			if (!System.getProperty("org.freeplane.nosplash", "false").equals("true")) {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						splash.setVisible(true);
					}
				});
			}
			viewController.invokeAndWait(() -> {
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
				Arrays.stream(FreeSelectNodeAction.Direction.values())
						 .forEach(d -> controller.addAction(new FreeSelectNodeAction(d)));
				controller.addAction(NextPresentationItemAction.createFoldingAction());
				controller.addAction(NextPresentationItemAction.createNotFoldingAction());
				controller.addAction(new ShowSelectionAsRectangleAction());
				controller.addAction(new ViewLayoutTypeAction(MapViewLayout.OUTLINE));
				FilterController.getCurrentFilterController().getConditionFactory().addConditionController(70,
				    new LogicalStyleFilterController());
				MapController.install();
				NodeHistory.install(controller);
				MapExplorerConditionController.installFilterConditions();
				final FreeplaneSurveyProperties freeplaneSurveyProperties = new FreeplaneSurveyProperties();
				if(ARE_SURVEYS_ENABLED && freeplaneSurveyProperties.mayAskUserToFillSurveys()) {
					controller.addApplicationLifecycleListener(new SurveyStarter(freeplaneSurveyProperties, new SurveyRunner(freeplaneSurveyProperties), Math.random()));
				}
			});
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
		mindMapModeController.getMapController().addUIMapChangeListener(lastOpenedList);
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
		controller.selectModeForBuild(null);
	}

	@Override
	public void createFrame() {
		Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController(MModeController.MODENAME);
		controller.selectModeForBuild(modeController);
		EventQueue.invokeLater(new Runnable() {

			private JFrame frame;
			private Container contentPane;

			@Override
			public void run() {
				viewController.init(Controller.getCurrentController());
				controller.selectMode(MModeController.MODENAME);
				showFrame();
				loadMaps();
				finishStartup();
			}

			private void showFrame() {
				splash.toBack();
				frame = (JFrame) viewController.getMenuComponent();
				final int extendedState = frame.getExtendedState();
				contentPane = frame.getContentPane();
				contentPane.setVisible(false);
				splash.dispose();
				splash = null;
				ToolTipManager.sharedInstance().setDismissDelay(12000);
				frame.setVisible(true);
				if (extendedState != frame.getExtendedState()) {
					frame.setExtendedState(extendedState);
				}
			}

			private void finishStartup() {
			    ExternalMapChangeMonitor.install(controller.getMapViewManager());
				focusCurrentView();
				contentPane.setVisible(true);
				frame.toFront();
				startupFinished = true;
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

	private void loadMaps() {
		final Controller controller = Controller.getCurrentController();
		final boolean alwaysLoadLastMaps = ResourceController.getResourceController().getBooleanProperty(
		    "always_load_last_maps");

		if (alwaysLoadLastMaps && !dontLoadLastMaps) {
			loadLastMaps();
		}
		loadMaps(controller, options.getFilesToOpenAsArray());
		if (controller.getMap() == null && !alwaysLoadLastMaps && !dontLoadLastMaps) {
			final AddOnsController addonsController = AddOnsController.getController();
			addonsController.setAutoInstallEnabled(false);
			loadLastMaps();
			addonsController.setAutoInstallEnabled(true);
		}
		final ModeController modeController = Controller.getCurrentModeController();
		if(userPropertiesStatus != UserPropertiesStatus.CURRENT_VERSION_FOUND
		        && ! dontLoadLastMaps){
			final String mapSource = ResourceController.getResourceController().getProperty(
			        userPropertiesStatus == UserPropertiesStatus.NOT_FOUND ? "tutorial_map" : "latest_features_map"
			        );
			MMapController mapController = (MMapController)modeController.getMapController();
            mapController.newDocumentationMap(mapSource);
	        MapModel map = controller.getMap();
            if (null != map) {
	            Timer docuMapTimer = new Timer(300, new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                    mapController.newDocumentationMap(mapSource);
	                }
	              });
	            docuMapTimer.setRepeats(false);
	            docuMapTimer.start();
	        }
		}
		if (null != controller.getMap()) {
			return;
		}
		controller.selectMode(MModeController.MODENAME);
		if(ResourceController.getResourceController().getBooleanProperty(CREATE_NEW_MAP_IF_NO_MAPS_ARE_LOADED))
		    MFileManager.getController(modeController).newMapFromDefaultTemplate();
	}

	private void loadLastMaps() {
	    final boolean loadLastMap = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAP);
	    final boolean loadLastMaps = ResourceController.getResourceController().getBooleanProperty(LOAD_LAST_MAPS);
	    if(loadLastMaps)
	    	viewController.openMapsOnStart();
	    if(userPropertiesStatus == UserPropertiesStatus.CURRENT_VERSION_FOUND && (loadLastMaps || loadLastMap))
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
			loadMap(args[i]);
		}
		MacOptions.macFilesToOpen.forEach(this::loadMap);
    }

    private void loadMap(String fileArgument) {
        try {
        	final LinkController linkController = LinkController.getController();
        	linkController.loadMap(fileArgument);
        }
        catch (final Exception ex) {
        	System.err.println("File " + fileArgument + " not loaded");
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
