package org.freeplane.features.url;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

//DOCEAR
public interface IMapInputStreamConverter {
	public Reader getConvertedStream(File f) throws FileNotFoundException, IOException;

}
