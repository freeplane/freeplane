package org.freeplane.features.fpsearch;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.features.filter.PseudoDamerauLevenshtein;


// TODO: improve UI!
// TODO: _optionally_ process localized prefs/menus!
public class FPSearchDialog extends JDialog implements DocumentListener, ActionListener {

    private JRadioButton searchMenusOption;
    private JRadioButton searchPrefsOption;
    private final int numberOfTextFieldColumns = 100;
    private JLabel searchLabel;
    private JTextField input;
    private PreferencesItemsResultTable resultTable;
    private PreferencesIndexer preferencesIndexer;
    private MenuStructureIndexer menuStructureIndexer;

    FPSearchDialog(Frame parent)
    {
        super(parent,"Freeplane internal search",true);

        loadSources();
        resultTable = new PreferencesItemsResultTable();

        searchLabel = new JLabel("Search for:");
        input = new JTextField("", numberOfTextFieldColumns);
        input.setEditable(true);
        input.setEnabled(true);

        JPanel typeChoicePanel = new JPanel();
        searchMenusOption = new JRadioButton("Search Menus");
        searchMenusOption.addActionListener(this);
        searchPrefsOption = new JRadioButton("Search Preferences");
        searchPrefsOption.addActionListener(this);
        typeChoicePanel.add(searchMenusOption);
        typeChoicePanel.add(searchPrefsOption);
        ButtonGroup typeChoiceButtonGroup = new ButtonGroup();
        typeChoiceButtonGroup.add(searchMenusOption);
        typeChoiceButtonGroup.add(searchPrefsOption);
        searchPrefsOption.setSelected(true);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 2));
        inputPanel.add(searchLabel);
        inputPanel.add(input);

        getContentPane().setLayout(new GridLayout(3, 1));
        getContentPane().add(typeChoicePanel);
        getContentPane().add(inputPanel);
        JScrollPane resultTableScrollPane = new JScrollPane(resultTable);
        getContentPane().add(resultTableScrollPane);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        pack();

        input.getDocument().addDocumentListener(this);

        //showPrefsDialog();
        setVisible(true);
    }

    private void loadSources()
    {
        preferencesIndexer = new PreferencesIndexer();
        menuStructureIndexer = new MenuStructureIndexer();
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

    public void actionPerformed(ActionEvent e){
        updateMatches(input.getText());
    }

    private void updateMatches(final String searchTerm)
    {
        //System.out.format("new text input: '%s'\n", searchTerm);

        PseudoDamerauLevenshtein pairwiseAlignment = new PseudoDamerauLevenshtein();

        if (searchMenusOption.isSelected())
        {
            java.util.List<String> matches = new LinkedList<>();
            for (final String menuPath :menuStructureIndexer.getMenuItems())
            {
                if (pairwiseAlignment.matches(searchTerm, menuPath, true))
                {
                    matches.add(menuPath);
                }
            }
            //resultList.setListData(matches.toArray());
        }
        else if (searchPrefsOption.isSelected())
        {
            resultTable.clear();

            java.util.List<PreferencesItem> matches = new LinkedList<>();
            for (final PreferencesItem prefsItem: preferencesIndexer.getPrefs())
                    //Arrays.asList(new PreferencesItem("tab", "sep", "single_instance", "Single Instance Mode"),
                    //        new PreferencesItem("tab", "sep", "single_instance2", "Single Instance Second")))
            {
                //pairwiseAlignment.init(searchTerm, prefsItem.key, true);
                //if (pairwiseAlignment.matches(searchTerm, prefsItem.key, true) ||
                //        pairwiseAlignment.matches(searchTerm, prefsItem.text, true))
                //if (pairwiseAlignment.matchProb() > 0.667)
                if (prefsItem.key.contains(searchTerm) || prefsItem.text.contains(searchTerm))
                {
                    matches.add(prefsItem);
                    //System.out.format("adding %s to table\n", prefsItem);
                    resultTable.addPreferencesItem(prefsItem);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame parent = new JFrame("FP Search Dialog Test");
            parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            parent.setPreferredSize(new Dimension(800, 600));
            parent.pack();
            parent.setVisible(true);

            FPSearchDialog fpSearchDialog = new FPSearchDialog(parent);
        });
    }
}
