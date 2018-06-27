package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;

class UpdateConfigurationAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "UpdateConfigurationAction";
	private static final long serialVersionUID = 1L;
	private ConfigurationSession configurationSession;
	private int attributeValue = 45;
	public UpdateConfigurationAction(ConfigurationSession configurationSession ) {

		super(ACTION_NAME);
		this.configurationSession = configurationSession;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		attributeValue++;
		configurationSession.update("ID_1053277958", "a", attributeValue);

		List<String> attributesList = new ArrayList<>();
		attributesList.add("a");
		attributesList.add("b");
		attributesList.add("area");

		Map<String, Object> attributeMap = configurationSession.readValues("ID_1053277958", attributesList);

		for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
			LogUtils.info(entry.getKey() + " " + entry.getValue());
		}
	}
}
