package org.freeplane.plugin.latex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

class JLatexViewer extends JComponent {
	private static final int DEFAULT_FONT_SIZE = 16;
	static String editorTitle = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float zoom = 0f;
	final private LatexNodeHook latexController;
	private LatexExtension model;
	private TeXFormula teXFormula; 

	JLatexViewer(final LatexNodeHook latexController, final LatexExtension latexExtension) {
		this.latexController = latexController;
		setBorder( BorderFactory.createLineBorder(Color.BLACK));
		setModel(latexExtension);
		if (JLatexViewer.editorTitle == null) {
			JLatexViewer.editorTitle = ResourceBundles.getText("plugins/latex/LatexNodeHook.editorTitle");
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					edit();
					SwingUtilities.getAncestorOfClass(NodeView.class, JLatexViewer.this).requestFocus();
					e.consume();
					return;
				}
			}
		});
	}

	private void edit() {
		final JTextArea textArea = new JTextArea(model.getEquation());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		final JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(500, 160));
		final JOptionPane editPane = new JOptionPane(editorScrollPane,JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		final JDialog edit = editPane.createDialog(JOptionPane.getFrameForComponent(this), JLatexViewer.editorTitle);
		edit.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		edit.setLocationRelativeTo(this);
		edit.setVisible(true);
		if (editPane.getValue().equals(JOptionPane.OK_OPTION)){
			final String eq = textArea.getText();
			latexController.setEquationUndoable(model, eq);
		}
	}

	private void calculateSize() {
		MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		final float mapZoom = mapView.getZoom();
		if(mapZoom == zoom){
			return;
		}
		this.zoom = mapZoom;
		Icon latexIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE * zoom);
		final Insets insets = getInsets();
		final Dimension dimension = new Dimension (latexIcon.getIconWidth() + insets.left + insets.right, 
			latexIcon.getIconHeight() + insets.top + insets.bottom);
		setPreferredSize(dimension);
    }
	
	@Override
	public void paint(final Graphics g) {
		Icon latexIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE * zoom);
		final Insets insets = getInsets();
		latexIcon.paintIcon(this, g, insets.left, insets.top);
		super.paint(g);
	}

	public void setModel(final LatexExtension latexExtension) {
		model = latexExtension;
		try {
	        teXFormula = new TeXFormula(model.getEquation());
			teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE);
        }
        catch (Exception e) {
			try {
		        teXFormula = new TeXFormula("\\mbox{" +e.getMessage() +"}");
				teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE);
	        }
	        catch (Exception e1) {
		        teXFormula = new TeXFormula("\\mbox{Can not parse given equation}");
	        }
        }
        zoom = 0;
		revalidate();
		repaint();
	}

	@Override
    public Dimension getPreferredSize() {
		calculateSize();
	    return super.getPreferredSize();
    }
	
	
}
