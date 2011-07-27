package org.freeplane.plugin.workspace.view;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.plugin.workspace.controller.NodeExpansionListener;
import org.freeplane.plugin.workspace.controller.NodeSelectionListener;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModel;

public class TreeView extends JPanel implements Autoscroll {

	private static final long serialVersionUID = 1L;
	private int margin = 12;
	
	protected JTree m_tree;
	protected DefaultTreeModel m_model;
	protected JTextField m_display;

	public TreeView() {
		this(new DefaultMutableTreeNode());
	}

	public TreeView(MutableTreeNode conigurationRoot) {
		this.setLayout(new BorderLayout());
		m_model = new WorkspaceTreeModel(conigurationRoot);

		m_tree = new JTree(m_model);
		m_tree.putClientProperty("JTree.lineStyle", "Angled");
		m_tree.setCellRenderer(new WorkspaceNodeRenderer());
		m_tree.setCellEditor(new WorkspaceCellEditor(m_tree, (DefaultTreeCellRenderer) m_tree.getCellRenderer()));
		m_tree.addTreeExpansionListener(new NodeExpansionListener());
		m_tree.addTreeSelectionListener(new NodeSelectionListener());
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(false);
		m_tree.setEditable(true);

		JScrollPane s = new JScrollPane();
		s.getViewport().add(m_tree);
		this.add(s, BorderLayout.CENTER);
	}

	DefaultMutableTreeNode getTreeNode(TreePath path) {
		return (DefaultMutableTreeNode) (path.getLastPathComponent());
	}

	public DefaultTreeModel getTreeModel() {
		return m_model;
	}

	public void addTreeMouseListener(MouseListener l) {
		this.m_tree.addMouseListener(l);
	}

	public void addTreeComponentListener(ComponentListener l) {
		this.m_tree.addComponentListener(l);
	}

	public void autoscroll(Point p) {
		int realrow = m_tree.getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1 : realrow < m_tree.getRowCount() - 1 ? realrow + 1 : realrow);
		m_tree.scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y + outer.y
				+ margin, outer.width - inner.width - inner.x + outer.x + margin);
	}

}
