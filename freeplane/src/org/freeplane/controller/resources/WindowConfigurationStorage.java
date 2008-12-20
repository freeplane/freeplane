package org.freeplane.controller.resources;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.IXMLParser;
import org.freeplane.io.xml.n3.nanoxml.IXMLReader;
import org.freeplane.io.xml.n3.nanoxml.StdXMLParser;
import org.freeplane.io.xml.n3.nanoxml.StdXMLReader;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLException;
import org.freeplane.io.xml.n3.nanoxml.XMLParserFactory;
import org.freeplane.io.xml.n3.nanoxml.XMLWriter;

abstract public class WindowConfigurationStorage{
  protected int x;

  protected int y;

  protected int width;

  protected int height;


  public int getX() {
    return this.x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return this.y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return this.width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return this.height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  protected IXMLElement unmarschall(final String marshalled,
                                    final JDialog dialog) {
	  if (marshalled != null) {
		  IXMLParser parser =  XMLParserFactory.createDefaultXMLParser();
		  IXMLReader xmlReader = new StdXMLReader(new StringReader(marshalled));
		  parser.setReader(xmlReader);
		  try {
	        IXMLElement storage = (IXMLElement) parser.parse();
	          if (storage != null) {
	        	  x = Integer.parseInt(storage.getAttribute("x", null));
	        	  y = Integer.parseInt(storage.getAttribute("y", null));
	        	  width = Integer.parseInt(storage.getAttribute("width", null));
	        	  height = Integer.parseInt(storage.getAttribute("height", null));
	        	  dialog.setLocation(x, y);		
	        	  dialog.setSize(new Dimension(width, height));
	        	  return storage;
	          }
        }
        catch (NumberFormatException e) {
	        e.printStackTrace();
        }
        catch (XMLException e) {
	        e.printStackTrace();
        }
	  }
	  final Frame rootFrame = JOptionPane.getFrameForComponent(dialog);
	  final Dimension prefSize = rootFrame.getSize();
	  prefSize.width = prefSize.width * 3 / 4;
	  prefSize.height = prefSize.height * 3 / 4;
	  dialog.setSize(prefSize);
	  return null;
  }
	
	private String marshall() {
		IXMLElement xml = new XMLElement();
		xml.setAttribute("x", Integer.toString(x));
		xml.setAttribute("y", Integer.toString(y));
		xml.setAttribute("width", Integer.toString(width));
		xml.setAttribute("height", Integer.toString(height));
		marschallSpecificElements(xml);
		StringWriter string = new StringWriter();
		XMLWriter writer = new XMLWriter(string);
		try {
	        writer.write(xml);
	        return string.toString();
        }
        catch (IOException e) {
	        e.printStackTrace();
	        return null;
        }
    }

	abstract protected void marschallSpecificElements(IXMLElement xml);

	public void storeDialogPositions(final JDialog dialog,
	                                 final String window_preference_storage_property) {
		setX((dialog.getX()));
		setY((dialog.getY()));
		setWidth((dialog.getWidth()));
		setHeight((dialog.getHeight()));
		final String marshalled = marshall();
		Controller.getResourceController().setProperty(
		    window_preference_storage_property, marshalled);
	}

}
