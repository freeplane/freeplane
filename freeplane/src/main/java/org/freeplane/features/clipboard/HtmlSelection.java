package org.freeplane.features.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// http://stackoverflow.com/questions/30518705/copy-jtextarea-as-text-html-dataflavor
public class HtmlSelection implements Transferable {
	private static List<DataFlavor> htmlFlavors = new ArrayList<DataFlavor>(3);

	static {
		try {
			htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
			htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
			htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private String html;

	public HtmlSelection(String html) {
		this.html = html;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return htmlFlavors.contains(flavor);
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (String.class.equals(flavor.getRepresentationClass())) {
			return html;
		}
		else if (Reader.class.equals(flavor.getRepresentationClass())) {
			return new StringReader(html);
		}
		else if (InputStream.class.equals(flavor.getRepresentationClass())) {
			// Java 7: return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
			try {
				return new ByteArrayInputStream(html.getBytes("UTF-8"));
			}
			catch (Exception e) {
				// won't happen
			}
		}
		throw new UnsupportedFlavorException(flavor);
	}
}
