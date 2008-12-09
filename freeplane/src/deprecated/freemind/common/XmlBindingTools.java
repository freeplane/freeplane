/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package deprecated.freemind.common;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import freemind.controller.actions.generated.instance.WindowConfigurationStorage;
import freemind.controller.actions.generated.instance.XmlAction;

/**
 * @author foltin Singleton
 */
public class XmlBindingTools {
	private static XmlBindingTools instance;
	private static IBindingFactory mBindingFactory;

	public static XmlBindingTools getInstance() {
		if (XmlBindingTools.instance == null) {
			XmlBindingTools.instance = new XmlBindingTools();
			try {
				XmlBindingTools.mBindingFactory = BindingDirectory
				    .getFactory(XmlAction.class);
			}
			catch (final JiBXException e) {
				org.freeplane.main.Tools.logException(e);
			}
		}
		return XmlBindingTools.instance;
	}

	private XmlBindingTools() {
	}

	public IMarshallingContext createMarshaller() {
		try {
			return XmlBindingTools.mBindingFactory.createMarshallingContext();
		}
		catch (final JiBXException e) {
			org.freeplane.main.Tools.logException(e);
			return null;
		}
	}

	public IUnmarshallingContext createUnmarshaller() {
		try {
			return XmlBindingTools.mBindingFactory.createUnmarshallingContext();
		}
		catch (final JiBXException e) {
			org.freeplane.main.Tools.logException(e);
			return null;
		}
	}

	public WindowConfigurationStorage decorateDialog(
	                                                 final JDialog dialog,
	                                                 final String window_preference_storage_property) {
		final String marshalled = Controller.getResourceController()
		    .getProperty(window_preference_storage_property);
		final WindowConfigurationStorage result = decorateDialog(marshalled,
		    dialog);
		return result;
	}

	public WindowConfigurationStorage decorateDialog(final String marshalled,
	                                                 final JDialog dialog) {
		if (marshalled != null) {
			final WindowConfigurationStorage storage = (WindowConfigurationStorage) unMarshall(marshalled);
			if (storage != null) {
				dialog.setLocation(storage.getX(), storage.getY());
				dialog.setSize(new Dimension(storage.getWidth(), storage
				    .getHeight()));
				return storage;
			}
		}
		final Frame rootFrame = JOptionPane.getFrameForComponent(dialog);
		final Dimension prefSize = rootFrame.getSize();
		prefSize.width = prefSize.width * 3 / 4;
		prefSize.height = prefSize.height * 3 / 4;
		dialog.setSize(prefSize);
		return null;
	}

	public String marshall(final XmlAction action) {
		final StringWriter writer = new StringWriter();
		final IMarshallingContext m = XmlBindingTools.getInstance()
		    .createMarshaller();
		try {
			m.marshalDocument(action, "UTF-8", null, writer);
		}
		catch (final JiBXException e) {
			org.freeplane.main.Tools.logException(e);
			return null;
		}
		final String result = writer.toString();
		return result;
	}

	public void storeDialogPositions(
	                                 final JDialog dialog,
	                                 final WindowConfigurationStorage storage,
	                                 final String window_preference_storage_property) {
		storage.setX((dialog.getX()));
		storage.setY((dialog.getY()));
		storage.setWidth((dialog.getWidth()));
		storage.setHeight((dialog.getHeight()));
		final String marshalled = marshall(storage);
		final String result = marshalled;
		Controller.getResourceController().setProperty(
		    window_preference_storage_property, result);
	}

	/**
	 */
	public XmlAction unMarshall(final Reader reader) {
		try {
			final IUnmarshallingContext u = XmlBindingTools.getInstance()
			    .createUnmarshaller();
			final XmlAction doAction = (XmlAction) u.unmarshalDocument(reader,
			    null);
			return doAction;
		}
		catch (final JiBXException e) {
			org.freeplane.main.Tools.logException(e);
			return null;
		}
	}

	public XmlAction unMarshall(final String inputString) {
		return unMarshall(new StringReader(inputString));
	}
}
