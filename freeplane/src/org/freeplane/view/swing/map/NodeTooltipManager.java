package org.freeplane.view.swing.map;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;

public class NodeTooltipManager{
	private static final String TOOL_TIP_MANAGER = "toolTipManager.";
	private static final String TOOL_TIP_MANAGER_INITIAL_DELAY = "toolTipManager.initialDelay";
	private static NodeTooltipManager instance;
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

	public static NodeTooltipManager getSharedInstance(){
		if(instance == null){
			instance = new NodeTooltipManager();
			final int maxWidth = ResourceController.getResourceController().getIntProperty(
			    "toolTipManager.max_tooltip_width", Integer.MAX_VALUE);
			NodeTooltip.setMaximumWidth(maxWidth);
			setTooltipDelays();
			ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
				public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
					if (propertyName.startsWith(TOOL_TIP_MANAGER)) {
						setTooltipDelays();
					}
				}
			});
		}
		return instance;
	}
	private static void setTooltipDelays() {
		final int initialDelay = ResourceController.getResourceController().getIntProperty(
		    TOOL_TIP_MANAGER_INITIAL_DELAY, 0);
		instance.setInitialDelay(initialDelay);
    }
	private NodeTooltipManager() {
		enterTimer = new Timer(750, new insideTimerAction());
		enterTimer.setRepeats(false);
		exitTimer = new Timer(750, new exitTimerAction());
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
//	    InputMap inputMap = tip.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//	    ActionMap actionMap = tip.getActionMap();
//
//	    if (inputMap != null && actionMap != null) {
//	    	final KeyStroke hideTip = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
//	    	final Action hideTipAction = new AbstractAction() {
//				public void actionPerformed(ActionEvent e) {
//					hideTipWindow();
//				}
//			};
//	    	inputMap.put(hideTip, "hideTip");
//	    	actionMap.put("hideTip", hideTipAction);
//	    }

		tip.setTipText(toolTipText);
		PopupFactory popupFactory = PopupFactory.getSharedInstance();
		final Point locationOnScreen = insideComponent.getLocationOnScreen();
		final int height = insideComponent.getHeight();
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(insideComponent.getGraphicsConfiguration());
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
		tipPopup = popupFactory.getPopup(insideComponent, tip, x, y);
		tipPopup.show();
	}

	public void hideTipWindow() {
		insideComponent = null;
		toolTipText = null;
		mouseEvent = null;
		if (tipPopup != null) {
			tipPopup.hide();
			tipPopup = null;
			tip = null;
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


	private class ComponentMouseListener extends MouseAdapter {

		public void mouseEntered(MouseEvent event) {
			initiateToolTip(event);
		}
		public void mouseMoved(MouseEvent event) {
			initiateToolTip(event);
		}
		public void mouseExited(MouseEvent event) {
				exitTimer.start();
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
	enterTimer.restart();
}

	public boolean isOutside(JComponent component, MouseEvent event) {
		if(component == null){
			return true;
		}
		final Point point = event.getLocationOnScreen();
		SwingUtilities.convertPointFromScreen(point, component);
	    return !(point.x >= 0 && point.y >= 0 && point.x < component.getWidth() && point.y < component.getHeight());
    }


	private class insideTimerAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (insideComponent != null && insideComponent.isShowing() && insideComponent.getMousePosition(true) != null) {
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
