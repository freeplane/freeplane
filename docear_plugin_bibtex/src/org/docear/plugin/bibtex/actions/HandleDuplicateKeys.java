package org.docear.plugin.bibtex.actions;

import java.util.HashSet;

import org.docear.plugin.bibtex.ReferencesController;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;
import net.sf.jabref.labelPattern.LabelPatternUtil;

public class HandleDuplicateKeys implements PostOpenAction {

    @Override
    public boolean isActionNecessary(ParserResult pr) {
	return pr.hasDuplicateKeys();
    }

    @Override
    public void performAction(BasePanel panel, ParserResult pr) {
	HashSet<String> foundKeys = new HashSet<String>();
        BibtexDatabase db = panel.database();
        for (BibtexEntry entry : db.getEntries()) {
            String key = entry.getCiteKey();
            if ((key == null) || (key.length() == 0)) {
        	continue;
            }

            if (!foundKeys.contains(key)) {
                foundKeys.add(key);
            }
            else {
        	entry = LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), panel.database(), entry);
        	foundKeys.add(entry.getCiteKey());
            }
        }
        
        ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
    }

}
