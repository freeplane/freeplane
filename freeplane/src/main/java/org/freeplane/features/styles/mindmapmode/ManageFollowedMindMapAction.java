package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.UrlManager;

@EnabledAction(checkOnPopup = true)
class ManageFollowedMindMapAction extends AFreeplaneAction{
	private static final long serialVersionUID = 1L;

	public ManageFollowedMindMapAction() {
		super("ManageFollowedMindMapAction");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final MapModel map = Controller.getCurrentController().getMap();
		MapStyle mapStyleController = MapStyle.getController();
		String followedMap = mapStyleController.getProperty(map, MapStyleModel.FOLLOWED_MAP_LOCATION_PROPERTY);
		if(followedMap != null) {

			final Object[] options;
			options = new Object[] { TextUtils.getText("open"), 
					TextUtils.getText("copyStyles"),
					TextUtils.getText("replaceStyles"),
					TextUtils.getText("unfollow"), 
					TextUtils.getText("close") };
			try {
				URI uri = new URI(followedMap);
				String followedMapPath = "file".equalsIgnoreCase(uri.getScheme()) ? Paths.get(uri).toFile().getAbsolutePath() : followedMap;
				String message = TextUtils.format("followed_map", followedMapPath);
				final int choice = JOptionPane.showOptionDialog(UITools.getMenuComponent(), message,
						getValue(Action.NAME).toString(), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
						options, options[4]);

				switch(choice) {
				case 0:
					UrlManager.getController().loadURL(uri);
					break;
				case 1:
					try {
						mapStyleController.copyStyles(uri, map, true);
					} catch (MalformedURLException e) {
						LogUtils.warn(e);
					}
					break;
				case 2:
					try {
						mapStyleController.replaceStyles(uri, map, true);
					} catch (MalformedURLException e) {
						LogUtils.warn(e);
					}
					break;
				case 3:
					mapStyleController.setProperty(map, MapStyleModel.FOLLOWED_MAP_LOCATION_PROPERTY, null);
					mapStyleController.setProperty(map, MapStyleModel.FOLLOWED_MAP_LAST_TIME, null);
					break;
				default:
					break;
				}
			} catch (URISyntaxException e1) {
				LogUtils.warn(e1);
			}
		}
		else {
			UITools.showMessage(TextUtils.getText("no_map_followed"), JOptionPane.INFORMATION_MESSAGE);
		}

	}

	@Override
	protected void setEnabled() {
		final MapModel map = Controller.getCurrentController().getMap();
		MapStyle mapStyleController = MapStyle.getController();
		boolean containsProperty = mapStyleController.getProperty(map, MapStyleModel.FOLLOWED_MAP_LOCATION_PROPERTY) != null;
		setEnabled(containsProperty);
	}
}
