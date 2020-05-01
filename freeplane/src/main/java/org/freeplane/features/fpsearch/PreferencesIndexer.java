package org.freeplane.features.fpsearch;

import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PreferencesIndexer {

    private ReadManager parseManager;
    private TreeXmlReader preferencesXmlReader;

    public PreferencesIndexer() {

        parseManager = new ReadManager();
        preferencesXmlReader = new TreeXmlReader(parseManager);
        parseManager.addElementHandler("tab", new IElementHandler() {
            @Override
            public Object createElement(Object parent, String tag, XMLElement attributes)
            {
                LogUtils.severe("found tag=" + tag);
                return null;
            }
        });

        load();
    }

    public void load() {
        final ResourceController resourceController = ResourceController.getResourceController();
        URL preferences = resourceController.getResource("/xml/preferences.xml");

        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new BufferedInputStream(preferences.openStream()));
            preferencesXmlReader.load(reader);
        }
        catch (final XMLException e) {
            throw new RuntimeException(e);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            FileUtils.silentlyClose(reader);
        }
    }

}
