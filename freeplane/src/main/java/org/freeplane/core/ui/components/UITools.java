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
package org.freeplane.core.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.application.FreeplaneSplashModern;

/**
 * Utilities for accessing the GUI, creating dialogs etc.: In scripts available as "global variable" <code>ui</code>.
 * <p>
 * In scripts this would be a simple way of opening a info popup:
 * <pre>
 * ui.informationMessage("Hello World!")
 * ui.informationMessage(ui.frame, "Hello World!") // longer version, equivalent
 * </pre>
 *
 * @author Dimitry Polivaev
 * @since 29.12.2008
 */
public class UITools {
	@SuppressWarnings("serial")
    public static final class InsertEolAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        	JTextComponent c = (JTextComponent) e.getSource();
        	c.replaceSelection("\n");
        }
    }

	public static final String MAIN_FREEPLANE_FRAME = "mainFreeplaneFrame";

	public static void addEscapeActionToDialog(final JDialog dialog) {
		class EscapeAction extends AbstractAction {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			};
		}
		UITools.addEscapeActionToDialog(dialog, new EscapeAction());
	}

	public static void addEscapeActionToDialog(final JDialog dialog, final Action action) {
		UITools.addKeyActionToDialog(dialog, action, "ESCAPE", "end_dialog");
	}

	public static void addKeyActionToDialog(final JDialog dialog, final Action action, final String keyStroke,
	                                        final String actionId) {
		action.putValue(Action.NAME, actionId);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke),
		    action.getValue(Action.NAME));
		dialog.getRootPane().getActionMap().put(action.getValue(Action.NAME), action);
	}

	public static void convertPointFromAncestor(final Component ancestor, final Point p, Component c) {
		int x, y;
		while (c != ancestor && c != null) {
			x = c.getX();
			y = c.getY();
			p.x -= x;
			p.y -= y;
			c = c.getParent();
		};
	}

	public static void convertPointToAncestor(final Component source, final Point point, final Class<?> ancestorClass) {
		final Component destination = SwingUtilities.getAncestorOfClass(ancestorClass, source);
		UITools.convertPointToAncestor(source, point, destination);
	}

	public static void convertRectangleToAncestor(final Component from, final Rectangle r, final Component destination) {
		Point p = new Point(r.x, r.y);
		UITools.convertPointToAncestor(from, p , destination);
		r.x = p.x;
		r.y = p.y;
	}

	public static void convertPointToAncestor(final Component from, final Point p, final Component destination) {
		int x, y;
		for (Component c = from; c != destination && c != null; c = c.getParent()) {
			x = c.getX();
			y = c.getY();
			p.x += x;
			p.y += y;
		};
	}

	static public void errorMessage(final Object message) {
		final String myMessage;
		if (message != null) {
			myMessage = message.toString();
		}
		else {
			myMessage = TextUtils.getText("undefined_error");
		}
		LogUtils.warn(myMessage);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), myMessage, "Freeplane", JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	
	static public Component getCurrentRootComponent(){
		return Controller.getCurrentController().getViewController().getCurrentRootComponent();
	}

	public static Frame getCurrentFrame() {
		final Component currentRootComponent = getCurrentRootComponent();
		return currentRootComponent instanceof Frame ? (Frame)currentRootComponent : JOptionPane.getFrameForComponent(currentRootComponent);
	}

	public static Frame getFrame() {
		final Component currentRootComponent = getMenuComponent();
		return currentRootComponent instanceof Frame ? (Frame)currentRootComponent : JOptionPane.getFrameForComponent(currentRootComponent);
	}

	static public Component getMenuComponent(){
		return Controller.getCurrentController().getViewController().getMenuComponent();
	}
	
	/** returns a KeyStroke if possible and null otherwise. */
	public static KeyStroke getKeyStroke(final String keyStrokeDescription) {
		if (keyStrokeDescription == null) {
			return null;
		}
		final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeDescription);
		if (keyStroke != null) {
			return keyStroke;
		}
		final int lastSpacePos = keyStrokeDescription.lastIndexOf(' ') + 1;
		final String modifiedDescription = keyStrokeDescription.substring(0, lastSpacePos) + "typed "
		        + keyStrokeDescription.substring(lastSpacePos);
		return KeyStroke.getKeyStroke(modifiedDescription);
	}

	/** formats a KeyStroke in a ledgible way, e.g. Control+V. Null is converted to "".
	 * Taken from MotifGraphicsUtils.paintMenuItem(). */
	public static String keyStrokeToString(KeyStroke keyStroke) {
		String acceleratorText = "";
		if (keyStroke != null) {
		    int modifiers = keyStroke.getModifiers();
		    if (modifiers > 0) {
			acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
			acceleratorText += "+";
		    }
		    acceleratorText += KeyEvent.getKeyText(keyStroke.getKeyCode());
		}
		return acceleratorText;
	}

	static public void informationMessage(final String message) {
		UITools.informationMessage(UITools.getCurrentRootComponent(), message);
	}

	static public void informationMessage(final Component frame, final String message) {
		UITools.informationMessage(frame, message, "Freeplane");
	}

	static public void informationMessage(final Component frame, final String message, final String title) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void informationMessage(final Component frame, final String text, final String string, final int type) {
		JOptionPane.showMessageDialog(frame, text, string, type);
	}

	static public void setBounds(final Component frame, int win_x, int win_y, int win_width, int win_height) {
		final Rectangle frameBounds = getValidFrameBounds(frame, win_x, win_y, win_width, win_height);
		frame.setBounds(frameBounds);
	}

	public static Rectangle getValidFrameBounds(final Component frame, int win_x, int win_y, int win_width,
			int win_height) {
		GraphicsConfiguration graphicsConfiguration = findGraphicsConfiguration(frame, win_x, win_y);
		final Rectangle screenBounds = getScreenBounds(graphicsConfiguration);
		int screenWidth = screenBounds.width;
		if(win_width != -1)
			win_width = Math.min(win_width, screenWidth );
		else
			win_width =  screenWidth * 4 / 5;
		int screenHeight = screenBounds.height;
		if(win_height != -1)
			win_height = Math.min(win_height, screenHeight);
		else
			win_height =  screenHeight * 4 / 5;
		if(win_x != -1){
			win_x = Math.min(screenWidth + screenBounds.x - win_width, win_x);
			win_x = Math.max(screenBounds.x, win_x);
		}
		else
			win_x = screenBounds.x + (screenWidth - win_width) / 2;
		if(win_y != -1){
			win_y = Math.max(screenBounds.y, win_y);
			win_y = Math.min(screenHeight + screenBounds.y - win_height, win_y);
		}
		else
			win_y = screenBounds.y + (screenHeight - win_height) / 2;
		final Rectangle frameBounds = new Rectangle( win_x, win_y, win_width, win_height);
		return frameBounds;
	}

	private static GraphicsConfiguration findGraphicsConfiguration(final Component component, int x, int y) {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	      GraphicsDevice[] gs = ge.getScreenDevices();
	      for (int j = 0; j < gs.length; j++) {
	          GraphicsDevice gd = gs[j];
	          GraphicsConfiguration[] gc = gd.getConfigurations();
	          for (int i=0; i < gc.length; i++) {
	              final Rectangle screenBounds = gc[i].getBounds();
	              if(screenBounds.contains(x, y))
	            	  return gc[i];
	          }
	      }
		return component != null ? component.getGraphicsConfiguration() : null;
	}

	public static Rectangle getAvailableScreenBounds(Component frame) {
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		return getScreenBounds(graphicsConfiguration);
    }

	public static Rectangle getScreenBounds(final GraphicsConfiguration graphicsConfiguration) {
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(graphicsConfiguration);
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Point screenLocation = screenBounds.getLocation();
		final Dimension screenSize = screenBounds.getSize();
		final int screenWidth = screenSize.width - screenInsets.left - screenInsets.right;
		final int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
		return new Rectangle(screenLocation.x + screenInsets.left,  screenLocation.y + screenInsets.top, screenWidth, screenHeight);
	}

	public static void setDialogLocationRelativeTo(final JDialog dialog, final Component c) {
		if (c == null || ! c.isShowing()) {
			return;
		}
		final Point compLocation = c.getLocationOnScreen();
		final int cw = c.getWidth();
		final int ch = c.getHeight();
		final Container parent = dialog.getParent();
		final Point parentLocation = parent.getLocationOnScreen();
		final int pw = parent.getWidth();
		final int ph = parent.getHeight();
		final int dw = dialog.getWidth();
		final int dh = dialog.getHeight();
		final Rectangle desktopBounds = getAvailableScreenBounds(c);
		final int minX = Math.max(parentLocation.x, desktopBounds.x);
		final int minY = Math.max(parentLocation.y, desktopBounds.y);
		final int maxX = Math.min(parentLocation.x + pw, desktopBounds.x + desktopBounds.width);
		final int maxY = Math.min(parentLocation.y + ph, desktopBounds.y + desktopBounds.height);
		int dx, dy;
		if (compLocation.x + cw < minX) {
			dx = minX;
		}
		else if (compLocation.x > maxX) {
			dx = maxX - dw;
		}
		else {
			final int leftSpace = compLocation.x - minX;
			final int rightSpace = maxX - (compLocation.x + cw);
			if (leftSpace > rightSpace) {
				if (leftSpace > dw) {
					dx = compLocation.x - dw;
				}
				else {
					dx = minX;
				}
			}
			else {
				if (rightSpace > dw) {
					dx = compLocation.x + cw;
				}
				else {
					dx = maxX - dw;
				}
			}
		}
		if (compLocation.y + ch < minY) {
			dy = minY;
		}
		else if (compLocation.y > maxY) {
			dy = maxY - dh;
		}
		else {
			final int topSpace = compLocation.y - minY;
			final int bottomSpace = maxY - (compLocation.y + ch);
			if (topSpace > bottomSpace) {
				if (topSpace > dh) {
					dy = compLocation.y - dh;
				}
				else {
					dy = minY;
				}
			}
			else {
				if (bottomSpace > dh) {
					dy = compLocation.y + ch;
				}
				else {
					dy = maxY - dh;
				}
			}
		}
		dialog.setLocation(dx, dy);
	}

	public static void setDialogLocationRelativeTo(final JDialog dialog,
	                                               final NodeModel node) {
		if (node == null) {
			return;
		}
		final IMapViewManager viewController = Controller.getCurrentController().getMapViewManager();
		viewController.scrollNodeToVisible(node);
		final Component c = viewController.getComponent(node);
		UITools.setDialogLocationRelativeTo(dialog, c);
	}

	public static void setDialogLocationUnder(final JDialog dialog, final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final IMapViewManager viewController = controller.getMapViewManager();
		final JComponent c = (JComponent) viewController.getComponent(node);
		final int x = 0;
		final int y = c.getHeight();
		final Point location = new Point(x, y);
		SwingUtilities.convertPointToScreen(location, c);
		UITools.setBounds(dialog, location.x, location.y, dialog.getWidth(), dialog.getHeight());
	}

	/**
	 * Shows the error message  "attributes_adding_empty_attribute_error"
	 */
	public static void showAttributeEmptyStringErrorMessage() {
		JOptionPane.showMessageDialog(null, TextUtils.getText("attributes_adding_empty_attribute_error"),
		    TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
	}

	static public void showMessage(String message, int messageType) {
		backOtherWindows();
		JTextArea infoPane = new JTextArea();
		infoPane.setEditable(false);
		infoPane.setMargin(new Insets(5,5,5,5));
		infoPane.setLineWrap(true);
		infoPane.setWrapStyleWord(true);
		infoPane.setText(message);
		infoPane.setColumns(60);
		JScrollPane scrollPane = new JScrollPane(infoPane);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		JOptionPane.showMessageDialog(getCurrentRootComponent(), scrollPane, "Freeplane", messageType);
	}
	public static int showConfirmDialog(final NodeModel node, final Object message, final String title,
	                                    final int optionType, final int messageType) {
		final Controller controller = Controller.getCurrentController();
		final IMapViewManager viewController = controller.getMapViewManager();
		final Component parentComponent;
		if (node == null) {
			parentComponent = getCurrentRootComponent();
		}
		else {
			viewController.scrollNodeToVisible(node);
			parentComponent = viewController.getComponent(node);
		}
		return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
	}

	public static int showConfirmDialog( final NodeModel node, final Object message,
	                                    final String title, final int optionType) {
		return showConfirmDialog( node, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
	}

	public static String showInputDialog( final NodeModel node, final String message,
	                                     final String initialValue) {
		if (node == null) {
			return null;
		}
		final Controller controller = Controller.getCurrentController();
		final IMapViewManager viewController = controller.getMapViewManager();
		viewController.scrollNodeToVisible(node);
		final Component parentComponent = viewController.getComponent(node);
		return JOptionPane.showInputDialog(parentComponent, message, initialValue);
	}

	public static String showInputDialog( final NodeModel node, final String text,
	                                     final String title, final int type) {
		if (node == null) {
			return null;
		}
		final Controller controller = Controller.getCurrentController();
		final IMapViewManager viewController = controller.getMapViewManager();
		viewController.scrollNodeToVisible(node);
		final Component parentComponent = viewController.getComponent(node);
		return JOptionPane.showInputDialog(parentComponent, text, title, type);
	}

	public static final String SCROLLBAR_INCREMENT = "scrollbar_increment";

	public static void setScrollbarIncrement(final JScrollPane scrollPane) {
		final int scrollbarIncrement = ResourceController.getResourceController()
		    .getIntProperty(SCROLLBAR_INCREMENT, 1);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(scrollbarIncrement);
		scrollPane.getVerticalScrollBar().setUnitIncrement(scrollbarIncrement);
	}

	public static void addScrollbarIncrementPropertyListener(final JScrollPane scrollPane) {
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (!propertyName.equals(SCROLLBAR_INCREMENT)) {
					return;
				}
				final int scrollbarIncrement = Integer.valueOf(newValue);
				scrollPane.getHorizontalScrollBar().setUnitIncrement(scrollbarIncrement);
				scrollPane.getVerticalScrollBar().setUnitIncrement(scrollbarIncrement);
			}
		});
	}

	public static Color getTextColorForBackground(final Color color) {
		final int red = color.getRed();
		final int blue = color.getBlue();
		final int green = color.getGreen();
		return red > 0x80 && blue > 0x80 && green > 0x80 ? Color.BLACK : Color.WHITE;
	}

	public static final Dimension MAX_BUTTON_DIMENSION = new Dimension(1000, 1000);

// FIXME: not used - can we remove it? -- Volker
//	public static Controller getController(Component c) {
//		if(c == null){
//			return null;
//		}
//	    final JRootPane rootPane = SwingUtilities.getRootPane(c);
//		if(rootPane == null){
//			return null;
//		}
//	    Controller controller = (Controller) rootPane.getClientProperty(Controller.class);
//	    if(controller != null){
//	    	return controller;
//	    }
//	    return getController(JOptionPane.getFrameForComponent(rootPane));
//    }

	public static void focusOn(JComponent component) {
		component.addAncestorListener(new AncestorListener() {
			public void ancestorRemoved(AncestorEvent event) {
			}

			public void ancestorMoved(AncestorEvent event) {
			}

			public void ancestorAdded(AncestorEvent event) {
				final JComponent component = event.getComponent();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						component.requestFocus();					}
				});
				component.removeAncestorListener(this);
			}
		});
    }

	public static BasicStroke createStroke(int width, final int[] dash) {
        final float[] fdash;
    	if(dash  != null){
    		fdash = new float[dash.length];
    		int i = 0;
    		for(float d : dash){
    			fdash[i++] = d;
    		}
    	}
    	else{
    		fdash = null;
    	}
    	final BasicStroke stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, fdash, 0f);
        return stroke;
    }

	public static void repaintAll(Container root) {
		root.repaint();
		for(int i = 0; i < root.getComponentCount(); i++){
			final Component component = root.getComponent(i);
			if(component instanceof Container){
				repaintAll((Container) component);
			}
			else{
				component.repaint();
			}
		}
	}

	public static JDialog createCancelDialog(final Component component, final String titel, final String text) {
        final String[] options = { TextUtils.getText("cancel") };
    	final JOptionPane infoPane = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
    	    options);
    	JDialog dialog = infoPane.createDialog(component, titel);
    	dialog.setModal(false);
    	return dialog;
    }

	public static void addTitledBorder(final JComponent c, final String title, final float size) {
        final TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        final Font titleFont = UIManager.getFont("TitledBorder.font");
        titledBorder.setTitleFont(titleFont.deriveFont(size));
    	final Border btnBorder = c.getBorder();
    	if(btnBorder != null){
    	final CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(titledBorder, btnBorder);
    	c.setBorder(compoundBorder);
    	}
    	else{
    		c.setBorder(titledBorder);
    	}
    }

	public static void backOtherWindows() {
	    Component owner = getMenuComponent();
		if(owner instanceof Window){
        	final Window[] ownedWindows = ((Window) owner).getOwnedWindows();
        	for(Window w : ownedWindows){
        		if(w.isVisible()){
        			w.toBack();
        		}
        	}
        }
    }

	public static JButton createHtmlLinkStyleButton(final URI uri, final String title) {
        final JButton button = new JButton("<html><a href='" + uri + "'>" + title);
    	button.setBorderPainted(false);
    	button.setOpaque(false);
    	button.setBackground(Color.lightGray);
    	button.setFocusable(false);
    	button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    	button.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			final ModeController modeController = Controller.getCurrentModeController();
    			final UrlManager urlManager = modeController.getExtension(UrlManager.class);
    			urlManager.loadURL(uri);
    		}
    	});
    	return button;
	}

	public static final int getComponentIndex(Component component) {
		if (component != null && component.getParent() != null) {
			Container c = component.getParent();
			for (int i = 0; i < c.getComponentCount(); i++) {
				if (c.getComponent(i) == component)
					return i;
			}
		}

		return -1;
	}

	public static final float FONT_SCALE_FACTOR;
	static {
		float factor = 1f;
		try {
	        factor = UITools.getScaleFactor();
        }
        catch (Exception e) {
        }
		FONT_SCALE_FACTOR = factor;
	}

	private static float getScaleFactor() {
		final int systemScreenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
		if(ResourceController.getResourceController().getBooleanProperty("apply_system_screen_resolution")){
			int windowX = ResourceController.getResourceController().getIntProperty("appwindow_x", 0);
			int windowY = ResourceController.getResourceController().getIntProperty("appwindow_y", 0);
			final GraphicsConfiguration graphicsConfiguration = findGraphicsConfiguration(null, windowX, windowY);
			if(graphicsConfiguration != null) {
				final AffineTransform normalizingTransform = graphicsConfiguration.getNormalizingTransform();
				return (float) normalizingTransform.getScaleX();
			}
			else
				return systemScreenResolution / 72f;
		}
		else
			return ResourceController.getResourceController().getIntProperty("user_defined_screen_resolution", systemScreenResolution)  / 72f;
    }
	
	public static Font scale(Font font) {
		return font.deriveFont(font.getSize2D()*FONT_SCALE_FACTOR);
	}
	
	public static Font scaleFontInt(Font font, double additionalFactor) {
		return font.deriveFont(font.getStyle(), Math.round(font.getSize2D()*UITools.FONT_SCALE_FACTOR * additionalFactor));
	}
	
	
	public static Font invertScale(Font font) {
		return font.deriveFont(font.getSize2D()/FONT_SCALE_FACTOR);
	}

	public static void showFrame() {
		final Component component = UITools.getMenuComponent();
		if(component instanceof Window) {
			Window window = (Window) component;
			final Window[] ownedWindows = window.getOwnedWindows();
			for (int i = 0; i < ownedWindows.length; i++) {
				final Window ownedWindow = ownedWindows[i];
				if (ownedWindow.getClass().equals(FreeplaneSplashModern.class) && ownedWindow.isVisible()) {
					ownedWindow.setVisible(false);
				}
			}
			if(window != null && ! window.isVisible()){
				window.setVisible(true);
				window.toFront();
			}
		}
    }

	public static boolean isEditingText() {
	    final Component focusOwner = FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		final boolean isTextComponentFocused = focusOwner instanceof JEditorPane;
		return isTextComponentFocused && focusOwner.isShowing() && ((JTextComponent)focusOwner).isEditable();
    }

	public static void executeWhenNodeHasFocus(final Runnable runnable) {
		final Component selectedComponent = Controller.getCurrentController().getMapViewManager().getSelectedComponent();
		if(selectedComponent != null && ! selectedComponent.hasFocus()){
			selectedComponent.addFocusListener(new  FocusListener() {
	
				@Override
				public void focusLost(FocusEvent e) {
				}
	
				@Override
				public void focusGained(FocusEvent e) {
					selectedComponent.removeFocusListener(this);
					runnable.run();
				}
			});
			selectedComponent.requestFocusInWindow();
		}
		else
			runnable.run();
	}

}
