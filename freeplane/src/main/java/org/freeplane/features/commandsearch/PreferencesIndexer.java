/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2020 Felix Natter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.commandsearch;

import static org.freeplane.features.commandsearch.SearchItem.ITEM_PATH_SEPARATOR;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanel;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;

class PreferencesIndexer
{
    private final ResourceController resourceController = ResourceController.getResourceController();
    private final String OPTIONPANEL_SEPARATOR_RESOURCE_PREFIX = "OptionPanel.separator.";
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
        prefs = new LinkedList<>();
        URL preferences = resourceController.getResource("/xml/preferences.xml");
        try(InputStreamReader reader = new InputStreamReader(preferences.openStream(), StandardCharsets.UTF_8))
        {
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
            String textKey;
            Attribute textAttribute = startElement.getAttributeByName(new QName("text"));
            if (textAttribute != null)
            {
                textKey = textAttribute.getValue();
            }
            else
            {
                textKey = OptionPanel.OPTION_PANEL_RESOURCE_PREFIX  + prefKey;
            }
            String prefText = TextUtils.getRawText(textKey,"[" + textKey + "]");
            if (prefText != null)
            {
                prefText = HtmlUtils.htmlToPlain(prefText);
            }
            String tooltipText = TextUtils.getRawText(OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + prefKey + ".tooltip", null);
            String currentTabTranslated = TextUtils.getRawText(OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + currentTab, null);
            String currentSeparatorTranslated = TextUtils.getRawText(OPTIONPANEL_SEPARATOR_RESOURCE_PREFIX + currentSeparator, null);
            String prefPath = currentSeparatorTranslated + ITEM_PATH_SEPARATOR + prefText;

            prefs.add(new PreferencesItem(currentTabTranslated, currentSeparatorTranslated, prefKey, prefText, prefPath, tooltipText));
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
