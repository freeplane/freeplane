package org.docear.plugin.communications;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
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
	
	public static String getMindMapID(File file) {
        try {
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String string;
			while ((string = reader.readLine()) != null) {
				buffer.append(string);
            }
            int startIndex = buffer.indexOf("mapID=\"");
            if(startIndex != -1){
                int endIndex = buffer.indexOf("\"", startIndex + 7);
                if(endIndex != -1){
                    return buffer.substring(startIndex + 7, endIndex);
                }
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }
}
