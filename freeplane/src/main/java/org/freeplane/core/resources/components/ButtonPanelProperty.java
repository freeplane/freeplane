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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormSpecs;

public class ButtonPanelProperty extends PropertyBean implements IPropertyControl, ActionListener {
    
    static public class ButtonIcon{
        public final Icon icon;
        public final String tooltip;
        public ButtonIcon(Icon icon, String tooltipLabel) {
            super();
            this.icon = icon;
            this.tooltip = tooltipLabel;
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
	private final Vector<String> possibleValues;
	private final Vector<JToggleButton> buttons;
    private int selectedIndex = 0;

	public ButtonPanelProperty(final String name, final Collection<String> possibles,
	                     final Collection<ButtonIcon> displayedItems) {
		super(name);
		possibleValues = new Vector<String>();
        possibleValues.addAll(possibles);
		buttonPanel = new JPanel(ToolbarLayout.vertical());
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
		    buttonPanel.add(button);
		}
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
		return buttonPanel;
	}

	@Override
    public void appendToForm(final DefaultFormBuilder builder) {
        int iconWidth = buttons.get(0).getIcon().getIconWidth();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, iconWidth, 0, 0));
 	    builder.append(buttonPanel, 3);
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
	}

	@Override
    public void actionPerformed(final ActionEvent e) {
		firePropertyChangeEvent();
	}

}
