package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;

public class NodeListWithReplacement extends NodeList{
	class HolderAccessor{
		HolderAccessor() {
	        super();
        }

		public void changeString(final TextHolder textHolder, final String newText) {
			textHolder.setText(newText);
		}

		public int getLength() {
			return mFlatNodeTableFilterModel.getRowCount();
		}

		public TextHolder[] getNodeHoldersAt(final int row) {
			return new TextHolder[]{
					(TextHolder) sorter.getValueAt(row, nodeTextColumn),
					(TextHolder) sorter.getValueAt(row, nodeDetailsColumn),
					(TextHolder) sorter.getValueAt(row, nodeNotesColumn)
			};
		}
	}

	private static final String REMINDER_TEXT_REPLACE = "plugins/TimeManagement.xml_Replace";
	final private JComboBox mFilterTextReplaceField;
	private final JCheckBox useRegexInReplace;

	public NodeListWithReplacement(String windowTitle, NodeFilter nodeFilter, boolean searchInAllMaps,
								   String windowPreferenceStorageProperty) {
		super(windowTitle, nodeFilter, searchInAllMaps, windowPreferenceStorageProperty);
		mFilterTextSearchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent pEvent) {
				if (pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					mFilterTextReplaceField.requestFocusInWindow();
				}
			}
		});
		mFilterTextReplaceField = new JComboBoxWithBorder();
		mFilterTextReplaceField.setEditable(true);
		mFilterTextReplaceField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent pEvent) {
				if (pEvent.getKeyCode() == KeyEvent.VK_DOWN) {
					tableView.requestFocusInWindow();
				}
				else if (pEvent.getKeyCode() == KeyEvent.VK_UP) {
					mFilterTextSearchField.requestFocusInWindow();
				}
			}
		});
		useRegexInReplace = new JCheckBox();
	}
	private static String replace(final Pattern p, String input, final String replacement) {
		final String result = HtmlUtils.getReplaceResult(p, input, replacement);
		return result;
	}
	private void replace(final HolderAccessor holderAccessor, boolean selectedOnly) {
		final String searchString = (String) mFilterTextSearchField.getSelectedItem();
		if(searchString == null)
			return;
		final String replaceString = (String) mFilterTextReplaceField.getSelectedItem();
		Pattern p;
		try {
			p = Pattern.compile(useRegexInFind.isSelected() ? searchString : Pattern.quote(searchString),
					matchCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
		}
		catch (final PatternSyntaxException e) {
			UITools.errorMessage(TextUtils.format("wrong_regexp", searchString, e.getMessage()));
			return;
		}
		final String replacement = replaceString == null ? "" : replaceString;
		final int length = holderAccessor.getLength();
		for (int i = 0; i < length; i++) {
			if( !selectedOnly || tableView.isRowSelected(i)){
				TextHolder[] textHolders = holderAccessor.getNodeHoldersAt(i);
				for(final TextHolder textHolder:textHolders){
					final String text = textHolder.getText();
					final String replaceResult;
					final String literalReplacement = useRegexInReplace.isSelected() ? replacement : Matcher.quoteReplacement(replacement);
					try {
						if (HtmlUtils.isHtmlNode(text)) {
							replaceResult = replace(p, text,literalReplacement);
						}
						else {
							replaceResult = p.matcher(text).replaceAll(literalReplacement);
						}
					}
					catch (Exception e) {
						final String message = e.getMessage();
						UITools.errorMessage(TextUtils.format("wrong_regexp", replacement, message != null ? message : e.getClass().getSimpleName()));
						return;
					}
					if (!StringUtils.equals(text, replaceResult)) {
						holderAccessor.changeString(textHolder, replaceResult);
					}
				}
			}
		}
		mFlatNodeTableFilterModel.resetFilter();
		mFilterTextSearchField.insertItemAt(mFilterTextSearchField.getSelectedItem(), 0);
		mFilterTextReplaceField.insertItemAt(mFilterTextReplaceField.getSelectedItem(), 0);
		mFilterTextSearchField.setSelectedItem("");
	}

	@Override
	protected void createSpecificUI(final Container contentPane, final GridBagConstraints layoutConstraints) {
		layoutConstraints.gridy++;
		layoutConstraints.weightx = 0.0;
		layoutConstraints.gridwidth = 1;
		contentPane.add(new JLabel(TextUtils.getText(REMINDER_TEXT_REPLACE)), layoutConstraints);
		layoutConstraints.gridx = 5;
		contentPane.add(new JLabel(TextUtils.getText("regular_expressions")), layoutConstraints);
		layoutConstraints.gridx++;
		contentPane.add(useRegexInReplace, layoutConstraints);
		layoutConstraints.gridx = 0;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
		layoutConstraints.gridy++;
		contentPane.add(/* new JScrollPane */(mFilterTextReplaceField), layoutConstraints);
	}
	@Override
	protected void createSpecificButtons(final Container container) {
		final AbstractAction replaceAllAction = new AbstractAction(TextUtils
		    .getText("plugins/TimeManagement.xml_Replace_All")) {
			/**
			     *
			     */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				replace(new HolderAccessor(), false);
			}
		};
		final JButton replaceAllButton = new JButton(replaceAllAction);
		final AbstractAction replaceSelectedAction = new AbstractAction(TextUtils
		    .getText("plugins/TimeManagement.xml_Replace_Selected")) {
			/**
			     *
			     */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				replace(new HolderAccessor(), true);
			}
		};
		final JButton replaceSelectedButton = new JButton(replaceSelectedAction);
		replaceSelectedAction.setEnabled(false);
		container.add(replaceAllButton);
		container.add(replaceSelectedButton);
		final ListSelectionModel rowSM1 = tableView.getSelectionModel();
		rowSM1.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				final boolean enable = !(lsm.isSelectionEmpty());
				replaceSelectedAction.setEnabled(enable);
			}
		});
	}
}
