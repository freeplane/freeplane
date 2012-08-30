package org.docear.plugin.pdfutilities.pdf;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.TreeMap;

import de.intarsys.pdf.content.ICSInterpreter;
import de.intarsys.pdf.content.text.CSCharacterParser;
import de.intarsys.pdf.cos.COSName;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontType3;
import de.intarsys.pdf.font.PDGlyphs;

public class CSFormatedTextExtractor extends CSCharacterParser {
	TreeMap<PdfTextEntity, StringBuilder> map = new TreeMap<PdfTextEntity, StringBuilder>();
	StringBuilder current;
	
	private double maxDX = 5;
	private double maxDY = 5;
	private int line = 0;

	
	public CSFormatedTextExtractor() {
		super();
	}

	private void append(char[] chars) {
		if(current == null) {
			return;
		}
		for (char c : chars) {
			try {
				if(current.length() > 0 && current.charAt(current.length()-1) <= 32) {
					while(current.length() > 0 && current.charAt(current.length()-1) <= 32) {
						current.deleteCharAt(current.length()-1);
					}
					current.append(" ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(c >= 32 ) {
				current.append(c);
			}
		}
	}

	private void append(String s) {
		if(current == null) {
			return;
		}		
		if(current.length() > 0 && current.charAt(current.length()-1) <= 32) {
			while(current.length() > 0 && current.charAt(current.length()-1) <= 32) {
				current.deleteCharAt(current.length()-1);
			}
			current.append(" ");
		}	
		current.append(s);
	}

	public TreeMap<PdfTextEntity, StringBuilder> getMap() {
		return map;
	}

	@Override
	protected void onCharacterFound(PDGlyphs glyphs, Rectangle2D rect) {
		char[] chars = glyphs.getChars();
		if (chars == null) {
			chars = new char[] { ' ' };
		}
		
		if(glyphs.getFont() instanceof PDFontType3) {
			current.length();
		}
		
		double dX = Math.abs(lastStopX - lastStartX);
		double dY = Math.abs(lastStopY - lastStartY);		
		if (dX < maxDX) {
			if (dY > maxDY && current != null && current.length() > 0) {
				append(" ");
				line+= Math.round(dY/rect.getHeight());
				
			}
		} else {
			if (current != null && current.length() > 0) {
				if (dY < maxDY) {
					append(" ");
				} else {
					append(" ");
					line+= Math.round(dY/rect.getHeight());
				}
			}
		}
		if(rect.getWidth() <= 0) {
			return;
		}
		PdfTextEntity entity = new PdfTextEntity(glyphs.getFont(), rect.getHeight(), line);
		current = map.get(entity);
		if(current == null) {
			current = new StringBuilder();
			map.put(entity, current);
		}
		append(chars);
	}

	@Override
	public void open(ICSInterpreter pInterpreter) {
		super.open(pInterpreter);
		map = new TreeMap<PdfTextEntity, StringBuilder>();
		line = 0;
	}

	@Override
	public void textSetFont(COSName name, PDFont font, float size) {
		super.textSetFont(name, font, size);
		AffineTransform tx;
		tx = (AffineTransform) getDeviceTransform().clone();
		tx.concatenate(textState.globalTransform);
		maxDX = textState.fontSize * 0.2 * tx.getScaleX();
		maxDY = textState.fontSize * 0.6 * tx.getScaleY();
	
//		if(font instanceof PDFontType3) {
//			//current = null;
//		//	return;
//		}
//		PdfTextEntity entity = new PdfTextEntity(font, size, line);
//		current = map.get(entity);
//		if(current == null) {
//			current = new StringBuilder();
//			map.put(entity, current);
//		}
	}

	@Override
	public void textSetTransform(float a, float b, float c, float d, float e, float f) {
		super.textSetTransform(a, b, c, d, e, f);
		AffineTransform tx;
		tx = (AffineTransform) getDeviceTransform().clone();
		tx.concatenate(textState.globalTransform);
		maxDX = textState.fontSize * 0.2 * tx.getScaleX();
		maxDY = textState.fontSize * 0.6 * tx.getScaleY();
	}

}
