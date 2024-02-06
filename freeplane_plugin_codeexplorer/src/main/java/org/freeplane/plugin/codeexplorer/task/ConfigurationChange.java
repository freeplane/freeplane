/*
 * Created on 9 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

public enum ConfigurationChange {SAME, CONFIGURATION, CODE_BASE;
    public static ConfigurationChange max(ConfigurationChange status1, ConfigurationChange status2) {
        return status1.ordinal() > status2.ordinal() ? status1 : status2;
    }}