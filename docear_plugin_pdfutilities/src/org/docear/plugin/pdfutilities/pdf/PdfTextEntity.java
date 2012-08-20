package org.docear.plugin.pdfutilities.pdf;

import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontStyle;

public class PdfTextEntity implements Comparable<PdfTextEntity>{
	private final PDFontStyle fontStyle;
	public final double fontSize;
	public final int line;
	
	public PdfTextEntity(PDFont style, double size, int line) {
		this.fontSize = size;
		this.fontStyle = style.getFontStyle();
		this.line = line;
	}

	public int compareTo(PdfTextEntity entity) {
		if(this.fontSize > entity.fontSize) {
			return -1;
		}
		else if(fontSize < entity.fontSize){
			return 1;
		}
		else if(fontStyle.getLabel().contains("Bold") && !entity.fontStyle.getLabel().contains("Bold")) {
			return -1;
		}
		else if(!fontStyle.getLabel().contains("Bold") && entity.fontStyle.getLabel().contains("Bold")) {
			return 1;
		}
		else if(fontStyle.getLabel().contains("BoldItalic") && !entity.fontStyle.getLabel().contains("BoldItalic")) {
			return -1;
		}
		else if(!fontStyle.getLabel().contains("BoldItalic") && entity.fontStyle.getLabel().contains("BoldItalic")) {
			return 1;
		}
		else if(fontStyle.getLabel().contains("Italic") && !entity.fontStyle.getLabel().contains("Italic")) {
			return -1;
		}
		else if(!fontStyle.getLabel().contains("Italic") && entity.fontStyle.getLabel().contains("Italic")) {
			return 1;
		}
		else if(line < entity.line) {
			return -1;
		}
		else if((line-1) > (entity.line)) {
			return 1;
		}
		return 0;
	}
	
	public String toString() {
		return "["+Double.toString(fontSize)+":"+fontStyle.getLabel()+"]";
	}
}