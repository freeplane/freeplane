package org.freeplane.features.note.mindmapmode;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
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
	private final JScrollPane viewerScrollPanel;
	private final JEditorPane htmlViewerPanel;
	private final JLabel iconViewerPanel;
	private final Color defaultCaretColor;
	private final NoteDocumentListener noteDocumentListener;

	NotePanel(NoteManager noteManager, NoteDocumentListener noteDocumentListener) {
		super(new CardLayout());
		this.noteDocumentListener = noteDocumentListener;
		setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		this.htmlEditorPanel = createHtmlEditorComponent(noteManager);
		this.defaultCaretColor = htmlEditorPanel.getEditorPane().getCaretColor();
		htmlEditorPanel.setVisible(false);
		add(htmlEditorPanel);

		this.viewerScrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		viewerScrollPanel.setVisible(false);
		add(viewerScrollPanel);
		htmlViewerPanel = new JEditorPane();
		htmlViewerPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
		htmlViewerPanel.setEditable(false);
		iconViewerPanel = new JLabel();
		
		MouseListener editStarter = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					editNote();
			}
			
		};
		htmlViewerPanel.addMouseListener(editStarter);
		iconViewerPanel.addMouseListener(editStarter);
		
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
		if(htmlEditorPanel.isVisible())
			return htmlEditorPanel.getDocument();
		else if(htmlViewerPanel.isVisible()) {
			Document document = htmlViewerPanel.getDocument();
			if(document instanceof HTMLDocument)
				return (HTMLDocument) document;
		}
		return new HTMLDocument();
	}

	private JComponent getMostRecentFocusOwner() {
		if(htmlEditorPanel.isVisible())
			return htmlEditorPanel.getMostRecentFocusOwner();
		else if (htmlViewerPanel.isVisible())
			return htmlViewerPanel;
		else if (iconViewerPanel.isVisible())
			return iconViewerPanel;
		else
			return null;
	}

	boolean needsSaving() {
		return htmlEditorPanel.needsSaving();
	}

	void setEditedContent(String note) {
		setVisible(htmlEditorPanel);
		htmlEditorPanel.setCurrentDocumentContent(note);
	}

	void setViewedContent(String note) {
		setVisible(htmlViewerPanel);
		if(! note.isEmpty()) {
			String contentType = HtmlUtils.isHtml(note) ? "text/html" : "text/plain";
			if(! htmlViewerPanel.getContentType().equals(contentType))
				htmlViewerPanel.setContentType(contentType);
		}
		htmlViewerPanel.setText(note);
	}

	void setViewedImage(Icon icon) {
		setVisible(iconViewerPanel);
		iconViewerPanel.setIcon(icon);
	}

	private void setVisible(JComponent component) {
		htmlEditorPanel.setVisible(component == htmlEditorPanel);
		viewerScrollPanel.setVisible(component != htmlEditorPanel);
		htmlViewerPanel.setVisible(component == htmlViewerPanel);
		iconViewerPanel.setVisible(component == iconViewerPanel);
		if(! htmlEditorPanel.isVisible())
			htmlEditorPanel.setCurrentDocumentContent("");
		if(htmlViewerPanel.isVisible())
			viewerScrollPanel.setViewportView(htmlViewerPanel);
		else
			htmlViewerPanel.setText("");
		if(iconViewerPanel.isVisible())
			viewerScrollPanel.setViewportView(iconViewerPanel);
		else
			iconViewerPanel.setIcon(null);
		revalidate();
	}

	boolean isEditable() {
		return htmlEditorPanel.isVisible();
	}
	
	String getDocumentText() {
		return htmlEditorPanel.getDocumentText();
	}

	private JEditorPane getEditorPane() {
		if(htmlEditorPanel.isVisible())
			return htmlEditorPanel.getMostRecentFocusOwner();
		else
			return htmlViewerPanel;
	}

	void updateCaretColor(Color noteForeground) {
		getEditorPane().setCaretColor(noteForeground != null ? noteForeground : defaultCaretColor);
	}

	StyleSheet getStyleSheet() {
		return getDocument().getStyleSheet();
	}

	void removeDocumentListener() {
		if(htmlEditorPanel.isVisible())
			htmlEditorPanel.getDocument().removeDocumentListener(noteDocumentListener);
	}

	public boolean requestFocusInWindow() {
		if (ResourceController.getResourceController().getBooleanProperty("goto_note_end_on_edit")) {
			final JEditorPane editorPane = getEditorPane();
			editorPane.setCaretPosition(editorPane.getDocument().getLength());
		}
		return getMostRecentFocusOwner().requestFocusInWindow();
	}

	void installDocumentListener() {
		if(htmlEditorPanel.isVisible())
			htmlEditorPanel.getDocument().addDocumentListener(noteDocumentListener);
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
	
	private void editNote() {
		final Controller controller = Controller.getCurrentController();
		IMapSelection selection = controller.getSelection();
		if(selection == null)
			return;
		final NodeModel node = selection.getSelected();
		if(node == null)
		new NoteDialogStarter().editNoteInDialog(node);
	}

}