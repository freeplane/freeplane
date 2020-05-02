package org.freeplane.features.fpsearch;

import org.freeplane.features.filter.PseudoDamerauLevenshtein;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedList;

// TODO: index menu: https://www.logicbig.com/tutorials/java-swing/menu-search-highlighting.html
// TODO: Controller.getCurrentController().getViewController().getFreeplaneMenuBar()
// TODO: optionally process localized prefs/menus!
public class FPSearchDialog extends JDialog implements DocumentListener {

    private final int numberOfTextFieldColumns = 100;
    private JLabel searchLabel;
    private JTextField input;
    private JList resultList;
    private PreferencesIndexer preferencesIndexer;

    FPSearchDialog(Frame parent)
    {
        super(parent,"Freeplane internal search",true);

        searchLabel = new JLabel("Search for:");
        input = new JTextField("hello", numberOfTextFieldColumns);
        input.setEditable(true);
        input.setEnabled(true);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 2));
        inputPanel.add(searchLabel);
        inputPanel.add(input);

        resultList = new JList();

        getContentPane().setLayout(new GridLayout(2, 1));
        getContentPane().add(inputPanel);
        getContentPane().add(resultList);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        pack();

        input.getDocument().addDocumentListener(this);

        preferencesIndexer = new PreferencesIndexer();

        setVisible(true);
    }

    public void changedUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }
    public void removeUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }
    public void insertUpdate(DocumentEvent e) {
        updateMatches(input.getText());
    }

    public void updateMatches(final String searchTerm)
    {
        //System.out.format("new text input: '%s'\n", searchTerm);

        PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();

        java.util.List<String> matches = new LinkedList<>();
        for (final PreferencesItem prefsItem: preferencesIndexer.getPrefs())
        {
            //pairwiseAlignment.init(searchTerm, prefsItem.key, true);
            if (pairwiseAlignment.matches(searchTerm, prefsItem.key, true) ||
                pairwiseAlignment.matches(searchTerm, prefsItem.text, true))
            //if (prefsItem.startsWith(searchString))
            //if (pairwiseAlignment.matchProb() > 0.667)
            {
                matches.add(prefsItem.toString());
            }
        }
        resultList.setListData(matches.toArray());
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame parent = new JFrame("FP Search Dialog Test");
                parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                parent.setPreferredSize(new Dimension(800, 600));
                parent.pack();
                parent.setVisible(true);

                FPSearchDialog fpSearchDialog = new FPSearchDialog(parent);
            }
        });
    }
}
