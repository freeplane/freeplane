package org.freeplane.plugin.workspace.view;

import java.io.File;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;


public class FileNode {
	
    protected File m_file;
    public FileNode(File file) {
        m_file = file;
    }
    public File getFile() {
        return m_file;
    }
    public String toString() {
        return m_file.getName().length() > 0 ? m_file.getName() :
            m_file.getPath();
    }
    // Alternatively we copud sub-class TreeNode
    public boolean expand(DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode flag =
            (DefaultMutableTreeNode)parent.getFirstChild();
        if (flag==null)   // No flag
            return false;
        Object obj = flag.getUserObject();
        if (!(obj instanceof Boolean))
            return false;      // Already expanded
        parent.removeAllChildren();  // Remove Flag
        File[] files = listFiles();
        if (files == null)
            return true;
        Vector v = new Vector();
        for (int k=0; k<files.length; k++) {
            File f = files[k];
//            if (!(f.isDirectory()))
//                continue;
            FileNode newNode = new FileNode(f);
            boolean isAdded = false;
            for (int i=0; i<v.size(); i++) {
                FileNode nd = (FileNode)v.elementAt(i);
                if (newNode.compareTo(nd) < 0) {
                    v.insertElementAt(newNode, i);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded)
                v.addElement(newNode);
        }
        for (int i=0; i<v.size(); i++) {
            FileNode nd = (FileNode)v.elementAt(i);
            IconData idata = new IconData(DirTree.ICON_FOLDER,
                DirTree.ICON_EXPANDEDFOLDER, nd);
            DefaultMutableTreeNode node = new
                DefaultMutableTreeNode(idata);
            parent.add(node);
            if (nd.hasSubDirs())
                node.add(new DefaultMutableTreeNode(
                    new Boolean(true) ));
        }
        return true;
    }
    public boolean hasSubDirs() {
        File[] files = listFiles();
        if (files == null)
            return false;
        for (int k=0; k<files.length; k++) {
            if (files[k].isDirectory())
                return true;
        }
        return false;
    }
    public int compareTo(FileNode toCompare) {
        return  m_file.getName().compareToIgnoreCase(
            toCompare.m_file.getName() );
    }
    protected File[] listFiles() {
        if (!m_file.isDirectory())
            return null;
        try {
            return m_file.listFiles();
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Error reading directory "+m_file.getAbsolutePath(),
                DirTree.APP_NAME, JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
}