/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.02.13</p>
 *
 * @author Lukasz Zielinski
 */
public class TempMemoryVF implements MemoryVF {
    private final VirtualFile virtualFile;
    private final TempFileSystem fs;
    
    public TempMemoryVF(VirtualFile virtualFile, TempFileSystem fs) {
        this.virtualFile = virtualFile;
        this.fs = fs;
    }

    @Override
    public String getName() {
        return virtualFile.getName();
    }

    @Override
    public long size() {
        return fs.getLength(virtualFile);
    }

    @NotNull
    @Override
    public String getContent() {
        try {
            return new String(fs.contentsToByteArray(virtualFile), CharsetName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return fs.getInputStream(virtualFile);    
    }
    
    @Override
    public VirtualFile asVirtualFile() {
        return virtualFile;
    }

    private void store(VirtualFile file, String content) throws IOException {
        long time = System.currentTimeMillis();
        OutputStream outputStream = fs.getOutputStream(file, null, time, time);
        byte[] bytes = content.getBytes(CharsetName);
        outputStream.write(bytes);
        outputStream.close();    
    }
    
    @Override
    public MemoryVF addChild(MemoryVF file) {
        try {
            VirtualFile newFile = fs.createChildFile(null, virtualFile, file.getName());
            newFile.setCharset(Charset.forName(CharsetName));
            store(newFile, file.getContent());
            return new TempMemoryVF(newFile, fs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setWritable(boolean writable) throws IOException {
        fs.setWritable(virtualFile, writable);    
    }

    @Override
    public void setContent(@NotNull String content) {
        virtualFile.setCharset(Charset.forName(CharsetName));
        try {
            store(virtualFile, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
