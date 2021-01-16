package org.freeplane.features.note.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController.NoteDocumentListener;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.text.mindmapmode.FreeplaneToSHTMLPropertyChangeAdapter;
import org.freeplane.features.text.mindmapmode.MTextController;

import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;

class NotePanel extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final SHTMLPanel htmlEditorPanel;
	private final Color defaultCaretColor;
	private final NoteDocumentListener noteDocumentListener;


	public NotePanel(NoteManager noteManager, NoteDocumentListener noteDocumentListener) {
		super(new BorderLayout());
		this.noteDocumentListener = noteDocumentListener;
		setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		SHTMLPanel htmlEditorPanel = createHtmlEditorComponent(noteManager);
		this.htmlEditorPanel = htmlEditorPanel;
		this.defaultCaretColor = htmlEditorPanel.getEditorPane().getCaretColor();

		ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "true");

	}

	private SHTMLPanel createHtmlEditorComponent(NoteManager noteManager) {
		SHTMLPanel htmlEditorPanel = MTextController.getController().createSHTMLPanel(NoteModel.EDITING_PURPOSE);
        htmlEditorPanel.shtmlPrefChanged("show_toolbars", 
                ResourceController.getResourceController().getProperty("simplyhtml.note.show_toolbars"), 
                ResourceController.getResourceController().getProperty("simplyhtml.show_toolbars"));
        htmlEditorPanel.shtmlPrefChanged("show_menu", 
                ResourceController.getResourceController().getProperty("simplyhtml.note.show_menu"), 
                ResourceController.getResourceController().getProperty("simplyhtml.show_menu"));

		// make sure that SHTML gets notified of relevant config changes!
        ResourceController.getResourceController().addPropertyChangeListener(
                new FreeplaneToSHTMLPropertyChangeAdapter("simplyhtml.", htmlEditorPanel));

        ResourceController.getResourceController().addPropertyChangeListener(
                new FreeplaneToSHTMLPropertyChangeAdapter("simplyhtml.note.", htmlEditorPanel));

		htmlEditorPanel.setMinimumSize(new Dimension(100, 100));
		final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();

		for (InputMap inputMap = editorPane.getInputMap(); inputMap != null; inputMap = inputMap.getParent()){
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl shift pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed SPACE"));
		}

		editorPane.addFocusListener(new FocusListener() {
			private SpellCheckerController spellCheckerController = null;
			private boolean enabled = false;
			@Override
			public void focusLost(final FocusEvent e) {
				if(! e.isTemporary()){
					spellCheckerController.enableAutoSpell(editorPane, false);
					enabled = false;
					noteManager.saveNote();
				}
			}

			@Override
			public void focusGained(final FocusEvent e) {
				if(! enabled){
					initSpellChecker();
					spellCheckerController.enableAutoSpell(editorPane, true);
					enabled = true;
				}
			}

			private void initSpellChecker() {
				if (spellCheckerController != null) {
					return;
				}
				spellCheckerController = SpellCheckerController.getController();
				spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
				spellCheckerController.enableShortKey(editorPane, true);
			}
		});

		htmlEditorPanel.getSourceEditorPane().addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if(! e.isTemporary()){
					noteManager.saveNote();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		add(htmlEditorPanel, BorderLayout.CENTER);
//		setDefaultFont();
		htmlEditorPanel.setOpenHyperlinkHandler(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent pE) {
				try {
					String uriText = pE.getActionCommand();
					LinkController.getController().loadURI(noteManager.getNode(), new URI(uriText));
				}
				catch (final Exception e) {
					LogUtils.severe(e);
				}
			}
		});
		return htmlEditorPanel;
	}

	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition,
	                                    final boolean pressed) {
		return super.processKeyBinding(ks, e, condition, pressed) || e.getKeyChar() == KeyEvent.VK_SPACE
		        || e.getKeyChar() == KeyEvent.VK_ALT;
	}

	private HTMLDocument getDocument() {
		return htmlEditorPanel.getDocument();
	}

	private JEditorPane getMostRecentFocusOwner() {
		return htmlEditorPanel.getMostRecentFocusOwner();
	}

	boolean needsSaving() {
		return htmlEditorPanel.needsSaving();
	}

	void setCurrentDocumentContent(String note) {
		htmlEditorPanel.setCurrentDocumentContent(note);
		
	}

	String getDocumentText() {
		return htmlEditorPanel.getDocumentText();
	}

	private JEditorPane getEditorPane() {
		return htmlEditorPanel.getEditorPane();
	}

	void updateCaretColor(Color noteForeground) {
		getEditorPane().setCaretColor(noteForeground != null ? noteForeground : defaultCaretColor);
	}

	StyleSheet getStyleSheet() {
		return getDocument().getStyleSheet();
	}

	void removeDocumentListener() {
		getDocument().removeDocumentListener(noteDocumentListener);
	}

	public boolean requestFocusInWindow() {
		if (ResourceController.getResourceController().getBooleanProperty("goto_note_end_on_edit")) {
			final JEditorPane editorPane = getEditorPane();
			editorPane.setCaretPosition(editorPane.getDocument().getLength());
		}
		return getMostRecentFocusOwner().requestFocusInWindow();
	}

	void installDocumentListener() {
		getDocument().addDocumentListener(noteDocumentListener);
	}
	
	void updateBaseUrl(URL url) {
		try {
			if (url != null) {
				getDocument().setBase(url);
			}
			else {
				getDocument().setBase(new URL("file: "));
			}
		}
		catch (final Exception e) {
		}
	}

}