package org.freeplane.features.text;

public enum ContentSyntax {
    XML, PLAIN;
    static public String specificType(String contentType) {
        return contentType.substring(contentType.indexOf('/') + 1);
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