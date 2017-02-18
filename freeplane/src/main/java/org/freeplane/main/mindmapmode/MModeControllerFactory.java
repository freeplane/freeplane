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
import java.awt.Container;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.core.ui.IKeyStrokeProcessor;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.SetAcceleratorOnNextClickAction;
import org.freeplane.core.ui.components.FButtonBar;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.action.ComponentBuilder;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.ui.menubuilders.menu.ComponentProvider;
import org.freeplane.core.ui.menubuilders.menu.JToolbarComponentBuilder;
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
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.encrypt.mindmapmode.MEncryptionController;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.export.mindmapmode.ImportMindmanagerFiles;
import org.freeplane.features.export.mindmapmode.ImportXmlFile;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.mindmapmode.IconSelectionPlugin;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.FoldingController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.mindmapmode.ChangeNodeLevelController;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.NewParentNode;
import org.freeplane.features.map.mindmapmode.RemoveAllAlwaysUnfoldedNodeFlagsAction;
import org.freeplane.features.map.mindmapmode.SetAlwaysUnfoldedNodeFlagsAction;
import org.freeplane.features.map.mindmapmode.SummaryNodeMapUpdater;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.RevisionPlugin;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.presentations.mindmapmode.PresentationController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.ShowFormatPanelAction;
import org.freeplane.features.styles.mindmapmode.styleeditorpanel.StyleEditorPanel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.text.mindmapmode.SortNodes;
import org.freeplane.features.text.mindmapmode.SplitNode;
import org.freeplane.features.time.CreationModificationPlugin;
import org.freeplane.features.ui.CollapseableBoxBuilder;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.ToggleToolbarAction;
import org.freeplane.features.ui.UIComponentVisibilityDispatcher;
import org.freeplane.features.ui.ViewController;
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
import org.freeplane.view.swing.map.ShowNotesInMapAction;
import org.freeplane.view.swing.map.attribute.AttributePanelManager;
import org.freeplane.view.swing.map.attribute.EditAttributesAction;
import org.freeplane.view.swing.ui.DefaultNodeKeyListener;
import org.freeplane.view.swing.ui.UserInputListenerFactory;
import org.freeplane.view.swing.ui.mindmapmode.MMapMouseListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDragListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeMotionListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeMouseWheelListener;

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
		final StyleEditorPanel panel = new StyleEditorPanel(modeController, uiFactory, true);
		final JScrollPane styleScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		UITools.setScrollbarIncrement(styleScrollPane);
		final JComponent tabs = (JComponent) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add(TextUtils.getText("format_panel"), styleScrollPane);
		new AttributePanelManager(modeController);
		new HierarchicalIcons();
		new AutomaticLayoutController();
		new BlinkingNodeHook();
		SummaryNode.install();
		final MMapController mapController = (MMapController) modeController.getMapController();
		mapController.addMapLifeCycleListener(new SummaryNodeMapUpdater(modeController, mapController));
		final AlwaysUnfoldedNode alwaysUnfoldedNode = new AlwaysUnfoldedNode();
		modeController.addAction(new SetAlwaysUnfoldedNodeFlagsAction(alwaysUnfoldedNode));
		modeController.addAction(new RemoveAllAlwaysUnfoldedNodeFlagsAction(alwaysUnfoldedNode));
		FreeNode.install();
		new CreationModificationPlugin();
		modeController.addExtension(ReminderHook.class, new ReminderHook(modeController));
		new AutomaticEdgeColorHook();
		new ViewerController();
		PresentationController.install(modeController);
		modeController.addAction(new AddAttributeAction());
		modeController.addAction(new RemoveFirstAttributeAction());
		modeController.addAction(new RemoveLastAttributeAction());
		modeController.addAction(new RemoveAllAttributesAction());
		modeController.addAction(new AddExternalImageAction());
		modeController.addAction(new RemoveExternalImageAction());
		modeController.addAction(new ChangeExternalImageAction());
		modeController.addAction(new ShowFormatPanelAction());
		modeController.addAction(new FitToPage());
		modeController.addAction(new UpdateCheckAction());
		MEncryptionController.install(new MEncryptionController(modeController));
		modeController.addAction(new IconSelectionPlugin());
		modeController.addAction(new NewParentNode());
		modeController.addAction(new SaveAll());
		modeController.addAction(new SortNodes());
		modeController.addAction(new SplitNode());
		new ChangeNodeLevelController(modeController);
		NodeHistory.install(modeController);
		modeController.addAction(new ImportXmlFile());
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

        final IMouseListener nodeMouseMotionListener = new MNodeMotionListener();
        userInputListenerFactory.setNodeMouseMotionListener(nodeMouseMotionListener);
		userInputListenerFactory.setNodeMouseWheelListener(new MNodeMouseWheelListener(userInputListenerFactory.getMapMouseWheelListener()));
		final JPopupMenu popupmenu = new JPopupMenu();
		userInputListenerFactory.setNodePopupMenu(popupmenu);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		controller.selectModeForBuild(modeController);
		new MMapController(modeController);
		final MFileManager fileManager = new MFileManager();
		UrlManager.install(fileManager);
		MMapIO.install(modeController);
		controller.getMapViewManager().addMapViewChangeListener(fileManager);
		new MIconController(modeController).install(modeController);
		new ProgressFactory().installActions(modeController);
		final MapController mapController = modeController.getMapController();
		EdgeController.install(new MEdgeController(modeController));
		CloudController.install(new MCloudController(modeController));
		NoteController.install(new MNoteController(modeController));
		userInputListenerFactory.setMapMouseListener(new MMapMouseListener());
		final MTextController textController = new MTextController(modeController);
		textController.install(modeController);
		LinkController.install(new MLinkController(modeController));
		NodeStyleController.install(new MNodeStyleController(modeController));
		ClipboardController.install(new MClipboardController());
		userInputListenerFactory.setNodeDragListener(new MNodeDragListener());
		userInputListenerFactory.setNodeDropTargetListener(new MNodeDropListener());
		LocationController.install(new MLocationController());
		final MLogicalStyleController logicalStyleController = new MLogicalStyleController(modeController);
		LogicalStyleController.install(logicalStyleController);
		logicalStyleController.initM();
		AttributeController.install(new MAttributeController(modeController));
		userInputListenerFactory.setNodeKeyListener(new DefaultNodeKeyListener(new IEditHandler() {
			public void edit(final KeyEvent e, final FirstAction action, final boolean editLong) {
				((MTextController) MTextController.getController(modeController)).getEventQueue().activate(e);
				textController.edit(action, editLong);
			}
		}));
		userInputListenerFactory.setNodeMotionListener(new MNodeMotionListener());
		modeController.addAction(new EditAttributesAction());
		SpellCheckerController.install(modeController);
		ExportController.install(new ExportController("/xml/ExportWithXSLT.xml"));
		MapStyle.install(true);
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		final FrameController frameController = (FrameController) controller.getViewController();
		UIComponentVisibilityDispatcher.install(frameController, toolbar, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolbar);
		userInputListenerFactory.addToolBar("/filter_toolbar", FilterController.TOOLBAR_SIDE, FilterController.getController(controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, frameController
		    .getStatusBar());
		final JTabbedPane formattingPanel = new JTabbedPane();
		Box resisableTabs = new CollapseableBoxBuilder(frameController).setPropertyNameBase("styleScrollPaneVisible").createBox(formattingPanel, Direction.RIGHT);
		userInputListenerFactory.addToolBar("/format", ViewController.RIGHT, resisableTabs);
		final JRootPane rootPane = ((RootPaneContainer)frameController.getMenuComponent()).getRootPane();
		final FButtonBar fButtonToolBar = new FButtonBar(rootPane);
		UIComponentVisibilityDispatcher.install(frameController, fButtonToolBar, "fbarVisible");
		fButtonToolBar.setVisible(ResourceController.getResourceController().getBooleanProperty("fbarVisible"));
		userInputListenerFactory.addToolBar("/fbuttons", ViewController.TOP, fButtonToolBar);
		userInputListenerFactory.setKeyEventProcessor(new IKeyStrokeProcessor() {
			@Override
			public boolean processKeyBinding(KeyStroke ks, KeyEvent e) {
				return ResourceController.getResourceController().getAcceleratorManager().processKeyBinding(ks, e) || fButtonToolBar.processKeyBinding(ks, e);
			}
		});
		controller.addAction(new ToggleToolbarAction("ToggleFBarAction", "/fbuttons"));
		SModeControllerFactory.install();
		modeController.addAction(new SetAcceleratorOnNextClickAction());
		modeController.addAction(new ShowNotesInMapAction());
		//userInputListenerFactory.getMenuBuilder().setAcceleratorChangeListener(fButtonToolBar);
		ResourceController.getResourceController().getAcceleratorManager().addAcceleratorChangeListener(modeController, fButtonToolBar);
		userInputListenerFactory.addToolBar("/icon_toolbar", ViewController.LEFT, ((MIconController) IconController
		    .getController()).getIconToolBarScrollPane());
		modeController.addAction(new ToggleToolbarAction("ToggleLeftToolbarAction", "/icon_toolbar"));
		new RevisionPlugin();
		FoldingController.install(new FoldingController());

		uiFactory = new MUIFactory();
		mapController.addNodeChangeListener(uiFactory);
		mapController.addNodeSelectionListener(uiFactory);
		mapController.addMapChangeListener(uiFactory);
		controller.getMapViewManager().addMapSelectionListener(uiFactory);
		modeController.addExtension(MUIFactory.class, uiFactory);

		modeController.addUiBuilder(Phase.ACTIONS, "main_toolbar_font_name", new ComponentBuilder(
		    new ComponentProvider() {
			    @Override
			    public Component createComponent(Entry entry) {
				    final Container fontBox = uiFactory.createFontBox();
				    return fontBox;
			    }
		    }), EntryVisitor.EMTPY);


		modeController.addUiBuilder(Phase.ACTIONS, "main_toolbar_font_size", new ComponentBuilder(
		    new ComponentProvider() {
			    @Override
			    public Component createComponent(Entry entry) {
				    return uiFactory.createSizeBox();
			    }
		    }), EntryVisitor.EMTPY);


		modeController.addUiBuilder(Phase.ACTIONS, "main_toolbar_style", new ComponentBuilder(
		    new ComponentProvider() {
			    @Override
			    public Component createComponent(Entry entry) {
				    return uiFactory.createStyleBox();
			    }
		    }), EntryVisitor.EMTPY);

		modeController.addUiBuilder(Phase.UI, "main_toolbar_zoom", new JToolbarComponentBuilder(
		    new ComponentProvider() {
			    @Override
			    public Component createComponent(Entry entry) {
				    return controller.getMapViewManager().createZoomBox();
			    }
		    }));


//		IconController.getController(modeController).addStateIconProvider(new IStateIconProvider() {
//			public UIIcon getStateIcon(NodeModel node) {
//				final URI link = NodeLinks.getLink(node);
//				return wrapIcon(LinkController.getLinkIcon(link, node));
//			}
//
//			private UIIcon wrapIcon(final Icon linkIcon) {
//				UIIcon icon = null;
//				if(linkIcon != null) {
//					if(linkIcon instanceof UIIcon) {
//						icon = (UIIcon) linkIcon;
//					}
//					else {
//    					icon = new UIIcon("ownIcon", null) {
//    						public Icon getIcon() {
//    							return linkIcon;
//    						}
//    					};
//					}
//				}
//				return icon;
//			}
//		});
	}
}
