/*
 * Created on 27 Jan 2023
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

public class PopupDialog {

    public static JDialog createOptionPanelPopupDialog(Component popup) {
        final Component optionComponent;
        if(popup instanceof JScrollPane)
            optionComponent = popup;
        else {
            final JAutoScrollBarPane scrollPane = new JAutoScrollBarPane(popup);
            UITools.setScrollbarIncrement(scrollPane);
            scrollPane.setBorder( BorderFactory.createEmptyBorder() );
            optionComponent = scrollPane;
        }
        JOptionPane pane = new JOptionPane(optionComponent);
        Component menuComponent = UITools.getMenuComponent();
        final JDialog d = pane.createDialog(menuComponent, popup.getName());
        d.getRootPane().applyComponentOrientation(menuComponent.getComponentOrientation());
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setModal(false);
        d.pack();
        closeWhenOwnerIsFocused(d);
        return d;
    }

    public static void closeWhenOwnerIsFocused(final JDialog d) {
        final Window frame = d.getOwner();
        d.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent e) {
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                frame.addWindowFocusListener(new WindowFocusListener() {
                    @Override
                    public void windowLostFocus(WindowEvent e) {
                    }

                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        d.setVisible(false);
                        frame.removeWindowFocusListener(this);
                    }
                });
                d.removeWindowFocusListener(this);
            }
        });
    }

    public static void closeOnEscape(JDialog dialog) {
        InputMap in = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "escape");
        ActionMap aMap = dialog.getRootPane().getActionMap();
        aMap.put("escape", new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                dialog.dispose();
            }
        });
    }
}