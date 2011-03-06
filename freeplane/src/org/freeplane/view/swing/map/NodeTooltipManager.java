package org.freeplane.view.swing.map;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.common.map.AMapChangeListenerAdapter;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;

public class NodeTooltipManager implements IExtension{
	private static final String TOOL_TIP_MANAGER = "toolTipManager.";
	private static final String TOOL_TIP_MANAGER_INITIAL_DELAY = "toolTipManager.initialDelay";
	private static final String RESOURCES_SHOW_NODE_TOOLTIPS = "show_node_tooltips";
	private Timer enterTimer;
	private Timer exitTimer;
	private String toolTipText;
	private JComponent insideComponent;
	private MouseEvent mouseEvent;
	
	private Popup tipPopup;
	/** The Window tip is being displayed in. This will be non-null if
	 * the Window tip is in differs from that of insideComponent's Window.
	 */
	private JToolTip tip;
	final private ComponentMouseListener componentMouseListener;
	private WeakReference<Component> focusOwnerRef;

	public static NodeTooltipManager getSharedInstance(ModeController modeController){
		{
			final NodeTooltipManager instance = (NodeTooltipManager) modeController.getExtension(NodeTooltipManager.class);
			if(instance != null){
				return instance;
			}
		}
		final NodeTooltipManager instance = new NodeTooltipManager();
		final int maxWidth = ResourceController.getResourceController().getIntProperty(
			"toolTipManager.max_tooltip_width", Integer.MAX_VALUE);
		NodeTooltip.setMaximumWidth(maxWidth);
		setTooltipDelays(instance);
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.startsWith(TOOL_TIP_MANAGER)) {
					setTooltipDelays(instance);
				}
			}
		});
		IMapChangeListener mapChangeListener = new AMapChangeListenerAdapter() {

			@Override
            public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
				instance.hideTipWindow();
            }

			@Override
            public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				instance.hideTipWindow();
            }

			@Override
            public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child,
                                    int newIndex) {
				instance.hideTipWindow();
            }
			
		};
		modeController.getMapController().addMapChangeListener(mapChangeListener);
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
		if (insideComponent == null || !insideComponent.isShowing())
			return;
		tip = insideComponent.createToolTip();
		tip.addComponentListener(new ComponentAdapter() {

			@Override
            public void componentResized(ComponentEvent e) {
				final NodeTooltip component = (NodeTooltip) e.getComponent();
				component.scrollUp();
				component.removeComponentListener(this);
            }
			
		});

		tip.setTipText(toolTipText);
		PopupFactory popupFactory = PopupFactory.getSharedInstance();
		final JComponent nearComponent;
//		if (insideComponent instanceof MainView) {
//			nearComponent = ((MainView)insideComponent).getNodeView().getContent();
//		}
//		else{
		nearComponent = insideComponent;
//		}
		final Point locationOnScreen = nearComponent.getLocationOnScreen();
		final int height = nearComponent.getHeight();
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(nearComponent.getGraphicsConfiguration());
		final Dimension screenSize = defaultToolkit.getScreenSize();
		final int minX = screenInsets.left;
		final int maxX = screenSize.width - screenInsets.right;
		final int minY = screenInsets.top;
		final int maxY = screenSize.height - screenInsets.bottom;
		int x = locationOnScreen.x;
		int y = locationOnScreen.y + height;
		final Dimension tipSize = tip.getPreferredSize();
		final int tipWidth = tipSize.width;
		if(x + tipWidth > maxX){
			x = maxX - tipWidth;
			if(x < minX){
				x = minX;
			}
		}
		final int tipHeight = tipSize.height;
		if(y + tipHeight > maxY){
			if(locationOnScreen.y - height > minY){
				y = locationOnScreen.y - tipHeight;
			}
			else{
				y = maxY - tipHeight;
				if(y < minY){
					y = minY;
				}
			}
		}
		focusOwnerRef = new WeakReference<Component>(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
		tipPopup = popupFactory.getPopup(nearComponent, tip, x, y);
		tipPopup.show();
	}

	private void hideTipWindow() {
		insideComponent = null;
		toolTipText = null;
		mouseEvent = null;
		if (tipPopup != null) {
			final Component component;
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(SwingUtilities.isDescendingFrom(focusOwner, tip)){
				component = focusOwnerRef.get();
			}
			else
				component = null;
			tipPopup.hide();
			if(component != null)
				component.requestFocusInWindow();
			tipPopup = null;
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

		public void mouseEntered(MouseEvent event) {
			initiateToolTip(event);
		}
		public void mouseMoved(MouseEvent event) {
			initiateToolTip(event);
		}
		public void mouseExited(MouseEvent event) {
			exitTimer.start();
		}
		public void mouseDragged(MouseEvent e) {
        }
		@Override
        public void mousePressed(MouseEvent e) {
	        hideTipWindow();
        }
	}
	
	private void initiateToolTip(MouseEvent event) {
	JComponent component = (JComponent) event.getSource();
	if(insideComponent == component){
		return;
	}
	hideTipWindow();
	insideComponent = component;
	mouseEvent = event;
	if(ResourceController.getResourceController().getBooleanProperty(RESOURCES_SHOW_NODE_TOOLTIPS))
		enterTimer.restart();
}

	private class insideTimerAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (insideComponent != null){ 
				if( insideComponent.isShowing() && insideComponent.getMousePosition(true) != null) {
					// Lazy lookup
					if (toolTipText == null && mouseEvent != null) {
						toolTipText = insideComponent.getToolTipText(mouseEvent);
					}
					if (toolTipText != null) {
						showTipWindow();
					}
					else {
						hideTipWindow();
					}
				}
				else{
					hideTipWindow();
				}
			}
		}
	}

	private class exitTimerAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(tip == null || insideComponent == null){
				return;
			}
			if(tip.getMousePosition(true) != null || insideComponent.getMousePosition(true) != null){
				exitTimer.restart();
				return;
			}
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusOwner != null){
				if(SwingUtilities.isDescendingFrom(focusOwner, tip)){
					exitTimer.restart();
					return;
				}
			}
			hideTipWindow();
		}
	}

}
