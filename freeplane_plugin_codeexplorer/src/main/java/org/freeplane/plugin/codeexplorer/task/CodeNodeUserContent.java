/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.util.TypeReference;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.plugin.codeexplorer.task.CodeNodeAttribute.ContentType;

import com.google.gson.annotations.SerializedName;

public class CodeNodeUserContent implements Comparable<CodeNodeUserContent> {

    private static final Comparator<CodeNodeUserContent> COMPARING_USER_CONTENT_BY_ID = Comparator.comparing(CodeNodeUserContent::getNodeIdWithoutGroupIndex);

    public static class Factory {
        public static final Factory INSTANCE = new Factory();
        public CodeNodeUserContent contentOf(NodeModel node) {
            String id = node.getID();
            Optional<CodeNodeDetails> details = Optional.ofNullable(DetailModel.getDetail(node))
                    .map(x -> toCodeNodeDetails(x))
                    .filter(x -> ! x.getContent().isEmpty());
            List<CodeNodeAttribute> attributes = attributes(node)
                    .filter(attribute -> ! attribute.isManaged())
                    .map(this::toCodeNodeAttribute)
                    .collect(Collectors.toList());
            String idWithoutGroupIndex = node.isRoot() ? id : id.substring(0, id.lastIndexOf('['));
            return new CodeNodeUserContent(idWithoutGroupIndex, details, attributes);
        }

        private CodeNodeDetails toCodeNodeDetails(DetailModel details) {
            return new CodeNodeDetails(details.getText(), details.isHidden());
        }

        private CodeNodeAttribute toCodeNodeAttribute(Attribute attribute) {
            final Object value = attribute.getValue();
            final ContentType contentType = (value instanceof String) ? ContentType.STRING : ContentType.OBJECT;
            return new CodeNodeAttribute(
                    contentType,
                    attribute.getName(),
                    contentType == ContentType.STRING ?  (String)value : TypeReference.toSpec(value));
        }

        public DetailModel fromCodeNodeDetails(CodeNodeDetails x) {
            return new DetailModel(null, x.getContent(), null, x.isHidden());
        }

        public Attribute fromCodeNodeAttribute(CodeNodeAttribute attribute) {
            final String valueSpec = attribute.getValue();
            return new Attribute(attribute.getName(),
                    attribute.getContentType() == ContentType.STRING
                    ? valueSpec: TypeReference.create(valueSpec));
        }


        public boolean hasCustomContent(NodeModel node) {
            return hasDetails(node) || hasCustomAttributes(node);
        }

        private boolean hasDetails(NodeModel node) {
            return DetailModel.getDetail(node) != null;
        }

        private boolean hasCustomAttributes(NodeModel node) {
            return customAttributes(node).anyMatch(x -> true);
        }

        private Stream<Attribute> customAttributes(NodeModel node) {
            Stream<Attribute> stream = attributes(node);
            return stream.filter(a -> ! a.isManaged());
        }

        private Stream<Attribute> attributes(NodeModel node) {
            NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
            Stream<Attribute> stream = attributes != null && attributes.getRowCount() > 0
                    ? attributes.getAttributes().stream()
                    : Stream.<Attribute>empty();
            return stream;
        }
     }

    @SerializedName("nodeId")
    private String nodeIdWithoutGroupIndex;

    @SerializedName("details")
    private Optional<CodeNodeDetails> details;

    @SerializedName("attributes")
    private List<CodeNodeAttribute> attributes;

    public CodeNodeUserContent(String nodeIdWithoutGroupIndex, Optional<CodeNodeDetails> details,
            List<CodeNodeAttribute> attributes) {
        super();
        this.nodeIdWithoutGroupIndex = nodeIdWithoutGroupIndex;
        this.details = details;
        this.attributes = attributes;
    }


    void initialize() {
        if(details == null)
            details = Optional.empty();
        if(attributes == null)
            attributes = Collections.emptyList();
    }

    public String getNodeIdWithoutGroupIndex() {
        return nodeIdWithoutGroupIndex;
    }

    public Optional<CodeNodeDetails> getDetails() {
        return details;
    }

    public List<CodeNodeAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "CodeNodeUserContent [nodeId=" + nodeIdWithoutGroupIndex + ", details=" + details + ", attributes="
                + attributes + "]";
    }

    @Override
    public int compareTo(CodeNodeUserContent other) {
        return COMPARING_USER_CONTENT_BY_ID.compare(this, other);
    }


    @Override
    public int hashCode() {
        return Objects.hash(nodeIdWithoutGroupIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CodeNodeUserContent other = (CodeNodeUserContent) obj;
        return Objects.equals(nodeIdWithoutGroupIndex, other.nodeIdWithoutGroupIndex);
    }




}
