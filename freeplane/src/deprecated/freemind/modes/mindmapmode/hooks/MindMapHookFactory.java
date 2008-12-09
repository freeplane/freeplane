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
package deprecated.freemind.modes.mindmapmode.hooks;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.controller.Controller;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.FreemindMenuBar;
import org.jibx.runtime.IUnmarshallingContext;

import deprecated.freemind.common.XmlBindingTools;
import deprecated.freemind.extensions.HookDescriptorPluginAction;
import deprecated.freemind.extensions.HookDescriptorRegistration;
import deprecated.freemind.extensions.HookFactoryAdapter;
import deprecated.freemind.extensions.HookInstanciationMethod;
import deprecated.freemind.extensions.IMindMapHook;
import deprecated.freemind.extensions.IModeControllerHook;
import deprecated.freemind.extensions.INodeHook;
import deprecated.freemind.extensions.ImportWizard;
import deprecated.freemind.extensions.IMindMapHook.IPluginBaseClassSearcher;
import freemind.controller.actions.generated.instance.Plugin;
import freemind.controller.actions.generated.instance.PluginAction;
import freemind.controller.actions.generated.instance.PluginMode;
import freemind.controller.actions.generated.instance.PluginRegistration;

/**
 * @author christianfoltin
 * @file HookFactory.java
 * @package freemind.modes
 */
/**
 * @author foltin
 */
public class MindMapHookFactory extends HookFactoryAdapter {
	private static Vector allPlugins = null;
	/** Contains PluginRegistrationType -> PluginType relations. */
	protected static HashSet allRegistrations;
	private static ImportWizard importWizard = null;
	private static HashMap pluginInfo = null;
	/**
	 * Match xml files in the accessories/plugin directory and not in its
	 * subdirectories.
	 */
	final private static String pluginPrefixRegEx = ".*(accessories(/|\\\\)|)plugins(/|\\\\)[^/\\\\]*";

	/**
	 *
	 */
	public MindMapHookFactory() {
		allRegistrationInstances = new HashMap();
	}

	/**
	 *
	 */
	private void actualizePlugins() {
		if (MindMapHookFactory.importWizard == null) {
			MindMapHookFactory.importWizard = new ImportWizard();
			MindMapHookFactory.importWizard.CLASS_LIST.clear();
			MindMapHookFactory.importWizard.buildClassList();
			MindMapHookFactory.pluginInfo = new HashMap();
			MindMapHookFactory.allPlugins = new Vector();
			MindMapHookFactory.allRegistrations = new HashSet();
			final IUnmarshallingContext unmarshaller = XmlBindingTools
			    .getInstance().createUnmarshaller();
			for (final Iterator i = MindMapHookFactory.importWizard.CLASS_LIST
			    .iterator(); i.hasNext();) {
				String xmlPluginFile = (String) i.next();
				if (!xmlPluginFile.matches(".*build.*")
				        && xmlPluginFile
				            .matches(MindMapHookFactory.pluginPrefixRegEx)) {
					/*
					 * Here, this is not the File.separatorChar!!!
					 */
					xmlPluginFile = xmlPluginFile.replace('\\', '/')
					        + MindMapHookFactory.importWizard.lookFor;
					final URL pluginURL = Controller.getResourceController()
					    .getFreeMindClassLoader().getResource(xmlPluginFile);
					Plugin plugin = null;
					try {
						final InputStream in = pluginURL.openStream();
						plugin = (Plugin) unmarshaller.unmarshalDocument(in,
						    null);
					}
					catch (final Exception e) {
						org.freeplane.main.Tools.logException(e);
						continue;
					}
					for (final Iterator j = plugin.getListChoiceList()
					    .iterator(); j.hasNext();) {
						final Object obj = j.next();
						if (obj instanceof PluginAction) {
							final PluginAction action = (PluginAction) obj;
							MindMapHookFactory.pluginInfo.put(
							    action.getLabel(),
							    new HookDescriptorPluginAction(xmlPluginFile,
							        plugin, action));
							MindMapHookFactory.allPlugins
							    .add(action.getLabel());
						}
						else if (obj instanceof PluginRegistration) {
							final PluginRegistration registration = (PluginRegistration) obj;
							MindMapHookFactory.allRegistrations
							    .add(new HookDescriptorRegistration(
							        xmlPluginFile, plugin, registration));
						}
					}
				}
			}
		}
	}

	public IModeControllerHook createIModeControllerHook(final String hookName) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		return (IModeControllerHook) createJavaHook(hookName, descriptor);
	}

	private IMindMapHook createJavaHook(
	                                    final String hookName,
	                                    final HookDescriptorPluginAction descriptor) {
		try {
			final ClassLoader loader = descriptor.getPluginClassLoader();
			final Class hookClass = Class.forName(descriptor.getClassName(),
			    true, loader);
			final IMindMapHook hook = (IMindMapHook) hookClass.newInstance();
			decorateHook(hookName, descriptor, hook);
			return hook;
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e,
			    "Error occurred loading hook: " + descriptor.getClassName()
			            + "\nException:");
			return null;
		}
	}

	/**
	 * Do not call this method directly. Call ModeController.createNodeHook
	 * instead.
	 */
	public INodeHook createNodeHook(final String hookName) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		return (INodeHook) createJavaHook(hookName, descriptor);
	}

	/**
	 */
	public void decorateAction(final String hookName,
	                           final AbstractAction action) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		final String name = descriptor.getName();
		if (name != null) {
			FreemindMenuBar.setLabelAndMnemonic(action, name);
		}
		else {
			action.putValue(Action.NAME, descriptor.getClassName());
		}
		final String docu = descriptor.getDocumentation();
		if (docu != null) {
			action.putValue(Action.SHORT_DESCRIPTION, docu);
			action.putValue(Action.LONG_DESCRIPTION, docu);
		}
		final String icon = descriptor.getIconPath();
		if (icon != null) {
			final ImageIcon imageIcon = new ImageIcon(descriptor
			    .getPluginClassLoader().getResource(icon));
			action.putValue(Action.SMALL_ICON, imageIcon);
		}
		final String key = descriptor.getKeyStroke();
		if (key != null) {
			action
			    .putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
		}
	}

	private void decorateHook(final String hookName,
	                          final HookDescriptorPluginAction descriptor,
	                          final IMindMapHook hook) {
		hook.setProperties(descriptor.getProperties());
		hook.setName(hookName);
		final IPluginBaseClassSearcher pluginBaseClassSearcher = new IPluginBaseClassSearcher() {
			public Object getPluginBaseObject() {
				return getPluginBaseClass(descriptor);
			}
		};
		hook.setPluginBaseClass(pluginBaseClassSearcher);
	}

	/**
	 */
	private HookDescriptorPluginAction getHookDescriptor(final String hookName) {
		final HookDescriptorPluginAction descriptor = (HookDescriptorPluginAction) MindMapHookFactory.pluginInfo
		    .get(hookName);
		if (hookName == null || descriptor == null) {
			throw new IllegalArgumentException("Unknown hook name " + hookName);
		}
		return descriptor;
	}

	/**
	 * @return returns a list of menu position strings for the
	 *         StructuredMenuHolder.
	 */
	public List getHookMenuPositions(final String hookName) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		return descriptor.menuPositions;
	}

	/**
	 */
	public HookInstanciationMethod getInstanciationMethod(final String hookName) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		return descriptor.getInstanciationMethod();
	}

	public JMenuItem getMenuItem(final String pHookName,
	                             final AbstractAction pHookAction) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(pHookName);
		if (descriptor.isSelectable()) {
			return new JCheckBoxMenuItem(pHookAction);
		}
		else {
			return new JMenuItem(pHookAction);
		}
	}

	/**
	 */
	private Object getPluginBaseClass(
	                                  final HookDescriptorPluginAction descriptor) {
		Object baseClass = null;
		final String label = descriptor.getPluginBase().getLabel();
		if (allRegistrationInstances.containsKey(label)) {
			baseClass = allRegistrationInstances.get(label);
		}
		return baseClass;
	}

	/**
	 * A plugin base class is a common registration class of multiple plugins.
	 * It is useful to embrace several related plugins (example: EncryptedNote
	 * -> Registration).
	 *
	 * @return the base class if declared and successfully instanciated or NULL.
	 */
	public Object getPluginBaseClass(final String hookName) {
		final HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
		return getPluginBaseClass(descriptor);
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public Vector getPossibleIModeControllerHooks() {
		return searchFor(IModeControllerHook.class, MModeController.class);
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public Vector getPossibleNodeHooks() {
		return searchFor(INodeHook.class, MModeController.class);
	}

	/**
	 * Each Plugin can have a list of HookRegistrations that are called after
	 * the corresponding mode is enabled. (Like singletons.) One of these can
	 * operate as the pluginBase that is accessible to every normal
	 * plugin_action via the getPluginBaseClass method.
	 *
	 * @return A list of RegistrationContainer elements. The field
	 *         hookRegistrationClass of RegistrationContainer is a class that is
	 *         (probably) of HookRegistration type. You have to register every
	 *         registration via the registerRegistrationContainer method when
	 *         instanciated (this is typically done in the ModeController).
	 */
	public List getRegistrations() {
		final Class mode = MModeController.class;
		actualizePlugins();
		final Vector returnValue = new Vector();
		for (final Iterator i = MindMapHookFactory.allRegistrations.iterator(); i
		    .hasNext();) {
			final HookDescriptorRegistration descriptor = (HookDescriptorRegistration) i
			    .next();
			boolean modeFound = false;
			for (final Iterator j = (descriptor.getListPluginModeList())
			    .iterator(); j.hasNext();) {
				final PluginMode possibleMode = (PluginMode) j.next();
				if (mode.getPackage().getName().equals(
				    possibleMode.getClassName())) {
					modeFound = true;
				}
			}
			if (!modeFound) {
				continue;
			}
			try {
				final Plugin plugin = descriptor.getPluginBase();
				final ClassLoader loader = descriptor.getPluginClassLoader();
				final Class hookRegistrationClass = Class.forName(descriptor
				    .getClassName(), true, loader);
				final RegistrationContainer container = new RegistrationContainer();
				container.hookRegistrationClass = hookRegistrationClass;
				container.correspondingPlugin = plugin;
				container.isPluginBase = descriptor.getIsPluginBase();
				returnValue.add(container);
			}
			catch (final ClassNotFoundException e) {
				org.freeplane.main.Tools.logException(e);
			}
		}
		return returnValue;
	}

	/**
	 * @return a string vector with representatives for plugins.
	 */
	private Vector searchFor(final Class baseClass, final Class mode) {
		actualizePlugins();
		final Vector returnValue = new Vector();
		final String modeName = mode.getPackage().getName();
		for (final Iterator i = MindMapHookFactory.allPlugins.iterator(); i
		    .hasNext();) {
			final String label = (String) i.next();
			final HookDescriptorPluginAction descriptor = getHookDescriptor(label);
			try {
				if (baseClass.isAssignableFrom(Class.forName(descriptor
				    .getBaseClass()))) {
					for (final Iterator j = descriptor.getModes().iterator(); j
					    .hasNext();) {
						final String pmode = (String) j.next();
						if (pmode.equals(modeName)) {
							returnValue.add(label);
						}
					}
				}
			}
			catch (final ClassNotFoundException e) {
				Logger.global.severe("Class not found.");
				org.freeplane.main.Tools.logException(e);
			}
		}
		return returnValue;
	}
}
