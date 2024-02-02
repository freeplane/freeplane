/*
 * Created on 4 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import com.google.gson.annotations.SerializedName;

public class CodeNodeAttribute {
    enum ContentType {STRING, OBJECT}

    @SerializedName("contentType")
    private ContentType contentType;

    @SerializedName("name")
    private String name;

    @SerializedName("value")
    private String value;

    public CodeNodeAttribute(ContentType contentType, String name, String value) {
        super();
        this.contentType = contentType;
        this.name = name;
        this.value = value;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CodeNodeAttribute [contentType=" + contentType + ", name=" + name + ", value="
                + value + "]";
    }


}
