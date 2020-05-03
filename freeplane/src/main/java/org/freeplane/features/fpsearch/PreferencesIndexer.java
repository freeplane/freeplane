package org.freeplane.features.fpsearch;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanel;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

class PreferencesIndexer
{
    private final ResourceController resourceController = ResourceController.getResourceController();
    private String currentTab;
    private String currentSeparator;
    private int tagsOpenendForCurrentPrefDeclaration = 0;

    private List<PreferencesItem> prefs;

    PreferencesIndexer()
    {
        load();
    }

    List<PreferencesItem> getPrefs()
    {
        return prefs;
    }

    private void load() {
        URL preferences = resourceController.getResource("/xml/preferences.xml");

        InputStreamReader reader = null;
        try
        {
            prefs = new LinkedList<>();

            reader = new InputStreamReader(preferences.openStream(), StandardCharsets.UTF_8);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement())
                {
                    startElement(event.asStartElement());
                }
                else if (event.isEndElement())
                {
                    endElement(event.asEndElement());
                }
            }

        }
        catch (final javax.xml.stream.XMLStreamException|IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            FileUtils.silentlyClose(reader);
        }
    }

    private void startElement(final StartElement startElement)
    {
        final String localPart = startElement.getName().getLocalPart();
        if (localPart.equals("tab"))
        {
            currentTab = startElement.getAttributeByName(new QName("name")).getValue();
        }
        else if (localPart.equals("separator"))
        {
            currentSeparator = startElement.getAttributeByName(new QName("name")).getValue();
        }
        else if (currentTab != null && currentSeparator != null && tagsOpenendForCurrentPrefDeclaration == 0)
        {
            // preference item of any type
            String prefType = startElement.getName().toString();
            Attribute name = startElement.getAttributeByName(new QName("name"));
            String prefKey = name.getValue();
            String prefText = TextUtils.getText(OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + prefKey);
            prefs.add(new PreferencesItem(currentTab, currentSeparator, prefKey, prefText));
            //System.out.format("tagsOpenendForCurrentPrefDeclaration=%d, prefKey=%s -> %s\n",
            //        tagsOpenendForCurrentPrefDeclaration, prefKey, prefText);

            tagsOpenendForCurrentPrefDeclaration = 1;
        }
        else if (tagsOpenendForCurrentPrefDeclaration > 0)
        {
            tagsOpenendForCurrentPrefDeclaration++;
        }
    }

    private void endElement(final EndElement endElement)
    {
        final String localPart = endElement.getName().getLocalPart();
        if (localPart.equals("tab"))
        {
            currentTab = null;
        }
        else if (localPart.equals("separator"))
        {
            currentSeparator = null;
        }
        else if (tagsOpenendForCurrentPrefDeclaration >= 1)
        {
            tagsOpenendForCurrentPrefDeclaration--;
        }
    }
}
