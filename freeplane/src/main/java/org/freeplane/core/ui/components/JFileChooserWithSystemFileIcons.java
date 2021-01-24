package org.freeplane.core.ui.components;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

public class JFileChooserWithSystemFileIcons extends JFileChooser{

    private static final long serialVersionUID = 1;

    private static final Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon");
    private static final Icon computerIcon = UIManager.getIcon("FileView.computerIcon");
    private static final Icon hardDriveIcon = UIManager.getIcon("FileView.hardDriveIcon");
    private static final Icon floppyDriveIcon = UIManager.getIcon("FileView.floppyDriveIcon");

    public JFileChooserWithSystemFileIcons() {
        super();
    }

    public JFileChooserWithSystemFileIcons(File currentDirectory, FileSystemView fsv) {
        super(currentDirectory, fsv);
    }

    public JFileChooserWithSystemFileIcons(File currentDirectory) {
        super(currentDirectory);
    }

    public JFileChooserWithSystemFileIcons(FileSystemView fsv) {
        super(fsv);
    }

    public JFileChooserWithSystemFileIcons(String currentDirectoryPath, FileSystemView fsv) {
        super(currentDirectoryPath, fsv);
    }

    public JFileChooserWithSystemFileIcons(String currentDirectoryPath) {
        super(currentDirectoryPath);
    }

    @Override
    public Icon getIcon(File f) {
        Icon icon = null;
        if (f != null) {
            if(getFileView() != null) {
                icon = getFileView().getIcon(f);
            }

            final FileView uiFileView;
			if(icon == null && directoryIcon != null && f.isDirectory()) {
				final FileSystemView fsv = getFileSystemView();

                if (fsv.isFloppyDrive(f)) {
                    icon = floppyDriveIcon;
                } else if (fsv.isDrive(f)) {
                    icon = hardDriveIcon;
                } else if (fsv.isComputerNode(f)) {
                    icon = computerIcon;
                }
                if(icon == null) {
                    icon = directoryIcon;
                }
			}
			if(icon == null) {
				uiFileView = getUI().getFileView(this);

				if(icon == null && uiFileView != null) {
					icon = uiFileView.getIcon(f);
				}
			}
        }
        return icon;
    }


}
