package org.docear.plugin.backup;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class Tools {
	public static byte[] zip(String mindmap) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			BufferedOutputStream bufos = null;
			bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
			bufos.write(mindmap.getBytes());
			bufos.close();
			byte[] retval = bos.toByteArray();
			bos.close();
			return retval;
		} catch (IOException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		return null;
	}
}
