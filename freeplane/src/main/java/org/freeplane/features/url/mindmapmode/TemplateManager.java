package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.TreeSet;

import org.freeplane.core.resources.ResourceController;

public class TemplateManager {
    File templateFile(final String userDefinedTemplateFilePath) {
        final ResourceController resourceController = ResourceController.getResourceController();
        final File userDefinedTemplateFile = new File(userDefinedTemplateFilePath);
        if (userDefinedTemplateFile.isAbsolute() && userDefinedTemplateFile.exists()
                && !userDefinedTemplateFile.isDirectory()) {
            return userDefinedTemplateFile;
        }
        for (final String filePath : new String[] { userDefinedTemplateFilePath,
                resourceController.getDefaultProperty(MFileManager.STANDARD_TEMPLATE) }) {
            for (final File userTemplates : new File[] { defaultUserTemplateDir(), defaultStandardTemplateDir() }) {
                if (userTemplates.isDirectory()) {
                    final File userStandard = new File(userTemplates, filePath);
                    if (userStandard.exists() && !userStandard.isDirectory()) {
                        if (!filePath.equals(userDefinedTemplateFilePath))
                            resourceController.setProperty(MFileManager.STANDARD_TEMPLATE, filePath);
                        return userStandard;
                    }
                }
            }
        }
        return null;
    }

    public File defaultUserTemplateDir() {
        final String userDir = ResourceController.getResourceController().getFreeplaneUserDirectory();
        final File userTemplates = new File(userDir, "templates");
        return userTemplates;
    }

    public File defaultStandardTemplateDir() {
        final String resourceBaseDir = ResourceController.getResourceController().getResourceBaseDir();
        final File allUserTemplates = new File(resourceBaseDir, "templates");
        return allUserTemplates;
    }


    public TreeSet<String> collectAvailableMapTemplates() {
        final TreeSet<String> templates = new TreeSet<String>();
        for (File dir : new File[] { defaultStandardTemplateDir(), defaultUserTemplateDir() })
            if (dir.isDirectory())
                templates.addAll(Arrays.asList(dir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(MFileManager.FREEPLANE_FILE_EXTENSION);
                    }
                })));
        return templates;
    }

}