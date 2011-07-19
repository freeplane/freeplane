package org.freeplane.plugin.workspace.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DirTree extends JPanel {
    public static final String APP_NAME = "Directories Tree";
    public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
    public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
    public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
    public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");
    protected JTree  m_tree;
    protected DefaultTreeModel m_model;
    protected JTextField m_display;
    
    
    public DirTree() { 
    	this.setLayout(new BorderLayout());
    	this.setMinimumSize(new Dimension(200, 480));
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));
        DefaultMutableTreeNode node;
        
        File[] roots = new File[1];
        roots[0] = new File("C:"+File.separator+"book");
        for (int k=0; k<roots.length; k++) {
            node = new DefaultMutableTreeNode(new IconData(ICON_DISK,
                null, new FileNode(roots[k])));
            top.add(node);
            node.add( new DefaultMutableTreeNode(new Boolean(true)));
        }
        
        m_model = new DefaultTreeModel(top);
        
        m_tree = new JTree(m_model);
        //m_tree.putClientProperty("JTree.lineStyle", "Angled");
        IconCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);
        m_tree.addTreeExpansionListener(new DirExpansionListener(m_model));
        m_tree.addTreeSelectionListener(new DirSelectionListener());
        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);
        
        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);
        this.add(s, BorderLayout.CENTER);
    }
    
    DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode)(path.getLastPathComponent());
    }
    
    
    FileNode getFileNode(DefaultMutableTreeNode node) {
        if (node == null)
            return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData)
            obj = ((IconData)obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode)obj;
        else
            return null;
    }
}

