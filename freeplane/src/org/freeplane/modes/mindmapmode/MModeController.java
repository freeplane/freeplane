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
package org.freeplane.modes.mindmapmode;

import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;

import org.freeplane.controller.Controller;
import org.freeplane.io.url.mindmapmode.FileManager;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.attribute.mindmapnode.MAttributeController;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.nodelocation.mindmapmode.MLocationController;
import org.freeplane.map.note.mindmapnode.MNoteController;
import org.freeplane.map.pattern.mindmapnode.MPatternController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.map.tree.view.MainView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.UserInputListenerFactory;
import org.freeplane.ui.FreemindMenuBar;
import org.freeplane.ui.MenuBuilder;
import org.freeplane.undo.IUndoHandler;
import org.freeplane.undo.IUndoableActor;

import deprecated.freemind.common.XmlBindingTools;
import deprecated.freemind.extensions.IHookFactory;
import deprecated.freemind.extensions.IHookRegistration;
import deprecated.freemind.extensions.IModeControllerHook;
import deprecated.freemind.extensions.INodeHook;
import deprecated.freemind.extensions.IHookFactory.RegistrationContainer;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionFactory;
import deprecated.freemind.modes.mindmapmode.actions.undo.CompoundActionHandler;
import deprecated.freemind.modes.mindmapmode.actions.undo.IHookAction;
import deprecated.freemind.modes.mindmapmode.actions.undo.UndoActionHandler;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapHookFactory;
import freemind.controller.actions.generated.instance.WindowConfigurationStorage;

public class MModeController extends ModeController {
	public interface IMindMapControllerPlugin {
	}

	static public final String MODENAME = "MindMap";
	static private RedoAction redo;
	static private UndoAction undo;
	final private ActionFactory actionFactory;
	private MAttributeController attributeController;
	private Vector hookActions;
	/**
	 * A general list of MindMapControllerPlugin s. Members need to be tested
	 * for the right class and casted to be applied.
	 */
	final private HashSet mPlugins = new HashSet();
	final private HashSet mRegisteredMouseWheelEventHandler = new HashSet();
	final private List mRegistrations;
	private IHookFactory nodeHookFactory;
	private MPatternController patternController;
	private UndoActionHandler undoActionHandler;

	MModeController() {
		super();
		actionFactory = new ActionFactory();
		mRegistrations = new Vector();
		/**
		 * This handler evaluates the compound xml actions. Don't delete it!
		 */
		new CompoundActionHandler(this);
		createActions();
	}

	public void addHook(final NodeModel focussed, final List selecteds,
	                    final String hookName) {
		((NodeHookAction) getAction("nodeHookAction")).addHook(focussed,
		    selecteds, hookName);
	}

	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public void addUndoableActor(final IUndoableActor actor) {
		final MindMapMapModel map = (MindMapMapModel) Controller
		    .getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.addActor(actor);
		undo.setEnabled(true);
		redo.setEnabled(false);
	}

	private void createActions() {
		undo = new UndoAction();
		redo = new RedoAction();
		undo.setRedo(redo);
		redo.setUndo(undo);
		addAction("undo", undo);
		addAction("redo", redo);
		undoActionHandler = new UndoActionHandler(this);
		getActionFactory().registerUndoHandler(undoActionHandler);
		addAction("nodeHookAction", new NodeHookAction("no_title", this));
		addAction("selectBranchAction", new SelectBranchAction());
		addAction("selectAllAction", new SelectAllAction());
	}

	public INodeHook createNodeHook(final String hookName,
	                                final NodeModel node, final MapModel map) {
		final MindMapHookFactory hookFactory = (MindMapHookFactory) getHookFactory();
		final INodeHook hook = hookFactory.createNodeHook(hookName);
		hook.setController(this);
		return hook;
	}

	/**
	 *
	 */
	void createNodeHookActions() {
		if (hookActions == null) {
			hookActions = new Vector();
			final MindMapHookFactory factory = (MindMapHookFactory) getHookFactory();
			final List nodeHookNames = factory.getPossibleNodeHooks();
			for (final Iterator i = nodeHookNames.iterator(); i.hasNext();) {
				final String hookName = (String) i.next();
				final NodeHookAction action = new NodeHookAction(hookName, this);
				hookActions.add(action);
			}
			final List modeControllerHookNames = factory
			    .getPossibleIModeControllerHooks();
			for (final Iterator i = modeControllerHookNames.iterator(); i
			    .hasNext();) {
				final String hookName = (String) i.next();
				final MindMapControllerHookAction action = new MindMapControllerHookAction(
				    hookName, this);
				hookActions.add(action);
			}
		}
	}

	public void deRegisterMouseWheelEventHandler(
	                                             final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public void deregisterPlugin(final IMindMapControllerPlugin pPlugin) {
		mPlugins.remove(pPlugin);
	}

	@Override
	public void doubleClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final NodeModel node = ((MainView) e.getComponent()).getNodeView()
		    .getModel();
		if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown()
		        && !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1
		        && (node.getLink() == null)) {
			((MTextController) getTextController()).edit(null, false, false);
		}
	}

	/**
	 * Enabled/Disabled all actions that are dependent on whether there is a map
	 * open or not.
	 */
	@Override
	public void enableActions(final boolean enabled) {
		super.enableActions(enabled);
		for (int i = 0; i < hookActions.size(); ++i) {
			((Action) hookActions.get(i)).setEnabled(enabled);
		}
		((MIconController) getIconController()).enableActions(enabled);
	}

	@Override
	public void execute(final IUndoableActor actor) {
		actor.act();
		addUndoableActor(actor);
	}

	public ActionFactory getActionFactory() {
		return actionFactory;
	}

	@Override
	public IAttributeController getAttributeController() {
		return attributeController;
	}

	@Override
	public IHookFactory getHookFactory() {
		if (nodeHookFactory == null) {
			nodeHookFactory = new MindMapHookFactory();
		}
		return nodeHookFactory;
	}

	@Override
	public String getModeName() {
		return MModeController.MODENAME;
	}

	public MPatternController getPatternController() {
		return patternController;
	}

	public Set getPlugins() {
		return Collections.unmodifiableSet(mPlugins);
	}

	@Override
	public Set getRegisteredMouseWheelEventHandler() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	public void invokeHook(final IModeControllerHook hook) {
		try {
			hook.setController(this);
			hook.startup();
			hook.shutdown();
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
		}
	}

	public boolean isUndoAction() {
		return ((MindMapMapModel) getMapView().getModel()).getUndoHandler()
		    .isUndoActionRunning();
	}

	/**
	 * @param node
	 * @param gap
	 * @param hgap
	 * @param i
	 */
	public void moveNodePosition(final NodeModel node, final int gap,
	                             final int hgap, final int i) {
		((MLocationController) getLocationController()).moveNodePosition(node,
		    gap, hgap, i);
	}

	@Override
	public void onUpdate(final NodeModel node, final Object property,
	                     final Object oldValue, final Object newValue) {
		super.onUpdate(node, property, oldValue, newValue);
	}

	@Override
	public void plainClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			getLinkController().loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			if (!node.getModeController().getMapController().hasChildren(node)) {
				doubleClick(e);
				return;
			}
			((MMapController) getMapController()).toggleFolded();
		}
	}

	public void registerPlugin(final IMindMapControllerPlugin pPlugin) {
		mPlugins.add(pPlugin);
	}

	/**
	 *
	 */
	public boolean save() {
		return ((FileManager) getUrlManager()).save(getMapView().getModel());
	}

	void setAttributeController(final MAttributeController attributeController) {
		this.attributeController = attributeController;
	}

	void setPatternController(final MPatternController patternController) {
		this.patternController = patternController;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		for (final Iterator i = mRegistrations.iterator(); i.hasNext();) {
			final IHookRegistration registrationInstance = (IHookRegistration) i
			    .next();
			registrationInstance.deRegister();
		}
		((MNoteController) getNoteController()).shutdownController();
		getHookFactory().deregisterAllRegistrationContainer();
		mRegistrations.clear();
	}

	/**
	 * This method is called after and before a change of the map mapView. Use
	 * it to perform the actions that cannot be performed at creation time.
	 */
	@Override
	public void startup() {
		final IHookFactory hookFactory = getHookFactory();
		final List pluginRegistrations = hookFactory.getRegistrations();
		for (final Iterator i = pluginRegistrations.iterator(); i.hasNext();) {
			try {
				final RegistrationContainer container = (RegistrationContainer) i
				    .next();
				final Class registrationClass = container.hookRegistrationClass;
				final Constructor hookConstructor = registrationClass
				    .getConstructor(new Class[] { ModeController.class });
				final IHookRegistration registrationInstance = (IHookRegistration) hookConstructor
				    .newInstance(new Object[] { this });
				hookFactory.registerRegistrationContainer(container,
				    registrationInstance);
				registrationInstance.register();
				mRegistrations.add(registrationInstance);
			}
			catch (final Exception e) {
				org.freeplane.main.Tools.logException(e);
			}
		}
		super.startup();
		((MNoteController) getNoteController()).startupController();
	}

	public void storeDialogPositions(
	                                 final JDialog dialog,
	                                 final WindowConfigurationStorage pStorage,
	                                 final String window_preference_storage_property) {
		XmlBindingTools.getInstance().storeDialogPositions(dialog, pStorage,
		    window_preference_storage_property);
	}

	public void undo() {
		undo.actionPerformed(null);
		redo.reset();
	}

	/**
	 */
	@Override
	public void updateMenus(final MenuBuilder builder) {
		((MIconController) getIconController()).updateIconToolbar();
		((MIconController) getIconController()).updateMenus(builder);
		getPatternController().createPatternSubMenu(builder,
		    UserInputListenerFactory.NODE_POPUP);
		final MindMapHookFactory hookFactory = (MindMapHookFactory) getHookFactory();
		for (int i = 0; i < hookActions.size(); ++i) {
			final AbstractAction hookAction = (AbstractAction) hookActions
			    .get(i);
			final String hookName = ((IHookAction) hookAction).getHookName();
			hookFactory.decorateAction(hookName, hookAction);
			final List hookMenuPositions = hookFactory
			    .getHookMenuPositions(hookName);
			for (final Iterator j = hookMenuPositions.iterator(); j.hasNext();) {
				final String key = (String) j.next();
				final int pos = key.lastIndexOf('/');
				final String relativeKey = key.substring(0, pos);
				if (relativeKey.startsWith("/main_toolbar")) {
					final JButton button = new JButton(hookAction);
					button.setText(null);
					builder.addComponent(relativeKey, button,
					    MenuBuilder.AS_CHILD);
				}
				else {
					final JMenuItem menuItem = hookFactory.getMenuItem(
					    hookName, hookAction);
					builder.addMenuItem(relativeKey, menuItem, key,
					    MenuBuilder.AS_CHILD);
				}
			}
		}
		final String formatMenuString = FreemindMenuBar.FORMAT_MENU;
		getPatternController().createPatternSubMenu(builder, formatMenuString);
	}

	@Override
	protected void updateMenus(final String resource) {
		super.updateMenus(resource);
	}
}
