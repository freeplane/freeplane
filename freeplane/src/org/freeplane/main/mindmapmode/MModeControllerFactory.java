/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.main.mindmapmode;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.DelayedMouseListener;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.SetAcceleratorOnNextClickAction;
import org.freeplane.core.ui.components.FButtonBar;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JResizer;
import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.mindmapmode.AddAttributeAction;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.attribute.mindmapmode.RemoveAllAttributesAction;
import org.freeplane.features.attribute.mindmapmode.RemoveFirstAttributeAction;
import org.freeplane.features.attribute.mindmapmode.RemoveLastAttributeAction;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.mindmapmode.AutomaticEdgeColorHook;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.encrypt.mindmapmode.MEncryptionController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.frame.ToggleToolbarAction;
import org.freeplane.features.frame.ViewController;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.mindmapmode.IconSelectionPlugin;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.UnfoldAll;
import org.freeplane.features.map.mindmapmode.ChangeNodeLevelController;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.NewParentNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.mode.mindmapmode.export.ExportController;
import org.freeplane.features.mode.mindmapmode.export.ImportMindmanagerFiles;
import org.freeplane.features.mode.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.RevisionPlugin;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.mindmapmode.AutomaticLayout;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.ShowFormatPanelAction;
import org.freeplane.features.styles.mindmapmode.StyleEditorPanel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.text.mindmapmode.SortNodes;
import org.freeplane.features.text.mindmapmode.SplitNode;
import org.freeplane.features.time.CreationModificationPlugin;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.main.mindmapmode.stylemode.SModeControllerFactory;
import org.freeplane.view.swing.features.BlinkingNodeHook;
import org.freeplane.view.swing.features.FitToPage;
import org.freeplane.view.swing.features.filepreview.AddExternalImageAction;
import org.freeplane.view.swing.features.filepreview.ChangeExternalImageAction;
import org.freeplane.view.swing.features.filepreview.RemoveExternalImageAction;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;
import org.freeplane.view.swing.features.progress.mindmapmode.ProgressFactory;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderHook;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.ShowNotesInMapAction;
import org.freeplane.view.swing.map.attribute.AttributePanelManager;
import org.freeplane.view.swing.map.attribute.EditAttributesAction;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DefaultNodeKeyListener;
import org.freeplane.view.swing.ui.DefaultNodeMouseMotionListener;
import org.freeplane.view.swing.ui.UserInputListenerFactory;
import org.freeplane.view.swing.ui.mindmapmode.MMouseMotionListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDragListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeMotionListener;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class MModeControllerFactory {
	private static MModeControllerFactory instance;

	public static MModeController createModeController() {
		return MModeControllerFactory.getInstance().createModeControllerImpl();
	}

	private static MModeControllerFactory getInstance() {
		if (instance == null) {
			instance = new MModeControllerFactory();
		}
		return instance;
	}

// // 	private Controller controller;
 	private MModeController modeController;
	private MUIFactory uiFactory;

	private void createAddIns() {
		final StyleEditorPanel panel = new StyleEditorPanel(uiFactory, true);
		panel.init();
		final JScrollPane styleScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		UITools.setScrollbarIncrement(styleScrollPane);
		final JComponent tabs = (JComponent) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add(TextUtils.getText("format_panel"), styleScrollPane);
		FormatController.install(new FormatController());
		ScannerController.install(new ScannerController());
		new AttributePanelManager(modeController);
		new HierarchicalIcons();
		new AutomaticLayout();
		new BlinkingNodeHook();
		SummaryNode.install();
		new CreationModificationPlugin();
		new ReminderHook();
		new AutomaticEdgeColorHook();
		new ViewerController();
		modeController.addAction(new AddAttributeAction());
		modeController.addAction(new RemoveFirstAttributeAction());
		modeController.addAction(new RemoveLastAttributeAction());
		modeController.addAction(new RemoveAllAttributesAction());
		modeController.addAction(new AddExternalImageAction());
		modeController.addAction(new RemoveExternalImageAction());
		modeController.addAction(new ChangeExternalImageAction());
		modeController.addAction(new ShowFormatPanelAction());
		modeController.addAction(new FitToPage());
		MEncryptionController.install(new MEncryptionController());
		modeController.addAction(new IconSelectionPlugin());
		modeController.addAction(new NewParentNode());
		modeController.addAction(new SaveAll());
		modeController.addAction(new SortNodes());
		modeController.addAction(new SplitNode());
		new ChangeNodeLevelController(modeController);
		NodeHistory.install(modeController);
		modeController.addAction(new ImportMindmanagerFiles());
	}

	private MModeController createModeControllerImpl() {
//		this.controller = controller;
		createStandardControllers();
		createAddIns();
		return modeController;
	}

	private void createStandardControllers() {
		final Controller controller = Controller.getCurrentController();
		modeController = new MModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		userInputListenerFactory.setNodeMouseMotionListener(new DelayedMouseListener( new DefaultNodeMouseMotionListener() {
			public void mouseClicked(final MouseEvent e) {
				if (wasFocused() && (e.getModifiers() & ~ (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == InputEvent.BUTTON1_MASK) {
					/* perform action only if one selected node. */
					final MapController mapController = modeController.getMapController();
					if (mapController.getSelectedNodes().size() != 1) {
						return;
					}
					final MainView component = (MainView) e.getComponent();
					if (component.isInFollowLinkRegion(e.getX())) {
						LinkController.getController().loadURL(e);
					}
					else {
						if (e.getClickCount() == 2 && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()
								&& !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
							((MTextController) TextController.getController()).edit(e, FirstAction.EDIT_CURRENT, e.isAltDown());
							return;
						}
						final Component selectedComponent = controller.getViewController().getSelectedComponent();
						final boolean isFocused = SwingUtilities.isDescendingFrom(e.getComponent(), selectedComponent);
						if(isFocused){
							mapController.toggleFolded(mapController.getSelectedNodes());
						}
					}
					e.consume();
				}
			}
		}, 2, MouseEvent.BUTTON1));
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		controller.selectModeForBuild(modeController);
		new MMapController(modeController);
		final MFileManager fileManager = new MFileManager();
		UrlManager.install(fileManager);
		controller.getMapViewManager().addMapViewChangeListener(fileManager);
		IconController.install(new MIconController(modeController));
		new ProgressFactory().installActions(modeController);
		final MapController mapController = modeController.getMapController();
		EdgeController.install(new MEdgeController(modeController));
		CloudController.install(new MCloudController(modeController));
		NoteController.install(new MNoteController(modeController));
		userInputListenerFactory.setMapMouseListener(new DefaultMapMouseListener(new MMouseMotionListener()));
		final MTextController textController = new MTextController(modeController);
		TextController.install(textController);
		LinkController.install(new MLinkController());
		NodeStyleController.install(new MNodeStyleController(modeController));
		ClipboardController.install(new MClipboardController());
		userInputListenerFactory.setNodeDragListener(new MNodeDragListener());
		userInputListenerFactory.setNodeDropTargetListener(new MNodeDropListener());
		LocationController.install(new MLocationController());
		LogicalStyleController.install(new MLogicalStyleController());
		AttributeController.install(new MAttributeController(modeController));
		userInputListenerFactory.setNodeKeyListener(new DefaultNodeKeyListener(new IEditHandler() {
			public void edit(final KeyEvent e, final FirstAction action, final boolean editLong) {
				textController.edit(e, action, editLong);
			}
		}));
		userInputListenerFactory.setNodeMotionListener(new MNodeMotionListener());
		modeController.addAction(new EditAttributesAction());
		SpellCheckerController.install();
		ExportController.install(new ExportController("/xml/ExportWithXSLT.xml"));
		new MapStyle(true);
		final JPopupMenu popupmenu = new JPopupMenu();
		userInputListenerFactory.setNodePopupMenu(popupmenu);
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		toolbar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolbar);
		userInputListenerFactory.addToolBar("/filter_toolbar", ViewController.TOP, FilterController.getController(
		    controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		final JTabbedPane tabs = new JTabbedPane();
		Box resisableTabs = Box.createHorizontalBox();
		resisableTabs.add(new JResizer(Direction.RIGHT));
		resisableTabs.add(tabs);
		resisableTabs.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "styleScrollPaneVisible");
		modeController.getUserInputListenerFactory().addToolBar("/format", ViewController.RIGHT, resisableTabs);
		final FButtonBar fButtonToolBar = new FButtonBar();
		fButtonToolBar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "fbarVisible");
		fButtonToolBar.setVisible(ResourceController.getResourceController().getBooleanProperty("fbarVisible"));
		userInputListenerFactory.addToolBar("/fbuttons", ViewController.TOP, fButtonToolBar);
		controller.addAction(new ToggleToolbarAction("ToggleFBarAction", "/fbuttons"));
		SModeControllerFactory.install();
		modeController.addAction(new SetAcceleratorOnNextClickAction());
		modeController.addAction(new ShowNotesInMapAction());
		userInputListenerFactory.getMenuBuilder().setAcceleratorChangeListener(fButtonToolBar);
		userInputListenerFactory.addToolBar("/icon_toolbar", ViewController.LEFT, ((MIconController) IconController
		    .getController()).getIconToolBarScrollPane());
		modeController.addAction(new ToggleToolbarAction("ToggleLeftToolbarAction", "/icon_toolbar"));
		new RevisionPlugin();
		new UnfoldAll();
		
		uiFactory = new MUIFactory();
		mapController.addNodeChangeListener(uiFactory);
		mapController.addNodeSelectionListener(uiFactory);
		mapController.addMapChangeListener(uiFactory);
		controller.getMapViewManager().addMapSelectionListener(uiFactory);
		final MToolbarContributor menuContributor = new MToolbarContributor(uiFactory);
		modeController.addMenuContributor(menuContributor);
		modeController.getOptionPanelBuilder().addValidator(new FormatController().createValidator());
	}
}
