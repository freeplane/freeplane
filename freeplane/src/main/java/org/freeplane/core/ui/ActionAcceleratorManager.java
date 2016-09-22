package org.freeplane.core.ui;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.GrabKeyDialog;
import org.freeplane.core.resources.components.IKeystrokeValidator;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.Pair;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.FreeplaneActions;
import org.freeplane.features.mode.ModeController;

public class ActionAcceleratorManager implements IKeyStrokeProcessor, IAcceleratorMap {

	<V> Pair<ModeController, V> key(V value) {
		return key(Controller.getCurrentModeController(), value);

	}

	<V> Pair<ModeController, V> key(ModeController modeController, V value) {
		return new Pair<ModeController, V>(modeController, value);

	}

	private static final String SHORTCUT_PROPERTY_PREFIX = "acceleratorFor.";

	private final Map<Pair<ModeController, KeyStroke>, AFreeplaneAction> accelerators = new HashMap<Pair<ModeController, KeyStroke>, AFreeplaneAction>();
	private final Map<Pair<ModeController, String>, KeyStroke> actionMap = new HashMap<Pair<ModeController, String>, KeyStroke>();
	private final Map<FreeplaneActions, Collection<IAcceleratorChangeListener>> changeListenersForActionCollection = new HashMap<FreeplaneActions, Collection<IAcceleratorChangeListener>>();

	private final Properties keysetProps;
	private final Properties defaultProps;
	private final Properties overwritttenDefaultProps;


	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

 	public ActionAcceleratorManager() {
 		overwritttenDefaultProps = new Properties();
 		loadDefaultAccelerators("/default_accelerators.properties");
 		if(Compat.isMacOsX() )
 			loadDefaultAccelerators("/default_accelerators_mac.properties");
 		defaultProps = new Properties();
		keysetProps = new Properties(defaultProps);
 	}

	private void loadDefaultAccelerators(String resource){
		try (final InputStream resourceStream = ResourceController.getResourceController().getResourceStream(resource)) {
			overwritttenDefaultProps.load(resourceStream);
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	public void loadAcceleratorPresets() {
	    try {
			if (ResourceController.getResourceController().getFreeplaneUserDirectory() != null) {
				final File defaultPresetsFile = getPresetsFile();
				if(defaultPresetsFile.exists()) {
					FileInputStream inputStream = null;
					try{
						inputStream = new FileInputStream(defaultPresetsFile);
						loadAcceleratorPresets(inputStream);
					}
					finally{
						FileUtils.silentlyClose(inputStream);
					}
				}
				else {
					updateAcceleratorsFromUserProperties();
				}
			}
		}
		catch (IOException ex) {
		}
    }

	private void updateAcceleratorsFromUserProperties() {
		final Properties properties = ResourceController.getResourceController().getProperties();
		Iterator<Entry<Object, Object>> propertyIterator = properties.entrySet().iterator();
		while (propertyIterator.hasNext()){
			Entry<Object, Object> property = propertyIterator.next();
			final String key = (String)property.getKey();
			final String oldPrefix = "acceleratorFor";
			if(key.startsWith(oldPrefix)){
				String newKey = SHORTCUT_PROPERTY_PREFIX +  key.substring(oldPrefix.length()).replaceFirst("\\$", "").replaceFirst("\\$\\d", "");
				String value = (String)property.getValue();
				loadAcceleratorPreset(newKey, value, new Properties());
				propertyIterator.remove();
			}
		}
		saveAcceleratorPresets();
	}

 	public void setAccelerator(final AFreeplaneAction action, final KeyStroke keyStroke) {
		setAccelerator(Controller.getCurrentModeController(), action, keyStroke);
	}

	private void setAccelerator(ModeController modeController, final AFreeplaneAction action, final KeyStroke keyStroke) {
 		if(action == null) {
 			return;
 		}
 		if(keyStroke != null) {
			final AFreeplaneAction oldAction = accelerators.put(key(modeController, keyStroke), action);
    		if(action == oldAction || (oldAction != null && action.getKey().equals(oldAction.getKey()))) {
    			return;
    		}
    		if (oldAction != null) {
    			if (acceleratorIsDefinedByUserProperties(oldAction, modeController, keysetProps)) {
    				accelerators.put(key(modeController, keyStroke), oldAction);
        			return;
    			}
    			else {
    				actionMap.remove(key(modeController, oldAction.getKey()));
    				fireAcceleratorChanged(modeController, oldAction, keyStroke, null);
    			}
    		}
 		}
		final KeyStroke removedAccelerator = removeAccelerator(modeController, action);
		final String actionKey = action.getKey();
		if(keyStroke != null) {
			actionMap.put(key(modeController, actionKey), keyStroke);
		}
		fireAcceleratorChanged(modeController, action, removedAccelerator, keyStroke);
	}

	@Override
	public void setUserDefinedAccelerator(AFreeplaneAction action) {
		final String actionKey = action.getKey();
		final String shortcutKey = getPropertyKey(actionKey);
		if(overwritttenDefaultProps.containsKey(shortcutKey))
			defaultProps.setProperty(shortcutKey, overwritttenDefaultProps.getProperty(shortcutKey));
		String accelerator = getShortcut(shortcutKey);
		if (accelerator != null){
			KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
			setAccelerator(action, ks);
		}
	}

	public void setDefaultAccelerator(final AFreeplaneAction action, String accelerator) {
		final String shortcutKey = getPropertyKey(action.getKey());
		if (null == getShortcut(shortcutKey)) {
			if(overwritttenDefaultProps.containsKey(shortcutKey))
				accelerator = overwritttenDefaultProps.getProperty(shortcutKey);
			accelerator = replaceModifiersForMac(accelerator);
			defaultProps.setProperty(shortcutKey, accelerator);
			KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
			setAccelerator(action, ks);
		}
	}

	@Override
	public void removeAction(FreeplaneActions freeplaneActions, AFreeplaneAction action) {
		final KeyStroke oldKeystroke = removeAccelerator(action);
		fireAcceleratorChanged(freeplaneActions, action, oldKeystroke, null);
	}


	public KeyStroke removeAccelerator(final AFreeplaneAction action) {
		return removeAccelerator(Controller.getCurrentModeController(), action);
	}

	private KeyStroke removeAccelerator(ModeController modeController, final AFreeplaneAction action)
	        throws AssertionError {
 		if(action == null) {
 			return null;
 		}
		final String actionKey = action.getKey();
		final KeyStroke oldAccelerator = actionMap.remove(key(modeController, actionKey));
		if (oldAccelerator != null) {
			final AFreeplaneAction oldAction = accelerators.remove(key(modeController, oldAccelerator));
			if (oldAction != null && !action.getKey().equals(oldAction.getKey())) {
				throw new AssertionError("unexpected action " + ActionUtils.getActionTitle(action) + " for accelerator " + oldAccelerator + "("+ActionUtils.getActionTitle(oldAction)+")");
			}
		}
		return oldAccelerator;
	}

 	public String getPropertyKey(final String key) {
		return getPropertyKey(Controller.getCurrentModeController(), key);
	}

	String getPropertyKey(final ModeController modeController, final String key) {
		return SHORTCUT_PROPERTY_PREFIX + modeController.getModeName() + "/" + key;
	}

 	public KeyStroke getAccelerator(AFreeplaneAction action) {
		final String actionKey = action.getKey();
		return getAccelerator(actionKey);
 	}

	public KeyStroke getAccelerator(final String actionKey) {
		KeyStroke ks = actionMap.get(key(actionKey));
 		return ks;
	}

	public void addAcceleratorChangeListener(FreeplaneActions freeplaneActions, IAcceleratorChangeListener changeListener) {
		synchronized (changeListenersForActionCollection) {
			Collection<IAcceleratorChangeListener> changeListeners = changeListenersForActionCollection.get(freeplaneActions);
			if (changeListeners == null) {
				changeListeners = new ArrayList<IAcceleratorChangeListener>();
				changeListenersForActionCollection.put(freeplaneActions, changeListeners);
			}
			if (!changeListeners.contains(changeListener)) {
				changeListeners.add(changeListener);
			}
		}
	}

	protected void fireAcceleratorChanged(FreeplaneActions freeplaneActions, AFreeplaneAction action, KeyStroke oldStroke,
	                                      KeyStroke newStroke) {
 		synchronized (changeListenersForActionCollection) {
			final Collection<IAcceleratorChangeListener> listeners = changeListenersForActionCollection.get(freeplaneActions);
			if(listeners != null) {
				for (IAcceleratorChangeListener listener : listeners) {
					listener.acceleratorChanged(action, oldStroke, newStroke);
				}
			}
		}
	}

 	private String getShortcut(String key) {
 		return keysetProps.getProperty(key,  null);
 	}

 	public void newAccelerator(final AFreeplaneAction action, final KeyStroke newAccelerator) {
		final String shortcutKey = getPropertyKey(action.getKey());
		final String oldShortcut;
		if(getAccelerator(action) != null)
			oldShortcut = getShortcut(shortcutKey);
		else
			oldShortcut = null;
		if (newAccelerator == null || !new KeystrokeValidator(action).isValid(newAccelerator, newAccelerator.getKeyChar())) {
			final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(oldShortcut);
			final IKeystrokeValidator validator = new KeystrokeValidator(action);
			grabKeyDialog.setValidator(validator);
			grabKeyDialog.setVisible(true);
			if (grabKeyDialog.isOK()) {
				final String shortcut = grabKeyDialog.getShortcut();
				final KeyStroke accelerator = UITools.getKeyStroke(shortcut);
				setAccelerator(action, accelerator);
				setKeysetProperty(shortcutKey, shortcut);
				LogUtils.info("created shortcut '" + shortcut + "' for action '" + action.getKey() + "', shortcutKey '"
				+ shortcutKey + "' (" + ActionUtils.getActionTitle(action) + ")");
			}
		}
		else{
			if(oldShortcut != null){
				final int replace = JOptionPane.showConfirmDialog(UITools.getCurrentRootComponent(), oldShortcut, TextUtils.removeTranslateComment(TextUtils.getText("remove_shortcut_question")), JOptionPane.YES_NO_OPTION);
				if (replace != JOptionPane.YES_OPTION) {
					return;
				}
			}
			setAccelerator(action, newAccelerator);
			setKeysetProperty(shortcutKey, toString(newAccelerator));
			LogUtils.info("created shortcut '" + toString(newAccelerator) + "' for action '" + action+ "', shortcutKey '" + shortcutKey + "' (" + ActionUtils.getActionTitle(action) + ")");
		}
		saveAcceleratorPresets();
	}

	public void saveAcceleratorPresets() {
		try {
			final FileOutputStream output = new FileOutputStream(getPresetsFile());
			storeAcceleratorPreset(output);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

 	public File getPresetsFile() {
 		File userDirectory = new File(ResourceController.getResourceController().getFreeplaneUserDirectory());
		if(!userDirectory.exists()) {
			userDirectory.mkdirs();
		}
		return new File(userDirectory, "accelerator.properties");
 	}

 	public void loadAcceleratorPresets(final InputStream in) {
		final Properties prop = new Properties();
		try {
			prop.load(in);
			for (final Entry<Object, Object> property : new ArrayList<Entry<Object, Object>>(prop.entrySet())) {
				String shortcutKey = (String) property.getKey();
				final String keystrokeString = (String) property.getValue();
				final String updatedShortcutKey = updateShortcutKey(shortcutKey);
				if(! updatedShortcutKey.equals(shortcutKey)) {
					prop.remove(shortcutKey);
					if (prop.get(updatedShortcutKey) == null)
						prop.setProperty(updatedShortcutKey, keystrokeString);
					else
						continue;
				}
				loadAcceleratorPreset(updatedShortcutKey, keystrokeString, prop);
			}
		}
		catch (final IOException e) {
			LogUtils.warn("shortcut presets not stored: "+e.getMessage());
		}
	}

 	final static Pattern oldKeyFormatPattern = Pattern.compile("\\$(.*?)\\$0$"); 
	String updateShortcutKey(final String shortcutKey) {
		String updatedShortcutKey = shortcutKey;
		final int dotPosition = "acceleratorFor".length();
		if(shortcutKey.length() > dotPosition && shortcutKey.charAt(dotPosition) != '.')
			updatedShortcutKey = "acceleratorFor." + shortcutKey.substring(dotPosition);
		if(updatedShortcutKey.endsWith("$0"))
			updatedShortcutKey = oldKeyFormatPattern.matcher(updatedShortcutKey).replaceFirst("$1");
		return updatedShortcutKey;
	}

 	private void loadAcceleratorPreset(final String shortcutKey, final String keystrokeString, Properties allPresets) {
 		if (!shortcutKey.startsWith(SHORTCUT_PROPERTY_PREFIX)) {
 			LogUtils.warn("wrong property key " + shortcutKey);
 			return;
 		}
 		final int pos = shortcutKey.indexOf("/", SHORTCUT_PROPERTY_PREFIX.length());
 		if (pos <= 0) {
 			LogUtils.warn("wrong property key " + shortcutKey);
 			return;
 		}
 		final String modeName = shortcutKey.substring(SHORTCUT_PROPERTY_PREFIX.length(), pos);
 		final String itemKey = shortcutKey.substring(pos + 1);
 		Controller controller = Controller.getCurrentController();
 		final ModeController modeController = controller.getModeController(modeName);
 		if (modeController != null) {
 			final KeyStroke keyStroke;
 			if (!keystrokeString.equals("")) {
				keyStroke = UITools.getKeyStroke(keystrokeString);
 				final AFreeplaneAction oldAction = accelerators.get(key(modeController, keyStroke));
 				if (! acceleratorIsDefinedByUserProperties(oldAction, modeController, allPresets))
					setAccelerator(modeController, oldAction, null);
 			}
 			else {
 				keyStroke = null;
 			}
 			final AFreeplaneAction action = modeController.getAction(itemKey);
 			if (action != null) {
 				setAccelerator(modeController, action, keyStroke);
 			}
 		}
 		setKeysetProperty(shortcutKey, keystrokeString);
 	}

	private boolean acceleratorIsDefinedByUserProperties(final AFreeplaneAction oldAction, final ModeController modeController,
			Hashtable<?, ?> userProperties) {
		if (oldAction != null) {
			final Object key = oldAction.getKey();
			final String oldShortcutKey = getPropertyKey(modeController, key.toString());
			final boolean acceleratorWasNotLoadedYet = userProperties.containsKey(oldShortcutKey) && !"".equals(userProperties.get(oldShortcutKey));
			return acceleratorWasNotLoadedYet;
		}
		else
			return false;
	}

	public void storeAcceleratorPreset(OutputStream out) {
 		try {
 			final OutputStream output = new BufferedOutputStream(out);
 			keysetProps.store(output, "");
 		}
 		catch (final IOException e1) {
 			UITools.errorMessage(TextUtils.removeTranslateComment(TextUtils.getText("can_not_save_key_set")));
 		}
 	}

	private static String toString(final KeyStroke newAccelerator) {
		return newAccelerator.toString().replaceFirst("pressed ", "");
	}

	private static boolean askForReplaceShortcutViaDialog(String oldMenuItemTitle) {
		final int replace = JOptionPane.showConfirmDialog(UITools.getCurrentRootComponent(),
				TextUtils.removeTranslateComment(TextUtils.format("replace_shortcut_question", oldMenuItemTitle)),
				TextUtils.removeTranslateComment(TextUtils.format("replace_shortcut_title")), JOptionPane.YES_NO_OPTION);
		return replace == JOptionPane.YES_OPTION;
	}

	public boolean canProcessKeyEvent(KeyEvent e) {
		KeyStroke ks;
		KeyStroke ksE = null;
		boolean pressed = (e.getID() == KeyEvent.KEY_PRESSED);
		if(e.getID() == KeyEvent.KEY_TYPED) {
			ks=KeyStroke.getKeyStroke(e.getKeyChar());
		} else {
			if(e.getKeyCode() != e.getExtendedKeyCode()) {
				ksE=KeyStroke.getKeyStroke(e.getExtendedKeyCode(), e.getModifiers(), !pressed);
			}
			ks=KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), !pressed);
		}
		return ksE != null && actionForAccelerator(ksE) != null || actionForAccelerator(ks) != null;
	}

	private AFreeplaneAction actionForAccelerator(KeyStroke ks) {
		return accelerators.get(key(ks));
	}

	public boolean processKeyBinding(KeyStroke ks, KeyEvent event) {
		AFreeplaneAction action = actionForAccelerator(ks);
		if(action == null) {
			final KeyStroke derivedKeyStroke = FreeplaneMenuBar.derive(ks, event.getKeyChar());
			action = actionForAccelerator(derivedKeyStroke);
		}
		if(action != null && action.isEnabled()) {
			if(action != null && SwingUtilities.notifyAction(action, ks, event, event.getComponent(), event.getModifiers())) {
				return true;
			}
		}
		return false;
	}

	private class KeystrokeValidator implements IKeystrokeValidator {
		private final AFreeplaneAction action;

		private KeystrokeValidator(AFreeplaneAction action) {
			this.action = action;
		}

		private boolean checkForOverwriteShortcut(final KeyStroke keystroke) {
			final AFreeplaneAction priorAssigned = accelerators.get(key(keystroke));
			if (priorAssigned == null || action.getKey().equals(priorAssigned.getKey())) {
				return true;
			}
			return replaceOrCancel(priorAssigned, ActionUtils.getActionTitle(priorAssigned));
		}

		private boolean replaceOrCancel(AFreeplaneAction action, String oldMenuItemTitle) {
			if (askForReplaceShortcutViaDialog(oldMenuItemTitle)) {
				setAccelerator(action, null);
				final String shortcutKey = getPropertyKey(action.getKey());
				keysetProps.remove(shortcutKey);
				return true;
			} else {
				return false;
			}
		}

		public boolean isValid(final KeyStroke keystroke, final Character keyChar) {
			if (keystroke == null) {
				return true;
			}
			if (actionMap.containsKey(key(action.getKey()))) {
				return true;
			}
			if (keyChar != KeyEvent.CHAR_UNDEFINED && (keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK)) == 0) {
				final String keyTypeActionString = ResourceController.getResourceController().getProperty("key_type_action",
						FirstAction.EDIT_CURRENT.toString());
				FirstAction keyTypeAction = FirstAction.valueOf(keyTypeActionString);
				return FirstAction.IGNORE.equals(keyTypeAction);
			}
			if (!checkForOverwriteShortcut(keystroke)) {
				return false;
			}
			final KeyStroke derivedKS = FreeplaneMenuBar.derive(keystroke, keyChar);
			if (derivedKS == keystroke) {
				return true;
			}
			return checkForOverwriteShortcut(derivedKS);
		}
	}

	private static String replaceModifiersForMac(String accelerator) {
		if (Compat.isMacOsX()) {
			accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
		}
		return accelerator;
	}

	private void setKeysetProperty(String key, String value) {
		keysetProps.setProperty(key, value);
	}
}
