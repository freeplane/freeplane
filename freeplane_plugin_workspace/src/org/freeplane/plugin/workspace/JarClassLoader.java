package org.freeplane.plugin.workspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class JarClassLoader extends ClassLoader {
    private final ZipFile file;

    public JarClassLoader(String filename) throws IOException {
        this.file = new ZipFile(filename);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ZipEntry entry = this.file.getEntry(name.replace('.', '/') + ".class");
        if (entry == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            byte[] array = new byte[1024];
            InputStream in = this.file.getInputStream(entry);
            ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
            int length = in.read(array);
            while (length > 0) {
                out.write(array, 0, length);
                length = in.read(array);
            }
            return defineClass(name, out.toByteArray(), 0, out.size());
        }
        catch (IOException exception) {
            throw new ClassNotFoundException(name, exception);
        }
    }
}