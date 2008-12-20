package deprecated.freemind.preferences.layout;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;


class OptionPanelWindowConfigurationStorage extends WindowConfigurationStorage {
  protected String panel;


  public String getPanel() {
    return this.panel;
  }

  public void setPanel(String panel) {
    this.panel = panel;
  }

@Override
protected void marschallSpecificElements(IXMLElement xml) {
	xml.setName("option_panel_window_configuration_storage");
	xml.setAttribute("panel", panel);
}
public static OptionPanelWindowConfigurationStorage decorateDialog(String marshalled,
                                                                   JDialog dialog) {
	OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
	IXMLElement xml = storage.unmarschall(marshalled, dialog);
	if(xml != null){
		storage.panel = xml.getAttribute("panel", null);
		return storage;
	}
	return null;
}

}
