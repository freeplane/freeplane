/*
 * Created on 5 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
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
    private final Supplier<Stream<V>> itemSupplier;
    private boolean filterIsRunning;
    private Predicate<String> acceptAll;
    private BiPredicate<V, String> acceptItem;
    private BiPredicate<V, String> selectItem;


    public JFilterableComboBox(Supplier<Stream<V>> itemSupplier,
            Predicate<String> acceptAll,
            BiPredicate<V, String> acceptItem,
            BiPredicate<V, String> selectItem) {
        super();
        this.itemSupplier = itemSupplier;
        this.acceptAll = acceptAll;
        this.acceptItem = acceptItem;
        this.selectItem = selectItem;
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
                    EventQueue.invokeLater(this::showPopup);
            }

            private void showPopup() {
                if(isShowing())
                    JFilterableComboBox.this.showPopup();
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
            final DefaultComboBoxModel<V> model = getModel();
            model.removeAllElements();
            JTextField textField = (JTextField) getEditor().getEditorComponent();
            final String text = textField.getText();
            final Stream<V> tagStream = itemSupplier.get();
            final Stream<V> addedItems;
            addedItems = init || acceptAll.test(text) ? tagStream
                    : tagStream.filter(item -> acceptItem.test(item, text));
            AtomicInteger index = new AtomicInteger(-1);
            addedItems.forEach(item -> {
                model.addElement(item);
                if (index.get() == -1 && selectItem.test(item, text)) {
                    index.set(model.getSize() - 1);
                }
            });
            int firstMatchingIndex = index.get();
            if(firstMatchingIndex >= 0)
                setSelectedIndex(firstMatchingIndex);

        } finally {
          filterIsRunning = false;
        }
    }

    @Override
    public DefaultComboBoxModel<V> getModel() {
         return (DefaultComboBoxModel<V>) super.getModel();
    }



}