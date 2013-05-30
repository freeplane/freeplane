package org.freeplane.uispec4j.framework;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.uispec4j.AbstractUIComponent;
import org.uispec4j.ComponentAmbiguityException;
import org.uispec4j.ItemNotFoundException;
import org.uispec4j.Key;
import org.uispec4j.Mouse;
import org.uispec4j.Table;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.finder.ComponentFinder;

public class Node extends TextBox {
	public static final String TYPE_NAME = "node";
	@SuppressWarnings("rawtypes")
    public static final Class[] SWING_CLASSES = { MainView.class };

	private final static AttributeTableMatcher attributeTableMatcher = new AttributeTableMatcher();
	private ComponentFinder finder;

	private MainView nodeMainView = null;
	private Table table;

	public Node(MainView nodeMainView) {
		super(nodeMainView);
		this.nodeMainView = nodeMainView;
	}

	public String getDescriptionTypeName() {
		return TYPE_NAME;
	}

	public String getName() {
		return nodeMainView.getText();
	}

	public JComponent getAwtComponent() {
		return nodeMainView;
	}

	public MainView getMainView() {
		return nodeMainView;
	}

	public NodeView getNodeView() {
		return nodeMainView.getNodeView();
	}

	public Table getAttributeTable() throws ComponentAmbiguityException, ItemNotFoundException {
		if (table == null) {
			final Component jtable = getFinder().getComponent(attributeTableMatcher);
			table = new Table((JTable) jtable);
		}
		return table;
	}

	public Table findAttributeTable() throws ComponentAmbiguityException, ItemNotFoundException {
		if (table == null) {
			final Component jtable = getFinder().findComponent(attributeTableMatcher);
			if(jtable != null){
				table = new Table((JTable) jtable);
			}
		}
		return table;
	}

	  public Assertion containsAttributeTable() {
		    return new Assertion() {
		      public void check() {
		        Assert.assertTrue(findAttributeTable() != null);
		      }
		    };
		  }

	private ComponentFinder getFinder() {
		if (finder == null) {
			finder = new ComponentFinder(getNodeView());
		}
		return finder;
	}

	public void click(int row, int column, Key.Modifier modifier) {
		Rectangle rect = new Rectangle(0, 0, nodeMainView.getWidth(), nodeMainView.getHeight());
		Mouse.doClickInRectangle(this, rect, false, modifier);
	}

	public void rightClick(int row, int column) {
		Rectangle rect = new Rectangle(0, 0, nodeMainView.getWidth(), nodeMainView.getHeight());
		Mouse.doClickInRectangle(this, rect, true, Key.Modifier.NONE);
	}

	public void doubleClick(int row, int column) {
		Rectangle rect = new Rectangle(0, 0, nodeMainView.getWidth(), nodeMainView.getHeight());
		Mouse.doClickInRectangle(this, rect, false, Key.Modifier.NONE);
		Mouse.doDoubleClickInRectangle(getAwtComponent(), rect);
	}

	public Trigger triggerClick(final int row, final int column, final Key.Modifier modifier) {
		return new Trigger() {
			public void run() throws Exception {
				click(row, column, modifier);
			}
		};
	}

	public Trigger triggerRightClick(final int row, final int column) {
		return new Trigger() {
			public void run() throws Exception {
				rightClick(row, column);
			}
		};
	}

	public Trigger triggerDoubleClick(final int row, final int column) {
		return new Trigger() {
			public void run() throws Exception {
				doubleClick(row, column);
			}
		};
	}

	public void selectAsTheOnlyOneSelected(){
		final NodeView node = getNodeView();
		node.getMap().selectAsTheOnlyOneSelected(node);
	}

	public void toggleSelected(){
		final NodeView node = getNodeView();
		node.getModel().setFolded(!node.getModel().isFolded());
	}

	public Node getSelected(){
		final NodeView node = getNodeView();
		return new Node(node.getMap().getSelected().getMainView());
	}

	public AbstractUIComponent pressKey(Key key) {
		pressKey(nodeMainView, key);
		return this;
	}
	
	private static void pressKey(final Component component, final Key key) {
		int keyCode = key.getCode();
		int modifier = key.getModifier().getCode();
		KeyEvent event = new KeyEvent(component, KeyEvent.KEY_PRESSED, 0, modifier, keyCode, (char)keyCode);
		if (component.getKeyListeners().length > 0) {
			for (int i = 0; i < component.getKeyListeners().length; i++) {
				KeyListener keyListener = component.getKeyListeners()[i];
				keyListener.keyPressed(event);
			}
		}
		SwingUtilities.processKeyBindings(event);
	}

}
