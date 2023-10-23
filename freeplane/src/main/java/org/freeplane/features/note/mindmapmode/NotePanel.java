package org.freeplane.features.note.mindmapmode;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.regex.Pattern;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.html.ScaledEditorKit;
import org.freeplane.core.ui.components.html.StyleSheetConfigurer;
import org.freeplane.core.util.HtmlProcessor;
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
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    final static Pattern HEAD = Pattern.compile("<head>.*</head>\n", Pattern.DOTALL);

	private static final long serialVersionUID = 1L;
	private final SHTMLPanel htmlEditorPanel;
	private final JScrollPane viewerScrollPanel;
	private final JEditorPane htmlViewerPanel;
	private final JLabel iconViewerPanel;
	private final Color defaultCaretColor;
	private final NoteDocumentListener noteDocumentListener;

    private final NoteManager noteManager;
    private final StyleSheet ownStyleSheet;

    private FocusListener sourcePanelFocusListener;

    private boolean isEditing = false;

	NotePanel(NoteManager noteManager, NoteDocumentListener noteDocumentListener) {
		super(new CardLayout());
        this.noteManager = noteManager;
		this.noteDocumentListener = noteDocumentListener;
		setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		this.htmlEditorPanel = createHtmlEditorComponent(noteManager);
		this.ownStyleSheet = StyleSheetConfigurer.createDefaultStyleSheet();
		this.defaultCaretColor = htmlEditorPanel.getEditorPane().getCaretColor();
		htmlEditorPanel.setVisible(false);
		add(htmlEditorPanel);

		this.viewerScrollPanel = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		viewerScrollPanel.setVisible(false);
		add(viewerScrollPanel);
		htmlViewerPanel = new JEditorPane();
		htmlViewerPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		htmlViewerPanel.setOpaque(true);
		htmlViewerPanel.setEditable(false);
		htmlViewerPanel.setEditorKitForContentType(CONTENT_TYPE_TEXT_HTML, ScaledEditorKit.create());
		iconViewerPanel = new JLabel();
		iconViewerPanel.setVerticalAlignment(SwingConstants.TOP);

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

		sourcePanelFocusListener = new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if(isEditing && ! e.isTemporary()){
				    noteManager.saveNote();
				    if(viewerScrollPanel.isVisible())
				        noteManager.updateEditor();

				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		};
        htmlEditorPanel.getSourceEditorPane().addFocusListener(sourcePanelFocusListener);
//		setDefaultFont();
		htmlEditorPanel.setOpenHyperlinkHandler(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent pE) {
				try {
					String uriText = pE.getActionCommand();
					LinkController.getController().loadURI(noteManager.getNode(), LinkController.createHyperlink(uriText));
				}
				catch (final Exception e) {
					LogUtils.severe(e);
				}
			}
		});
		return htmlEditorPanel;
	}



	@Override
	public void setComponentOrientation(ComponentOrientation o) {
		if(o != super.getComponentOrientation()) {
			htmlEditorPanel.getEditorPane().setComponentOrientation(o);
			htmlViewerPanel.setComponentOrientation(o);
			iconViewerPanel.setComponentOrientation(o);
			super.setComponentOrientation(o);
		}
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

	private boolean needsSaving() {
		if (htmlEditorPanel.isVisible() && htmlEditorPanel.needsSaving())
		    return true;
        else if (viewerScrollPanel.isVisible()) {
            Component view = viewerScrollPanel.getViewport().getView();
            if (view instanceof JTextComponent)
                return ((JTextComponent)view).isEditable();
            else
                return false;
        } else
            return false;

	}

	void setEditedContent(String note, String ownRule, StyleSheet customStyleSheet, Color foreground, Color background) {
	    isEditing = true;
		setVisible(htmlEditorPanel);
		htmlEditorPanel.setCurrentDocumentContent("");
        updateStyleSheet(ownRule, customStyleSheet);
        updateColors(foreground, background);
        HtmlProcessor.configureUnknownTags(htmlEditorPanel.getDocument());
		htmlEditorPanel.setCurrentDocumentContent(note);
		if(note.isEmpty()) {
			final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
			editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
            editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		}
	}


    void removeViewedContent() {
        setVisible(htmlViewerPanel);
        String contentType = CONTENT_TYPE_TEXT_PLAIN;
        htmlViewerPanel.setText("");
        if(! htmlViewerPanel.getContentType().equals(contentType))
            htmlViewerPanel.setContentType(contentType);
    }
	void setViewedContent(String note, String ownRule, StyleSheet customStyleSheet, Color foreground, Color background) {
		setVisible(htmlViewerPanel);
		String contentType = HtmlUtils.isHtml(note) ? CONTENT_TYPE_TEXT_HTML : CONTENT_TYPE_TEXT_PLAIN;
		htmlViewerPanel.setText("");
		if(! htmlViewerPanel.getContentType().equals(contentType))
		    htmlViewerPanel.setContentType(contentType);
		if(contentType == CONTENT_TYPE_TEXT_HTML) {
		    HtmlProcessor.configureUnknownTags((HTMLDocument)htmlViewerPanel.getDocument());
            updateStyleSheet(ownRule, customStyleSheet);
        }
		updateColors(foreground, background);
		htmlViewerPanel.setText(note);
        if(note.isEmpty()) {
            htmlViewerPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
            htmlViewerPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        }
	}

	void setViewedImage(Icon icon, HorizontalTextAlignment alignment ) {
		setVisible(iconViewerPanel);
		iconViewerPanel.setIcon(icon);
		iconViewerPanel.setHorizontalAlignment(alignment.swingConstant);
	}

	private void setVisible(JComponent component) {
		htmlEditorPanel.setVisible(component == htmlEditorPanel);
		viewerScrollPanel.setVisible(component != htmlEditorPanel);
		htmlViewerPanel.setVisible(component == htmlViewerPanel);
		iconViewerPanel.setVisible(component == iconViewerPanel);
		if(! htmlEditorPanel.isVisible())
			htmlEditorPanel.setCurrentDocumentContent("");
		if(htmlViewerPanel.isVisible())
			setViewerComponent(htmlViewerPanel);
		else
			htmlViewerPanel.setText("");
		if(iconViewerPanel.isVisible())
			setViewerComponent(iconViewerPanel);
		else
			iconViewerPanel.setIcon(null);
		revalidate();
	}

	private String getDocumentText() {
	    if(htmlEditorPanel.isVisible())
	        return htmlEditorPanel.getDocumentText();
	    else if(viewerScrollPanel.isVisible()) {
	        Component view = viewerScrollPanel.getViewport().getView();
	        if(view instanceof JTextComponent)
	            return ((JTextComponent)view).getText();
	    }
	    return "";
	}

	private JEditorPane getEditorPane() {
		if(htmlEditorPanel.isVisible())
			return htmlEditorPanel.getMostRecentFocusOwner();
		else
			return htmlViewerPanel;
	}

	private void updateColors(Color noteForeground, Color noteBackground) {
	    final Color caretColor = noteForeground != null ? noteForeground : defaultCaretColor;
	    final JEditorPane editorPane;
	    if(htmlEditorPanel.isVisible()) {
	    	editorPane = htmlEditorPanel.getEditorPane();
	    }
	    else {
	    	editorPane = htmlViewerPanel;
	    }
	    editorPane.setCaretColor(caretColor);
	    editorPane.setForeground(noteForeground);
	    editorPane.setBackground(noteBackground);
	}

	private void updateStyleSheet(String ownRule, StyleSheet customStyleSheet) {
		HTMLDocument document = getDocument();
		StyleSheet styleSheet = document.getStyleSheet();
		StyleSheetConfigurer.resetStyles(styleSheet, 1);
		ownStyleSheet.removeStyle("body");
		ownStyleSheet.removeStyle("p");
		ownStyleSheet.addRule(ownRule);
		styleSheet.addStyleSheet(ownStyleSheet);
		styleSheet.addStyleSheet(customStyleSheet);
	}

	void removeDocumentListener() {
		if(htmlEditorPanel.isVisible())
			htmlEditorPanel.getDocument().removeDocumentListener(noteDocumentListener);
	}

	private boolean requestFocusInEditorPane() {
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
			final HTMLDocument document = getDocument();
			if (url != null) {
				document.setBase(url);
			}
			else {
				document.setBase(new URL("file: "));
			}
		}
		catch (final Exception e) {
		}
	}

	void editNote() {
	       if (htmlEditorPanel.isVisible()) {
	           requestFocusInEditorPane();
	           return;
	       }

		final Controller controller = Controller.getCurrentController();
		IMapSelection selection = controller.getSelection();
		if(selection == null)
			return;
		final NodeModel node = selection.getSelected();
		if(node != null) {
            NoteModel note = NoteModel.getNote(node);
            if(note ==  null){
            	note = new NoteModel();
            }
            Component view = viewerScrollPanel.getViewport().getView();
            JEditorPane textPane = MTextController.getController().createEditorPane(() -> viewerScrollPanel, node, note, note.getTextOr(""));
            if(textPane != null) {
                textPane.requestFocusInWindow();
                textPane.addFocusListener(sourcePanelFocusListener);
                textPane.getDocument().addDocumentListener(noteDocumentListener);
                textPane.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                            isEditing = false;
                            setViewerComponent(view);
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                    }
                    @Override
                    public void keyPressed(KeyEvent e) {
                    }
                });
                isEditing = true;
            }
        }
	}

	private void setViewerComponent(Component view) {
	       Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	       if(isEditing && focusOwner instanceof JTextComponent
	               && ((JTextComponent)focusOwner).isEditable()
	               && SwingUtilities.isDescendingFrom(focusOwner, this))
	           return;
	       isEditing = false;
	       viewerScrollPanel.setViewportView(view);
	       if(viewerScrollPanel.getRowHeader() != null)
	           viewerScrollPanel.setRowHeader(null);
    }

    void saveNote() {
	       if (! needsSaving()) {
	            return;
	        }
	        String documentText = getDocumentText();
            String text  = HtmlUtils.isHtml(documentText) ? HEAD.matcher(documentText).replaceFirst("") :  documentText ;
	        noteManager.saveNote(text);
	    }

    void stopEditing() {
        isEditing = false;
    }

}