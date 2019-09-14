/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.plugin.script;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import jsyntaxpane.actions.ActionUtils;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.BlindIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.script.IFreeplaneScriptErrorHandler;

/**
 */
class ScriptEditorPanel extends JDialog {

	static final String GROOVY_EDITOR_FONT = "groovy_editor_font";
	static final String GROOVY_EDITOR_FONT_SIZE = "groovy_editor_font_size";

	private static final String internalCharset = "UTF-16BE";

	final private class CancelAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private CancelAction(final String pArg0) {
			super(pArg0);
		}

		public void actionPerformed(final ActionEvent arg0) {
			disposeDialog(true);
		}
	}

	final private class ExitAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ExitAction(final String pArg0) {
			super(pArg0);
		}

		public void actionPerformed(final ActionEvent arg0) {
			storeCurrent();
			disposeDialog(false);
		}
	}

	public interface IScriptModel {
		/**
		 * @return the index of the new script.
		 */
		int addNewScript();

		ScriptEditorWindowConfigurationStorage decorateDialog(ScriptEditorPanel pPanel,
		                                                      String pWindow_preference_storage_property);

		void endDialog(boolean pIsCanceled);

		Object executeScript(int pIndex, PrintStream outStream, IFreeplaneScriptErrorHandler pErrorHandler);

		int getAmountOfScripts();

		/**
		 * @param pIndex
		 *            zero-based
		 * @return a script
		 */
		ScriptHolder getScript(int pIndex);

		boolean isDirty();

		void setScript(int pIndex, ScriptHolder pScript);

		void storeDialogPositions(ScriptEditorPanel pPanel, ScriptEditorWindowConfigurationStorage pStorage,
		                          String pWindow_preference_storage_property);
	}

	final private class NewScriptAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private NewScriptAction(final String pArg0) {
			super(pArg0);
		}

		public void actionPerformed(final ActionEvent arg0) {
			storeCurrent();
			mLastSelected = null;
			final int scriptIndex = mScriptModel.addNewScript();
			updateFields();
			select(scriptIndex);
		}
	}

	final private class ResultFieldStream extends OutputStream {
		private final byte[] buf = new byte[2];
		private int i = 0;

		@Override
		public void write(final int pByte) throws IOException {
			buf[i++] = (byte) pByte;
			if (i == 2) {
				mScriptResultField.append(new String(buf, internalCharset));
				i = 0;
			}
		}

		@Override
		public void write(final byte b[], int off, int len) throws IOException {
			if (i == 1) {
				write(b[off++]);
				len--;
			}
			if (len <= 0) {
				return;
			}
			final int len2 = len & ~1;
			mScriptResultField.append(new String(b, off, len2, internalCharset));
			if (len2 != len) {
				write(b[len2]);
			}
		}
	}

	final private class RunAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private RunAction(final String pArg0) {
			super(pArg0);
		}

		public void actionPerformed(final ActionEvent arg0) {
			storeCurrent();
			if (!mScriptList.isSelectionEmpty()) {
				mScriptResultField.setText("");
				Object result = null;
				try {
					result = mScriptModel.executeScript(mScriptList.getSelectedIndex(), getPrintStream(),
						getErrorHandler());
                }
                catch (Throwable e2) {
				// make sure the complete stack trace is logged!
				LogUtils.warn(e2);
        			Throwable cause = e2.getCause();
					String causeMessage = "";
					if(cause != null && cause.getMessage()!= null)
						causeMessage = cause.getMessage();
        			final String message = e2.getMessage() != null ? e2.getMessage() : "";
        			UITools.errorMessage(e2.getClass().getName() + ": " + causeMessage
        			        + ((causeMessage.length() != 0 && message.length() != 0) ? ", " : "") + message);
        			result = message;
                }
				getPrintStream().print(TextUtils.getText("plugins/ScriptEditor/window.Result") + result);
			}
		}
	}

	public static class ScriptHolder {
		String mScript;
		String mScriptName;

		/**
		 * @param pScriptName
		 *            script name (starting with "script"
		 *            (ScriptingEngine.SCRIPT_PREFIX))
		 * @param pScript
		 *            script content
		 */
		public ScriptHolder(final String pScriptName, final String pScript) {
			super();
			mScript = pScript;
			mScriptName = pScriptName;
		}

		public String getScript() {
			return mScript;
		}

		public String getScriptName() {
			return mScriptName;
		}

		public ScriptHolder setScript(final String pScript) {
			mScript = pScript;
			return this;
		}

		public ScriptHolder setScriptName(final String pScriptName) {
			mScriptName = pScriptName;
			return this;
		}
	}

	final private class SignAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
// // 		final private Controller controller;

		private SignAction( final String pArg0) {
			super(pArg0);
//			this.controller = controller;
		}

		public void actionPerformed(final ActionEvent arg0) {
			storeCurrent();
			if (!mScriptList.isSelectionEmpty()) {
				final int selectedIndex = mScriptList.getSelectedIndex();
				final ScriptHolder script = mScriptModel.getScript(selectedIndex);
				final String signedScript = new SignedScriptHandler().signScript(script.mScript);
				script.setScript(signedScript);
				mScriptModel.setScript(selectedIndex, script);
				mScriptTextField.setText(signedScript);
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	private static final String WINDOW_PREFERENCE_STORAGE_PROPERTY = "plugins.script.ScriptEditorPanel/window_positions";
	final private JSplitPane mCentralPanel;
	final private JSplitPane mCentralUpperPanel;
	private Integer mLastSelected = null;
	final private DefaultListModel mListModel;
	final private AbstractAction mRunAction;
	final private JList mScriptList;
	final private IScriptModel mScriptModel;
	final private JTextArea mScriptResultField;
	final private JTextComponent mScriptTextField;
	final private SignAction mSignAction;
	final private JLabel mStatus;

	public ScriptEditorPanel( final IScriptModel pScriptModel,
	                         final boolean pHasNewScriptFunctionality) {
		super(UITools.getCurrentFrame(), true /* modal */);
		mScriptModel = pScriptModel;
		this.setTitle(TextUtils.getText("plugins/ScriptEditor/window.title"));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				disposeDialog(true);
			}
		});
		UITools.addEscapeActionToDialog(this, new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				disposeDialog(true);
			}
		});
		final Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		mListModel = new DefaultListModel();
		mScriptList = new JList(mListModel);
		mScriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mScriptList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent pEvent) {
				if (pEvent.getValueIsAdjusting()) {
					return;
				}
				select(mScriptList.getSelectedIndex());
			}
		});
		final JEditorPane editorPane = new JEditorPane();
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		editorPane.setBackground(Color.WHITE);
		editorPane.setForeground(Color.BLACK);
		editorPane.setSelectedTextColor(Color.BLUE);
		mScriptTextField = editorPane;
		mScriptTextField.setEnabled(false);
		mCentralUpperPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mScriptList, new JScrollPane(mScriptTextField));
		try {
			editorPane.setContentType("text/groovy");

			final String fontName = ResourceController.getResourceController().getProperty(GROOVY_EDITOR_FONT);
			final int fontSize = ResourceController.getResourceController().getIntProperty(GROOVY_EDITOR_FONT_SIZE);
			final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
			editorPane.setFont(font);

		} catch (Exception e) {
			LogUtils.warn(e);
			editorPane.setContentType("text/plain");
		}
		mCentralUpperPanel.setContinuousLayout(true);
		mScriptResultField = new JTextArea();
		mScriptResultField.setEditable(false);
		mScriptResultField.setWrapStyleWord(true);
		mCentralPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mCentralUpperPanel, new JScrollPane(
		    mScriptResultField));
		mCentralPanel.setDividerLocation(0.8);
		mCentralPanel.setContinuousLayout(true);
		contentPane.add(mCentralPanel, BorderLayout.CENTER);
		mStatus = new JLabel();
		contentPane.add(mStatus, BorderLayout.SOUTH);
		mScriptTextField.addCaretListener(new CaretListener() {
			public void caretUpdate(final CaretEvent arg0) {
				final int caretPosition = mScriptTextField.getCaretPosition();
				try {
	                final int lineOfOffset = ActionUtils.getLineNumber(mScriptTextField, caretPosition);
	                mStatus.setText("Line: " + (lineOfOffset + 1) + ", Column: "
	                	+ (caretPosition - ActionUtils.getLineNumber(mScriptTextField, lineOfOffset) + 1));
                }
                catch (Exception e) {
	                e.printStackTrace();
                }
			}
		});
		updateFields();
		mScriptTextField.repaint();
		final JMenuBar menuBar = new JMenuBar();
		final JMenu menu = new JMenu();
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, TextUtils.getRawText("plugins/ScriptEditor.menu_actions"));
		if (pHasNewScriptFunctionality) {
			addAction(menu, new NewScriptAction(TextUtils.getRawText("plugins/ScriptEditor.new_script")));
		}
		mRunAction = new RunAction(TextUtils.getRawText("plugins/ScriptEditor.run"));
		mRunAction.setEnabled(false);
		addAction(menu, mRunAction);
		mSignAction = new SignAction(TextUtils.getRawText("plugins/ScriptEditor.sign"));
		mSignAction.setEnabled(false);
		addAction(menu, mSignAction);
		final AbstractAction cancelAction = new CancelAction(TextUtils.getRawText("plugins/ScriptEditor.cancel"));
		addAction(menu, cancelAction);
		final AbstractAction exitAction = new ExitAction(TextUtils.getRawText("plugins/ScriptEditor.exit"));
		addAction(menu, exitAction);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
		final ScriptEditorWindowConfigurationStorage storage = mScriptModel.decorateDialog(this,
		    ScriptEditorPanel.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		if (storage != null) {
			mCentralUpperPanel.setDividerLocation(storage.getLeftRatio());
			mCentralPanel.setDividerLocation(storage.getTopRatio());
		}
		else {
			mCentralUpperPanel.setDividerLocation(100);
			mCentralPanel.setDividerLocation(240);
		}
	}

	private void addAction(final JMenu menu, final AbstractAction action) {
		final JMenuItem item = menu.add(action);
		LabelAndMnemonicSetter.setLabelAndMnemonic(item, (String) action.getValue(Action.NAME));
		item.setIcon(new BlindIcon(UIBuilder.ICON_SIZE));
	}

	/**
	 * @param pIsCanceled
	 */
	private void disposeDialog(final boolean pIsCanceled) {
		if (!mScriptList.isSelectionEmpty()) {
			select(mScriptList.getSelectedIndex());
		}
		if (pIsCanceled && mScriptModel.isDirty()) {
			final int action = JOptionPane.showConfirmDialog(this, TextUtils
			    .getText("ScriptEditorPanel.changed_cancel"), "Freeplane", JOptionPane.OK_CANCEL_OPTION);
			if (action == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		final ScriptEditorWindowConfigurationStorage storage = new ScriptEditorWindowConfigurationStorage();
		storage.setLeftRatio(mCentralUpperPanel.getDividerLocation());
		storage.setTopRatio(mCentralPanel.getDividerLocation());
		mScriptModel.storeDialogPositions(this, storage, ScriptEditorPanel.WINDOW_PREFERENCE_STORAGE_PROPERTY);
		this.setVisible(false);
		this.dispose();
		mScriptModel.endDialog(pIsCanceled);
	}

	IFreeplaneScriptErrorHandler getErrorHandler() {
		return new IFreeplaneScriptErrorHandler() {
			public void gotoLine(final int pLineNumber) {
				ActionUtils.setCaretPosition(mScriptTextField, pLineNumber, 1);
			}
		};
	}

	PrintStream getPrintStream() {
		try {
			return new PrintStream(new ResultFieldStream(), false, internalCharset);
		}
		catch (final UnsupportedEncodingException e) {
			return null;
		}
	}

	private void select(final int pIndex) {
		mScriptTextField.setEnabled(pIndex >= 0);
		mRunAction.setEnabled(pIndex >= 0);
		mSignAction.setEnabled(pIndex >= 0);
		if (pIndex < 0) {
			mScriptTextField.setText("");
			return;
		}
		storeCurrent();
		mScriptTextField.setText(mScriptModel.getScript(pIndex).getScript());
		mLastSelected = new Integer(pIndex);
		if (pIndex >= 0 && mScriptList.getSelectedIndex() != pIndex) {
			mScriptList.setSelectedIndex(pIndex);
		}
	}

	private void storeCurrent() {
		if (mLastSelected != null) {
			final int oldIndex = mLastSelected.intValue();
			mScriptModel.setScript(oldIndex, mScriptModel.getScript(oldIndex).setScript(mScriptTextField.getText()));
		}
	}

	private void updateFields() {
		mListModel.clear();
		for (int i = 0; i < mScriptModel.getAmountOfScripts(); ++i) {
			final ScriptHolder script = mScriptModel.getScript(i);
			mListModel.addElement(script.getScriptName());
		}
	}
}
