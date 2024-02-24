/*
 * Created on 6 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import org.freeplane.features.attribute.AttributeTableLayoutModel;

import com.google.gson.annotations.SerializedName;

public class CodeAttributeConfiguration {
    @SerializedName("attributeViewType")
    private  String attributeViewType;

    public CodeAttributeConfiguration() {
        super();
        this.attributeViewType = AttributeTableLayoutModel.HIDE_ALL;
    }

    void initialize() {
        if(attributeViewType == null)
            attributeViewType = AttributeTableLayoutModel.HIDE_ALL;
    }

    public String getAttributeViewType() {
        return attributeViewType;
    }

    public void setAttributeViewType(String attributeViewType) {
        this.attributeViewType = attributeViewType;
    }

    @Override
    public String toString() {
        return "CodeAttributeConfiguration [attributeViewType=" + attributeViewType + "]";
    }
}
