package org.freeplane.features.url.mindmapmode;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class TemplateManager {
    public static final String TEMPLATE_SCHEME = "template";
    public static final TemplateManager INSTANCE = new TemplateManager();


    public File existingTemplateFile(final String filePath) {
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


    public File writeableTemplateFile(final String location) {
        if(location == null)
            return null;
        try {
            URI uri = new URI(location);
            if ("file".equals(uri.getScheme())) {
                    return Paths.get(uri).toFile();
            }
            else if (TEMPLATE_SCHEME.equals(uri.getScheme())){
                return new File(defaultUserTemplateDir(), uri.getPath().substring(1));
            }
        } catch (URISyntaxException e) {
            LogUtils.severe(e);
        }
        return null;
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
    
    public URI normalizeTemplateLocation(URI locationUri) {
        if(! "file".equals(locationUri.getScheme()))
            return locationUri;
        String location = locationUri.toString();
        String userTemplateLocation = defaultUserTemplateDir().toURI().toString();
        String normalizedLocation;
        if(location.startsWith(userTemplateLocation)) {
            normalizedLocation = TEMPLATE_SCHEME + ":/" + location.substring(userTemplateLocation.length());
        } else {
            String standardTemplateLocation = defaultStandardTemplateDir().toURI().toString();
            if(location.startsWith(standardTemplateLocation)) {
                normalizedLocation = TEMPLATE_SCHEME + ":/" + location.substring(standardTemplateLocation.length());
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

    public URI expandExistingTemplateLocation(String location) {
        try {
            return location == null ? null : expandExistingTemplateLocation(new URI(location));
        } catch (URISyntaxException e) {
            LogUtils.severe(e);
            return null;
        }
    }

    public URI expandExistingTemplateLocation (URI locationUri) {
        if(locationUri == null || ! TEMPLATE_SCHEME.equals(locationUri.getScheme()))
            return locationUri;
        String path = locationUri.getPath().substring(1);
        return existingTemplateFile(path).toURI();
    }
    
    public URI expandTemplateLocation (URI locationUri) {
        if(! TEMPLATE_SCHEME.equals(locationUri.getScheme()))
            return locationUri;
        String path = locationUri.getPath().substring(1);
        return templateFile(path).toURI();
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
    
    public  String describeNormalizedLocation(URI location) {
        String message;
        if (TemplateManager.TEMPLATE_SCHEME.equals(location.getScheme())) {
            message = location.getPath().substring(1);
        } else {
            String followedMapPath = "file".equalsIgnoreCase(location.getScheme()) 
                    ? Paths.get(location).toFile().getAbsolutePath() : location.toString();
            message = followedMapPath;
        }
        return message;
    }


    public String describeNormalizedLocation(String location) {
        if(location == null)
            return TextUtils.getText("no_template_associated");
        try {
            return describeNormalizedLocation(new URI(location));
        } catch (URISyntaxException e) {
            LogUtils.severe(e);
            return TextUtils.getText("no_template_associated");
        }
    }

}