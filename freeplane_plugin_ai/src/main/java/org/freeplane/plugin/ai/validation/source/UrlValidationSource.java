package org.freeplane.plugin.ai.validation.source;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 远程 URL 数据源(预留)。
 * 
 * <p>按需实现,当前仅占位。
 */
public final class UrlValidationSource implements ValidationSource {
    
    private final URL url;
    private String cachedContent;
    
    public UrlValidationSource(URL url) {
        this.url = url;
    }
    
    @Override
    public String readContent() throws IOException {
        if (cachedContent == null) {
            try (var is = url.openStream()) {
                cachedContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return cachedContent;
    }
    
    @Override
    public SourceType getSourceType() {
        return SourceType.URL_REMOTE;
    }
    
    @Override
    public boolean isReady() {
        // 异步检查连通性(简化为同步检查)
        try {
            url.openConnection().connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        return "url=" + url.toString();
    }
}
