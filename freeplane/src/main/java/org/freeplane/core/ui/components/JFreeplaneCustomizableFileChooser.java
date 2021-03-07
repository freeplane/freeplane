package org.freeplane.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.freeplane.core.util.Compat;

public class JFreeplaneCustomizableFileChooser extends JFileChooser{

    private static final long serialVersionUID = 1;

    private final List<JComponent> optionComponents = new ArrayList<>();

    public JFreeplaneCustomizableFileChooser() {
        super();
    }

    public JFreeplaneCustomizableFileChooser(File currentDirectory) {
        super(currentDirectory);
    }

    @Override
	protected void setup(FileSystemView view) {
        putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
    	super.setup(view);
	}



	@Override
	public Dimension getPreferredSize() {
		return fixAquaFileChooserUIPreferredSize();
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

	@SuppressWarnings("serial")
	@Override
	public void setCurrentDirectory(File dir) {
		if(dir != null && Compat.isWindowsOS() && dir.getClass().equals(File.class)
				&& Boolean.FALSE.equals(getClientProperty("FileChooser.useShellFolder"))) {
			try {
				setDirectoryBehavingLikeShellFolder(dir);
			return;
			}
			catch (IOException e) {
			}
		}
		super.setCurrentDirectory(dir);
	}

	private void setDirectoryBehavingLikeShellFolder(File dir) throws IOException {
			File shellFile = new File(dir.getCanonicalPath()) {
				@Override
				public File getParentFile() {
					return getFileSystemView().getParentDirectory(this);
				}

				@Override
				public File getCanonicalFile() throws IOException {
					return this;
				}

				@Override
				public String getCanonicalPath() throws IOException {
					return getPath();
				}
			};
			super.setCurrentDirectory(shellFile);
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
        return dialog;
    }

    public void addOptionComponent(final JComponent component) {
        optionComponents.add(component);
    }


}
