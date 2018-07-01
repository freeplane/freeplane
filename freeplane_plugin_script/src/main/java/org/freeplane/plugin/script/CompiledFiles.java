package org.freeplane.plugin.script;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.freeplane.core.util.LogUtils;


class CompiledFiles {
	private static final String FILE = "file";
	private static final String COMPILED_FILES = "compiledFiles";
	private static final String UTF_8 = "utf-8";
	private static final String COMPILATION_TIME = "compilationTime";	
	private final Set<String> filePaths;
	private final long compilationTime;
	
	void addAll(Collection<File> files) {
		for(File file : files) {
			filePaths.add(file.getAbsolutePath());
		}
	}
	
	CompiledFiles(long compilationTime, Set<String> filePaths) {
		super();
		this.compilationTime = compilationTime;
		this.filePaths = filePaths;
	}

	CompiledFiles(long compilationTime) {
		this.compilationTime = compilationTime;
		this.filePaths = new HashSet<>();
	}
	
	Collection<File> filterNewAndNewer(Collection<File> files) {
		ArrayList<File> filteredFiles = new ArrayList<>();
		for(File file : files) {
			if(! filePaths.contains(file.getAbsolutePath()) || file.lastModified() >= compilationTime)
				filteredFiles.add(file);
		}
		return filteredFiles;
	}

	public void write(File output) {
		try {
			writeThrowExceptions(output);
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
	}

	private void writeThrowExceptions(File output)
			throws FactoryConfigurationError, XMLStreamException, IOException, FileNotFoundException {
		try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output))) {
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLEventWriter eventWriter = outputFactory
					.createXMLEventWriter(out);
			writeThrowExceptions(eventWriter);
		}
	}
	
	void writeThrowExceptions(XMLEventWriter writer) throws FactoryConfigurationError, XMLStreamException {
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent eol = eventFactory.createDTD("\n");
        // create and write Start Tag
        StartDocument startDocument = eventFactory.createStartDocument(UTF_8);
        writer.add(startDocument);

        // create config open tag
        StartElement configStartElement = eventFactory.createStartElement("",
                "", COMPILED_FILES);
        writer.add(configStartElement);
        final Attribute compilationTimeAttribute = eventFactory.createAttribute(COMPILATION_TIME, Long.toString(compilationTime));
        writer.add(compilationTimeAttribute);
        writer.add(eol);
        for (String filePath : filePaths) {
        	createNode(writer, FILE, filePath);
        }

        writer.add(eventFactory.createEndElement("", "", COMPILED_FILES));
        writer.add(eol);
        writer.add(eventFactory.createEndDocument());
        writer.close();
	}

	private void createNode(XMLEventWriter writer, String name,
	                        String value) throws XMLStreamException {

		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent eol = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");
		// create Start node
		StartElement sElement = eventFactory.createStartElement("", "", name);
		writer.add(tab);
		writer.add(sElement);
		// create Content
		Characters characters = eventFactory.createCharacters(value);
		writer.add(characters);
		// create End node
		EndElement eElement = eventFactory.createEndElement("", "", name);
		writer.add(eElement);
		writer.add(eol);

	}

	static CompiledFiles readThrowExceptions(XMLEventReader eventReader) throws XMLStreamException {
		long compilationTime = 0;
		Set<String> files = new HashSet<>();
		
		while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                // If we have an item element, we create a new item
                if (startElement.getName().getLocalPart().equals(COMPILED_FILES)) {
                    // We read the attributes from this tag and add the date
                    // attribute to our object
                    Iterator<Attribute> attributes = startElement
                            .getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals(COMPILATION_TIME)) {
                        	final String value = attribute.getValue();
							compilationTime = Long.parseLong(value);
                        }

                    }
                }

                if (event.isStartElement()) {
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(FILE)) {
                        event = eventReader.nextEvent();
                        files.add(event.asCharacters().getData());
                    }
                }
            }
		}
		
		return new CompiledFiles(compilationTime, files);
	}

	public static CompiledFiles read(File input) {
		try {
			return readThrowExceptions(input);
		}
		catch (FileNotFoundException e) {
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
		return new CompiledFiles(0, Collections.<String>emptySet());
	}


	public static CompiledFiles readThrowExceptions(File input) throws XMLStreamException, IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(input))) {
        	XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        	XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
        	return readThrowExceptions(eventReader);
        }
	}

}
