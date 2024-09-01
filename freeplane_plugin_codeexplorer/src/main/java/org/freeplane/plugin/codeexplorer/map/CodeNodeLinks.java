/*
 * Created on 19 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Collection;
import java.util.Collections;
import org.freeplane.core.extension.Configurable;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.plugin.codeexplorer.connectors.CodeLinkController;

public class CodeNodeLinks extends NodeLinks{
    private final CodeLinkController linkController;
    private CodeNode node;
    private Configurable mapViewComponent;


    public CodeNodeLinks(CodeLinkController linkController, Configurable mapViewComponent, CodeNode node) {
        super(Collections.emptyList());
        this.linkController = linkController;
        this.mapViewComponent = mapViewComponent;
        this.node = node;
    }


    @Override
    public Collection<? extends NodeLinkModel> getLinks() {
      return  linkController.getLinksFrom(node, mapViewComponent);
    }

}
