package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

public class TemplateManager {
    public static final TemplateManager INSTANCE = new TemplateManager();


    File existingTemplateFile(final String filePath) {
        final File userDefinedTemplateFile = new File(filePath);
        if (userDefinedTemplateFile.isAbsolute()
                && userDefinedTemplateFile.exists() && !userDefinedTemplateFile.isDirectory()) {
                return userDefinedTemplateFile;
        }
        else {
            File templateFile = templateFile(filePath);
            if(templateFile.exists())
                return templateFile;
        }
        return fallbackTemplate();
    }


    File writeableTemplateFile(final String filePath) {
        final File userDefinedTemplateFile = new File(filePath);
        if (userDefinedTemplateFile.isAbsolute()) {
                return userDefinedTemplateFile;
        }
        else {
            return new File(defaultUserTemplateDir(), filePath);
        }
    }
    
    private File fallbackTemplate() {
        final ResourceController resourceController = ResourceController.getResourceController();
        String fallback = resourceController.getDefaultProperty(MFileManager.STANDARD_TEMPLATE);
        File fallbackFile = templateFile(fallback);
        Objects.requireNonNull(fallbackFile);
        return fallbackFile;
    }

    private File templateFile(final String filePath) {
        File defaultUserTemplateDir = defaultUserTemplateDir();
        for (final File userTemplates : new File[] { defaultUserTemplateDir, defaultStandardTemplateDir() }) {
            if (userTemplates.isDirectory()) {
                final File fullPath = new File(userTemplates, filePath);
                if (fullPath.exists() && !fullPath.isDirectory()) {
                    return fullPath;
                }
            }
        }
        return new File(defaultUserTemplateDir, filePath);
    }
    
    URI normalizeTemplateLocation(URI locationUri) {
        if(! "file".equals(locationUri.getScheme()))
            return locationUri;
        String location = locationUri.toString();
        String userTemplateLocation = defaultUserTemplateDir().toURI().toString();
        String normalizedLocation;
        if(location.startsWith(userTemplateLocation)) {
            normalizedLocation = "template:/" + location.substring(userTemplateLocation.length());
        } else {
            String standardTemplateLocation = defaultStandardTemplateDir().toURI().toString();
            if(location.startsWith(standardTemplateLocation)) {
                normalizedLocation = "template:/" + location.substring(standardTemplateLocation.length());
            } 
            else
                return locationUri;
        }
        try {
            return new URI(normalizedLocation);
        } catch (URISyntaxException e) {
            LogUtils.severe(e);
            return locationUri;
        }
    }
    
    URI expandTemplateLocation (URI locationUri) {
        if(! "template".equals(locationUri.getScheme()))
            return locationUri;
        String path = locationUri.getPath().substring(1);
        return existingTemplateFile(path).toURI();
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