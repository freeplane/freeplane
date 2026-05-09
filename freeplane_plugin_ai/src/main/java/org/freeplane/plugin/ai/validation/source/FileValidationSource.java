package org.freeplane.plugin.ai.validation.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件导入数据源 —— 从 .mm/.json 文件读取内容。
 * 
 * <p>接入 MapRestController.handleImportMap 导入前的环检测拦截。
 */
public final class FileValidationSource implements ValidationSource {
    
    private final Path filePath;
    private final String filename;
    private String content; // 兼容旧构造函数或缓存
    
    /**
     * @param content  文件内容字符串(已读入内存)
     * @param filename 文件名(用于日志)
     */
    public FileValidationSource(String content, String filename) {
        this.filePath = null;
        this.filename = filename;
        this.content = content;
    }
    
    /**
     * @param filePath   文件路径
     * @param filename   文件名(用于日志)
     */
    public FileValidationSource(Path filePath, String filename) {
        this.filePath = filePath;
        this.filename = filename;
        this.content = null;
    }
    
    @Override
    public String readContent() throws IOException {
        if (content != null) {
            return content; // 已缓存内容
        }
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
    
    @Override
    public SourceType getSourceType() {
        return SourceType.FILE_IMPORT;
    }
    
    @Override
    public boolean isReady() {
        if (content != null) {
            return true; // 已缓存内容
        }
        return filePath != null && Files.exists(filePath);
    }
    
    @Override
    public String getDescription() {
        return "file=" + filename;
    }
}
