package org.freeplane.plugin.workspace.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class TreeView extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String APP_NAME = "Directories Tree";
    public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
    public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
    public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
    public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");
    protected JTree  m_tree;
    protected DefaultTreeModel m_model;
    protected JTextField m_display;
    
    
    public TreeView() { 
    	this(new DefaultMutableTreeNode());
    }
    
    public TreeView(MutableTreeNode conigurationRoot) {
    	this.setLayout(new BorderLayout());
    	this.setMinimumSize(new Dimension(200, 480));
        
        m_model = new DefaultTreeModel(conigurationRoot);
        
        m_tree = new JTree(m_model);
        m_tree.putClientProperty("JTree.lineStyle", "Angled");
        IconCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);
        m_tree.addTreeExpansionListener(new NodeExpansionListener());
        m_tree.addTreeSelectionListener(new NodeSelectionListener());
        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.setShowsRootHandles(false);
        m_tree.setEditable(false);
        
        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);
        this.add(s, BorderLayout.CENTER);
	}

	DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode)(path.getLastPathComponent());
    }

	public DefaultTreeModel getTreeModel() {		
		return m_model;
	}
    
}

