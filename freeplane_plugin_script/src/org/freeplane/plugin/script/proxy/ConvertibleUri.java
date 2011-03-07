package org.freeplane.plugin.script.proxy;

import java.net.URI;

public class ConvertibleUri extends Convertible {
	private final URI uri;

	public ConvertibleUri(final URI uri) {
	    super(uri.toString());
	    this.uri = uri;
    }

	@Override
    public Object getObject() {
	    return uri;
    }

	@Override
    public boolean isNum() {
		return false;
    }

	@Override
    public boolean isDate() {
		return false;
    }
}
