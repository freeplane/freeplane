package org.freeplane.addins.mindmapmode.styles;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;


class ManageStyleEditorWindowConfigurationStorage extends WindowConfigurationStorage {
  protected int dividerPosition;


  public int getDividerPosition() {
    return this.dividerPosition;
  }

  public void setDividerPosition(int dividerPosition) {
    this.dividerPosition = dividerPosition;
  }

  @Override
  protected void marschallSpecificElements(IXMLElement xml) {
	  xml.setName("manage_style_editor_window_configuration_storage");
	  xml.setAttribute("divide_position", Integer.toString(dividerPosition));
  }

  public static ManageStyleEditorWindowConfigurationStorage decorateDialog(String marshalled,
                                                                           JDialog dialog) {
	  ManageStyleEditorWindowConfigurationStorage storage = new ManageStyleEditorWindowConfigurationStorage();
	  IXMLElement xml = storage.unmarschall(marshalled, dialog);
	  if(xml != null){
		  storage.dividerPosition = Integer.parseInt(xml.getAttribute("divide_position", null));
		  return storage;
	  }
	  return null;
  }

}
