package org.freeplane.features.text;

public enum ContentSyntax {
    XML, PLAIN;
    static public String specificType(String contentType) {
        final String specificType = contentType.substring(contentType.indexOf('/') + 1);
		return specificType.isEmpty() ? null : specificType;
    }
    
    public final String prefix;
    

    private ContentSyntax() {
        this.prefix = name().toLowerCase() + "/";
    }
    
    public String with(String suffix) {
        return suffix != null ? prefix + suffix : prefix;
    }
    
    public boolean matches(String contentType) {
        return contentType.startsWith(prefix);
    }
    
    
}