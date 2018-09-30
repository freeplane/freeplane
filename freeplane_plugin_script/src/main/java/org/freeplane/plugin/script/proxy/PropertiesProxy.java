package org.freeplane.plugin.script.proxy;

import java.util.Set;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.plugin.script.ScriptExecution;
import org.freeplane.plugin.script.proxy.Proxy.Properties;

public class PropertiesProxy extends AbstractProxy<MapModel> implements Properties {
    PropertiesProxy(final MapModel delegate, final ScriptExecution scriptExecution) {
        super(delegate, scriptExecution);
    }

    @Override
    public Convertible getAt(String key) {
        final String value = MapStyle.getController().getProperty(getDelegate(), key);
        return value == null ? null : new Convertible(value);
    }

    @Override
    public Convertible putAt(String key, Object value) {
        final String string = Convertible.toString(value);
        MapStyle.getController().setProperty(getDelegate(), key, string);
        return new Convertible(string);
    }

    @Override
    public Set<String> keySet() {
        return MapStyle.getController().getPropertiesReadOnly(getDelegate()).keySet();
    }
}
