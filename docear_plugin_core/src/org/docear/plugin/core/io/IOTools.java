package org.docear.plugin.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class IOTools {

	public static final String getStringFromStream(InputStream is, String charsetName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, charsetName));
		StringBuilder sb = new StringBuilder();
	
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + System.getProperty("line.separator"));
		}
	
		br.close();
		return sb.toString();
	}
}
