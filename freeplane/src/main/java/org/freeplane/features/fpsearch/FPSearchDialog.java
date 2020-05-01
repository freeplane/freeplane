package org.freeplane.features.fpsearch;

import org.freeplane.features.filter.PseudoDamerauLevenshtein;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

// TODO: index menu: https://www.logicbig.com/tutorials/java-swing/menu-search-highlighting.html
// TODO: Controller.getCurrentController().getViewController().getFreeplaneMenuBar()
public class FPSearchDialog extends JDialog implements DocumentListener {

    private final int numberOfTextFieldColumns = 100;
    private JLabel searchLabel;
    private JTextField input;
    private JList resultList;
    private PreferencesIndexer preferencesIndexer = new PreferencesIndexer();

    private java.util.List<String> listOfPrefs;

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

        listOfPrefs = new ArrayList<>();
        listOfPrefs.add("Foo");
        listOfPrefs.add("TextAlignmentVertical");
        listOfPrefs.add("TextAlignmentHorizonal");
        listOfPrefs.add("bar");
        listOfPrefs.add("ApproxSearch");

        input.getDocument().addDocumentListener(this);

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
        System.out.format("new text input: '%s'\n", searchTerm);

        PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();

        java.util.List<String> matches = new LinkedList<>();
        for (final String prefsItem: listOfPrefs)
        {
            pairwiseAlignment.init(searchTerm, prefsItem, true, false);
            //if (pairwiseAlignment.matches(searchTerm, prefsItem, true, false))
            //if (prefsItem.startsWith(searchString))
            if (pairwiseAlignment.matchProb() > 0.667)
            {
                matches.add(prefsItem);
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
