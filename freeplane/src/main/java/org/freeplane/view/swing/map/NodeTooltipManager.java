package org.freeplane.view.swing.map;


import java.awt.Component;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.ref.WeakReference;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MouseInsideListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class NodeTooltipManager implements IExtension{
	private static final String TOOL_TIP_MANAGER = "toolTipManager.";
	private static final String TOOL_TIP_MANAGER_INITIAL_DELAY = "toolTipManager.initialDelay";
	private static final String RESOURCES_SHOW_NODE_TOOLTIPS = "show_node_tooltips";
	private final Timer enterTimer;
	private final Timer exitTimer;
	private String toolTipText;
	private JComponent insideComponent;
	private MouseEvent mouseEvent;

	private JPopupMenu tipPopup;
	/** The Window tip is being displayed in. This will be non-null if
	 * the Window tip is in differs from that of insideComponent's Window.
	 */
	private JToolTip tip;
	final private ComponentMouseListener componentMouseListener;
	private WeakReference<Component> focusOwnerRef;
	private boolean mouseOverComponent;
	private MouseInsideListener mouseInsideTooltipListener;

	public static NodeTooltipManager getSharedInstance(ModeController modeController){
		{
			final NodeTooltipManager instance = modeController.getExtension(NodeTooltipManager.class);
			if(instance != null){
				return instance;
			}
		}
		final NodeTooltipManager instance = new NodeTooltipManager();
		setTooltipDelays(instance);
		UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			@Override
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.startsWith(TOOL_TIP_MANAGER)) {
					setTooltipDelays(instance);
				}
			}
		});
		IMapChangeListener mapChangeListener = new IMapChangeListener() {

			@Override
            public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
				instance.hideTipWindow();
            }

			@Override
            public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				instance.hideTipWindow();
            }

			@Override
            public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
				instance.hideTipWindow();
            }

		};
		MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(mapChangeListener);
		INodeSelectionListener nodeSelectionListener = new INodeSelectionListener() {

			@Override
			public void onSelect(NodeModel node) {
				NodeView view = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, instance.insideComponent);
				if(view != null && node.equals(view.getModel()))
					return;
				instance.hideTipWindow();
			}

			@Override
			public void onDeselect(NodeModel node) {
			}
		};
		mapController.addNodeSelectionListener(nodeSelectionListener);
		modeController.addExtension(NodeTooltipManager.class, instance);
		return instance;
	}
	private static void setTooltipDelays(NodeTooltipManager instance) {
		final int initialDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_INITIAL_DELAY, 0);
		instance.setInitialDelay(initialDelay);
    }
	private NodeTooltipManager() {
		enterTimer = new Timer(750, new insideTimerAction());
		enterTimer.setRepeats(false);
		exitTimer = new Timer(150, new exitTimerAction());
		exitTimer.setRepeats(false);
		componentMouseListener = new ComponentMouseListener();
		mouseOverComponent = false;
	}

	/**
	* Specifies the initial delay value.
	*
	* @param milliseconds  the number of milliseconds to delay
	*        (after the cursor has paused) before displaying the
	*        tooltip
	* @see #getInitialDelay
	*/
	public void setInitialDelay(int milliseconds) {
		enterTimer.setInitialDelay(milliseconds);
	}

	/**
	 * Returns the initial delay value.
	 *
	 * @return an integer representing the initial delay value,
	 *		in milliseconds
	 * @see #setInitialDelay
	 */
	public int getInitialDelay() {
		return enterTimer.getInitialDelay();
	}


	private void showTipWindow() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (insideComponent == null || !insideComponent.isShowing() || focusOwner == null)
			return;
		tip = insideComponent.createToolTip();
		tip.setTipText(toolTipText);
		final JComponent nearComponent = insideComponent;
		focusOwnerRef = new WeakReference<Component>(focusOwner);
		tipPopup = new JPopupMenu();
		tipPopup.setLayout(new GridLayout(1, 1));
		tipPopup.add(tip);
		mouseInsideTooltipListener = new MouseInsideListener(tipPopup);
		final Component placedComponent = tipPopup;
		final Window window = SwingUtilities.getWindowAncestor(nearComponent);
		Point location = UITools.findBestLocation(placedComponent, nearComponent);
		SwingUtilities.convertPointFromScreen(location, nearComponent);
  		tipPopup.show(nearComponent, location.x, location.y);
		focusOwner.requestFocusInWindow();
        exitTimer.start();
	}

	private void hideTipWindow() {
		insideComponent = null;
		toolTipText = null;
		mouseEvent = null;
		if (tipPopup != null && tip != null) {
			final Component component;
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, tip)){
				component = focusOwnerRef.get();
			}
			else
				component = null;
			tipPopup.setVisible(false);
			if(component != null)
				component.requestFocusInWindow();
			tipPopup = null;
			mouseInsideTooltipListener = null;
			tip = null;
			focusOwnerRef = null;
			enterTimer.stop();
			exitTimer.stop();
		}
	}

	/**
	 * Registers a component for tooltip management.
	 * <p>
	 * This will register key bindings to show and hide the tooltip text
	 * only if <code>component</code> has focus bindings. This is done
	 * so that components that are not normally focus traversable, such
	 * as <code>JLabel</code>, are not made focus traversable as a result
	 * of invoking this method.
	 *
	 * @param component  a <code>JComponent</code> object to add
	 * @see JComponent#isFocusTraversable
	 */
	public void registerComponent(JComponent component) {
		component.removeMouseListener(componentMouseListener);
		component.removeMouseMotionListener(componentMouseListener);
		component.addMouseListener(componentMouseListener);
		component.addMouseMotionListener(componentMouseListener);
	}

	/**
	 * Removes a component from tooltip control.
	 *
	 * @param component  a <code>JComponent</code> object to remove
	 */
	public void unregisterComponent(JComponent component) {
		component.removeMouseListener(componentMouseListener);
	}


	private class ComponentMouseListener extends MouseAdapter implements MouseMotionListener{

		@Override
        public void mouseEntered(MouseEvent event) {
			initiateToolTip(event);
		}
		@Override
        public void mouseMoved(MouseEvent event) {
			initiateToolTip(event);
		}
		@Override
        public void mouseExited(MouseEvent event) {
			if(insideComponent == event.getComponent())
				mouseOverComponent = false;
		}

		@Override
        public void mouseDragged(MouseEvent e) {
        }
		@Override
        public void mousePressed(MouseEvent e) {
	        hideTipWindow();
        }
	}

	private void initiateToolTip(MouseEvent event) {
		JComponent component = (JComponent) event.getSource();
		Window focusedWindow = FocusManager.getCurrentManager().getFocusedWindow();
		if (focusedWindow == null) {
			return;
		}
		if(insideComponent == component){
			mouseOverComponent = true;
			return;
		}
		hideTipWindow();
		insideComponent = component;
		mouseEvent = event;
		if(ResourceController.getResourceController().getBooleanProperty(RESOURCES_SHOW_NODE_TOOLTIPS))
			enterTimer.restart();
	}

	protected boolean isMouseOverComponent() {
		return mouseOverComponent;
	}


	private class insideTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (insideComponent != null){
				if (isMouseOverComponent() && !editorActive()) {
					// Lazy lookup
					if (toolTipText == null && mouseEvent != null) {
						toolTipText = insideComponent.getToolTipText(mouseEvent);
					}
					if (toolTipText != null) {
						showTipWindow();
						return;
					}
				}
				hideTipWindow();
			}
		}

		private boolean editorActive() {
			return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextComponent;
		}
	}

	private class exitTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(tip == null || insideComponent == null){
				return;
			}
            final KeyboardFocusManager currentKeyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            final Window activeWindow = currentKeyboardFocusManager.getActiveWindow();
            if(activeWindow instanceof JDialog && ((JDialog) activeWindow).isModal()
            		&& ! SwingUtilities.isDescendingFrom(Controller.getCurrentController().getMapViewManager().getMapViewComponent(), activeWindow)){
                hideTipWindow();
                return;
            }

			if(isMouseOverTip() || isMouseOverComponent()){
				exitTimer.restart();
				return;
			}
            final Component focusOwner = currentKeyboardFocusManager.getFocusOwner();
			if(focusOwner != null){
				if(SwingUtilities.isDescendingFrom(focusOwner, tip)){
					exitTimer.restart();
					return;
				}
			}
			hideTipWindow();
		}

		protected boolean isMouseOverTip() {
	        return mouseInsideTooltipListener.isMouseInside();
        }
	}

}
