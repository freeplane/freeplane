/*
 * Created on 4 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import com.google.gson.annotations.SerializedName;

public class CodeNodeDetails {

    @SerializedName("content")
    private String content;

    @SerializedName("hidden")
    private boolean hidden;

    public CodeNodeDetails(String content, boolean hidden) {
        super();
        this.content = content;
        this.hidden = hidden;
    }

    public String getContent() {
        return content;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return "CodeNodeDetails [content=" + content + ", hidden=" + hidden + "]";
    }



}
