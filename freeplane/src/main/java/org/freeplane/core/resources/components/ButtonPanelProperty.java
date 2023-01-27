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
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.PopupDialog;
import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ButtonPanelProperty extends PropertyBean implements IPropertyControl, ActionListener {

    public enum ComponentBefore {
        NOTHING, SEPARATOR
    }

    static public class ButtonIcon{
        public final Icon icon;
        public final String tooltip;
        public final ComponentBefore componentBefore;
        public ButtonIcon(Icon icon, String tooltipLabel, ComponentBefore componentBefore) {
            super();
            this.icon = icon;
            this.tooltip = tooltipLabel;
            this.componentBefore = componentBefore;
        }

    }

	static public Vector<Object> translate(final String[] possibles) {
		final Vector<Object> displayedItems = new Vector<Object>(possibles.length);
		for (int i = 0; i < possibles.length; i++) {
			String alternativeKey = possibles[i];
			String alternativeText = TextUtils.getText(alternativeKey, null);
			String key = "OptionPanel." + alternativeKey;
			String text = alternativeText != null ? TextUtils.getText(key, alternativeText) : TextUtils.getText(key);
			displayedItems.add(text);
		}
		return displayedItems;
	}
	private static class SizeChanger extends ComponentAdapter {
	    static final SizeChanger INSTANCE = new SizeChanger();
	    @Override
	    public void componentResized(ComponentEvent e) {
	        Component component = e.getComponent();
	        Dimension preferredSize = component.getPreferredSize();
	        int width = component.getWidth();
	        int height = component.getHeight();
	        if(width != preferredSize.width || height != preferredSize.height) {
	            component.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
	            component.revalidate();
	        }
	    }
	}

	private final JPanel buttonPanel;
	private final JButton startButton;
	private final Vector<String> possibleValues;
	private final Vector<JToggleButton> buttons;
    private int selectedIndex = 0;

	public ButtonPanelProperty(final String name, final Collection<String> values,
	                     final Collection<ButtonIcon> displayedItems) {
		super(name);
		possibleValues = new Vector<String>();
        possibleValues.addAll(values);
		buttonPanel = new JPanel(ToolbarLayout.horizontal());
		buttonPanel.addComponentListener(SizeChanger.INSTANCE);
		buttons = new Vector<JToggleButton>(displayedItems.size());
		int i = 0;
		for(ButtonIcon item : displayedItems) {
		    JToggleButton button = new JToggleButton(item.icon);
		    button.setToolTipText(item.tooltip);
		    buttons.add(button);
		    int buttonIndex = i++;
		    button.addActionListener(event -> {
		        setSelected(button);
		        selectedIndex = buttonIndex;
		        firePropertyChangeEvent();

		    });
		    if(item.componentBefore == ComponentBefore.SEPARATOR)
		        buttonPanel.add(new JSeparator());
		    buttonPanel.add(button);
		}
        startButton = new JButton();
        startButton.addActionListener(this::showButtonPanel);
	}

    private void setSelected(JToggleButton button) {
        buttons.forEach(b -> b.setSelected(b == button));
    }

	@Override
	public String getValue() {
		return possibleValues.get(selectedIndex);
	}

	@Override
	public JComponent getValueComponent() {
		return startButton;
	}

	@Override
    public void appendToForm(final DefaultFormBuilder builder) {
	    appendToForm(builder, startButton);
	}

	public Vector<String> getPossibleValues() {
		return possibleValues;
	}

	@Override
    public void setEnabled(final boolean pEnabled) {
	    buttons.forEach(b -> b.setEnabled(pEnabled));
		super.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		if (possibleValues.contains(value)) {
			selectedIndex = possibleValues.indexOf(value);
			setSelected(buttons.elementAt(selectedIndex));
		}
		else{
			LogUtils.severe("Can't set the value:" + value + " into the combo box " + getName() + " containing values " + possibleValues);
			if (possibleValues.size() > 0) {
			    selectedIndex = 0;
			}
		}
		JToggleButton selectedButton = buttons.get(selectedIndex);
		startButton.setIcon(selectedButton.getIcon());
		startButton.setToolTipText(selectedButton.getToolTipText());
	}

	@Override
    public void actionPerformed(final ActionEvent e) {
		firePropertyChangeEvent();
	}

    private void showButtonPanel(ActionEvent e) {
        Window owner = SwingUtilities.getWindowAncestor(startButton);
        final JDialog d = new JDialog(owner, ModalityType.MODELESS);
        d.getRootPane().applyComponentOrientation(owner.getComponentOrientation());
        d.getContentPane().add(buttonPanel);
        PopupDialog.closeWhenOwnerIsFocused(d);
        PopupDialog.closeOnEscape(d);
        Point eventLocation = new Point(0, startButton.getHeight());
        SwingUtilities.convertPointToScreen(eventLocation, startButton);
        d.pack();
        UITools.setBounds(d, eventLocation.x, eventLocation.y,
                d.getWidth(), d.getHeight());

        JToggleButton selectedButton = buttons.get(selectedIndex);
        selectedButton.requestFocusInWindow();
        d.setVisible(true);

    }
}
