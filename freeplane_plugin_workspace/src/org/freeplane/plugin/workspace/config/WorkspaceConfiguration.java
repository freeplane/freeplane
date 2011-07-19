package org.freeplane.plugin.workspace.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.config.node.EmptyCreator;
import org.freeplane.plugin.workspace.config.node.GroupNodeCreator;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	
	public WorkspaceConfiguration(URL xmlFile) {
		readManager = new ReadManager();
	}
	
	private void initReadManager() {
		readManager.addElementHandler("workspace_structure", new EmptyCreator());
		readManager.addElementHandler("group", new GroupNodeCreator());
//		readManager.addElementHandler("tab", new TabCreator());
//		readManager.addElementHandler("separator", new SeparatorCreator());
//		readManager.addElementHandler("text", new TextCreator());
//		readManager.addElementHandler("string", new StringOptionCreator());
//		readManager.addElementHandler("font", new FontOptionCreator());
//		readManager.addElementHandler("boolean", new BooleanOptionCreator());
//		readManager.addElementHandler("number", new NumberOptionCreator());
//		readManager.addElementHandler("path", new PathOptionCreator());
//		readManager.addElementHandler("color", new ColorOptionCreator());
//		readManager.addElementHandler("combo", new ComboOptionCreator());
//		readManager.addElementHandler("languages", new LanguagesComboCreator());
//		readManager.addElementHandler("key", new KeyOptionCreator());
//		readManager.addElementHandler("remind_value", new RemindValueCreator());
//		readManager.addElementHandler("action", new ActionCreator());
//		readManager.addElementHandler("password", new PasswordOptionCreator());
//		readManager.addElementHandler("radiobutton", new RadioButtonCreator());
	}
	
	public void load(final URL menu) {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(menu.openStream())));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}
}
