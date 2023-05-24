/*
 * Created on 28 Jan 2023
 *
 * author dimitry
 */
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.PopupDialog;
import org.freeplane.core.ui.components.ToolbarLayout;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.styles.mindmapmode.styleeditorpanel.IconFont;

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
        JToggleButton prototypeButton = null;
        for(ButtonSelectorPanel.ButtonIcon item : displayedItems) {
            JToggleButton button = prototypeButton = new JToggleButton(item.icon);
            button.setName(item.tooltip);
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
        addRevertButton(prototypeButton);
    }

    private final static String RESET_RESOURCE = "reset_property_text";
    private static final String TEXT = TextUtils.getText(RESET_RESOURCE);
    private void addRevertButton(AbstractButton sizePrototype) {
        JButton button = new JButton();
        button.setPreferredSize(sizePrototype.getPreferredSize());
        button.setFont(IconFont.FONT.deriveFont(0.8f * sizePrototype.getIcon().getIconHeight()));
        JButton revertButton = button;
        revertButton.setText(IconFont.REVERT_CHARACTER);
        revertButton.setToolTipText(TEXT);
        revertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedIndex = -1;
                if(callback != null)
                    callback.run();
           }
        });
        buttonPanel.add(revertButton);
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
        return selectedIndex >= 0 ? possibleValues.get(selectedIndex) : null;
    }
    public Vector<String> getPossibleValues() {
        return possibleValues;
    }

    public void setEnabled(final boolean pEnabled) {
        buttons.forEach(b -> b.setEnabled(pEnabled));
    }

    public void setValue(final String value) {
        int index = indexOf(value);
        selectedIndex = index;
        if (selectedIndex >= 0) {
            setSelected(buttons.elementAt(selectedIndex));
        }
    }

    public JToggleButton getSelectedButton() {
        return buttons.get(selectedIndex);
    }

    public JToggleButton getButton(String value) {
        int index = indexOf(value);
        return index >= 0 ? buttons.elementAt(index) : null;
    }

    public int indexOf(String value) {
        if (possibleValues.contains(value)) {
            int index = possibleValues.indexOf(value);
            return index;
        }
        else{
            LogUtils.severe("Can't find value:" + value + " in buttons containing values " + possibleValues);
            if (possibleValues.size() > 0) {
                return 0;
            }
            else
                return -1;
        }
    }


    public Component getButtonPanel() {
        return buttonPanel;
    }

    public void showButtonDialog(Component parentComponent, Runnable callback) {
        EventQueue.invokeLater(() -> showButtonDialogNow( parentComponent, callback));
    }

    private void showButtonDialogNow(Component parentComponent, Runnable callback) {
        Window owner = SwingUtilities.getWindowAncestor(parentComponent);
        final JDialog dialog = new JDialog(owner, ModalityType.MODELESS);
        dialog.setResizable(false);
        dialog.setUndecorated(true);
        dialog.getRootPane().applyComponentOrientation(owner.getComponentOrientation());
        dialog.getContentPane().add(getButtonPanel());
        PopupDialog.closeWhenOwnerIsFocused(dialog);
        PopupDialog.closeOnEscape(dialog);
        Point eventLocation = new Point(0, parentComponent.getHeight());
        SwingUtilities.convertPointToScreen(eventLocation, parentComponent);
        dialog.pack();
        UITools.setBounds(dialog, eventLocation.x, eventLocation.y,
                dialog.getWidth(), dialog.getHeight());

        JToggleButton selectedButton = getSelectedButton();
        selectedButton.requestFocusInWindow();
        setCallback(() -> {
            dialog.dispose();
            callback.run();
        });
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                setCallback(null);
            }

        });
        dialog.setVisible(true);
    }
}