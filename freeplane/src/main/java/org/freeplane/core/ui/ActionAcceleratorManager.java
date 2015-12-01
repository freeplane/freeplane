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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JComponent;
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
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.Pair;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class ActionAcceleratorManager implements IKeyStrokeProcessor, IAcceleratorChangeListener, IAcceleratorMap {

	<V> Pair<ModeController, V> key(V value) {
		return key(Controller.getCurrentModeController(), value);

	}

	<V> Pair<ModeController, V> key(ModeController modeController, V value) {
		return new Pair<ModeController, V>(modeController, value);

	}

	private static final String SHORTCUT_PROPERTY_PREFIX = "acceleratorFor.";

	private final Map<Pair<ModeController, KeyStroke>, AFreeplaneAction> accelerators = new HashMap<>();
	private final Map<Pair<ModeController, String>, KeyStroke> actionMap = new HashMap<>();
	private final List<IAcceleratorChangeListener> changeListeners = new ArrayList<IAcceleratorChangeListener>();

	private final Properties keysetProps;
	private final Properties defaultProps;


	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

 	public ActionAcceleratorManager() {
		keysetProps = new Properties();
		defaultProps = new Properties();
 	}

	public void loadDefaultAcceleratorPresets() {
	    try {
			if (ResourceController.getResourceController().getFreeplaneUserDirectory() != null)
				loadAcceleratorPresets(new FileInputStream(getPresetsFile()));
		}
		catch (IOException ex) {
		}
    }

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

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
    		if (keyStroke != null && oldAction != null) {
    			UITools.errorMessage(TextUtils.removeTranslateComment(TextUtils.format("action_keystroke_in_use_error", keyStroke, getActionTitle(action.getKey()), getActionTitle(oldAction.getKey()))));
				accelerators.put(key(modeController, keyStroke), oldAction);
    			final String shortcutKey = getPropertyKey(action.getKey());

    			keysetProps.setProperty(shortcutKey, "");
    			return;
    		}
 		}
		final KeyStroke removedAccelerator = removeAccelerator(modeController, action);
		final String actionKey = action.getKey();
		if(keyStroke != null) {
			actionMap.put(key(modeController, actionKey), keyStroke);
		}
		fireAcceleratorChanged(action, removedAccelerator, keyStroke);
	}

	private String getActionTitle(String key) {
 		String title = TextUtils.getText(key+".text");
		if(title == null || title.isEmpty()) {
			title = key;
		}
		return TextUtils.removeTranslateComment(title);
 	}

	@Deprecated
	public void setDefaultAccelerator(String actionKey, String accel) {
		setDefaultAccelerator(Controller.getCurrentModeController().getAction(actionKey), accel);
	}

	@Override
	public void setDefaultAccelerator(AFreeplaneAction action) {
		final String actionKey = action.getKey();
		final String shortcutKey = getPropertyKey(actionKey);
		String accelerator = ResourceController.getResourceController().getProperty(shortcutKey, null);
		if (accelerator != null)
			setDefaultAccelerator(action, accelerator);
	}

	public void setDefaultAccelerator(final AFreeplaneAction action, String accelerator) {

		final String shortcutKey = getPropertyKey(action.getKey());
		if (null == getProperty(shortcutKey)) {
			if (Compat.isMacOsX()) {
				accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
			}
			defaultProps.setProperty(shortcutKey, accelerator);
			KeyStroke ks = KeyStroke.getKeyStroke(accelerator);
			setAccelerator(action, ks);
		}
	}

	@Override
	public void removeAction(AFreeplaneAction action) {
		final String shortcutKey = getPropertyKey(action.getKey());
		defaultProps.remove(shortcutKey);
		removeAccelerator(action);
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
				throw new AssertionError("unexpected action " + action.getKey() + " for accelerator " + oldAccelerator + "("+oldAction.getKey()+")");
			}
		}
		return oldAccelerator;
	}

 	public String getPropertyKey(final String key) {
		return SHORTCUT_PROPERTY_PREFIX + Controller.getCurrentModeController().getModeName() + "/" + key;
	}

 	public KeyStroke getAccelerator(AFreeplaneAction action) {
		KeyStroke ks = actionMap.get(key(action.getKey()));
 		return ks;
 	}

 	public void addAcceleratorChangeListener(IAcceleratorChangeListener changeListener) {
		synchronized (changeListeners) {
			if(!changeListeners.contains(changeListener)) {
				changeListeners.add(changeListener);
			}
		}
	}

 	protected void fireAcceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
 		synchronized (changeListeners) {
			for (IAcceleratorChangeListener listener : changeListeners) {
				listener.acceleratorChanged(action, oldStroke, newStroke);
			}
		}
	}

 	private String getProperty(String key) {
 		return keysetProps.getProperty(key, defaultProps.getProperty(key, null));
 	}

 	public void newAccelerator(final AFreeplaneAction action, final KeyStroke newAccelerator) {
		final String shortcutKey = getPropertyKey(action.getKey());
		final String oldShortcut = getProperty(shortcutKey);
		if (newAccelerator == null || !new KeystrokeValidator(action).isValid(newAccelerator, newAccelerator.getKeyChar())) {
			final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(oldShortcut);
			final IKeystrokeValidator validator = new KeystrokeValidator(action);
			grabKeyDialog.setValidator(validator);
			grabKeyDialog.setVisible(true);
			if (grabKeyDialog.isOK()) {
				final String shortcut = grabKeyDialog.getShortcut();
				final KeyStroke accelerator = UITools.getKeyStroke(shortcut);
				setAccelerator(action, accelerator);
				keysetProps.setProperty(shortcutKey, shortcut);
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
			keysetProps.setProperty(shortcutKey, toString(newAccelerator));
			LogUtils.info("created shortcut '" + toString(newAccelerator) + "' for action '" + action+ "', shortcutKey '" + shortcutKey + "' (" + ActionUtils.getActionTitle(action) + ")");
		}
		try {
			if(!getPresetsFile().exists()) {
					getPresetsFile().createNewFile();
			}
			storeAcceleratorPreset(new FileOutputStream(getPresetsFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

 	public File getPresetsFile() {
 		File ribbonsDir = new File(ResourceController.getResourceController().getFreeplaneUserDirectory());
		if(!ribbonsDir.exists()) {
			ribbonsDir.mkdirs();
		}
		return new File(ribbonsDir, "accelerator.properties");
 	}

 	public void loadAcceleratorPresets(final InputStream in) {
		final Properties prop = new Properties();
		try {
			prop.load(in);
			for (final Entry<Object, Object> property : prop.entrySet()) {
				final String shortcutKey = (String) property.getKey();
				final String keystrokeString = (String) property.getValue();
				if (!shortcutKey.startsWith(SHORTCUT_PROPERTY_PREFIX)) {
					LogUtils.warn("wrong property key " + shortcutKey);
					continue;
				}
				final int pos = shortcutKey.indexOf("/", SHORTCUT_PROPERTY_PREFIX.length());
				if (pos <= 0) {
					LogUtils.warn("wrong property key " + shortcutKey);
					continue;
				}
				final String modeName = shortcutKey.substring(SHORTCUT_PROPERTY_PREFIX.length(), pos);
				final String itemKey = shortcutKey.substring(pos + 1);
				Controller controller = Controller.getCurrentController();
				final ModeController modeController = controller.getModeController(modeName);
				if (modeController != null) {
    				final AFreeplaneAction action = modeController.getAction(itemKey);
    				if (action == null) {
    					LogUtils.warn("wrong key in " + shortcutKey);
    					continue;
    				}
    				final KeyStroke keyStroke;
    				if (!keystrokeString.equals("")) {
    					keyStroke = UITools.getKeyStroke(parseKeyStroke(keystrokeString).toString());
						final AFreeplaneAction oldAction = accelerators.get(key(modeController, keyStroke));
    					if (oldAction != null) {
							setAccelerator(modeController, oldAction, null);
    						final Object key = oldAction.getKey();
    						final String oldShortcutKey = getPropertyKey(key.toString());
    						keysetProps.setProperty(oldShortcutKey, "");
    					}
    				}
    				else {
    					keyStroke = null;
    				}
					setAccelerator(modeController, action, keyStroke);
				}
				keysetProps.setProperty(shortcutKey, keystrokeString);
			}
		}
		catch (final IOException e) {
			LogUtils.warn("shortcut presets not stored: "+e.getMessage());
		}
	}

	public void storeAcceleratorPreset(OutputStream out) {
 		try {
 			final OutputStream output = new BufferedOutputStream(out);
 			keysetProps.store(output, "");
 			output.close();
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
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public boolean processKeyBinding(KeyStroke ks, KeyEvent event, int condition, boolean pressed, boolean consumed) {
		if (!consumed && condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
			AFreeplaneAction action = accelerators.get(key(ks));
			if(action == null) {
				final KeyStroke derivedKeyStroke = FreeplaneMenuBar.derive(ks, event.getKeyChar());
				action = accelerators.get(key(derivedKeyStroke));
			}
			if(action != null && action.isEnabled()) {
				if(action != null && SwingUtilities.notifyAction(action, ks, event, event.getComponent(), event.getModifiers())) {
					return true;
				}
			}
		}
		return false;
	}

	public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
		final String actionKey = action.getKey();
		KeyStroke ks = actionMap.put(key(actionKey), newStroke);
		if(ks != null) {
			accelerators.remove(key(ks));
		}
		accelerators.put(key(newStroke), action);
	}

	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
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
				keysetProps.setProperty(shortcutKey, "");
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

	public static KeyStroke parseKeyStroke(String accelerator) {
		if (accelerator != null) {
			if (Compat.isMacOsX()) {
				accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
			}
			else {
				accelerator = accelerator.replaceFirst("META", "CONTROL").replaceFirst("meta", "control");
			}
			return KeyStroke.getKeyStroke(accelerator);
		}
		return null;
	}
}
