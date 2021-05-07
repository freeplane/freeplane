package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.features.ui.FrameController;

public class JFreeplaneCustomizableFileChooser extends JFileChooser{
    private static final String FILE_CHOOSER_SPECIAL_FOLDERS_PROPERTY = "file_chooser_shows_special_folders";

	private static final String USE_SHELL_FOLDER_JAVA_PROPERTY = "FileChooser.useShellFolder";

	private static final long serialVersionUID = 1;

	private final List<JComponent> optionComponents = new ArrayList<>();

    private boolean areSpecialFoldersShown;

	private boolean isFileHidingDisabledForCurrentDirectory = false;

    public JFreeplaneCustomizableFileChooser() {
        super();
        initializeSpecialFolderShownFlag();
    }

	private void initializeSpecialFolderShownFlag() {
		boolean areSpecialFoldersShown = ResourceController.getResourceController().getBooleanProperty(FILE_CHOOSER_SPECIAL_FOLDERS_PROPERTY);
		if(this.areSpecialFoldersShown != areSpecialFoldersShown)
			this.areSpecialFoldersShown = areSpecialFoldersShown;
	}

    public JFreeplaneCustomizableFileChooser(File currentDirectory) {
        super(currentDirectory);
        initializeSpecialFolderShownFlag();
    }

    @Override
	protected void setup(FileSystemView view) {
    	initializeSpecialFolderShownFlag();
        putClientProperty(USE_SHELL_FOLDER_JAVA_PROPERTY, areSpecialFoldersShown);
    	super.setup(view);
	}

	@Override
	public Dimension getPreferredSize() {
		return fixAquaFileChooserUIPreferredSize();
	}



	@Override
	public boolean isFileHidingEnabled() {
		if(isFileHidingDisabledForCurrentDirectory)
			return false;
		return super.isFileHidingEnabled();
	}

	private Dimension fixAquaFileChooserUIPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if(isPreferredSizeSet() || ! getUI().getClass().getSimpleName().equals("AquaFileChooserUI")) {
			return preferredSize;
		}
		LayoutManager layout = getLayout();
		Dimension layoutPrefSize = layout.preferredLayoutSize(this);
		Dimension maximumSize = super.getMaximumSize();
		int width = Math.min(maximumSize.width, Math.max(preferredSize.width, layoutPrefSize.width));
		int height = Math.min(maximumSize.height, Math.max(preferredSize.height, layoutPrefSize.height));
		return new Dimension(width, height);
	}

	@Override
	public void setCurrentDirectory(File dir) {
		if(dir != null && ! areSpecialFoldersShown && Compat.isWindowsOS() && dir.getClass().equals(File.class)) {
			try {
				setDirectoryBehavingLikeShellFolder(dir);
			return;
			}
			catch (IOException e) {
			}
		}
		if(UIManager.getLookAndFeel().getName().equals(FrameController.VAQUA_LAF_NAME)) {
			final boolean wasFileHidingEnabled = isFileHidingEnabled();
			isFileHidingDisabledForCurrentDirectory = isDirectoryHiddenInVAqua(dir);
			final boolean isFileHidingEnabled = isFileHidingEnabled();
			if(wasFileHidingEnabled != isFileHidingEnabled) {
				SwingUtilities.invokeLater(() ->
					firePropertyChange(FILE_HIDING_CHANGED_PROPERTY, wasFileHidingEnabled, isFileHidingEnabled));
			}
		}
		super.setCurrentDirectory(dir);
	}

	private boolean isDirectoryHiddenInVAqua(File directory) {
		for (; directory != null; directory = directory.getParentFile()) {
			if (getFileSystemView().isHiddenFile(directory))
				return true;
		}
		return false;
	}

	private void setDirectoryBehavingLikeShellFolder(File dir) throws IOException {
		File shellFolder = sun.awt.shell.ShellFolder.getShellFolder(dir);
		super.setCurrentDirectory(shellFolder);
	}


	@Override
	public boolean accept(File f) {
		if(! areSpecialFoldersShown && Compat.isWindowsOS() && f.getName().endsWith(".lnk")) {
			return false;
		}
		return super.accept(f);
	}



	@FunctionalInterface
    public interface Customizer extends Consumer<JDialog>{
        Customizer DEFAULT = d -> {};
    }

    private Consumer<JDialog> customizer = Customizer.DEFAULT;

    public void addCustomizer(Customizer newCustomizer) {
        customizer = customizer.andThen(newCustomizer);
    }

    public Consumer<JDialog> getCustomizer() {
        return customizer;
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        final JDialog dialog = super.createDialog(parent);
        customizer.accept(dialog);
        if(optionComponents.size() == 1) {
           dialog.getContentPane().add(optionComponents.get(0), BorderLayout.SOUTH);
        }
        else if(optionComponents.size() > 1) {
            Box optionBox = Box.createVerticalBox();
            optionComponents.forEach(c -> c.setAlignmentX(LEFT_ALIGNMENT));
            optionComponents.forEach(optionBox::add);
            dialog.getContentPane().add(optionBox, BorderLayout.SOUTH);
        }
        if(!dialog.getContentPane().isValid())
            dialog.pack();

        if(Compat.isMacOsX()) {
            ActionMap am = getActionMap();
            InputMap globalInputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            KeyStroke ks  = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.META_MASK | InputEvent.SHIFT_MASK);
                    globalInputMap.put(ks, ks);
			am.put(ks, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean isHiding = isFileHidingEnabled();
                    setFileHidingEnabled(!isHiding);
				}
			});
        }

        return dialog;
    }

    public void addOptionComponent(final JComponent component) {
        optionComponents.add(component);
    }


}
