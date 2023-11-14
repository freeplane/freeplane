package org.freeplane.main.codeexplorermode;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

class CodeInformationPanel extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    CodeInformationPanel() {
        addTab("Tab 1", new JLabel("Tab 1"));
        addTab("Tab 2", new JLabel("Tab 2"));
	}
}