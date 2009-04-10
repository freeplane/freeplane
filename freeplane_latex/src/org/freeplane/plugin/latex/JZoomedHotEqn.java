package org.freeplane.plugin.latex;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.freeplane.core.resources.FreeplaneResourceBundle;

import atp.sHotEqn;

class JZoomedHotEqn extends sHotEqn {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	static String editorTitle = null;
	static private double zoom = 1f;
	final private LatexNodeHook latexController;
	private LatexExtension model;

	JZoomedHotEqn(final LatexNodeHook latexController, final LatexExtension latexExtension) {
		this.latexController = latexController;
		setDebug(false);
		setEditable(false);
		setBorder(true);
		model = latexExtension;
		setEquation(latexExtension.getEquation());
		if (JZoomedHotEqn.editorTitle == null) {
			JZoomedHotEqn.editorTitle = FreeplaneResourceBundle.getText("plugins/latex/LatexNodeHook.editorTitle");
		}
	}

	private void edit() {
		final JTextArea textArea = new JTextArea(getEquation());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		final JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(500, 160));
		final JDialog edit = new JDialog(JOptionPane.getFrameForComponent(this), JZoomedHotEqn.editorTitle, true);
		edit.getContentPane().add(editorScrollPane);
		edit.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		edit.pack();
		edit.setLocationRelativeTo(this);
		edit.setVisible(true);
		final String eq = textArea.getText();
		latexController.setEquationUndoable(model, eq);
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension dimension = isValid() ? super.getPreferredSize() : getSizeof(getEquation());
		dimension.height *= JZoomedHotEqn.zoom;
		dimension.width *= JZoomedHotEqn.zoom;
		return dimension;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			edit();
			e.consume();
			return;
		}
		super.mouseClicked(e);
	}

	@Override
	public void paint(final Graphics g) {
		if (JZoomedHotEqn.zoom != 1F) {
			final Graphics2D g2 = (Graphics2D) g;
			final AffineTransform transform = g2.getTransform();
			g2.scale(JZoomedHotEqn.zoom, JZoomedHotEqn.zoom);
			super.paint(g);
			g2.setTransform(transform);
		}
		else {
			super.paint(g);
		}
	}

	@Override
	public void setBounds(final int x, final int y, final int w, final int h) {
		if (JZoomedHotEqn.zoom < 1f) {
			super.setBounds(x, y, (int) (w / JZoomedHotEqn.zoom), (int) (h / JZoomedHotEqn.zoom));
		}
		else {
			super.setBounds(x, y, (w), (h));
		}
	}

	public void setModel(final LatexExtension latexExtension) {
		model = latexExtension;
		setEquation(latexExtension.getEquation());
		revalidate();
		repaint();
	}
}
