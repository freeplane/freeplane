package plugins.script;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;


public class ScriptEditorWindowConfigurationStorage extends WindowConfigurationStorage {
  protected int leftRatio;

  protected int topRatio;


  public int getLeftRatio() {
    return this.leftRatio;
  }

  public void setLeftRatio(int leftRatio) {
    this.leftRatio = leftRatio;
  }

  public int getTopRatio() {
    return this.topRatio;
  }

  public void setTopRatio(int topRatio) {
    this.topRatio = topRatio;
  }

@Override
protected void marschallSpecificElements(IXMLElement xml) {
	xml.setName("manage_style_editor_window_configuration_storage");
	xml.setAttribute("left_ratio", Integer.toString(leftRatio));
	xml.setAttribute("top_ratio", Integer.toString(topRatio));
}

public static ScriptEditorWindowConfigurationStorage decorateDialog(String marshalled,
                                                                   JDialog dialog) {
	ScriptEditorWindowConfigurationStorage storage = new ScriptEditorWindowConfigurationStorage();
	IXMLElement xml = storage.unmarschall(marshalled, dialog);
	if(xml != null){
		storage.leftRatio = Integer.parseInt(xml.getAttribute("left_ratio", null));
		storage.topRatio = Integer.parseInt(xml.getAttribute("top_ratio", null));
		return storage;
	}
	return null;
}
}
