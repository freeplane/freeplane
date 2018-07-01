package org.freeplane.plugin.script;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.junit.Test;

public class CompiledFilesSpec {

	@SuppressWarnings("serial")
	private File file(String name, final long lastModified) {
		return new File(name) {
			@Override
			public long lastModified() {
				return lastModified;
			}
			
		};
	}
	
	@Test
	public void filtersEmptySet() throws Exception {
		final CompiledFiles uut = new CompiledFiles(0, Collections.<String>emptySet());
		Collection<File> files = Collections.emptySet();
		assertThat(uut.filterNewAndNewer(files)).containsExactly();
	}
	
	
	@Test
	public void filteringRemovesOlderFile() throws Exception {
		final CompiledFiles uut = new CompiledFiles(2, Collections.singleton("file"));
		final File file = file("file", 1);
		Collection<File> files = Collections.singleton(file);
		assertThat(uut.filterNewAndNewer(files)).containsExactly();
	}

	
	@Test
	public void filteringKeepsNewFile() throws Exception {
		final CompiledFiles uut = new CompiledFiles(2, Collections.singleton("file"));
		final File newfile = file("newFile", 1);
		Collection<File> files = Collections.singleton(newfile);
		assertThat(uut.filterNewAndNewer(files)).containsExactly(newfile);
	}
	
	@Test
	public void savesToXml() throws Exception {
		final CompiledFiles uut = new CompiledFiles(2, Collections.singleton("file"));
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        // create XMLEventWriter
        final StringWriter stream = new StringWriter();
		XMLEventWriter eventWriter = outputFactory
                .createXMLEventWriter(stream);
		uut.writeThrowExceptions(eventWriter);
		
		String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?><compiledFiles compilationTime=\"2\">\n" + 
				"	<file>file</file>\n" + 
				"</compiledFiles>\n";
		assertThat(stream.toString()).isEqualTo(expected);

	}
	
	@Test
	public void loadsFromXml() throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><compiledFiles compilationTime=\"2\">\n" + 
				"	<file>file</file>\n" + 
				"</compiledFiles>\n";
        // Setup a new eventReader

        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(xml));
        
        CompiledFiles uut = CompiledFiles.readThrowExceptions(eventReader);
		
        final CompiledFiles expected = new CompiledFiles(2, Collections.singleton("file"));
		assertThat(uut).isEqualToComparingFieldByField(expected);

	}
	
	@Test
	public void createsFromCollection() throws Exception {
        final CompiledFiles compiledFiles = new CompiledFiles((long) 2);
		compiledFiles.addAll(Collections.singleton(new File("file")));
		CompiledFiles uut = compiledFiles;
		
        final CompiledFiles expected = new CompiledFiles(2, Collections.singleton(new File("file").getAbsolutePath()));
		assertThat(uut).isEqualToComparingFieldByField(expected);

	}
}
