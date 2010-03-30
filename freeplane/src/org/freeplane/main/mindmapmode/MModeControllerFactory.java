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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ToggleToolbarAction;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.SetAcceleratorOnNextClickAction;
import org.freeplane.core.ui.components.FButtonBar;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.features.common.addins.encrypt.EnterPassword;
import org.freeplane.features.common.addins.misc.BlinkingNodeHook;
import org.freeplane.features.common.addins.misc.CreationModificationPlugin;
import org.freeplane.features.common.addins.misc.HierarchicalIcons;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.addins.ChangeNodeLevelController;
import org.freeplane.features.mindmapmode.addins.IconSelectionPlugin;
import org.freeplane.features.mindmapmode.addins.LoadAcceleratorPresetsAction;
import org.freeplane.features.mindmapmode.addins.NewParentNode;
import org.freeplane.features.mindmapmode.addins.RevisionPlugin;
import org.freeplane.features.mindmapmode.addins.SaveAll;
import org.freeplane.features.mindmapmode.addins.SortNodes;
import org.freeplane.features.mindmapmode.addins.SplitNode;
import org.freeplane.features.mindmapmode.addins.UnfoldAll;
import org.freeplane.features.mindmapmode.addins.encrypt.EncryptedMap;
import org.freeplane.features.mindmapmode.addins.export.ExportToImage;
import org.freeplane.features.mindmapmode.addins.export.ExportToOoWriter;
import org.freeplane.features.mindmapmode.addins.export.ExportWithXSLT;
import org.freeplane.features.mindmapmode.addins.export.ImportMindmanagerFiles;
import org.freeplane.features.mindmapmode.addins.styles.AutomaticLayout;
import org.freeplane.features.mindmapmode.addins.styles.MLogicalStyleController;
import org.freeplane.features.mindmapmode.addins.styles.MUIFactory;
import org.freeplane.features.mindmapmode.addins.styles.ShowFormatPanelAction;
import org.freeplane.features.mindmapmode.addins.styles.StyleEditorPanel;
import org.freeplane.features.mindmapmode.addins.time.ReminderHook;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.map.MMapController;
import org.freeplane.features.mindmapmode.nodelocation.MLocationController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.note.MNoteController;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.main.mindmapmode.stylemode.SModeControllerFactory;
import org.freeplane.view.swing.addins.FitToPage;
import org.freeplane.view.swing.addins.filepreview.ViewerController;
import org.freeplane.view.swing.addins.nodehistory.NodeHistory;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.attribute.EditAttributesAction;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DefaultNodeKeyListener;
import org.freeplane.view.swing.ui.DefaultNodeMouseMotionListener;
import org.freeplane.view.swing.ui.UserInputListenerFactory;
import org.freeplane.view.swing.ui.mindmapmode.MMouseMotionListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;
import org.freeplane.view.swing.ui.mindmapmode.MNodeMotionListener;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class MModeControllerFactory {
	private static MModeControllerFactory instance;

	public static MModeController createModeController(final Controller controller) {
		return MModeControllerFactory.getInstance().createModeControllerImpl(controller);
	}

	private static MModeControllerFactory getInstance() {
		if (instance == null) {
			instance = new MModeControllerFactory();
		}
		return instance;
	}

	private Controller controller;
	private MModeController modeController;
	private MUIFactory uiFactory;

	private void createAddIns() {
		new HierarchicalIcons(modeController);
		new AutomaticLayout(modeController);
		new BlinkingNodeHook(modeController);
		new CreationModificationPlugin(modeController);
		new ReminderHook(modeController);
		new ViewerController(modeController);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		final StyleEditorPanel panel = new StyleEditorPanel(modeController, uiFactory, true);
		panel.init(modeController);
		final JScrollPane styleScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		styleScrollPane.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "styleScrollPaneVisible");
		modeController.getUserInputListenerFactory().addToolBar("/format", ViewController.RIGHT, styleScrollPane);
		menuBuilder.addAnnotatedAction(new ShowFormatPanelAction(controller));
		menuBuilder.addAnnotatedAction(new FitToPage(controller));
		menuBuilder.addAnnotatedAction(new EncryptedMap(modeController));
		menuBuilder.addAnnotatedAction(new EnterPassword(modeController));
		menuBuilder.addAnnotatedAction(new IconSelectionPlugin(controller));
		menuBuilder.addAnnotatedAction(new NewParentNode(controller));
		menuBuilder.addAnnotatedAction(new SaveAll(controller));
		menuBuilder.addAnnotatedAction(new SortNodes(controller));
		menuBuilder.addAnnotatedAction(new SplitNode(controller));
		new UnfoldAll(modeController).addActionsAtMenuBuilder(menuBuilder);
		new ChangeNodeLevelController(modeController.getController()).addActionsAtMenuBuilder(menuBuilder);
		ExportWithXSLT.createXSLTExportActions(modeController, "/xml/ExportWithXSLT.xml");
		ExportToImage.createActions(modeController);
		NodeHistory.install(modeController);
		menuBuilder.addAnnotatedAction(new ExportToOoWriter(controller));
		menuBuilder.addAnnotatedAction(new ImportMindmanagerFiles(controller));
		LoadAcceleratorPresetsAction.install(modeController);
	}

	private MModeController createModeControllerImpl(final Controller controller) {
		this.controller = controller;
		createStandardControllers();
		createAddIns();
		return modeController;
	}

	private void createStandardControllers() {
		modeController = new MModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		userInputListenerFactory.setNodeMouseMotionListener(new DefaultNodeMouseMotionListener(modeController) {
			@Override
			public void mouseReleased(final MouseEvent e) {
				stopTimerForDelayedSelection();
				final ModeController modeController = controller.getModeController();
				extendSelection(e);
				showPopupMenu(e);
				if (e.isConsumed()) {
					return;
				}
				if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
					/* perform action only if one selected node. */
					final MapController mapController = modeController.getMapController();
					if (mapController.getSelectedNodes().size() != 1) {
						return;
					}
					final MainView component = (MainView) e.getComponent();
					if (component.isInFollowLinkRegion(e.getX())) {
						LinkController.getController(modeController).loadURL(e);
					}
					else {
						final NodeModel node = (component).getNodeView().getModel();
						if (!mapController.hasChildren(node)) {
							/* If the link exists, follow the link; toggle folded otherwise */
							if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown() && !e.isMetaDown()
							        && !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
								((MTextController) TextController.getController(modeController)).edit(null, false,
								    false);
							}
							return;
						}
						mapController.toggleFolded(mapController.getSelectedNodes().listIterator());
					}
					e.consume();
				}
			}
		});
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		modeController.setMapController(new MMapController(modeController));
		final MFileManager fileManager = new MFileManager(modeController);
		UrlManager.install(modeController, fileManager);
		controller.getMapViewManager().addMapViewChangeListener(fileManager);
		IconController.install(modeController, new MIconController(modeController));
		NodeStyleController.install(modeController, new MNodeStyleController(modeController));
		final MapController mapController = modeController.getMapController();
		uiFactory = new MUIFactory(modeController);
		mapController.addNodeChangeListener(uiFactory);
		mapController.addNodeSelectionListener(uiFactory);
		mapController.addMapChangeListener(uiFactory);
		controller.getMapViewManager().addMapSelectionListener(uiFactory);
		final MToolbarContributor menuContributor = new MToolbarContributor(modeController, uiFactory);
		modeController.addMenuContributor(menuContributor);
		EdgeController.install(modeController, new MEdgeController(modeController));
		CloudController.install(modeController, new MCloudController(modeController));
		NoteController.install(modeController, new MNoteController(modeController));
		LinkController.install(modeController, new MLinkController(modeController));
		userInputListenerFactory.setMapMouseListener(new DefaultMapMouseListener(controller, new MMouseMotionListener(
		    modeController)));
		final MTextController textController = new MTextController(modeController);
		TextController.install(modeController, textController);
		userInputListenerFactory.setNodeKeyListener(new DefaultNodeKeyListener(controller, new IEditHandler() {
			public void edit(final KeyEvent e, final boolean addNew, final boolean editLong) {
				textController.edit(e, addNew, editLong);
			}
		}));
		ClipboardController.install(modeController, new MClipboardController(modeController));
		userInputListenerFactory.setNodeDropTargetListener(new MNodeDropListener(modeController));
		LocationController.install(modeController, new MLocationController(modeController));
		LogicalStyleController.install(modeController, new MLogicalStyleController(modeController));
		userInputListenerFactory.setNodeMotionListener(new MNodeMotionListener(modeController));
		AttributeController.install(modeController, new MAttributeController(modeController));
		modeController.addAction(new EditAttributesAction(controller));
		SpellCheckerController.install(modeController);
		new MapStyle(modeController, true);
		final JPopupMenu popupmenu = new JPopupMenu();
		userInputListenerFactory.setNodePopupMenu(popupmenu);
		final FreeplaneToolBar toolbar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		toolbar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolbar);
		userInputListenerFactory.addToolBar("/filter_toolbar", ViewController.TOP, FilterController.getController(
		    controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		final FButtonBar fButtonToolBar = new FButtonBar(modeController);
		fButtonToolBar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "fbarVisible");
		fButtonToolBar.setVisible(ResourceController.getResourceController().getBooleanProperty("fbarVisible"));
		userInputListenerFactory.addToolBar("/fbuttons", ViewController.TOP, fButtonToolBar);
		controller.addAction(new ToggleToolbarAction(controller, "ToggleFBarAction", "/fbuttons"));
		SModeControllerFactory.createModeController(modeController);
		modeController.addAction(new SetAcceleratorOnNextClickAction(controller));
		userInputListenerFactory.getMenuBuilder().setAcceleratorChangeListener(fButtonToolBar);
		userInputListenerFactory.addToolBar("/icon_toolbar", ViewController.LEFT, ((MIconController) IconController
		    .getController(modeController)).getIconToolBarScrollPane());
		modeController.addAction(new ToggleToolbarAction(controller, "ToggleLeftToolbarAction", "/icon_toolbar"));
		new RevisionPlugin(modeController);
		userInputListenerFactory.setMenuStructure("/xml/mindmapmodemenu.xml");
		userInputListenerFactory.updateMenus(modeController);
		final MenuBuilder builder = modeController.getUserInputListenerFactory().getMenuBuilder();
		((MIconController) IconController.getController(modeController)).updateIconToolbar();
		((MIconController) IconController.getController(modeController)).updateMenus(builder);
		modeController.updateMenus();
	}
}
