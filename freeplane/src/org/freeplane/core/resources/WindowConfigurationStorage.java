package org.freeplane.core.resources;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.io.XMLElement;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

abstract public class WindowConfigurationStorage {
	protected int height;
	protected int width;
	protected int x;
	protected int y;

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	abstract protected void marschallSpecificElements(IXMLElement xml);

	private String marshall() {
		final IXMLElement xml = new XMLElement();
		xml.setAttribute("x", Integer.toString(x));
		xml.setAttribute("y", Integer.toString(y));
		xml.setAttribute("width", Integer.toString(width));
		xml.setAttribute("height", Integer.toString(height));
		marschallSpecificElements(xml);
		final StringWriter string = new StringWriter();
		final XMLWriter writer = new XMLWriter(string);
		try {
			writer.write(xml);
			return string.toString();
		}
		catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void setX(final int x) {
		this.x = x;
	}

	public void setY(final int y) {
		this.y = y;
	}

	public void storeDialogPositions(final JDialog dialog,
	                                 final String window_preference_storage_property) {
		setX((dialog.getX()));
		setY((dialog.getY()));
		setWidth((dialog.getWidth()));
		setHeight((dialog.getHeight()));
		final String marshalled = marshall();
		Controller.getResourceController().setProperty(window_preference_storage_property,
		    marshalled);
	}

	protected IXMLElement unmarschall(final String marshalled, final JDialog dialog) {
		if (marshalled != null) {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(new StringReader(marshalled));
			parser.setReader(xmlReader);
			try {
				final IXMLElement storage = (IXMLElement) parser.parse();
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
			catch (final NumberFormatException e) {
				e.printStackTrace();
			}
			catch (final XMLException e) {
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
}
