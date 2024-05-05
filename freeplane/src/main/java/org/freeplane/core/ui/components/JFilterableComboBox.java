/*
 * Created on 5 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class JFilterableComboBox<V> extends JComboBox<V> {
    private static final long serialVersionUID = 1L;
    private final Supplier<Collection<V>> itemSupplier;
    private boolean filterIsRunning;
    private BiPredicate<Collection<V>, String> acceptAll;
    private BiPredicate<V, String> acceptItem;


    public JFilterableComboBox(Supplier<Collection<V>> itemSupplier,
            BiPredicate<Collection<V>, String> acceptAll,
            BiPredicate<V, String> acceptItem) {
        super();
        this.itemSupplier = itemSupplier;
        this.acceptAll = acceptAll;
        this.acceptItem = acceptItem;
        updateListItems(true);
        Timer timer = new Timer(200, x -> updateListItems(false));
        timer.setRepeats(false);
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                if(! filterIsRunning)
                    timer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if(! filterIsRunning)
                    timer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if(! filterIsRunning)
                    timer.restart();
            }
        };
        addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                updateListItems(false);
                JTextField textField = (JTextField) getEditor().getEditorComponent();
                textField.getDocument().addDocumentListener(documentListener);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                JTextField textField = (JTextField) getEditor().getEditorComponent();
                textField.getDocument().removeDocumentListener(documentListener);

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(! isPopupVisible())
                    showPopup();
            }

        });
    }

    @Override
    public void configureEditor(ComboBoxEditor anEditor, Object anItem) {
        if(! filterIsRunning)
            super.configureEditor(anEditor, anItem);
    }



    public boolean isFilterRunning() {
        return filterIsRunning;
    }

    private void updateListItems(boolean init) {
        if(filterIsRunning)
            return;
        filterIsRunning = true;
        try {
            final DefaultComboBoxModel<V> model = (DefaultComboBoxModel<V>) getModel();
            model.removeAllElements();
            JTextField textField = (JTextField) getEditor().getEditorComponent();
            final String text = textField.getText();
            Collection<V> items = itemSupplier.get();
            final Stream<V> tagStream = items.stream();
            if(init || acceptAll.test(items, text)) {
                tagStream.forEach(model::addElement);
            } else
                tagStream
                .filter(item -> acceptItem.test(item, text))
                .forEach(model::addElement);
        } finally {
          filterIsRunning = false;
        }
    }

}