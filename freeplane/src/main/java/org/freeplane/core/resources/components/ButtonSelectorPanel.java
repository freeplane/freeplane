/*
 * Created on 28 Jan 2023
 *
 * author dimitry
 */
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class ButtonSelectorPanel{
    public enum ComponentBefore {
        NOTHING, SEPARATOR
    }

    static public class ButtonIcon{
        public final Icon icon;
        public final String tooltip;
        public final ButtonSelectorPanel.ComponentBefore componentBefore;
        public ButtonIcon(Icon icon, String tooltipLabel, ButtonSelectorPanel.ComponentBefore componentBefore) {
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
        static final ButtonSelectorPanel.SizeChanger INSTANCE = new SizeChanger();
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
    private static final Action CLICK = new AbstractAction() {
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ((JToggleButton)(e.getSource())).doClick();
         }

    };

    private final JPanel buttonPanel;
    private final Vector<String> possibleValues;
    private final Vector<JToggleButton> buttons;
    private int selectedIndex = 0;
    private Runnable callback;
    public ButtonSelectorPanel(final Collection<String> values,
            final Collection<ButtonSelectorPanel.ButtonIcon> displayedItems) {
        possibleValues = new Vector<String>();
        possibleValues.addAll(values);
        buttonPanel = new JPanel(ToolbarLayout.horizontal());
        buttonPanel.addComponentListener(SizeChanger.INSTANCE);
        buttons = new Vector<JToggleButton>(displayedItems.size());
        int i = 0;
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke("ENTER");
        KeyStroke spaceKeyStroke = KeyStroke.getKeyStroke("SPACE");
        for(ButtonSelectorPanel.ButtonIcon item : displayedItems) {
            JToggleButton button = new JToggleButton(item.icon);
            button.setToolTipText(item.tooltip);
            buttons.add(button);
            int buttonIndex = i++;
            button.addActionListener(event -> {
                setSelected(button);
                selectedIndex = buttonIndex;
                if(callback != null)
                    callback.run();
            });
            button.getInputMap().put(enterKeyStroke, "ON_CLICK");
            button.getInputMap().put(spaceKeyStroke, "ON_CLICK");
            button.getActionMap().put("ON_CLICK", CLICK);
            if(item.componentBefore == ComponentBefore.SEPARATOR)
                buttonPanel.add(new JSeparator());
            buttonPanel.add(button);
        }
    }
    private void setSelected(JToggleButton button) {
        buttons.forEach(b -> b.setSelected(b == button));
    }

    public Runnable getCallback() {
        return callback;
    }
    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
    public String getValue() {
        return possibleValues.get(selectedIndex);
    }
    public Vector<String> getPossibleValues() {
        return possibleValues;
    }

    public void setEnabled(final boolean pEnabled) {
        buttons.forEach(b -> b.setEnabled(pEnabled));
    }

    public void setValue(final String value) {
        if (possibleValues.contains(value)) {
            selectedIndex = possibleValues.indexOf(value);
            setSelected(buttons.elementAt(selectedIndex));
        }
        else{
            LogUtils.severe("Can't set the value:" + value + " into buttons containing values " + possibleValues);
            if (possibleValues.size() > 0) {
                selectedIndex = 0;
            }
        }
    }
    JToggleButton getSelectedButton() {
        return buttons.get(selectedIndex);
    }
    public Component getButtonPanel() {
        return buttonPanel;
    }
}